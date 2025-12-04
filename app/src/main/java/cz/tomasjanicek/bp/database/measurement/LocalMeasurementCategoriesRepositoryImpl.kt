package cz.tomasjanicek.bp.database.measurement

import cz.tomasjanicek.bp.model.MeasurementCategory
import cz.tomasjanicek.bp.model.MeasurementCategoryField
import cz.tomasjanicek.bp.model.MeasurementCategoryWithFields
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalMeasurementCategoriesRepositoryImpl @Inject constructor(
    private val categoryDao: MeasurementCategoryDao
) : ILocalMeasurementCategoriesRepository {
    override fun getAllCategories(): Flow<List<MeasurementCategory>> =
        categoryDao.getAllCategories()

    override fun getAllCategoriesWithFields(): Flow<List<MeasurementCategoryWithFields>> =
        categoryDao.getAllCategoriesWithFields()

    override fun getCategoryWithFieldsById(id: Long): Flow<MeasurementCategoryWithFields?> =
        categoryDao.getCategoryWithFieldsById(id)

    override fun getFieldsForCategory(categoryId: Long): Flow<List<MeasurementCategoryField>> =
        categoryDao.getFieldsForCategory(categoryId)

    override suspend fun insertCategory(category: MeasurementCategory): Long =
        categoryDao.insertCategory(category)

    override suspend fun updateCategory(category: MeasurementCategory) {
        categoryDao.updateCategory(category)
    }

    override suspend fun deleteCategory(category: MeasurementCategory) {
        categoryDao.deleteCategory(category)
    }

    override suspend fun insertField(field: MeasurementCategoryField): Long =
        categoryDao.insertField(field)

    override suspend fun insertFields(fields: List<MeasurementCategoryField>) {
        categoryDao.insertFields(fields)
    }

    override suspend fun updateField(field: MeasurementCategoryField) {
        categoryDao.updateField(field)
    }

    override suspend fun deleteField(field: MeasurementCategoryField) {
        categoryDao.deleteField(field)
    }
}
