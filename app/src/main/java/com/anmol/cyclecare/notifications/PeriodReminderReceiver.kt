package com.anmol.cyclecare.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.anmol.cyclecare.MainActivity
import com.anmol.cyclecare.R

class PeriodReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "cyclecare_period_reminders"

        private const val NOTIFICATION_ID = 4102
        const val EXTRA_IS_TEST_NOTIFICATION = "is_test_notification"
    }

    override fun onReceive(
        context: Context,
        intent: Intent?
    ) {
        val isTestNotification = intent?.getBooleanExtra(
            EXTRA_IS_TEST_NOTIFICATION,
            false
        ) == true

        // Normal reminders require reminders to be enabled.
        // Test notifications can be sent independently.
        if (
            !isTestNotification &&
            !PeriodReminderScheduler.areRemindersEnabled(context)
        ) {
            return
        }

        // Check Android 13+ notification permission.
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Create the notification channel.
        createNotificationChannel(context)

        // Open CycleCare when the notification is tapped.
        val openAppIntent = Intent(
            context,
            MainActivity::class.java
        )

        val pendingIntent = PendingIntent.getActivity(
            context,
            4103,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        // Select the appropriate notification content.
        val notificationTitle = if (isTestNotification) {
            "CycleCare test notification 💗"
        } else {
            "A little reminder for you, Priyanshi 🌷"
        }

        val notificationMessage = if (isTestNotification) {
            "Success! CycleCare notifications are working. 🌸"
        } else {
            "Your predicted period is approaching. " +
                    "Be gentle with yourself and take care. 💗"
        }

        val expandedMessage = if (isTestNotification) {
            "Your test notification arrived successfully! " +
                    "CycleCare can send notifications to your phone. " +
                    "Remember to take care of yourself. 💗"
        } else {
            "Your predicted period is approaching, Priyanshi. " +
                    "Keep your essentials ready, rest when you need to, " +
                    "and remember to be kind to yourself. 🌸"
        }

        // Build the notification.
        val notification = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notificationTitle)
            .setContentText(notificationMessage)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(expandedMessage)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Display the notification if notifications are enabled.
        if (
            NotificationManagerCompat.from(context)
                .areNotificationsEnabled()
        ) {
            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Period Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description =
                    "Gentle reminders before the predicted period"
            }

            val manager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }
}