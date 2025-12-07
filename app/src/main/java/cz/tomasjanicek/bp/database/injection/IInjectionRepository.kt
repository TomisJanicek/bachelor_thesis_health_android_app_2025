package cz.tomasjanicek.bp.database.injection

import androidx.room.Query
import cz.tomasjanicek.bp.model.Injection
import kotlinx.coroutines.flow.Flow

interface IInjectionRepository {
    fun getAllInjections(): Flow<List<Injection>>
    suspend fun saveInjection(injection: Injection)
    suspend fun deleteInjection(id: Long)

    fun getInjectionById(id: Long): Flow<Injection>
}