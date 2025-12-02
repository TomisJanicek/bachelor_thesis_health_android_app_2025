package cz.tomasjanicek.bp.database.doctor

import cz.tomasjanicek.bp.model.Doctor
import kotlinx.coroutines.flow.Flow

interface ILocalDoctorsRepository {
    fun getAll(): Flow<List<Doctor>>

    suspend fun insert(doctor: Doctor): Long

    suspend fun update(doctor: Doctor)

    suspend fun delete(doctor: Doctor)

    suspend fun getDoctor(id: Long?): Doctor?

    fun getDoctorWithData(doctorId: Long): Flow<Doctor?>
}