package cz.tomasjanicek.bp.database.measurement

import cz.tomasjanicek.bp.model.Measurement
import cz.tomasjanicek.bp.model.MeasurementValue
import cz.tomasjanicek.bp.model.MeasurementWithValues
import kotlinx.coroutines.flow.Flow

interface ILocalMeasurementsRepository {

    fun getAllMeasurements(): Flow<List<Measurement>>

    fun getAllMeasurementsWithValues(): Flow<List<MeasurementWithValues>>

    fun getMeasurementWithValuesById(id: Long): Flow<MeasurementWithValues?>

    fun getMeasurementsByCategory(categoryId: Long): Flow<List<Measurement>>

    suspend fun insertMeasurement(measurement: Measurement): Long

    suspend fun insertMeasurementWithValues(
        measurement: Measurement,
        values: List<MeasurementValue>
    ): Long

    suspend fun updateMeasurement(measurement: Measurement)

    suspend fun updateMeasurementWithValues(
        measurement: Measurement,
        values: List<MeasurementValue>
    )

    suspend fun deleteMeasurement(measurement: Measurement)

    suspend fun deleteAllMeasurements()

    /** NOVÁ METODA: Načte měření s hodnotami v daném časovém rozsahu. */
    fun getMeasurementsWithValuesBetween(startDate: Long, endDate: Long): Flow<List<MeasurementWithValues>>


    fun getValuesByCategory(categoryId: Long): Flow<List<MeasurementValue>>

}