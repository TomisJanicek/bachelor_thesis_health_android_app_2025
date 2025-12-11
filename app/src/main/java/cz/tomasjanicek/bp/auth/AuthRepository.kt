package cz.tomasjanicek.bp.auth

import android.content.Context
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    suspend fun signInWithGoogle(context: Context): Result<Boolean>
    suspend fun signOut()
    fun getCurrentUser(): FirebaseUser?

    suspend fun setGuestMode(enabled: Boolean) // <-- ZMĚNA NA SUSPEND
    fun isGuestMode(): Boolean

    // Volitelné: Metoda pro úplné ukončení hosta (pokud je to jiné než setGuestMode(false))
    suspend fun endGuestMode()
}