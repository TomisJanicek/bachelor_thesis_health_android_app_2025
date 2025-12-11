package cz.tomasjanicek.bp.database.cycle

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cz.tomasjanicek.bp.model.CycleRecord
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface CycleRecordDao {
    /** Vloží nebo přepíše záznam pro dané datum. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(cycleRecord: CycleRecord)

    /** Smaže záznam pro dané datum. */
    @Query("DELETE FROM cycle_records WHERE date = :date")
    suspend fun deleteByDate(date: LocalDate)

    /** Vrátí všechny záznamy seřazené od nejnovějšího. */
    @Query("SELECT * FROM cycle_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<CycleRecord>>

    /** Vrátí záznam pro konkrétní datum. */
    @Query("SELECT * FROM cycle_records WHERE date = :date")
    suspend fun getRecordForDate(date: LocalDate): CycleRecord?

    @Query("SELECT * FROM cycle_records")
    suspend fun getAllList(): List<CycleRecord>

    @Query("DELETE FROM cycle_records")
    suspend fun deleteAll()
}