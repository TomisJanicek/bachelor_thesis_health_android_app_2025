package cz.tomasjanicek.bp.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import cz.tomasjanicek.bp.database.AppDatabase
import cz.tomasjanicek.bp.database.DatabaseCleaner
import cz.tomasjanicek.bp.repository.GoogleDriveRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GoogleModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            // Pokud používáš LocalDate/Time v modelech a chceš je v JSONu hezky,
            // zaregistruj adaptéry. Pokud máš v modelech Long (timestamp), není to potřeba.
            // .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            // .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create()
    }

    @Provides
    @Singleton
    fun provideGoogleDriveRepository(
        @ApplicationContext context: Context,
        db: AppDatabase,
        gson: Gson,
        databaseCleaner: DatabaseCleaner // <-- Injectneme cleaner
    ): GoogleDriveRepository {
        return GoogleDriveRepository(context, db, gson, databaseCleaner)
    }

    @Provides
    @Singleton
    fun provideDatabaseCleaner(db: AppDatabase): DatabaseCleaner {
        return DatabaseCleaner(db)
    }
}