package cz.tomasjanicek.bp.database.doctor

import cz.tomasjanicek.bp.model.Doctor
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalDoctorsRepositoryImpl @Inject constructor(
    private val doctorDao: DoctorDao
) : ILocalDoctorsRepository {

    override fun getAll(): Flow<List<Doctor>> = doctorDao.getAll()

    override suspend fun insert(doctor: Doctor): Long =
        doctorDao.insert(doctor)

    override suspend fun update(doctor: Doctor) {
        doctorDao.update(doctor)
    }

    override suspend fun delete(doctor: Doctor) {
        doctorDao.delete(doctor)
    }

    override suspend fun getDoctor(id: Long): Doctor? =
        doctorDao.getDoctor(id)
}