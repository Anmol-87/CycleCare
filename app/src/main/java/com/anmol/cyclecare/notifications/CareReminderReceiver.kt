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
import com.anmol.cyclecare.data.local.CareResponseEntity
import com.anmol.cyclecare.data.local.CycleCareDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class CareReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_YES =
            "com.anmol.cyclecare.action.CARE_YES"

        const val ACTION_NO =
            "com.anmol.cyclecare.action.CARE_NO"

        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_REMINDER_TYPE = "reminder_type"

        const val TYPE_WATER = "WATER"
        const val TYPE_FOOD = "FOOD"
        const val TYPE_FRUITS = "FRUITS"

        private const val CHANNEL_ID = "anmol_care_responses"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        if (action != ACTION_YES && action != ACTION_NO) return

        val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID) ?: return
        val reminderType = intent.getStringExtra(EXTRA_REMINDER_TYPE) ?: return

        if (reminderType !in setOf(TYPE_WATER, TYPE_FOOD, TYPE_FRUITS)) return

        val answer = if (action == ACTION_YES) "YES" else "NO"
        val appContext = context.applicationContext
        val pendingResult = goAsync()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val database = CycleCareDatabase.getDatabase(appContext)
                val dao = database.careResponseDao()

                // Avoid saving the same reminder twice.
                val previousResponse = dao.getResponseForReminder(reminderId)

                if (previousResponse == null) {
                    dao.saveResponse(
                        CareResponseEntity(
                            reminderType = reminderType,
                            response = answer,
                            answeredAt = System.currentTimeMillis(),
                            reminderId = reminderId
                        )
                    )

                    showFollowUpNotification(
                        context = appContext,
                        reminderType = reminderType,
                        answer = answer,
                        reminderId = reminderId
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showFollowUpNotification(
        context: Context,
        reminderType: String,
        answer: String,
        reminderId: String
    ) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        createNotificationChannel(context)

        val message = when (reminderType) {
            TYPE_WATER -> {
                if (answer == "YES") {
                    "Bahot badhiya! 🥰❤️ Aise hi apna khayal rakhna!"
                } else {
                    "Chalo chalo, jaldi se pani pi lo! 💧❤️"
                }
            }

            TYPE_FOOD -> {
                if (answer == "YES") {
                    "Bahot badhiya! 🥰❤️ Aise hi apna khayal rakhna!"
                } else {
                    "Chalo chalo, jaldi se kuch kha lo! 🍛❤️"
                }
            }

            TYPE_FRUITS -> {
                if (answer == "YES") {
                    "Bahot badhiya! 🥰🍓 Healthy rehna, meri favourite person!"
                } else {
                    "Chalo, aaj thode fruits kha lo! 🍎❤️"
                }
            }

            else -> return
        }

        val openAppIntent = Intent(context, MainActivity::class.java)

        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Precious Feature ❤️")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context)
            .notify(reminderId.hashCode() xor 0x5A5A, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Precious Feature Responses",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Caring replies to water, food, and fruit reminders"
            }

            manager.createNotificationChannel(channel)
        }
    }
}