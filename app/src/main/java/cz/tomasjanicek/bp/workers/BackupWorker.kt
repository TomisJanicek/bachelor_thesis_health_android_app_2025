package cz.tomasjanicek.bp.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.auth.api.signin.GoogleSignIn
import cz.tomasjanicek.bp.repository.GoogleDriveRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val driveRepository: GoogleDriveRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 1. Zjistíme, zda je uživatel přihlášený
            val context = applicationContext
            val account = GoogleSignIn.getLastSignedInAccount(context)

            if (account == null) {
                // Uživatel není přihlášen, nemůžeme zálohovat
                // Vracíme success, abychom systém nenutili to zkoušet znovu
                return@withContext Result.success()
            }

            // 2. Provedeme zálohu
            val result = driveRepository.backupToDrive(account)

            if (result.isSuccess) {
                return@withContext Result.success()
            } else {
                // Pokud to selhalo (např. chyba sítě), WorkManager to zkusí později znovu
                return@withContext Result.retry()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext Result.failure()
        }
    }
}