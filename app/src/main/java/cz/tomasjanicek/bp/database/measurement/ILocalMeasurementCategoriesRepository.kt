package cz.tomasjanicek.bp.database.measurement

import cz.tomasjanicek.bp.model.MeasurementCategory
import cz.tomasjanicek.bp.model.MeasurementCategoryField
import cz.tomasjanicek.bp.model.MeasurementCategoryWithFields
import kotlinx.coroutines.flow.Flow

interface ILocalMeasurementCategoriesRepository {

    fun getAllCategories(): Flow<List<MeasurementCategory>>

    fun getAllCategoriesWithFields(): Flow<List<MeasurementCategoryWithFields>>

    fun getCategoryWithFieldsById(id: Long): Flow<MeasurementCategoryWithFields?>

    fun getFieldsForCategory(categoryId: Long): Flow<List<MeasurementCategoryField>>

    suspend fun insertCategory(category: MeasurementCategory): Long

    suspend fun updateCategory(category: MeasurementCategory)

    suspend fun deleteCategory(category: MeasurementCategory)

    suspend fun insertField(field: MeasurementCategoryField): Long

    suspend fun insertFields(fields: List<MeasurementCategoryField>)

    suspend fun updateField(field: MeasurementCategoryField)

    suspend fun deleteField(field: MeasurementCategoryField)
}