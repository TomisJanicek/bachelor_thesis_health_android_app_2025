package cz.tomasjanicek.bp.database.examination

import cz.tomasjanicek.bp.model.Examination
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class LocalExaminationsRepositoryImpl @Inject constructor(
    private val examinationDao: ExaminationDao
) : ILocalExaminationsRepository {

    override fun getAll(): Flow<List<Examination>> = examinationDao.getAll()

    override suspend fun insert(examination: Examination): Long =
        examinationDao.insert(examination)

    override suspend fun update(examination: Examination) {
        examinationDao.update(examination)
    }

    override suspend fun delete(examination: Examination) {
        examinationDao.delete(examination)
    }

    override suspend fun getExamination(id: Long): Examination =
        examinationDao.getExamination(id)
}