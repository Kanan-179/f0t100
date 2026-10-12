package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object HabitNotificationHelper {

    private const val CHANNEL_ID = "habit_daily_reminder_channel"
    private const val CHANNEL_NAME = "Gündəlik Vərdiş Xatırlatmaları"
    private const val CHANNEL_DESC = "Kənan üçün gündəlik vərdiş və taskları qeyd etmək xatırlatmaları"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Sends the personalized reminder specifically requested by Kenan:
     * "Kənan, nə etmisənsə daxil et!"
     */
    fun sendKenanDailyReminder(context: Context): Boolean {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Kənan, günün necə keçdi? 🌟")
            .setContentText("Kənan, nə etmisənsə daxil et! Vərdiş və tasklarını indi qeyd et! 🔥")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Kənan, nə etmisənsə daxil et! Gün ərzində etdiyin vərdişləri və tamamladığın taskları gözünün qarşısında görmək üçün indi qeyd et! 💪")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        return try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                notificationManager.areNotificationsEnabled()
            ) {
                notificationManager.notify(NOTIFICATION_ID, builder.build())
                true
            } else {
                false
            }
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            false
        }
    }
}
