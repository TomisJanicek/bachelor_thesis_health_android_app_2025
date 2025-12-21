package cz.tomasjanicek.bp.workers

import android.content.Context
import android.util.Log
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
        Log.d("BackupWorker", "🚀 START: BackupWorker se probudil a začíná pracovat.") // <-- LOG

        try {
            val context = applicationContext
            val account = GoogleSignIn.getLastSignedInAccount(context)

            if (account == null) {
                Log.e("BackupWorker", "❌ CHYBA: Uživatel není přihlášen, končím.") // <-- LOG
                return@withContext Result.success()
            }

            Log.d("BackupWorker", "ℹ️ Uživatel nalezen: ${account.email}, odesílám data...") // <-- LOG

            val result = driveRepository.backupToDrive(account)

            if (result.isSuccess) {
                Log.d("BackupWorker", "✅ ÚSPĚCH: Záloha byla úspěšně nahrána na Google Drive.") // <-- LOG
                return@withContext Result.success()
            } else {
                Log.e("BackupWorker", "⚠️ NEÚSPĚCH: Záloha selhala, zkusím to později. Chyba: ${result.exceptionOrNull()?.message}") // <-- LOG
                return@withContext Result.retry()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("BackupWorker", "❌ KRITICKÁ CHYBA: ${e.message}") // <-- LOG
            return@withContext Result.failure()
        }
    }
}