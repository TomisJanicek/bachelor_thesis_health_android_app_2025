package cz.tomasjanicek.bp.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DatabaseCleaner @Inject constructor(
    private val database: AppDatabase
) {
    suspend fun clearAllData() {
        // TOTO JE TA OPRAVA: Přesun na IO vlákno
        withContext(Dispatchers.IO) {
            // 1. Smazání závislých entit
            database.measurementDao().deleteAllMeasurements()
            database.measurementCategoryDao().deleteAllCategories()
            database.medicineDao().deleteAllReminders()
            database.medicineDao().deleteAllMedicines()
            database.examinationDao().deleteAll()
            database.cycleRecordDao().deleteAll()
            database.injectionDao().deleteAll()

            // 2. Smazání hlavních entit
            database.doctorDao().deleteAll()

            // 3. Pojistka Roomu
            database.clearAllTables()
        }
    }
}