package cz.tomasjanicek.bp.database.measurement

import cz.tomasjanicek.bp.model.MeasurementCategory
import cz.tomasjanicek.bp.model.MeasurementCategoryField
import cz.tomasjanicek.bp.model.MeasurementCategoryWithFields
import cz.tomasjanicek.bp.model.data.MeasurementData
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
    override suspend fun initializeDefaultCategories() {
        // Projdeme všechny definované defaultní kategorie
        for (def in MeasurementData.defaultCategories) {

            // 1. Zkontrolujeme, zda už kategorie existuje podle jména
            val existing = categoryDao.getCategoryByName(def.name)

            if (existing == null) {
                // 2. Pokud neexistuje, vytvoříme kategorii
                val newCategory = MeasurementCategory(
                    name = def.name,
                    description = def.description
                )

                // Vložíme a získáme vygenerované ID
                val newCategoryId = categoryDao.insertCategory(newCategory)

                // 3. Vytvoříme a vložíme pole pro tuto kategorii
                val fieldsToInsert = def.fields.map { fieldDef ->
                    MeasurementCategoryField(
                        categoryId = newCategoryId, // Použijeme ID nové kategorie
                        name = fieldDef.name,
                        label = fieldDef.label,
                        unit = fieldDef.unit,
                        minValue = fieldDef.min,
                        maxValue = fieldDef.max
                    )
                }

                categoryDao.insertFields(fieldsToInsert)
            }
            // Pokud existuje, neděláme nic (respektujeme uživatelská data)
        }
    }
    override suspend fun initializeCategoriesIfEmpty() {
        val count = categoryDao.getCount()
        if (count == 0) {
            // Tabulka je prázdná -> Vkládáme defaultní data
            for (def in MeasurementData.defaultCategories) {
                // 1. Vytvoříme a vložíme kategorii
                val newCategory = MeasurementCategory(
                    name = def.name,
                    description = def.description
                )

                // insertCategory vrací nové ID (Long)
                val newCategoryId = categoryDao.insertCategory(newCategory)

                // 2. Vytvoříme pole pro tuto kategorii s použitím získaného ID
                val fieldsToInsert = def.fields.map { fieldDef ->
                    MeasurementCategoryField(
                        categoryId = newCategoryId, // Vazba na rodiče
                        name = fieldDef.name,
                        label = fieldDef.label,
                        unit = fieldDef.unit,
                        minValue = fieldDef.min,
                        maxValue = fieldDef.max
                    )
                }

                // 3. Vložíme pole hromadně
                categoryDao.insertFields(fieldsToInsert)
            }
        }
    }
    override suspend fun createSelectedDefaultCategories(selectedCategories: List<MeasurementData.CategoryDef>) {
        for (def in selectedCategories) {
            // 1. Zkontrolujeme duplicitu podle jména
            val existing = categoryDao.getCategoryByName(def.name)

            if (existing == null) {
                // 2. Vytvoříme kategorii
                val newCategory = MeasurementCategory(
                    name = def.name,
                    description = def.description
                )
                val newCategoryId = categoryDao.insertCategory(newCategory)

                // 3. Vytvoříme pole
                val fieldsToInsert = def.fields.map { fieldDef ->
                    MeasurementCategoryField(
                        categoryId = newCategoryId,
                        name = fieldDef.name,
                        label = fieldDef.label,
                        unit = fieldDef.unit,
                        minValue = fieldDef.min,
                        maxValue = fieldDef.max
                    )
                }
                categoryDao.insertFields(fieldsToInsert)
            }
        }
        backupScheduler.scheduleBackup()
    }
}