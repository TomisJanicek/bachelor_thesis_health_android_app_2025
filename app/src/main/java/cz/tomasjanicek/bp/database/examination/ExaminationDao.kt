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

    @Insert
    suspend fun insert(examination: Examination): Long

    @Update
    suspend fun update(examination: Examination)

    @Query("SELECT * FROM examinations WHERE id = :id")
    suspend fun getExamination(id: Long): Examination

    @Delete
    suspend fun delete(examination: Examination)

    @Query("DELETE FROM examinations")
    suspend fun deleteAll()

    // --- PŘIDEJ TUTO METODU ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(examinations: List<Examination>)
    // --- KONEC PŘIDANÉ METODY ---

    @Transaction // Nezbytné pro relační dotazy!
    @Query("SELECT * FROM examinations")
    fun getAllWithDoctors(): Flow<List<ExaminationWithDoctor>>


}