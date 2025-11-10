package cz.tomasjanicek.bp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cz.tomasjanicek.bp.database.examination.ExaminationDao
import cz.tomasjanicek.bp.database.doctor.DoctorDao
import cz.tomasjanicek.bp.model.Doctor
import cz.tomasjanicek.bp.model.Examination

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
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
