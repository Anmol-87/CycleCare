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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.anmol.cyclecare.R

class CareScheduledReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "CycleCareCareReminder"
        private const val CHANNEL_ID = "anmol_care_reminders"

        private const val ACTION_CARE_REMINDER =
            "com.anmol.cyclecare.action.CARE_REMINDER"

        private const val EXTRA_REMINDER_ID = "reminder_id"
        private const val EXTRA_REMINDER_TYPE = "reminder_type"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(
            TAG,
            "Receiver invoked: action=${intent.action}"
        )

        if (intent.action != ACTION_CARE_REMINDER) {
            Log.w(TAG, "Unexpected action; ignoring broadcast")
            return
        }

        val reminderId =
            intent.getStringExtra(EXTRA_REMINDER_ID)

        val reminderType =
            intent.getStringExtra(EXTRA_REMINDER_TYPE)

        if (reminderId == null || reminderType == null) {
            Log.e(TAG, "Missing reminder ID or reminder type")
            return
        }

        Log.d(
            TAG,
            "Processing reminder: id=$reminderId, type=$reminderType"
        )

        if (!CareReminderScheduler.isEnabled(context)) {
            Log.w(TAG, "Anmol Feature reminders are disabled")
            return
        }

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Notification permission is not granted")
            return
        }

        val question = when (reminderType) {
            CareReminderReceiver.TYPE_WATER ->
                "Pani piya ki nahi? 💧❤️"

            CareReminderReceiver.TYPE_FOOD ->
                "Khana khaya ki nahi? 🍛❤️"

            CareReminderReceiver.TYPE_FRUITS ->
                "Aaj fruits khaye ki nahi? 🍎❤️"

            else -> {
                Log.e(TAG, "Unknown reminder type: $reminderType")
                return
            }
        }

        createNotificationChannel(context)

        val notificationId = reminderId.hashCode()

        val yesIntent = createResponsePendingIntent(
            context = context,
            action = CareReminderReceiver.ACTION_YES,
            reminderId = reminderId,
            reminderType = reminderType,
            requestCode = notificationId * 2
        )

        val noIntent = createResponsePendingIntent(
            context = context,
            action = CareReminderReceiver.ACTION_NO,
            reminderId = reminderId,
            reminderType = reminderType,
            requestCode = notificationId * 2 + 1
        )

        val notification = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Anmol Feature ❤️")
            .setContentText(question)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(question)
            )
            .addAction(0, "✅ Haan", yesIntent)
            .addAction(0, "❌ Nahi", noIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        Log.d(
            TAG,
            "Posting notification: id=$notificationId, type=$reminderType"
        )

        try {
            NotificationManagerCompat.from(context)
                .notify(notificationId, notification)

            Log.d(TAG, "Notification posted successfully")
        } catch (exception: SecurityException) {
            Log.e(
                TAG,
                "Security exception while posting notification",
                exception
            )
        } catch (exception: Exception) {
            Log.e(
                TAG,
                "Failed to post notification",
                exception
            )
        }

        // Schedule future reminders after this alarm fires.
        CareReminderScheduler.scheduleTodayAndTomorrow(context)
    }

    private fun createResponsePendingIntent(
        context: Context,
        action: String,
        reminderId: String,
        reminderType: String,
        requestCode: Int
    ): PendingIntent {
        val responseIntent = Intent(
            context,
            CareReminderReceiver::class.java
        ).apply {
            this.action = action
            putExtra(
                CareReminderReceiver.EXTRA_REMINDER_ID,
                reminderId
            )
            putExtra(
                CareReminderReceiver.EXTRA_REMINDER_TYPE,
                reminderType
            )
        }

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            responseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Anmol Feature Care Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description =
                    "Water, food, and fruit reminders"
            }

            manager.createNotificationChannel(channel)

            Log.d(TAG, "Notification channel created or verified")
        }
    }
}