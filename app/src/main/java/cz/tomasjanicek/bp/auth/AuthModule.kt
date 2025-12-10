package cz.tomasjanicek.bp.auth

import android.content.Context
import dagger.Module
import dagger.Provides
import com.google.firebase.auth.FirebaseAuth
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

    // Pokud jsi vytvořil AuthRepository jako interface a AuthRepositoryImpl jako třídu:
    @Provides
    @Singleton
    fun provideAuthRepository(
        @ApplicationContext context: Context,
        firebaseAuth: FirebaseAuth
    ): AuthRepository {
        return AuthRepositoryImpl(context, firebaseAuth)
    }
}