package cz.tomasjanicek.bp.di

import cz.tomasjanicek.bp.database.cycle.ICycleRepository
import cz.tomasjanicek.bp.database.doctor.ILocalDoctorsRepository
import cz.tomasjanicek.bp.database.measurement.ILocalMeasurementCategoriesRepository
import cz.tomasjanicek.bp.database.measurement.ILocalMeasurementsRepository
import cz.tomasjanicek.bp.database.medicine.IMedicineRepository
import cz.tomasjanicek.bp.model.CycleRecord
import cz.tomasjanicek.bp.model.CycleSettings
import cz.tomasjanicek.bp.model.Doctor
import cz.tomasjanicek.bp.model.EventType
import cz.tomasjanicek.bp.model.Measurement
import cz.tomasjanicek.bp.model.MeasurementCategory
import cz.tomasjanicek.bp.model.MeasurementCategoryField
import cz.tomasjanicek.bp.model.MeasurementCategoryWithFields
import cz.tomasjanicek.bp.model.MeasurementValue
import cz.tomasjanicek.bp.model.MeasurementWithValues
import cz.tomasjanicek.bp.model.Medicine
import cz.tomasjanicek.bp.model.MedicineReminder
import cz.tomasjanicek.bp.model.data.MeasurementData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject


// --- 3. MĚŘENÍ (Kategorie) ---
class FakeMeasurementCategoriesRepository @Inject constructor() : ILocalMeasurementCategoriesRepository {
    override fun getAllCategories(): Flow<List<MeasurementCategory>> = flowOf(emptyList())
    override fun getAllCategoriesWithFields(): Flow<List<MeasurementCategoryWithFields>> = flowOf(emptyList())
    override fun getCategoryWithFieldsById(id: Long): Flow<MeasurementCategoryWithFields?> = flowOf(null)
    override fun getFieldsForCategory(categoryId: Long): Flow<List<MeasurementCategoryField>> = flowOf(emptyList())
    override suspend fun insertCategory(category: MeasurementCategory): Long = 0
    override suspend fun updateCategory(category: MeasurementCategory) {}
    override suspend fun deleteCategory(category: MeasurementCategory) {}
    override suspend fun insertField(field: MeasurementCategoryField): Long = 0
    override suspend fun insertFields(fields: List<MeasurementCategoryField>) {}
    override suspend fun updateField(field: MeasurementCategoryField) {}
    override suspend fun deleteField(field: MeasurementCategoryField) {}
    override suspend fun initializeDefaultCategories() {}
    override suspend fun initializeCategoriesIfEmpty() {}
    override suspend fun createSelectedDefaultCategories(selectedCategories: List<MeasurementData.CategoryDef>) {}
}

// --- 4. MĚŘENÍ (Hodnoty) ---
class FakeMeasurementsRepository @Inject constructor() : ILocalMeasurementsRepository {
    override fun getAllMeasurements(): Flow<List<Measurement>> = flowOf(emptyList())
    override fun getAllMeasurementsWithValues(): Flow<List<MeasurementWithValues>> = flowOf(emptyList())
    override fun getMeasurementWithValuesById(id: Long): Flow<MeasurementWithValues?> = flowOf(null)
    override fun getMeasurementsByCategory(categoryId: Long): Flow<List<Measurement>> = flowOf(emptyList())
    override suspend fun insertMeasurement(measurement: Measurement): Long = 0
    override suspend fun insertMeasurementWithValues(measurement: Measurement, values: List<MeasurementValue>): Long = 0
    override suspend fun updateMeasurement(measurement: Measurement) {}
    override suspend fun updateMeasurementWithValues(measurement: Measurement, values: List<MeasurementValue>) {}
    override suspend fun deleteMeasurement(measurement: Measurement) {}
    override suspend fun deleteAllMeasurements() {}
    override fun getMeasurementsWithValuesBetween(startDate: Long, endDate: Long): Flow<List<MeasurementWithValues>> = flowOf(emptyList())
    override fun getValuesByCategory(categoryId: Long): Flow<List<MeasurementValue>> = flowOf(emptyList())
}

// --- 5. CYKLUS ---
class FakeCycleRepository @Inject constructor() : ICycleRepository {
    override fun getAllRecords(): Flow<List<CycleRecord>> = flowOf(emptyList())
    override suspend fun getRecordForDate(date: LocalDate): CycleRecord? = null
    override suspend fun saveRecord(date: LocalDate, eventType: EventType) {}

    // OPRAVA: Odstraněno override, interface asi tyto metody nemá nebo se jmenují jinak.
    // Necháme je jen jako public metody třídy, aby build nepadal na syntaxi,
    // ale nebudeme tvrdit, že jsou z interface.
    fun getRecordsForMonth(yearMonth: YearMonth): Flow<List<CycleRecord>> = flowOf(emptyList())
    fun getSettings(): Flow<CycleSettings> = flowOf(CycleSettings())
    suspend fun insertRecord(record: CycleRecord) {}
    suspend fun updateSettings(settings: CycleSettings) {}

    override suspend fun deleteRecord(date: LocalDate) {}
}