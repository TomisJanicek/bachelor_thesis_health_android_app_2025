package cz.tomasjanicek.bp.database.examination

import cz.tomasjanicek.bp.database.doctor.DoctorDao
import cz.tomasjanicek.bp.model.Examination
import cz.tomasjanicek.bp.model.ExaminationWithDoctor
import cz.tomasjanicek.bp.services.BackupScheduler
import cz.tomasjanicek.bp.services.notification.AlarmScheduler
import cz.tomasjanicek.bp.ui.screens.settings.SettingsManager
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class LocalExaminationsRepositoryImpl @Inject constructor(
    private val examinationDao: ExaminationDao,
    private val doctorDao: DoctorDao,
    private val backupScheduler: BackupScheduler,
    private val alarmScheduler: AlarmScheduler,
    private val settingsManager: SettingsManager
) : ILocalExaminationsRepository {

    override fun getAll(): Flow<List<Examination>> = examinationDao.getAll()

    // --- VLOŽENÍ NOVÉ (NEBO PŘEPIS EXISTUJÍCÍ) PROHLÍDKY ---
    override suspend fun insert(examination: Examination): Long {
        // 1. Uložit do DB (Díky REPLACE v DAO toto funguje i pro update)
        val id = examinationDao.insert(examination)

        // 2. Naplánovat zálohu
        backupScheduler.scheduleBackup()

        // 3. Naplánovat notifikaci
        scheduleNotificationForExamination(examination.copy(id = id))

        return id
    }

    // --- ÚPRAVA EXISTUJÍCÍ PROHLÍDKY ---
    override suspend fun update(examination: Examination) {
        examinationDao.update(examination)
        backupScheduler.scheduleBackup()
        scheduleNotificationForExamination(examination)
    }

    // --- SMAZÁNÍ PROHLÍDKY ---
    override suspend fun delete(examination: Examination) {
        examinationDao.delete(examination)
        backupScheduler.scheduleBackup()
        if (examination.id != null) {
            alarmScheduler.cancelNotification(examination.id)
        }
    }

    // --- POMOCNÁ METODA PRO PLÁNOVÁNÍ ---
    private suspend fun scheduleNotificationForExamination(examination: Examination) {
        if (settingsManager.notificationsEnabled.value) {
            val prefTime = settingsManager.examNotificationTime.value
            val triggerTime = prefTime.calculateTriggerTime(examination.dateTime)

            var doctorName = ""
            if (examination.doctorId != null) {
                // Zde voláme DoctorDao. Předpokládám, že metoda getDoctor existuje a vrací Doctor?
                // Pokud vrací Flow, použij .firstOrNull() v coroutine scope
                try {
                    val doctor = doctorDao.getDoctor(examination.doctorId)
                    if (doctor != null) {
                        doctorName = ": " + (doctor.specialization)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val formatter = DateTimeFormatter.ofPattern("d. M. yyyy HH:mm")
            val formattedDate = Instant.ofEpochMilli(examination.dateTime)
                .atZone(ZoneId.systemDefault())
                .format(formatter)

            examination.id?.let { id ->
                alarmScheduler.scheduleNotification(
                    id = id,
                    dateTime = triggerTime,
                    title = "Připomínka prohlídky$doctorName",
                    message = "${examination.purpose} - $formattedDate"
                )
            }
        }
    }

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