package cz.tomasjanicek.bp.database.injection

import cz.tomasjanicek.bp.model.Injection
import cz.tomasjanicek.bp.services.BackupScheduler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InjectionRepositoryImpl @Inject constructor(
    private val injectionDao: InjectionDao,
    private val backupScheduler: BackupScheduler
) : IInjectionRepository {
    override fun getAllInjections(): Flow<List<Injection>> = injectionDao.getAllInjections()
    override suspend fun saveInjection(injection: Injection) {
        injectionDao.insertOrUpdateInjection(injection)
        backupScheduler.scheduleBackup()
    }

    override suspend fun deleteInjection(id: Long) {
        injectionDao.deleteInjectionById(id)
        backupScheduler.scheduleBackup()
    }
    override fun getInjectionById(id: Long): Flow<Injection> = injectionDao.getInjectionById(id)
}