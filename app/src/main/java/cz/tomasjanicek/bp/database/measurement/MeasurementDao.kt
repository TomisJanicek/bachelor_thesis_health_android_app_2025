package cz.tomasjanicek.bp.database.measurement

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import cz.tomasjanicek.bp.model.Measurement
import cz.tomasjanicek.bp.model.MeasurementValue
import cz.tomasjanicek.bp.model.MeasurementWithValues
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {

    // --- ZÁKLADNÍ MĚŘENÍ ---

    @Query("SELECT * FROM measurements ORDER BY measuredAt DESC")
    fun getAllMeasurements(): Flow<List<Measurement>>

    @Transaction
    @Query("SELECT * FROM measurements ORDER BY measuredAt DESC")
    fun getAllMeasurementsWithValues(): Flow<List<MeasurementWithValues>>

    @Transaction
    @Query("SELECT * FROM measurements WHERE id = :id")
    fun getMeasurementWithValuesById(id: Long): Flow<MeasurementWithValues?>

    @Query("SELECT * FROM measurements WHERE categoryId = :categoryId ORDER BY measuredAt DESC")
    fun getMeasurementsByCategory(categoryId: Long): Flow<List<Measurement>>

    @Insert
    suspend fun insertMeasurement(measurement: Measurement): Long

    @Update
    suspend fun updateMeasurement(measurement: Measurement)

    @Delete
    suspend fun deleteMeasurement(measurement: Measurement)

    @Query("DELETE FROM measurements")
    suspend fun deleteAllMeasurements()

    // --- HODNOTY MĚŘENÍ ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertValues(values: List<MeasurementValue>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertValue(value: MeasurementValue): Long

    @Update
    suspend fun updateValue(value: MeasurementValue)

    @Delete
    suspend fun deleteValue(value: MeasurementValue)

    @Query("DELETE FROM measurement_values WHERE measurementId = :measurementId")
    suspend fun deleteValuesForMeasurement(measurementId: Long)

    // --- HELPER TRANSAKCE ---

    /**
     * Vloží jedno měření + všechny jeho hodnoty v jedné transakci.
     */
    @Transaction
    suspend fun insertMeasurementWithValues(
        measurement: Measurement,
        values: List<MeasurementValue>
    ): Long {
        val id = insertMeasurement(measurement)
        if (values.isNotEmpty()) {
            val withIds = values.map { it.copy(measurementId = id) }
            insertValues(withIds)
        }
        return id
    }

    /**
     * Aktualizuje existující měření a kompletně přepíše jeho hodnoty.
     */
    @Transaction
    suspend fun updateMeasurementWithValues(
        measurement: Measurement,
        values: List<MeasurementValue>
    ) {
        updateMeasurement(measurement)
        deleteValuesForMeasurement(measurement.id)
        if (values.isNotEmpty()) {
            val withIds = values.map { it.copy(measurementId = measurement.id) }
            insertValues(withIds)
        }
    }
    @Query("""
    SELECT v.* FROM measurement_values AS v
    INNER JOIN measurements AS m ON v.measurementId = m.id
    WHERE m.categoryId = :categoryId
""")
    fun getValuesByCategory(categoryId: Long): Flow<List<MeasurementValue>>
}