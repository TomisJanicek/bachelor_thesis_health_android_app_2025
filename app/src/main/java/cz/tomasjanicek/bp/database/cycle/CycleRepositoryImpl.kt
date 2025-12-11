package cz.tomasjanicek.bp.database.cycle

import androidx.compose.animation.core.copy
import cz.tomasjanicek.bp.model.CycleRecord
import cz.tomasjanicek.bp.model.EventType
import cz.tomasjanicek.bp.services.BackupScheduler
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

class CycleRepositoryImpl @Inject constructor(
    private val dao: CycleRecordDao,
    private val backupScheduler: BackupScheduler // <-- Inject
) : ICycleRepository {

    override fun getAllRecords(): Flow<List<CycleRecord>> = dao.getAllRecords()

    override suspend fun getRecordForDate(date: LocalDate): CycleRecord? =
        dao.getRecordForDate(date)

    override suspend fun saveRecord(date: LocalDate, eventType: EventType) {
        val record = CycleRecord(date = date, eventType = eventType)
        dao.upsert(record)
        backupScheduler.scheduleBackup() // <-- Trigger
    }

    override suspend fun deleteRecord(date: LocalDate) {
        dao.deleteByDate(date)
        backupScheduler.scheduleBackup() // <-- Trigger
    }
}