package cz.tomasjanicek.bp.database.measurement

import cz.tomasjanicek.bp.model.MeasurementCategory
import cz.tomasjanicek.bp.model.MeasurementCategoryField
import cz.tomasjanicek.bp.model.MeasurementCategoryWithFields
import cz.tomasjanicek.bp.services.BackupScheduler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalMeasurementCategoriesRepositoryImpl @Inject constructor(
    private val categoryDao: MeasurementCategoryDao,
    private val backupScheduler: BackupScheduler
) : ILocalMeasurementCategoriesRepository {
    override fun getAllCategories(): Flow<List<MeasurementCategory>> =
        categoryDao.getAllCategories()

    override fun getAllCategoriesWithFields(): Flow<List<MeasurementCategoryWithFields>> =
        categoryDao.getAllCategoriesWithFields()

    override fun getCategoryWithFieldsById(id: Long): Flow<MeasurementCategoryWithFields?> =
        categoryDao.getCategoryWithFieldsById(id)

    override fun getFieldsForCategory(categoryId: Long): Flow<List<MeasurementCategoryField>> =
        categoryDao.getFieldsForCategory(categoryId)

    override suspend fun insertCategory(category: MeasurementCategory): Long {
        val id = categoryDao.insertCategory(category)
        backupScheduler.scheduleBackup()
        return id
    }


    override suspend fun updateCategory(category: MeasurementCategory) {
        categoryDao.updateCategory(category)
        backupScheduler.scheduleBackup()
    }

    override suspend fun deleteCategory(category: MeasurementCategory) {
        categoryDao.deleteCategory(category)
        backupScheduler.scheduleBackup()
    }

    override suspend fun insertField(field: MeasurementCategoryField): Long {
        val id = categoryDao.insertField(field)
        backupScheduler.scheduleBackup()
        return id
    }

    override suspend fun insertFields(fields: List<MeasurementCategoryField>) {
        categoryDao.insertFields(fields)
        backupScheduler.scheduleBackup()
    }

    override suspend fun updateField(field: MeasurementCategoryField) {
        categoryDao.updateField(field)
        backupScheduler.scheduleBackup()
    }

    override suspend fun deleteField(field: MeasurementCategoryField) {
        categoryDao.deleteField(field)
        backupScheduler.scheduleBackup()
    }
}
