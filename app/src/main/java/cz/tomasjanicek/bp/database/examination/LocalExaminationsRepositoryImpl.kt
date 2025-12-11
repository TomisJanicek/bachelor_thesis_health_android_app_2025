package cz.tomasjanicek.bp.database.examination

import cz.tomasjanicek.bp.model.Examination
import cz.tomasjanicek.bp.model.ExaminationWithDoctor
import cz.tomasjanicek.bp.services.BackupScheduler
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class LocalExaminationsRepositoryImpl @Inject constructor(
    private val examinationDao: ExaminationDao,
    private val backupScheduler: BackupScheduler
) : ILocalExaminationsRepository {

    override fun getAll(): Flow<List<Examination>> = examinationDao.getAll()

    override suspend fun insert(examination: Examination): Long {
        val id = examinationDao.insert(examination)
        backupScheduler.scheduleBackup()
        return id
    }

    override suspend fun update(examination: Examination) {
        examinationDao.update(examination)
        backupScheduler.scheduleBackup()
    }

    override suspend fun delete(examination: Examination) {
        examinationDao.delete(examination)
        backupScheduler.scheduleBackup()
    }

    override suspend fun getExamination(id: Long): Examination =
        examinationDao.getExamination(id)

    override fun getAllWithDoctors(): Flow<List<ExaminationWithDoctor>> =
        examinationDao.getAllWithDoctors()

    override suspend fun getExaminationsByDoctor(doctorId: Long): List<Examination> {
        return examinationDao.getExaminationsByDoctor(doctorId)
    }

    override fun getExaminationWithDoctorById(id: Long): Flow<ExaminationWithDoctor?> {
        return examinationDao.getExaminationWithDoctorById(id)
    }
}