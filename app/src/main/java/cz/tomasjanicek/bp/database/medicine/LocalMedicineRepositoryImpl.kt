package cz.tomasjanicek.bp.database.medicine

import cz.tomasjanicek.bp.model.EndingType
import cz.tomasjanicek.bp.model.Medicine
import cz.tomasjanicek.bp.model.MedicineReminder
import cz.tomasjanicek.bp.model.RegularityType
import cz.tomasjanicek.bp.model.ReminderStatus
import cz.tomasjanicek.bp.services.BackupScheduler
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
    private val backupScheduler: BackupScheduler
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

    override suspend fun saveMedicineAndGenerateReminders(medicine: Medicine) {
        // 1. Uložíme nastavení léku (nebo ho aktualizujeme) a získáme jeho platné ID
        val savedMedicineId = dao.saveMedicine(medicine)

        // Vytvoříme si kopii léku už se správným ID
        val medicineWithId = medicine.copy(id = savedMedicineId)

        // 2. Smažeme VŠECHNY budoucí (ještě neproběhnuté) připomínky pro tento lék
        // To je důležité při úpravě - staré plánování musí pryč.
        dao.deleteFutureRemindersForMedicine(savedMedicineId, System.currentTimeMillis())

        // 3. Vygenerujeme NOVÉ připomínky
        val newReminders = generateRemindersForMedicine(medicineWithId)

        // 4. Pokud jsme nějaké vygenerovali, uložíme je do databáze
        if (newReminders.isNotEmpty()) {
            dao.insertReminders(newReminders)
        }
        backupScheduler.scheduleBackup()
    }

    override suspend fun updateReminderStatus(reminderId: Long, isCompleted: Boolean) {
        val reminder = dao.getReminderById(reminderId)
        reminder?.let {
            it.status = if (isCompleted) ReminderStatus.COMPLETED else ReminderStatus.PLANNED
            it.completionDateTime = if (isCompleted) System.currentTimeMillis() else null
            dao.updateReminder(it)
            backupScheduler.scheduleBackup()
        }
    }

    override suspend fun deleteMedicineAndReminders(medicineId: Long) {
        // Smažeme všechny připomínky (minulé i budoucí)
        dao.deleteRemindersForMedicine(medicineId)
        // A následně i samotné nastavení léku
        dao.deleteMedicine(medicineId)
        backupScheduler.scheduleBackup()
    }

    override suspend fun getMedicineByIdOnce(id: Long): Medicine? {
        // .firstOrNull() vezme první hodnotu z Flow a pak ho zruší.
        // Přesně to potřebujeme ve ViewModelu.
        return dao.getMedicineById(id).firstOrNull()
    }

    /**
     * Klíčová privátní funkce, která na základě nastavení léku vygeneruje
     * konkrétní záznamy `MedicineReminder`.
     * @param medicine Lék s již platným ID.
     */
    private fun generateRemindersForMedicine(medicine: Medicine): List<MedicineReminder> {
        val reminders = mutableListOf<MedicineReminder>()

        if (medicine.isRegular) {
            // --- Logika pro PRAVIDELNÉ léky ---
            val today = LocalDate.now()
            val startDate = Instant.ofEpochMilli(medicine.startDate ?: 0)
                .atZone(ZoneId.systemDefault()).toLocalDate()

            // Začneme od dnešního dne, nebo od startovního data, pokud je v budoucnosti.
            var currentDate = if (startDate.isAfter(today)) startDate else today

            // Koncové datum pro kontrolu ve smyčce (pokud existuje)
            val endDate = medicine.endDate?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            }

            // Maximální počet dávek pro kontrolu ve smyčce
            val maxDoses = if (medicine.endingType == EndingType.AFTER_DOSES) {
                medicine.doseCount
            } else {
                null
            }
            var dosesGenerated = 0

            // Generujeme připomínky na max. 90 dní dopředu, ale respektujeme i další podmínky
            for (i in 0..89) {
                val dateToSchedule = currentDate.plusDays(i.toLong())

                // --- PODMÍNKA 1: Ukončení k datu ---
                if (endDate != null && dateToSchedule.isAfter(endDate)) {
                    break // Přerušíme smyčku, pokud jsme za koncovým datem
                }

                val dayOfWeek = dateToSchedule.dayOfWeek

                val shouldScheduleForThisDay = when (medicine.regularityType) {
                    RegularityType.DAILY -> true
                    RegularityType.WEEKLY -> medicine.regularDays?.contains(dayOfWeek) ?: false
                }

                if (shouldScheduleForThisDay) {
                    medicine.regularTimes.forEach { timeInMinutes ->
                        // --- PODMÍNKA 2: Ukončení po počtu dávek ---
                        if (maxDoses != null && dosesGenerated >= maxDoses) {
                            return reminders // Ukončíme celou funkci, máme dost dávek
                        }

                        val plannedDateTime =
                            dateToSchedule.atTime(timeInMinutes / 60, timeInMinutes % 60)
                                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                        // Přidáme pouze připomínky, které ještě neproběhly
                        if (plannedDateTime >= System.currentTimeMillis()) {
                            reminders.add(
                                MedicineReminder(
                                    medicineId = medicine.id,
                                    plannedDateTime = plannedDateTime,
                                    status = ReminderStatus.PLANNED
                                )
                            )
                            dosesGenerated++ // Zvýšíme počítadlo vygenerovaných dávek
                        }
                    }
                }
            }
        } else {
            // --- Logika pro JEDNORÁZOVÉ léky (zůstává stejná) ---
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