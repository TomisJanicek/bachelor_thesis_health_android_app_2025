package cz.tomasjanicek.bp.di

import cz.tomasjanicek.bp.database.injection.IInjectionRepository
import cz.tomasjanicek.bp.model.Injection
import cz.tomasjanicek.bp.model.InjectionCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FakeInjectionRepository @Inject constructor() : IInjectionRepository {

    private val fakeInjections = listOf(
        Injection(
            id = 1,
            name = "Tetanus",
            disease = "Tetanus",
            category = InjectionCategory.MANDATORY,
            date = System.currentTimeMillis()
        )
    )

    override fun getAllInjections(): Flow<List<Injection>> = flowOf(fakeInjections)

    override suspend fun saveInjection(injection: Injection) { /* No-op */ }
    override suspend fun deleteInjection(id: Long) { /* No-op */ }
    override fun getInjectionById(id: Long): Flow<Injection> = flowOf(fakeInjections.first())
}