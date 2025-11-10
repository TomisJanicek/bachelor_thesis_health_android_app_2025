package cz.tomasjanicek.bp.database.doctor

import androidx.room.*
import cz.tomasjanicek.bp.model.Doctor
import kotlinx.coroutines.flow.Flow

@Dao
interface DoctorDao {

    @Query("SELECT * FROM doctors")
    fun getAll(): Flow<List<Doctor>>

    @Insert
    suspend fun insert(doctor: Doctor): Long

    @Update
    suspend fun update(doctor: Doctor)

    @Delete
    suspend fun delete(doctor: Doctor)

    @Query("SELECT * FROM doctors WHERE id = :id")
    suspend fun getDoctor(id: Long): Doctor?

    @Query("DELETE FROM doctors")
    suspend fun deleteAll()
}