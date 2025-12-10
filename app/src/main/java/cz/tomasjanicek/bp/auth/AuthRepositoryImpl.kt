package cz.tomasjanicek.bp.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import cz.tomasjanicek.bp.BuildConfig
import cz.tomasjanicek.bp.ui.screens.settings.SettingsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val settingsManager: SettingsManager
) : AuthRepository {

    override suspend fun signInWithGoogle(uiContext: Context): Result<Boolean> {
        return try {
            // 1. Nastavení Credential Manageru
            val credentialManager = CredentialManager.create(context)

            // 2. Příprava Google Sign-In requestu
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // false = nabídne všechny účty
                .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // 3. Zobrazení dialogu uživateli
            val result = credentialManager.getCredential(
                request = request,
                context = uiContext
            )

            // 4. Zpracování výsledku (OPRAVENÁ LOGIKA)
            val credential = result.credential

            // Google Sign-In vrací "CustomCredential", musíme ověřit jeho typ
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                try {
                    // "Rozbalíme" data z CustomCredential do GoogleIdTokenCredential
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    // 5. Výměna Google Tokenu za Firebase User session
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    firebaseAuth.signInWithCredential(firebaseCredential).await()

                    Result.success(true)
                } catch (e: GoogleIdTokenParsingException) {
                    Result.failure(Exception("Chyba při čtení Google dat: ${e.message}"))
                }
            } else {
                // Pokud přijde něco jiného než Google credential
                Result.failure(Exception("Neznámý typ credentialu: ${credential.type}"))
            }

        } catch (e: GetCredentialException) {
            // Uživatel zrušil dialog nebo chyba sítě
            Result.failure(e)
        } catch (e: Exception) {
            // Jiná neočekávaná chyba
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        try {
            // 1. Vymazání stavu v Credential Manageru (aby nezůstal "viset" výběr účtu)
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())

            // 2. Odhlášení z Firebase
            firebaseAuth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            // I když selže clearCredentialState, odhlásíme alespoň Firebase
            firebaseAuth.signOut()
        }
    }

    // NOVÁ METODA (pomocná)
    override fun getCurrentUser() = firebaseAuth.currentUser

    override fun setGuestMode(enabled: Boolean) {
        settingsManager.setGuestMode(enabled)
    }

    override fun isGuestMode(): Boolean {
        return settingsManager.isGuestMode()
    }
}