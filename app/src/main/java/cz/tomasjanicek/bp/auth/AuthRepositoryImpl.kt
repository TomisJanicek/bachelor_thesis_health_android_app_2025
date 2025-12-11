package cz.tomasjanicek.bp.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import cz.tomasjanicek.bp.BuildConfig
import cz.tomasjanicek.bp.database.DatabaseCleaner
import cz.tomasjanicek.bp.repository.GoogleDriveRepository
import cz.tomasjanicek.bp.ui.screens.settings.SettingsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val settingsManager: SettingsManager,
    private val databaseCleaner: DatabaseCleaner,
    private val driveRepository: GoogleDriveRepository
) : AuthRepository {

    override suspend fun signInWithGoogle(uiContext: Context): Result<Boolean> {
        return try {
            // 1. Nastavení Credential Manageru
            val credentialManager = CredentialManager.create(context)

            // 2. Příprava Google Sign-In requestu
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
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

            // 4. Zpracování výsledku
            val credential = result.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {

                try {
                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    // 5. Výměna Google Tokenu za Firebase User session
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    firebaseAuth.signInWithCredential(firebaseCredential).await()

                    // --- FÁZE 2: MOST PRO DRIVE API (OPRAVA) ---
                    // CredentialManager nás přihlásil, ale Drive API potřebuje 'GoogleSignInAccount'.
                    // Musíme si ho vyžádat pomocí 'Silent Sign In', který proběhne bez UI,
                    // protože uživatel právě dal souhlas.

                    try {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA)) // Důležité: Oprávnění pro Drive
                            .build()

                        val googleSignInClient = GoogleSignIn.getClient(context, gso)

                        // Získáme objekt účtu pro Drive Repository
                        val googleAccount = googleSignInClient.silentSignIn().await()

                        // 6. OBNOVA DAT
                        driveRepository.restoreFromDrive(googleAccount)

                    } catch (e: Exception) {
                        // Pokud selže obnova (např. chyba sítě nebo neexistující záloha),
                        // nebudeme shazovat celý login, jen to zalogujeme.
                        e.printStackTrace()
                    }

                    // Vypneme host mode
                    settingsManager.setGuestMode(false)

                    Result.success(true)
                } catch (e: GoogleIdTokenParsingException) {
                    Result.failure(Exception("Chyba při čtení Google dat: ${e.message}"))
                }
            } else {
                Result.failure(Exception("Neznámý typ credentialu: ${credential.type}"))
            }

        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        // 1. JSEM HOST? -> Rovnou smazat a pryč
        if (isGuestMode()) {
            Log.d("AuthRepo", "Odhlašuji hosta -> mažu DB")
            databaseCleaner.clearAllData()
            settingsManager.setGuestMode(false)
            return // Hotovo, končíme
        }

        // 2. JSEM GOOGLE UŽIVATEL? -> Musíme zálohovat
        val googleAccount = GoogleSignIn.getLastSignedInAccount(context)

        if (googleAccount != null) {
            Log.d("AuthRepo", "Odhlašuji Google Usera -> Zahajuji zálohu")

            // Pokus o zálohu
            val backupResult = driveRepository.backupToDrive(googleAccount)

            if (backupResult.isSuccess) {
                Log.d("AuthRepo", "Záloha OK -> Mažu lokální data a odhlašuji")

                // Až teď mažeme data!
                databaseCleaner.clearAllData()

                // Odhlášení ze služeb
                val credentialManager = CredentialManager.create(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
                firebaseAuth.signOut()

                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                GoogleSignIn.getClient(context, gso).signOut()

                settingsManager.setGuestMode(false)
            } else {
                // Záloha selhala! Vyhodíme chybu, aby ViewModel věděl, že nemá odcházet.
                val errorMsg = backupResult.exceptionOrNull()?.message ?: "Neznámá chyba zálohy"
                Log.e("AuthRepo", "Záloha selhala: $errorMsg")
                throw Exception("Nepodařilo se zálohovat data: $errorMsg. Odhlášení zrušeno.")
            }
        } else {
            // Divný stav: Nejsme host, ale nemáme Google účet?
            // Pro jistotu smažeme data (asi nějaký zbloudilý login).
            Log.w("AuthRepo", "Nenalezen Google účet, ale nejsme Host. Mažu data.")
            databaseCleaner.clearAllData()
            firebaseAuth.signOut()
            settingsManager.setGuestMode(false)
        }
    }

    override fun getCurrentUser() = firebaseAuth.currentUser

    override suspend fun setGuestMode(enabled: Boolean) {
        if (enabled) {
            databaseCleaner.clearAllData()
        }
        settingsManager.setGuestMode(enabled)
    }

    override fun isGuestMode(): Boolean {
        return settingsManager.isGuestMode()
    }

    override suspend fun endGuestMode() {
        databaseCleaner.clearAllData()
        settingsManager.setGuestMode(false)
    }
}