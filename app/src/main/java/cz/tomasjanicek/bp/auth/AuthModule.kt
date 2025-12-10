package cz.tomasjanicek.bp.auth

import android.content.Context
import dagger.Module
import dagger.Provides
import com.google.firebase.auth.FirebaseAuth
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
        settingsManager: SettingsManager // <--- PŘIDÁNO: Hilt nám ho sem pošle
    ): AuthRepository {
        // Musíme ho předat do konstruktoru
        return AuthRepositoryImpl(context, firebaseAuth, settingsManager)
    }
}