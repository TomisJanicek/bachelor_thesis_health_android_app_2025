package cz.tomasjanicek.bp.database.measurement

import cz.tomasjanicek.bp.model.Measurement
import cz.tomasjanicek.bp.model.MeasurementValue
import cz.tomasjanicek.bp.model.MeasurementWithValues
import cz.tomasjanicek.bp.services.BackupScheduler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalMeasurementsRepositoryImpl @Inject constructor(
    private val measurementDao: MeasurementDao,
    private val backupScheduler: BackupScheduler
) : ILocalMeasurementsRepository {

    override fun getAllMeasurements(): Flow<List<Measurement>> =
        measurementDao.getAllMeasurements()

    override fun getAllMeasurementsWithValues(): Flow<List<MeasurementWithValues>> =
        measurementDao.getAllMeasurementsWithValues()

    override fun getMeasurementWithValuesById(id: Long): Flow<MeasurementWithValues?> =
        measurementDao.getMeasurementWithValuesById(id)

    override fun getMeasurementsByCategory(categoryId: Long): Flow<List<Measurement>> =
        measurementDao.getMeasurementsByCategory(categoryId)

    override suspend fun insertMeasurement(measurement: Measurement): Long {
        val id = measurementDao.insertMeasurement(measurement)
        backupScheduler.scheduleBackup()
        return id
    }

    override suspend fun insertMeasurementWithValues(
        measurement: Measurement,
        values: List<MeasurementValue>
    ): Long {
        val id = measurementDao.insertMeasurementWithValues(measurement, values)
        backupScheduler.scheduleBackup()
        return id
    }

    override suspend fun updateMeasurement(measurement: Measurement) {
        measurementDao.updateMeasurement(measurement)
        backupScheduler.scheduleBackup()
    }

    override suspend fun updateMeasurementWithValues(
        measurement: Measurement,
        values: List<MeasurementValue>
    ) {
        measurementDao.updateMeasurementWithValues(measurement, values)
        backupScheduler.scheduleBackup()
    }

    override suspend fun deleteMeasurement(measurement: Measurement) {
        measurementDao.deleteMeasurement(measurement)
        backupScheduler.scheduleBackup()
    }

    override suspend fun deleteAllMeasurements() {
        measurementDao.deleteAllMeasurements()
        backupScheduler.scheduleBackup()
    }

    override fun getMeasurementsWithValuesBetween(
        startDate: Long,
        endDate: Long
    ): Flow<List<MeasurementWithValues>> {
        return measurementDao.getMeasurementsWithValuesBetween(startDate, endDate)
    }

    override fun getValuesByCategory(categoryId: Long): Flow<List<MeasurementValue>> =
        measurementDao.getValuesByCategory(categoryId)
}