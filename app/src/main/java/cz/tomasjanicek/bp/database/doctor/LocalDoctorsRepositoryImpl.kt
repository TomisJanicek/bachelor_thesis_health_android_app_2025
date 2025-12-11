package cz.tomasjanicek.bp.database.doctor

import cz.tomasjanicek.bp.model.Doctor
import cz.tomasjanicek.bp.model.data.DoctorData
import cz.tomasjanicek.bp.services.BackupScheduler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalDoctorsRepositoryImpl @Inject constructor(
    private val doctorDao: DoctorDao,
    private val backupScheduler: BackupScheduler
) : ILocalDoctorsRepository {

    override fun getAll(): Flow<List<Doctor>> = doctorDao.getAll()

    override suspend fun insert(doctor: Doctor): Long {
        val id = doctorDao.insert(doctor)
        backupScheduler.scheduleBackup()
        return id
    }

    override suspend fun update(doctor: Doctor) {
        doctorDao.update(doctor)
        backupScheduler.scheduleBackup()
    }

    override suspend fun delete(doctor: Doctor) {
        doctorDao.delete(doctor)
        backupScheduler.scheduleBackup()
    }

    // Hard reset - smaže úplně všechno
    override suspend fun deleteAll() {
        doctorDao.deleteAll()
        backupScheduler.scheduleBackup()
    }

    // Soft reset - smaže data uživatele, nechá specializace a obrázky
    override suspend fun resetDoctors() {
        doctorDao.resetAllDoctorData()
        backupScheduler.scheduleBackup()
    }

    // Inicializace - pokud je DB prázdná, naplní ji defaultním seznamem
    override suspend fun initializeDoctorsIfEmpty() {
        val count = doctorDao.getCount()
        if (count == 0) {
            // Zde bereme data z tvého objektu DoctorData
            doctorDao.insertAll(DoctorData.defaultDoctors)
        }
    }

    override suspend fun getDoctor(id: Long?): Doctor? =
        doctorDao.getDoctor(id)

    override fun getDoctorWithData(doctorId: Long): Flow<Doctor?> {
        return doctorDao.getDoctorWithData(doctorId)
    }
}