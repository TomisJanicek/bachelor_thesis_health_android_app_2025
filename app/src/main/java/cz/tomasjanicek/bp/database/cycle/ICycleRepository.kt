package cz.tomasjanicek.bp.database.cycle

import cz.tomasjanicek.bp.model.CycleRecord
import cz.tomasjanicek.bp.model.EventType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ICycleRepository {
    fun getAllRecords(): Flow<List<CycleRecord>>
    suspend fun getRecordForDate(date: LocalDate): CycleRecord?
    suspend fun saveRecord(date: LocalDate, eventType: EventType)
    suspend fun deleteRecord(date: LocalDate)
}