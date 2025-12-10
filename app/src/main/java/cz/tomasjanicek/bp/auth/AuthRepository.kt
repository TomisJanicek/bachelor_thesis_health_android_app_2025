package cz.tomasjanicek.bp.auth

import android.content.Context
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    suspend fun signInWithGoogle(context: Context): Result<Boolean>
    suspend fun signOut() // <--- NOVÉ
    fun getCurrentUser(): FirebaseUser?
}