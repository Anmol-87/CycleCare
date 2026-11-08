package com.anmol.cyclecare.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AffirmationBootReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent?
    ) {

        when (intent?.action) {

            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {

                DailyAffirmationScheduler.scheduleNext(
                    context.applicationContext
                )
            }
        }
    }
}