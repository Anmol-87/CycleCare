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
import com.anmol.cyclecare.utils.AffirmationMessages

class DailyAffirmationReceiver : BroadcastReceiver() {

    companion object {

        const val CHANNEL_ID = "cyclecare_daily_affirmations"

        private const val NOTIFICATION_ID = 8102

        private const val PREFS_NAME =
            "cyclecare_affirmation_preferences"

        private const val KEY_LAST_INDEX = "last_affirmation_index"

        const val EXTRA_TEST_NOTIFICATION = "test_affirmation"
    }

    override fun onReceive(
        context: Context,
        intent: Intent?
    ) {

        val isTest = intent?.getBooleanExtra(
            EXTRA_TEST_NOTIFICATION,
            false
        ) == true

        if (!isTest) {

            val preferences = context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

            if (!preferences.getBoolean("affirmations_enabled", true)) {
                return
            }

            // Schedule the next morning's notification.
            DailyAffirmationScheduler.scheduleNext(context)
        }

        // Check notification permission.
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return
        }

        createNotificationChannel(context)

        val message = if (isTest) {
            "Good morning Priyanshi! ❤️ Aaj ka din tumhare liye bahut special ho. Hamesha smile karti rehna! 🌸"
        } else {
            getNextAffirmation(context)
        }

        val openAppIntent = Intent(
            context,
            MainActivity::class.java
        )

        val pendingIntent = PendingIntent.getActivity(
            context,
            8103,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("A little reminder for you 🌸")
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context)
            .notify(
                if (isTest) 8104 else NOTIFICATION_ID,
                notification
            )
    }

    private fun getNextAffirmation(context: Context): String {

        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val messages = AffirmationMessages.messages

        if (messages.isEmpty()) {
            return "Good morning Priyanshi! Keep smiling! ❤️"
        }

        val lastIndex = preferences.getInt(KEY_LAST_INDEX, -1)

        val nextIndex = (lastIndex + 1) % messages.size

        preferences.edit()
            .putInt(KEY_LAST_INDEX, nextIndex)
            .apply()

        return messages[nextIndex]
    }

    private fun createNotificationChannel(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daily Affirmations",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {

                description =
                    "Sweet daily Hinglish affirmations for Priyanshi"

            }

            val manager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }
}