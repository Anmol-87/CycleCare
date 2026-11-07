package com.anmol.cyclecare.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDate
import java.time.ZoneId

object CareReminderScheduler {

    private const val PREFS_NAME = "anmol_care_preferences"
    private const val KEY_ENABLED = "care_reminders_enabled"

    private const val ACTION_CARE_REMINDER =
        "com.anmol.cyclecare.action.CARE_REMINDER"

    private const val EXTRA_REMINDER_ID = "reminder_id"
    private const val EXTRA_REMINDER_TYPE = "reminder_type"

    private data class Schedule(
        val id: String,
        val type: String,
        val hour: Int
    )

    private val dailySchedule = listOf(
        Schedule("water_09", CareReminderReceiver.TYPE_WATER, 9),
        Schedule("water_12", CareReminderReceiver.TYPE_WATER, 12),
        Schedule("water_15", CareReminderReceiver.TYPE_WATER, 15),
        Schedule("food_15", CareReminderReceiver.TYPE_FOOD, 15),
        Schedule("fruits_17", CareReminderReceiver.TYPE_FRUITS, 17),
        Schedule("water_18", CareReminderReceiver.TYPE_WATER, 18)
    )

    fun isEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, false)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENABLED, enabled)
            .apply()

        if (enabled) {
            scheduleTodayAndTomorrow(context)
        } else {
            cancelAll(context)
        }
    }

    fun scheduleTodayAndTomorrow(context: Context) {
        if (!isEnabled(context)) return

        val today = LocalDate.now()
        dailySchedule.forEach { schedule ->
            scheduleDate(context, today, schedule)
            scheduleDate(context, today.plusDays(1), schedule)
        }
    }

    private fun scheduleDate(
        context: Context,
        date: LocalDate,
        schedule: Schedule
    ) {
        val reminderDateTime = date.atTime(schedule.hour, 0)
            .atZone(ZoneId.systemDefault())

        if (reminderDateTime.toInstant().toEpochMilli() <= System.currentTimeMillis()) {
            return
        }

        val reminderId = "${schedule.id}_${date}"

        val intent = Intent(context, CareScheduledReminderReceiver::class.java).apply {
            action = ACTION_CARE_REMINDER
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_REMINDER_TYPE, schedule.type)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderDateTime.toInstant().toEpochMilli(),
                pendingIntent
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderDateTime.toInstant().toEpochMilli(),
                pendingIntent
            )
        }
    }

    fun cancelAll(context: Context) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val today = LocalDate.now()

        // Cancel current and next day's scheduled alarms.
        for (date in listOf(today, today.plusDays(1))) {
            dailySchedule.forEach { schedule ->
                val reminderId = "${schedule.id}_${date}"

                val intent = Intent(
                    context,
                    CareScheduledReminderReceiver::class.java
                ).apply {
                    action = ACTION_CARE_REMINDER
                    putExtra(EXTRA_REMINDER_ID, reminderId)
                    putExtra(EXTRA_REMINDER_TYPE, schedule.type)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    reminderId.hashCode(),
                    intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )

                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                }
            }
        }
    }
}