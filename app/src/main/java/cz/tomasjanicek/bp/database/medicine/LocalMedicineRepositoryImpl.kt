package cz.tomasjanicek.bp.database.medicine

import cz.tomasjanicek.bp.model.Medicine
import cz.tomasjanicek.bp.model.MedicineReminder
import cz.tomasjanicek.bp.model.RegularityType
import cz.tomasjanicek.bp.model.ReminderStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject
import java.time.Instant
import kotlinx.coroutines.flow.firstOrNull // Přidej tento import
import java.time.format.DateTimeFormatter

class LocalMedicineRepositoryImpl @Inject constructor(
    private val dao: MedicineDao
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
    }

    override suspend fun updateReminderStatus(reminderId: Long, isCompleted: Boolean) {
        val reminder = dao.getReminderById(reminderId)
        reminder?.let {
            it.status = if (isCompleted) ReminderStatus.COMPLETED else ReminderStatus.PLANNED
            it.completionDateTime = if (isCompleted) System.currentTimeMillis() else null
            dao.updateReminder(it)
        }
    }

    override suspend fun deleteMedicineAndReminders(medicineId: Long) {
        // Smažeme všechny připomínky (minulé i budoucí)
        dao.deleteRemindersForMedicine(medicineId)
        // A následně i samotné nastavení léku
        dao.deleteMedicine(medicineId)
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

            // Začneme od dnešního dne, nebo od startovního data, pokud je v budoucnosti.
            val today = LocalDate.now()
            val startDate = Instant.ofEpochMilli(medicine.startDate ?: 0).atZone(ZoneId.systemDefault()).toLocalDate()
            var currentDate = if (startDate.isAfter(today)) startDate else today

            // Vygenerujeme připomínky na 90 dní dopředu (dostatečná rezerva)
            for (i in 0..89) {
                val dateToSchedule = currentDate.plusDays(i.toLong())
                val dayOfWeek = dateToSchedule.dayOfWeek

                val shouldScheduleForThisDay = when (medicine.regularityType) {
                    RegularityType.DAILY -> true // Pro denní typ plánujeme každý den
                    RegularityType.WEEKLY -> medicine.regularDays?.contains(dayOfWeek) ?: false // Pro týdenní jen vybrané dny
                }

                if (shouldScheduleForThisDay) {
                    medicine.regularTimes.forEach { timeInMinutes ->
                        // Převedeme čas na milisekundy
                        val plannedDateTime = dateToSchedule.atTime(timeInMinutes / 60, timeInMinutes % 60)
                            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                        reminders.add(
                            MedicineReminder(
                                medicineId = medicine.id,
                                plannedDateTime = plannedDateTime,
                                status = ReminderStatus.PLANNED
                            )
                        )
                    }
                }
            }
        } else {
            // --- Logika pro JEDNORÁZOVÉ léky ---
            medicine.singleDates?.forEach { dateTimeMillis ->
                // Přidáme jen ty, které ještě neproběhly
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