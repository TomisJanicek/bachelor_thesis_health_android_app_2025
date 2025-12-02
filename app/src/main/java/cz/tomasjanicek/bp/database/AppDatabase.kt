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
    version = 3,
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
                    "health_app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                // 💡 po vytvoření instance zkusíme naplnit sample daty
                CoroutineScope(Dispatchers.IO).launch {
                    prepopulateIfEmpty(instance)
                }

                instance
            }
        }

        /**
         * Naplní databázi ukázkovými daty, pokud je prázdná.
         */
        private suspend fun prepopulateIfEmpty(database: AppDatabase) {
            val doctorDao = database.doctorDao()
            val examinationDao = database.examinationDao()

            // 👉 pokud v tabulce doktorů nic není, považujeme DB za prázdnou
            val count = doctorDao.getCount()
            if (count == 0) {
                doctorDao.insertAll(sampleDoctors)
                examinationDao.insertAll(sampleExaminations)
            }
        }
    }
}