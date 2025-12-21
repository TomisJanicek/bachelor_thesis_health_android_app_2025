package cz.tomasjanicek.bp.database.examination

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import cz.tomasjanicek.bp.model.Examination
import cz.tomasjanicek.bp.model.ExaminationWithDoctor
import kotlinx.coroutines.flow.Flow

@Dao
interface ExaminationDao {
    @Query("SELECT * FROM examinations")
    fun getAll(): Flow<List<Examination>>

    // --- OPRAVA ZDE: Přidáno onConflict = OnConflictStrategy.REPLACE ---
    // Toto zajistí, že pokud vložíme záznam s ID, které už existuje,
    // Room ho přepíše (aktualizuje) místo toho, aby shodil aplikaci chybou.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(examination: Examination): Long

    @Update
    suspend fun update(examination: Examination)

    @Query("SELECT * FROM examinations WHERE id = :id")
    suspend fun getExamination(id: Long): Examination

    @Delete
    suspend fun delete(examination: Examination)

    @Query("DELETE FROM examinations")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(examinations: List<Examination>)

    @Transaction
    @Query("SELECT * FROM examinations")
    fun getAllWithDoctors(): Flow<List<ExaminationWithDoctor>>

    @Query("SELECT * FROM examinations WHERE doctorId = :doctorId ORDER BY dateTime DESC")
    suspend fun getExaminationsByDoctor(doctorId: Long): List<Examination>

    @Transaction
    @Query("SELECT * FROM examinations WHERE id = :examinationId")
    fun getExaminationWithDoctorById(examinationId: Long): Flow<ExaminationWithDoctor?>

    @Query("SELECT * FROM examinations")
    suspend fun getAllList(): List<Examination>
}