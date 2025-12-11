package cz.tomasjanicek.bp.database

import cz.tomasjanicek.bp.model.data.DoctorData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DatabaseCleaner @Inject constructor(
    private val database: AppDatabase
) {
    /**
     * Smaže kompletně všechna data v aplikaci, ALE obnoví defaultní doktory.
     */
    suspend fun clearAllData() {
        withContext(Dispatchers.IO) {
            // 1. Smazání závislých entit (od konce)
            database.measurementDao().deleteAllMeasurements()
            database.measurementCategoryDao().deleteAllCategories()
            database.medicineDao().deleteAllReminders()
            database.medicineDao().deleteAllMedicines()
            database.examinationDao().deleteAll()
            database.cycleRecordDao().deleteAll()
            database.injectionDao().deleteAll()

            // 2. Smazání hlavních entit
            database.doctorDao().deleteAll()

            // 3. Pojistka Roomu - smaže úplně všechno (včetně tabulek)
            database.clearAllTables()

            // --- 4. ZMŠTVÝCHVSTÁNÍ DOKTORŮ (Re-seed) --- 🚑
            // Teď je databáze prázdná. Hned tam vrátíme defaultní seznam.
            // Protože jsme smazali i měření a prohlídky, nevadí, že budou mít nová ID.
            database.doctorDao().insertAll(DoctorData.defaultDoctors)
        }
    }
}