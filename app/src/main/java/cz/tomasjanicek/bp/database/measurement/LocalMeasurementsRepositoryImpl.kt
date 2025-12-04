package cz.tomasjanicek.bp.database.measurement

import cz.tomasjanicek.bp.model.Measurement
import cz.tomasjanicek.bp.model.MeasurementValue
import cz.tomasjanicek.bp.model.MeasurementWithValues
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalMeasurementsRepositoryImpl @Inject constructor(
    private val measurementDao: MeasurementDao
) : ILocalMeasurementsRepository {

    override fun getAllMeasurements(): Flow<List<Measurement>> =
        measurementDao.getAllMeasurements()

    override fun getAllMeasurementsWithValues(): Flow<List<MeasurementWithValues>> =
        measurementDao.getAllMeasurementsWithValues()

    override fun getMeasurementWithValuesById(id: Long): Flow<MeasurementWithValues?> =
        measurementDao.getMeasurementWithValuesById(id)

    override fun getMeasurementsByCategory(categoryId: Long): Flow<List<Measurement>> =
        measurementDao.getMeasurementsByCategory(categoryId)

    override suspend fun insertMeasurement(measurement: Measurement): Long =
        measurementDao.insertMeasurement(measurement)

    override suspend fun insertMeasurementWithValues(
        measurement: Measurement,
        values: List<MeasurementValue>
    ): Long = measurementDao.insertMeasurementWithValues(measurement, values)

    override suspend fun updateMeasurement(measurement: Measurement) {
        measurementDao.updateMeasurement(measurement)
    }

    override suspend fun updateMeasurementWithValues(
        measurement: Measurement,
        values: List<MeasurementValue>
    ) {
        measurementDao.updateMeasurementWithValues(measurement, values)
    }

    override suspend fun deleteMeasurement(measurement: Measurement) {
        measurementDao.deleteMeasurement(measurement)
    }

    override suspend fun deleteAllMeasurements() {
        measurementDao.deleteAllMeasurements()
    }

    override fun getValuesByCategory(categoryId: Long): Flow<List<MeasurementValue>> =
        measurementDao.getValuesByCategory(categoryId)
}