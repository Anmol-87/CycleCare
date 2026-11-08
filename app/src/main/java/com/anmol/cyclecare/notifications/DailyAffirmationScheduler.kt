package com.anmol.cyclecare.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDateTime
import java.time.ZoneId

object DailyAffirmationScheduler {

    private const val REQUEST_CODE = 8101

    private const val PREFS_NAME = "cyclecare_affirmation_preferences"
    private const val KEY_ENABLED = "affirmations_enabled"

    private const val MORNING_HOUR = 8
    private const val MORNING_MINUTE = 0

    fun scheduleNext(context: Context) {

        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        if (!preferences.getBoolean(KEY_ENABLED, true)) {
            cancel(context)
            return
        }

        val now = LocalDateTime.now()

        var nextTime = now
            .withHour(MORNING_HOUR)
            .withMinute(MORNING_MINUTE)
            .withSecond(0)
            .withNano(0)

        if (!nextTime.isAfter(now)) {
            nextTime = nextTime.plusDays(1)
        }

        val triggerTime = nextTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val alarmManager = context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

        val pendingIntent = createPendingIntent(context)

        alarmManager.cancel(pendingIntent)

        try {
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()
            ) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancel(context: Context) {

        val alarmManager = context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

        alarmManager.cancel(createPendingIntent(context))
    }

    private fun createPendingIntent(
        context: Context
    ): PendingIntent {

        val intent = Intent(
            context,
            DailyAffirmationReceiver::class.java
        )

        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }
    fun sendTestNotification(context: Context) {
        val intent = Intent(
            context,
            DailyAffirmationReceiver::class.java
        ).apply {
            putExtra(
                DailyAffirmationReceiver.EXTRA_TEST_NOTIFICATION,
                true
            )
        }

        context.sendBroadcast(intent)
    }
}