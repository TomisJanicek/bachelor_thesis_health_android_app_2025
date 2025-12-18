package cz.tomasjanicek.bp.database.medicine

import cz.tomasjanicek.bp.model.Medicine
import cz.tomasjanicek.bp.model.MedicineReminder
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface IMedicineRepository {
    /** Načte naplánované připomínky pro zadaný den. */
    fun getPlannedRemindersForDate(date: LocalDate): Flow<List<MedicineReminder>>

    /** Načte dokončené připomínky pro zadaný den. */
    fun getCompletedRemindersForDate(date: LocalDate): Flow<List<MedicineReminder>>

    fun getTodaysPlannedReminders(): Flow<List<MedicineReminder>>
    fun getTodaysCompletedReminders(): Flow<List<MedicineReminder>>

    fun getMedicineById(id: Long): Flow<Medicine?>

    suspend fun saveMedicineAndGenerateReminders(medicine: Medicine)
    suspend fun updateReminderStatus(reminderId: Long, isCompleted: Boolean)
    suspend fun deleteMedicineAndReminders(medicineId: Long)

    suspend fun getMedicineByIdOnce(id: Long): Medicine?
}