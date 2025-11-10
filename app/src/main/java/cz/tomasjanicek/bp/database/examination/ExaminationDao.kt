package cz.tomasjanicek.bp.database.examination

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cz.tomasjanicek.bp.model.Examination
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

}