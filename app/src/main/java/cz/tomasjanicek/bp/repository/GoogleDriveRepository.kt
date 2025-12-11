package cz.tomasjanicek.bp.repository

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.gson.Gson
import cz.tomasjanicek.bp.database.AppDatabase
import cz.tomasjanicek.bp.database.DatabaseCleaner
import cz.tomasjanicek.bp.model.backupData.BackupData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Collections
import javax.inject.Inject

class GoogleDriveRepository @Inject constructor(
    private val context: Context,
    private val database: AppDatabase,
    private val gson: Gson, // Injectujeme Gson (musí být v DI)
    private val databaseCleaner: DatabaseCleaner
) {

    companion object {
        private const val BACKUP_FILE_NAME = "health_app_backup_v1.json"
        // Použijeme 'appDataFolder', aby soubor nebyl vidět běžně na disku a uživatel ho nesmazal
        private const val BACKUP_FOLDER = "appDataFolder"
    }

    /**
     * Vytvoří instanci Drive služby z přihlášeného účtu.
     */
    private fun getDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, Collections.singleton(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = account.account

        return Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("BP Health App").build()
    }

    /**
     * ZÁLOHA: Vezme data z DB -> JSON -> Google Drive
     */
    suspend fun backupToDrive(account: GoogleSignInAccount): Result<Boolean> = withContext(
        Dispatchers.IO) {
        try {
            val driveService = getDriveService(account)

            // 1. Získání dat z databáze
            val backupData = BackupData(
                doctors = database.doctorDao().getAllList(),
                examinations = database.examinationDao().getAllList(),
                measurementCategories = database.measurementCategoryDao().getAllCategoriesList(),
                measurementFields = database.measurementCategoryDao().getAllFieldsList(),
                measurements = database.measurementDao().getAllMeasurementsList(),
                measurementValues = database.measurementDao().getAllValuesList(),
                medicines = database.medicineDao().getAllMedicinesList(),
                medicineReminders = database.medicineDao().getAllRemindersList(),
                cycleRecords = database.cycleRecordDao().getAllList(),
                injections = database.injectionDao().getAllList()
            )

            // 2. Serializace do JSON
            val jsonContent = gson.toJson(backupData)

            // 3. Najít existující zálohu (pokud je)
            val fileList = driveService.files().list()
                .setSpaces(BACKUP_FOLDER)
                .setQ("name = '$BACKUP_FILE_NAME'")
                .setFields("files(id, name)")
                .execute()

            val fileMetadata = com.google.api.services.drive.model.File()
            fileMetadata.name = BACKUP_FILE_NAME
            fileMetadata.parents = Collections.singletonList(BACKUP_FOLDER)

            val mediaContent = ByteArrayContent.fromString("application/json", jsonContent)

            if (fileList.files.isEmpty()) {
                // Vytvořit nový soubor
                driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
            } else {
                // Aktualizovat existující
                val fileId = fileList.files[0].id
                driveService.files().update(fileId, null, mediaContent).execute()
            }

            return@withContext Result.success(true)

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext Result.failure(e)
        }
    }

    /**
     * OBNOVA: Google Drive -> JSON -> Smazat DB -> Vložit data
     */
    suspend fun restoreFromDrive(account: GoogleSignInAccount): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService(account)

            // 1. Najít soubor zálohy
            val fileList = driveService.files().list()
                .setSpaces(BACKUP_FOLDER)
                .setQ("name = '$BACKUP_FILE_NAME'")
                .setFields("files(id, name)")
                .execute()

            if (fileList.files.isEmpty()) {
                return@withContext Result.failure(Exception("Záloha nenalezena"))
            }

            val fileId = fileList.files[0].id

            // 2. Stáhnout obsah
            val inputStream = driveService.files().get(fileId).executeMediaAsInputStream()
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonContent = reader.readText()

            // 3. Deserializace
            val backupData = gson.fromJson(jsonContent, BackupData::class.java)

            // 4. Obnova databáze (v transakci)
            database.runInTransaction {
                // A) Smazat všechna lokální data
                database.clearAllTables()
                // Pozor: clearAllTables je drastické, ale nejbezpečnější pro prevenci duplicit ID.

                // B) Vložit data ze zálohy
                // Musíme použít runBlocking nebo volat DAO metody, které nejsou suspend uvnitř runInTransaction,
                // ale protože jsme v suspend funkci v IO vlákně, můžeme volat suspend DAO metody "normálně"
                // pokud DAO podporuje vkládání seznamů.

                // Poznámka: Uvnitř runInTransaction nemůžeme volat suspend funkce přímo jednoduše.
                // Proto je lepší udělat logiku smazání a vložení sekvenčně v IO vlákně, ale s rizikem selhání v polovině.
                // IDEÁLNÍ ŘEŠENÍ: DAO metody pro insertAll (které máš) jsou suspend.
            }

            // Alternativní bezpečnější vkládání (protože runInTransaction vs suspend je tricky):
            repopulateDatabase(backupData)

            return@withContext Result.success(true)

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext Result.failure(e)
        }
    }

    // Pomocná metoda pro vkládání (musí běžet v coroutine, protože DAO jsou suspend)
    private suspend fun repopulateDatabase(data: BackupData) {
        databaseCleaner.clearAllData()

        // Zjednodušení: Použijeme clearAllTables, ale musíme to udělat mimo transakci Roomu, pokud vkládáme přes suspend.
        database.clearAllTables()

        // Vložení dat (pořadí je důležité kvůli cizím klíčům!)

        // 1. Nezávislé entity
        if (data.doctors.isNotEmpty()) database.doctorDao().insertAll(data.doctors)
        if (data.medicines.isNotEmpty()) {
            // Medicines vkládáme po jedné, nebo přidáme insertList do DAO
            data.medicines.forEach { database.medicineDao().saveMedicine(it) }
        }
        if (data.cycleRecords.isNotEmpty()) {
            data.cycleRecords.forEach { database.cycleRecordDao().upsert(it) }
        }
        if (data.injections.isNotEmpty()) {
            data.injections.forEach { database.injectionDao().insertOrUpdateInjection(it) }
        }

        // 2. Kategorie a pole (Categories -> Fields)
        if (data.measurementCategories.isNotEmpty()) {
            data.measurementCategories.forEach { database.measurementCategoryDao().insertCategory(it) }
        }
        if (data.measurementFields.isNotEmpty()) {
            database.measurementCategoryDao().insertFields(data.measurementFields)
        }

        // 3. Závislé entity (Examinations, Reminders)
        if (data.examinations.isNotEmpty()) database.examinationDao().insertAll(data.examinations)
        if (data.medicineReminders.isNotEmpty()) database.medicineDao().insertReminders(data.medicineReminders)

        // 4. Měření (Measurements -> Values)
        if (data.measurements.isNotEmpty()) {
            database.measurementDao().insertAllMeasurements(data.measurements)
        }
        if (data.measurementValues.isNotEmpty()) {
            database.measurementDao().insertValues(data.measurementValues)
        }
    }
}