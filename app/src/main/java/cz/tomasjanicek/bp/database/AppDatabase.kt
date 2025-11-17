package cz.tomasjanicek.bp.database

import android.content.Context
import androidx.activity.result.launch
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import cz.tomasjanicek.bp.database.examination.ExaminationDao
import cz.tomasjanicek.bp.database.doctor.DoctorDao
import cz.tomasjanicek.bp.model.Doctor
import cz.tomasjanicek.bp.model.Examination
import cz.tomasjanicek.bp.model.sampleDoctors
import cz.tomasjanicek.bp.model.sampleExaminations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Doctor::class,
        Examination::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun doctorDao(): DoctorDao
    abstract fun examinationDao(): ExaminationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "health_app_database"   // název si klidně uprav
                )
                    // při vývoji je fajn destruktivní migrace,
                    // do produkce ji můžeš později odstranit
                    .fallbackToDestructiveMigration()
                    .addCallback(createCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
        // --- ZDE ZAČÍNÁ NOVÝ KÓD ---
        /**
         * Vytvoří a vrátí callback, který naplní databázi při jejím prvním vytvoření.
         */
        private fun createCallback(context: Context): Callback {
            return object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // onCreate se volá pouze jednou. Spustíme vkládání v coroutine.
                    CoroutineScope(Dispatchers.IO).launch {
                        // Získáme DAO instance přes existující INSTANCE
                        getDatabase(context).let { database ->
                            val doctorDao = database.doctorDao()
                            val examinationDao = database.examinationDao()

                            // Vložíme ukázková data
                            doctorDao.insertAll(sampleDoctors)
                            examinationDao.insertAll(sampleExaminations)
                        }
                    }
                }
            }
        }
        // --- ZDE KONČÍ NOVÝ KÓD ---
    }
}
