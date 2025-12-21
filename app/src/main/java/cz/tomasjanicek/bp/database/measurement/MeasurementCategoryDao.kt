package cz.tomasjanicek.bp.database.measurement

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import cz.tomasjanicek.bp.model.MeasurementCategory
import cz.tomasjanicek.bp.model.MeasurementCategoryField
import cz.tomasjanicek.bp.model.MeasurementCategoryWithFields
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementCategoryDao {

    // --- Kategorie ---

    @Query("SELECT * FROM measurement_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<MeasurementCategory>>

    @Transaction
    @Query("SELECT * FROM measurement_categories ORDER BY name ASC")
    fun getAllCategoriesWithFields(): Flow<List<MeasurementCategoryWithFields>>

    @Transaction
    @Query("SELECT * FROM measurement_categories WHERE id = :id")
    fun getCategoryWithFieldsById(id: Long): Flow<MeasurementCategoryWithFields?>

    @Insert
    suspend fun insertCategory(category: MeasurementCategory): Long

    @Update
    suspend fun updateCategory(category: MeasurementCategory)

    @Delete
    suspend fun deleteCategory(category: MeasurementCategory)

    @Query("DELETE FROM measurement_categories")
    suspend fun deleteAllCategories()

    // --- Fields (definice hodnot v kategorii) ---

    @Query("SELECT * FROM measurement_category_fields WHERE categoryId = :categoryId ORDER BY id ASC")
    fun getFieldsForCategory(categoryId: Long): Flow<List<MeasurementCategoryField>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertField(field: MeasurementCategoryField): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFields(fields: List<MeasurementCategoryField>)

    @Update
    suspend fun updateField(field: MeasurementCategoryField)

    @Delete
    suspend fun deleteField(field: MeasurementCategoryField)

    @Query("DELETE FROM measurement_category_fields WHERE categoryId = :categoryId")
    suspend fun deleteFieldsByCategoryId(categoryId: Long)

    @Query("SELECT * FROM measurement_categories")
    suspend fun getAllCategoriesList(): List<MeasurementCategory>

    @Query("SELECT * FROM measurement_category_fields")
    suspend fun getAllFieldsList(): List<MeasurementCategoryField>

    @Query("SELECT * FROM measurement_categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): MeasurementCategory?

    @Query("SELECT COUNT(*) FROM measurement_categories")
    suspend fun getCount(): Int
}