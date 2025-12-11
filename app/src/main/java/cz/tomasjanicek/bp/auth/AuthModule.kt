package cz.tomasjanicek.bp.auth

import android.content.Context
import dagger.Module
import dagger.Provides
import com.google.firebase.auth.FirebaseAuth
import cz.tomasjanicek.bp.database.DatabaseCleaner
import cz.tomasjanicek.bp.repository.GoogleDriveRepository
import cz.tomasjanicek.bp.ui.screens.settings.SettingsManager
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(
        @ApplicationContext context: Context,
        firebaseAuth: FirebaseAuth,
        settingsManager: SettingsManager,
        databaseCleaner: DatabaseCleaner, // <-- Inject
        driveRepository: GoogleDriveRepository // <-- Inject
    ): AuthRepository {
        return AuthRepositoryImpl(
            context,
            firebaseAuth,
            settingsManager,
            databaseCleaner,
            driveRepository
        )
    }
}