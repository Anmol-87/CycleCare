package com.anmol.cyclecare.notifications

import android.util.Log
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PeriodReminderBootReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent?
    ) {
        val action = intent?.action
        Log.d("CycleCareBoot", "Boot receiver triggered. Action: $action")

        if (
            action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        if (!PeriodReminderScheduler.areRemindersEnabled(context)) {
            return
        }

        val pendingResult = goAsync()
        val appContext = context.applicationContext

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val predictedDate =
                    PeriodReminderDateProvider.getPredictedPeriodDate(
                        appContext
                    )

                PeriodReminderScheduler.rescheduleSavedReminder(
                    context = appContext,
                    predictedPeriodDate = predictedDate
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}