package cz.tomasjanicek.bp.database.injection

import cz.tomasjanicek.bp.model.Injection
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InjectionRepositoryImpl @Inject constructor(
    private val injectionDao: InjectionDao
) : IInjectionRepository {
    override fun getAllInjections(): Flow<List<Injection>> = injectionDao.getAllInjections()
    override suspend fun saveInjection(injection: Injection) = injectionDao.insertOrUpdateInjection(injection)
    override suspend fun deleteInjection(id: Long) = injectionDao.deleteInjectionById(id)
    override fun getInjectionById(id: Long): Flow<Injection> = injectionDao.getInjectionById(id)
}