package cz.tomasjanicek.bp.database.examination

import cz.tomasjanicek.bp.database.doctor.DoctorDao
import cz.tomasjanicek.bp.model.Examination
import cz.tomasjanicek.bp.model.ExaminationWithDoctor
import cz.tomasjanicek.bp.services.BackupScheduler
import cz.tomasjanicek.bp.services.notification.AlarmScheduler
import cz.tomasjanicek.bp.ui.screens.settings.SettingsManager
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class LocalExaminationsRepositoryImpl @Inject constructor(
    private val examinationDao: ExaminationDao,
    private val doctorDao: DoctorDao, // <--- PŘIDAT TOTO (možná budeš muset přidat import)
    private val backupScheduler: BackupScheduler,
    private val alarmScheduler: AlarmScheduler,
    private val settingsManager: SettingsManager
) : ILocalExaminationsRepository {

    override fun getAll(): Flow<List<Examination>> = examinationDao.getAll()

    // --- VLOŽENÍ NOVÉ PROHLÍDKY ---
    override suspend fun insert(examination: Examination): Long {
        // 1. Uložit do DB a získat vygenerované ID
        val id = examinationDao.insert(examination)

        // 2. Naplánovat zálohu na Google Drive
        backupScheduler.scheduleBackup()

        // 3. Naplánovat notifikaci (budík)
        // Vytvoříme kopii objektu s novým ID, abychom mohli budík správně identifikovat
        scheduleNotificationForExamination(examination.copy(id = id))

        return id
    }

    // --- ÚPRAVA EXISTUJÍCÍ PROHLÍDKY ---
    override suspend fun update(examination: Examination) {
        // 1. Aktualizovat v DB
        examinationDao.update(examination)

        // 2. Naplánovat zálohu
        backupScheduler.scheduleBackup()

        // 3. Přeplánovat notifikaci
        // AlarmManager automaticky přepíše starý budík, pokud má stejné ID (pendingIntent request code)
        scheduleNotificationForExamination(examination)
    }

    // --- SMAZÁNÍ PROHLÍDKY ---
    override suspend fun delete(examination: Examination) {
        // 1. Smazat z DB
        examinationDao.delete(examination)

        // 2. Naplánovat zálohu
        backupScheduler.scheduleBackup()

        // 3. Zrušit notifikaci, pokud existuje
        if (examination.id != null) {
            alarmScheduler.cancelNotification(examination.id)
        }
    }

    // --- POMOCNÁ METODA PRO PLÁNOVÁNÍ ---
    private suspend fun scheduleNotificationForExamination(examination: Examination) {
        // A) Zkontrolujeme, zda má uživatel zapnuté notifikace v nastavení
        if (settingsManager.notificationsEnabled.value) {

            // B) Zjistíme preferovaný čas
            val prefTime = settingsManager.examNotificationTime.value

            // C) Vypočítáme čas budíku
            val triggerTime = prefTime.calculateTriggerTime(examination.dateTime)

            // D) Získáme jméno doktora (pokud je přiřazen)
            var doctorName = ""
            if (examination.doctorId != null) {
                // Musíš mít v DoctorDao metodu getDoctorById(id), která vrací Doctor? nebo Flow
                // Předpokládám, že máš metodu getDoctor(id) nebo podobnou.
                // Pokud v DAO vracíš Flow, použij .firstOrNull().
                // Pokud vracíš přímo objekt (suspend), stačí zavolat.

                // Příklad (uprav podle svého DoctorDao):
                val doctor = doctorDao.getDoctor(examination.doctorId)
                if (doctor != null) {
                    doctorName = ": " + (doctor.specialization)
                }
            }
            // --- NOVÉ: Formátování data ---
            val formatter = java.time.format.DateTimeFormatter.ofPattern("d. M. yyyy HH:mm")
            val formattedDate = java.time.Instant.ofEpochMilli(examination.dateTime)
                .atZone(java.time.ZoneId.systemDefault())
                .format(formatter)

            // E) Naplánujeme
            examination.id?.let { id ->
                alarmScheduler.scheduleNotification(
                    id = id,
                    dateTime = triggerTime,
                    title = "Připomínka prohlídky$doctorName",
                    message = "${examination.purpose} - $formattedDate" // (${prefTime.label.lowercase()})"
                )
            }
        }
    }

    // --- OSTATNÍ METODY (beze změny) ---

    override suspend fun getExamination(id: Long): Examination =
        examinationDao.getExamination(id)

    override fun getAllWithDoctors(): Flow<List<ExaminationWithDoctor>> =
        examinationDao.getAllWithDoctors()

    override suspend fun getExaminationsByDoctor(doctorId: Long): List<Examination> {
        return examinationDao.getExaminationsByDoctor(doctorId)
    }

    override fun getExaminationWithDoctorById(id: Long): Flow<ExaminationWithDoctor?> {
        return examinationDao.getExaminationWithDoctorById(id)
    }
}