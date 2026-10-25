package com.anmol.cyclecare.notifications

import android.content.Context
import com.anmol.cyclecare.data.local.CycleCareDatabase
import java.time.LocalDate
import kotlinx.coroutines.flow.first

object PeriodReminderDateProvider {

    private const val PREFERENCES_NAME = "cyclecare_preferences"
    private const val KEY_CYCLE_LENGTH = "cycle_length"
    private const val DEFAULT_CYCLE_LENGTH = 28

    suspend fun getPredictedPeriodDate(
        context: Context
    ): LocalDate? {

        val appContext = context.applicationContext

        // Read the same cycle-length preference as PeriodViewModel.
        val preferences = appContext.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

        val cycleLength = preferences.getInt(
            KEY_CYCLE_LENGTH,
            DEFAULT_CYCLE_LENGTH
        ).coerceIn(15, 60)

        // Read saved period records from Room.
        val database = CycleCareDatabase.getDatabase(appContext)

        val periods = database.periodDao()
            .getAllPeriods()
            .first()

        // The DAO sorts records by startDate DESC.
        val latestPeriod = periods.firstOrNull()
            ?: return null

        // Match PeriodViewModel's prediction calculation.
        return runCatching {
            LocalDate.parse(latestPeriod.startDate)
                .plusDays(cycleLength.toLong())
        }.getOrNull()
    }
}