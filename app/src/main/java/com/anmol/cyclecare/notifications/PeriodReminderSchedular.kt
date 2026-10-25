package com.anmol.cyclecare.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDate
import java.time.ZoneId

object PeriodReminderScheduler {

    private const val PREFS_NAME = "cyclecare_reminder_preferences"
    private const val KEY_ENABLED = "reminders_enabled"
    private const val KEY_DAYS_BEFORE = "reminder_days_before"

    private const val REQUEST_CODE = 4101

    private const val REMINDER_HOUR = 9
    private const val REMINDER_MINUTE = 0

    fun areRemindersEnabled(context: Context): Boolean {
        return context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).getBoolean(KEY_ENABLED, false)
    }

    fun getDaysBefore(context: Context): Int {
        return context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).getInt(KEY_DAYS_BEFORE, 2).coerceIn(1, 3)
    }

    fun setReminderPreferences(
        context: Context,
        enabled: Boolean,
        daysBefore: Int,
        predictedPeriodDate: LocalDate?
    ) {
        require(daysBefore in 1..3) {
            "Reminder days must be between 1 and 3."
        }

        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).edit()
            .putBoolean(KEY_ENABLED, enabled)
            .putInt(KEY_DAYS_BEFORE, daysBefore)
            .apply()

        if (enabled && predictedPeriodDate != null) {
            scheduleReminder(
                context = context,
                predictedPeriodDate = predictedPeriodDate,
                daysBefore = daysBefore
            )
        } else {
            cancelReminder(context)
        }
    }

    fun rescheduleSavedReminder(
        context: Context,
        predictedPeriodDate: LocalDate?
    ) {
        if (!areRemindersEnabled(context) || predictedPeriodDate == null) {
            cancelReminder(context)
            return
        }

        scheduleReminder(
            context = context,
            predictedPeriodDate = predictedPeriodDate,
            daysBefore = getDaysBefore(context)
        )
    }

    fun scheduleReminder(
        context: Context,
        predictedPeriodDate: LocalDate,
        daysBefore: Int
    ) {
        if (!areRemindersEnabled(context)) return
        if (daysBefore !in 1..3) return

        val reminderDate = predictedPeriodDate.minusDays(
            daysBefore.toLong()
        )

        val reminderTime = reminderDate
            .atTime(REMINDER_HOUR, REMINDER_MINUTE)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val alarmManager = context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

        val pendingIntent = createPendingIntent(context)

        // Cancel any previously scheduled reminder.
        alarmManager.cancel(pendingIntent)

        // Don't schedule reminders for dates that have already passed.
        if (reminderTime <= System.currentTimeMillis()) return

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            // Use an inexact alarm if exact-alarm access is unavailable.
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
        }
    }

    fun cancelReminder(context: Context) {
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
            PeriodReminderReceiver::class.java
        )

        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }
}