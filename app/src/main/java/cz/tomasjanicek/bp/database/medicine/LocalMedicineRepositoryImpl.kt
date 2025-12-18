package cz.tomasjanicek.bp.database.medicine

import cz.tomasjanicek.bp.model.EndingType
import cz.tomasjanicek.bp.model.Medicine
import cz.tomasjanicek.bp.model.MedicineReminder
import cz.tomasjanicek.bp.model.RegularityType
import cz.tomasjanicek.bp.model.ReminderStatus
import cz.tomasjanicek.bp.services.BackupScheduler
import cz.tomasjanicek.bp.services.notification.AlarmScheduler
import cz.tomasjanicek.bp.ui.screens.settings.SettingsManager
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject
import java.time.Instant
import kotlinx.coroutines.flow.firstOrNull // Přidej tento import
import java.time.format.DateTimeFormatter

class LocalMedicineRepositoryImpl @Inject constructor(
    private val dao: MedicineDao,
    private val backupScheduler: BackupScheduler,
    private val alarmScheduler: AlarmScheduler,
    private val settingsManager: SettingsManager
) : IMedicineRepository {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun getPlannedRemindersForDate(date: LocalDate): Flow<List<MedicineReminder>> {
        val dateString = date.format(dateFormatter)
        return dao.getPlannedRemindersForDate(dateString)
    }

    override fun getCompletedRemindersForDate(date: LocalDate): Flow<List<MedicineReminder>> {
        val dateString = date.format(dateFormatter)
        return dao.getCompletedRemindersForDate(dateString)
    }

    override fun getTodaysPlannedReminders(): Flow<List<MedicineReminder>> =
        dao.getTodaysPlannedReminders()

    override fun getTodaysCompletedReminders(): Flow<List<MedicineReminder>> =
        dao.getTodaysCompletedReminders()

    override fun getMedicineById(id: Long): Flow<Medicine?> = dao.getMedicineById(id)

    // --- HLAVNÍ METODA PRO ULOŽENÍ A PŘEPOČÍTÁNÍ ---
    override suspend fun saveMedicineAndGenerateReminders(medicine: Medicine) {
        // 1. Uložit nastavení léku a získat ID
        val savedMedicineId = dao.saveMedicine(medicine)
        val medicineWithId = medicine.copy(id = savedMedicineId)
        val now = System.currentTimeMillis()

        // 2. ÚKLID STARÝCH BUDÍKŮ (Důležité!)
        // Nejdřív načteme ty, co chceme smazat, abychom znali jejich ID a zrušili budíky
        val oldFutureReminders = dao.getFuturePlannedRemindersForMedicine(savedMedicineId, now)
        oldFutureReminders.forEach { reminder ->
            alarmScheduler.cancelNotification(reminder.id)
        }

        // 3. Smazat staré budoucí připomínky z DB
        dao.deleteFutureRemindersForMedicine(savedMedicineId, now)

        // 4. Vygenerovat nové připomínky (podle toho, jestli je lék pravidelný nebo ne)
        val newReminders = generateRemindersForMedicine(medicineWithId)

        if (newReminders.isNotEmpty()) {
            // 5. Vložit nové do DB
            dao.insertReminders(newReminders)

            // 6. NAPLÁNOVAT NOTIFIKACE PRO NOVÉ PŘIPOMÍNKY
            if (settingsManager.notificationsEnabled.value) {
                // Načteme offset (o kolik minut dříve připomenout)
                val offsetMinutes = settingsManager.medicineNotificationTime.value.minutesOffset

                // Musíme si je načíst z DB znovu, abychom měli vygenerovaná ID (autoincrement)
                // Pokud bychom použili 'newReminders' přímo, mají ID=0
                val freshReminders = dao.getFuturePlannedRemindersForMedicine(savedMedicineId, now)

                freshReminders.forEach { reminder ->
                    // Vypočítáme čas: Čas léku MÍNUS offset
                    val triggerTime = reminder.plannedDateTime - (offsetMinutes * 60 * 1000)

                    val formattedDosage = if (medicine.dosage % 1.0 == 0.0) {
                        medicine.dosage.toInt().toString()
                    } else {
                        medicine.dosage.toString()
                    }

                    alarmScheduler.scheduleNotification(
                        id = reminder.id,
                        dateTime = triggerTime,
                        title = "Čas na lék",
                        message = "Vezměte si ${medicine.name} ($formattedDosage ${medicine.unit.label})"
                    )
                }
            }
        }
        backupScheduler.scheduleBackup()
    }

    override suspend fun updateReminderStatus(reminderId: Long, isCompleted: Boolean) {
        val reminder = dao.getReminderById(reminderId)
        reminder?.let {
            it.status = if (isCompleted) ReminderStatus.COMPLETED else ReminderStatus.PLANNED
            it.completionDateTime = if (isCompleted) System.currentTimeMillis() else null
            dao.updateReminder(it)

            // Pokud splněno -> zrušit notifikaci (pokud ještě neproběhla)
            if (isCompleted) {
                alarmScheduler.cancelNotification(reminderId)
            }

            backupScheduler.scheduleBackup()
        }
    }

    override suspend fun deleteMedicineAndReminders(medicineId: Long) {
        // 1. Zrušit všechny budoucí alarmy
        val futureReminders = dao.getFuturePlannedRemindersForMedicine(medicineId)
        futureReminders.forEach { alarmScheduler.cancelNotification(it.id) }

        // 2. Smazat z DB
        dao.deleteRemindersForMedicine(medicineId)
        dao.deleteMedicine(medicineId)

        backupScheduler.scheduleBackup()
    }

    override suspend fun getMedicineByIdOnce(id: Long): Medicine? {
        return dao.getMedicineById(id).firstOrNull()
    }

    /**
     * Generuje seznam připomínek.
     * Automaticky pozná, zda jde o PRAVIDELNÝ nebo JEDNORÁZOVÝ lék.
     */
    private fun generateRemindersForMedicine(medicine: Medicine): List<MedicineReminder> {
        val reminders = mutableListOf<MedicineReminder>()

        if (medicine.isRegular) {
            // --- A) PRAVIDELNÉ UŽÍVÁNÍ ---
            val today = LocalDate.now()
            val startDate = Instant.ofEpochMilli(medicine.startDate ?: System.currentTimeMillis())
                .atZone(ZoneId.systemDefault()).toLocalDate()

            // Začínáme od start date, ale ne v minulosti (pokud uživatel edituje starý lék)
            var currentDate = if (startDate.isAfter(today)) startDate else today

            val endDate = medicine.endDate?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            }

            val maxDoses = if (medicine.endingType == EndingType.AFTER_DOSES) {
                medicine.doseCount
            } else {
                null
            }
            var dosesGenerated = 0

            // Generujeme na 90 dní dopředu
            for (i in 0..89) {
                val dateToSchedule = currentDate.plusDays(i.toLong())

                // Kontrola koncového data
                if (endDate != null && dateToSchedule.isAfter(endDate)) {
                    break
                }

                val dayOfWeek = dateToSchedule.dayOfWeek

                // Má se v tento den brát?
                val shouldScheduleForThisDay = when (medicine.regularityType) {
                    RegularityType.DAILY -> true
                    RegularityType.WEEKLY -> medicine.regularDays?.contains(dayOfWeek) ?: false
                }

                if (shouldScheduleForThisDay) {
                    medicine.regularTimes.forEach { timeInMinutes ->
                        // Kontrola počtu dávek
                        if (maxDoses != null && dosesGenerated >= maxDoses) {
                            return reminders
                        }

                        val plannedDateTime =
                            dateToSchedule.atTime(timeInMinutes / 60, timeInMinutes % 60)
                                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                        // Přidáme jen ty, co jsou v budoucnosti (nebo teď)
                        if (plannedDateTime >= System.currentTimeMillis()) {
                            reminders.add(
                                MedicineReminder(
                                    medicineId = medicine.id,
                                    plannedDateTime = plannedDateTime,
                                    status = ReminderStatus.PLANNED
                                )
                            )
                            dosesGenerated++
                        }
                    }
                }
            }
        } else {
            // --- B) JEDNORÁZOVÉ UŽÍVÁNÍ ---
            // Tady to bylo jednoduché, jen projdeme seznam vybraných termínů
            medicine.singleDates?.forEach { dateTimeMillis ->
                if (dateTimeMillis >= System.currentTimeMillis()) {
                    reminders.add(
                        MedicineReminder(
                            medicineId = medicine.id,
                            plannedDateTime = dateTimeMillis,
                            status = ReminderStatus.PLANNED
                        )
                    )
                }
            }
        }
        return reminders
    }
}