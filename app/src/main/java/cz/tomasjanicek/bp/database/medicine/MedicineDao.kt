package cz.tomasjanicek.bp.database.medicine

import androidx.room.*
import cz.tomasjanicek.bp.model.Medicine
import cz.tomasjanicek.bp.model.MedicineReminder
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {

    // --- Operace pro Nastavení léků (Medicine) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMedicine(medicine: Medicine): Long

    @Query("SELECT * FROM medicines WHERE id = :id")
    fun getMedicineById(id: Long): Flow<Medicine?>

    @Query("DELETE FROM medicines WHERE id = :id")
    suspend fun deleteMedicine(id: Long)

    // --- Operace pro Připomínky (MedicineReminder) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<MedicineReminder>)

    @Update
    suspend fun updateReminder(reminder: MedicineReminder)

    @Query("""
        SELECT * FROM medicine_reminders
        WHERE date(plannedDateTime / 1000, 'unixepoch', 'localtime') = :dateString
        AND status = 'PLANNED'
        ORDER BY plannedDateTime ASC
    """)
    fun getPlannedRemindersForDate(dateString: String): Flow<List<MedicineReminder>>

    @Query("""
        SELECT * FROM medicine_reminders
        WHERE date(plannedDateTime / 1000, 'unixepoch', 'localtime') = :dateString
        AND status = 'COMPLETED'
        ORDER BY completionDateTime DESC
    """)
    fun getCompletedRemindersForDate(dateString: String): Flow<List<MedicineReminder>>


    // --- Původní (stále užitečné) dotazy ---

    @Query("""
        SELECT * FROM medicine_reminders
        WHERE date(plannedDateTime / 1000, 'unixepoch', 'localtime') = date('now', 'localtime')
        AND status = 'PLANNED'
        ORDER BY plannedDateTime ASC
    """)
    fun getTodaysPlannedReminders(): Flow<List<MedicineReminder>>

    @Query("""
        SELECT * FROM medicine_reminders
        WHERE date(plannedDateTime / 1000, 'unixepoch', 'localtime') = date('now', 'localtime')
        AND status = 'COMPLETED'
        ORDER BY completionDateTime DESC
    """)
    fun getTodaysCompletedReminders(): Flow<List<MedicineReminder>>

    @Query("SELECT * FROM medicine_reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): MedicineReminder?

    @Query("DELETE FROM medicine_reminders WHERE medicineId = :medicineId")
    suspend fun deleteRemindersForMedicine(medicineId: Long)

    @Query("""
        DELETE FROM medicine_reminders 
        WHERE medicineId = :medicineId 
        AND plannedDateTime >= :sinceTimestamp
        AND status = 'PLANNED'
    """)
    suspend fun deleteFutureRemindersForMedicine(medicineId: Long, sinceTimestamp: Long)

    @Query("SELECT * FROM medicines")
    suspend fun getAllMedicinesList(): List<Medicine>

    @Query("SELECT * FROM medicine_reminders")
    suspend fun getAllRemindersList(): List<MedicineReminder>

    @Query("DELETE FROM medicines")
    suspend fun deleteAllMedicines()

    @Query("DELETE FROM medicine_reminders")
    suspend fun deleteAllReminders()

    @Query("SELECT * FROM medicine_reminders WHERE medicineId = :medicineId AND status = 'PLANNED' AND plannedDateTime > :now")
    suspend fun getFuturePlannedRemindersForMedicine(medicineId: Long, now: Long = System.currentTimeMillis()): List<MedicineReminder>


}