package cz.tomasjanicek.bp.services.notification

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cz.tomasjanicek.bp.database.examination.ExaminationDao
import cz.tomasjanicek.bp.database.medicine.MedicineDao

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmScheduler: AlarmScheduler
    @Inject lateinit var medicineDao: MedicineDao
    @Inject lateinit var examinationDao: ExaminationDao

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {

            // Spustíme coroutinu pro čtení z DB
            CoroutineScope(Dispatchers.IO).launch {
                val now = System.currentTimeMillis()

                // 1. Přeplánovat léky
                val reminders = medicineDao.getAllRemindersList()
                reminders.forEach { reminder ->
                    if (reminder.plannedDateTime > now && reminder.status.name == "PLANNED") {
                        alarmScheduler.scheduleNotification(
                            id = reminder.id,
                            dateTime = reminder.plannedDateTime,
                            title = "Čas na lék",
                            message = "Je čas užít váš lék"
                        )
                    }
                }

                // 2. Přeplánovat prohlídky
                val exams = examinationDao.getAllList()
                exams.forEach { exam ->
                    // OPRAVA: Používáme exam.dateTime místo exam.date
                    // exam.id může být null, proto použijeme bezpečné volání nebo podmínku
                    val examId = exam.id

                    if (examId != null && exam.dateTime > now) {
                        alarmScheduler.scheduleNotification(
                            id = examId,
                            dateTime = exam.dateTime, // <-- TADY BOLA CHYBA (date -> dateTime)
                            title = "Blíží se prohlídka",
                            message = "${exam.purpose} (Nezapomeňte na návštěvu lékaře)"
                        )
                    }
                }
            }
        }
    }
}