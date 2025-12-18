package cz.tomasjanicek.bp.services.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import cz.tomasjanicek.bp.MainActivity
import cz.tomasjanicek.bp.R

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "health_app_channel_high_priority"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_ID = "extra_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Zdravotní deník"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Připomínka"
        val notificationId = intent.getIntExtra(EXTRA_ID, 0)

        showNotification(context, title, message, notificationId)
    }

    private fun showNotification(context: Context, title: String, message: String, id: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Nastavení zvuku (defaultní zvuk notifikace)
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // 2. Vytvoření kanálu (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Důležitá upozornění", // Změna názvu pro uživatele
                NotificationManager.IMPORTANCE_HIGH // HIGH = Vyskočí na obrazovku + Zvuk
            ).apply {
                description = "Upozornění na léky a prohlídky"
                enableLights(true)
                enableVibration(true)
                // Důležité: Nastavení viditelnosti pro zamčenou obrazovku
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC

                // Nastavení zvuku pro kanál
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(alarmSound, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 3. Pending Intent (kliknutí otevře aplikaci)
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Sestavení notifikace
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round) // Zkontroluj, jestli máš tuto ikonu
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX) // MAX = Heads-up notifikace
            .setCategory(NotificationCompat.CATEGORY_REMINDER) // Kategorie pomáhá systému
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Viditelné na zamčené obrazovce
            .setSound(alarmSound) // Zvuk pro starší Androidy
            .setVibrate(longArrayOf(0, 500, 200, 500)) // Vibrace: ticho, bzz, ticho, bzz
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }
}