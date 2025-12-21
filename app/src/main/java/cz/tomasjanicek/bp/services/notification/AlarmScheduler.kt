package cz.tomasjanicek.bp.services.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleNotification(id: Long, dateTime: Long, title: String, message: String) {
        // 1. Kontrola času - neplánujeme do minulosti
        if (dateTime <= System.currentTimeMillis()) return

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NotificationReceiver.EXTRA_TITLE, title)
            putExtra(NotificationReceiver.EXTRA_MESSAGE, message)
            putExtra(NotificationReceiver.EXTRA_ID, id.toInt())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. LOGIKA PRO PŘESNÝ BUDÍK (Android 12+ / API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // A) Uživatel NEMÁ povolené přesné budíky
                Log.w("AlarmScheduler", "Chybí oprávnění SCHEDULE_EXACT_ALARM. Přesměrovávám do nastavení.")

                // Otevřeme nastavení, aby to mohl povolit
                try {
                    val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    settingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(settingsIntent)

                    // Volitelné: Zobrazit uživateli vysvětlení
                    Toast.makeText(context, "Pro upozornění povolte 'Budíky a připomenutí'", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // --- KRITICKY DŮLEŽITÉ ---
                // Tady musíme SKONČIT (return).
                // Nesmíme pokračovat dál k `setExact...`, jinak aplikace SPADNE na SecurityException.
                return
            }
        }

        // 3. NASTAVENÍ BUDÍKU (Zabaleno v try-catch proti pádu)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Pro Android 6+ (Doze mode)
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    dateTime,
                    pendingIntent
                )
            } else {
                // Pro starší Androidy
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    dateTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Pokud aplikace i přes kontrolu spadne (např. uživatel odebral právo v milisekundě mezi kontrolou a akcí)
            // Zachytíme pád, zalogujeme ho, ale neshodíme aplikaci.
            Log.e("AlarmScheduler", "Chyba při nastavování budíku: SecurityException", e)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelNotification(id: Long) {
        try {
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}