package cz.tomasjanicek.bp.database

import android.content.Context
import androidx.activity.result.launch
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import cz.tomasjanicek.bp.database.cycle.CycleRecordDao
import cz.tomasjanicek.bp.database.examination.ExaminationDao
import cz.tomasjanicek.bp.database.doctor.DoctorDao
import cz.tomasjanicek.bp.database.injection.InjectionDao
import cz.tomasjanicek.bp.database.measurement.MeasurementCategoryDao
import cz.tomasjanicek.bp.database.measurement.MeasurementDao
import cz.tomasjanicek.bp.database.medicine.MedicineDao
import cz.tomasjanicek.bp.model.CycleRecord
import cz.tomasjanicek.bp.model.Doctor
import cz.tomasjanicek.bp.model.Examination
import cz.tomasjanicek.bp.model.Injection
import cz.tomasjanicek.bp.model.Measurement
import cz.tomasjanicek.bp.model.MeasurementCategory
import cz.tomasjanicek.bp.model.MeasurementCategoryField
import cz.tomasjanicek.bp.model.MeasurementValue
import cz.tomasjanicek.bp.model.Medicine
import cz.tomasjanicek.bp.model.MedicineReminder
import cz.tomasjanicek.bp.model.generateSampleMeasurementsAndValues
import cz.tomasjanicek.bp.model.sampleCategoryBloodPressure
import cz.tomasjanicek.bp.model.sampleCategoryWeight
import cz.tomasjanicek.bp.model.sampleDoctors
import cz.tomasjanicek.bp.model.sampleExaminations
import cz.tomasjanicek.bp.model.sampleFieldWeight
import cz.tomasjanicek.bp.model.sampleFieldsBloodPressure
import cz.tomasjanicek.bp.services.Converters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Doctor::class,
        Examination::class,
        MeasurementCategory::class,
        MeasurementCategoryField::class,
        Measurement::class,
        MeasurementValue::class,
        Medicine::class,
        MedicineReminder::class,
        CycleRecord::class,
        Injection::class
    ],
    version = 11,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun doctorDao(): DoctorDao
    abstract fun examinationDao(): ExaminationDao
    abstract fun measurementCategoryDao(): MeasurementCategoryDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun medicineDao(): MedicineDao
    abstract fun cycleRecordDao(): CycleRecordDao
    abstract fun injectionDao(): InjectionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "health_app_database"
                )
                    // fallbackToDestructiveMigration() zajistí, že se při zvýšení verze
                    // stará databáze smaže a vytvoří se nová. Pro vývoj je to ideální.
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                // Spustíme prepopulaci v IO coroutine
                CoroutineScope(Dispatchers.IO).launch {
                    prepopulateIfEmpty(instance)
                }

                instance
            }
        }

        /**
         * Naplní databázi ukázkovými daty, pokud je prázdná.
         * Tuto funkci neměníme, data pro cyklus jsou specifická pro uživatele.
         */
        private suspend fun prepopulateIfEmpty(database: AppDatabase) {
            // Získáme DAO objekty
            val doctorDao = database.doctorDao()
            val examinationDao = database.examinationDao()
            val categoryDao = database.measurementCategoryDao()
            val measurementDao = database.measurementDao()

            // 👉 pokud v tabulce doktorů nic není, považujeme DB za prázdnou
            val count = doctorDao.getCount()
            if (count == 0) {
                // Vložíme doktory a vyšetření
                doctorDao.insertAll(sampleDoctors)
                examinationDao.insertAll(sampleExaminations)

                // Vložíme kategorie měření a jejich pole
                categoryDao.insertCategory(sampleCategoryWeight)
                categoryDao.insertField(sampleFieldWeight)
                categoryDao.insertCategory(sampleCategoryBloodPressure)
                categoryDao.insertFields(sampleFieldsBloodPressure)

                // Vygenerujeme a vložíme měření a jejich hodnoty
                val (measurements, values) = generateSampleMeasurementsAndValues()
                measurementDao.insertAllMeasurements(measurements)
                measurementDao.insertValues(values)
            }
        }
    }
}