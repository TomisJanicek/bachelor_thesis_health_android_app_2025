package cz.tomasjanicek.bp.services

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import cz.tomasjanicek.bp.workers.BackupWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupScheduler @Inject constructor(
    // DŮLEŽITÉ: Zde musí být @ApplicationContext
    @ApplicationContext private val context: Context
) {

    fun scheduleBackup() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Musí být internet
            // .setRequiresBatteryNotLow(true) // Volitelné: Nezálohovat, když je málo baterky
            .build()

        // "Debouncing" - zpoždění 15 minut
        // Pokud přijde další požadavek se stejným jménem ("backup_work"),
        // ten předchozí se zruší a čas běží znovu.
        val workRequest = OneTimeWorkRequestBuilder<BackupWorker>()
            .setConstraints(constraints)
            .setInitialDelay(15, TimeUnit.SECONDS)
            .addTag("backup_work")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "auto_backup",         // Unikátní název práce
            ExistingWorkPolicy.REPLACE, // DŮLEŽITÉ: REPLACE resetuje časovač
            workRequest
        )
    }

    // Volitelné: Metoda pro okamžitou zálohu (např. při manuálním kliknutí v nastavení)
    fun backupNow() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<BackupWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "manual_backup",
            ExistingWorkPolicy.KEEP, // Pokud už běží, necháme ji doběhnout
            workRequest
        )
    }
}