package com.anmol.cyclecare

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.anmol.cyclecare.data.local.CycleCareDatabase
import com.anmol.cyclecare.data.repository.PeriodRepository
import com.anmol.cyclecare.notifications.DailyAffirmationScheduler
import com.anmol.cyclecare.notifications.PeriodReminderScheduler
import com.anmol.cyclecare.ui.screens.CalendarScreen
import com.anmol.cyclecare.ui.screens.CareHistoryScreen
import com.anmol.cyclecare.ui.screens.CycleCareScreen
import com.anmol.cyclecare.ui.screens.HomeScreen
import com.anmol.cyclecare.ui.screens.InsightsScreen
import com.anmol.cyclecare.ui.screens.ProfileScreen
import com.anmol.cyclecare.ui.theme.CycleCareTheme
import com.anmol.cyclecare.viewmodel.CareViewModel
import com.anmol.cyclecare.viewmodel.PeriodViewModel
import com.anmol.cyclecare.viewmodel.PeriodViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var periodViewModel: PeriodViewModel
    private lateinit var careViewModel: CareViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Initialize the local database.
        val database = CycleCareDatabase.getDatabase(
            applicationContext
        )

        // Initialize the period repository.
        val repository = PeriodRepository(
            database.periodDao()
        )

        // Create the period ViewModel factory.
        val periodFactory = PeriodViewModelFactory(
            repository = repository,
            application = application
        )

        // Obtain the existing period ViewModel.
        periodViewModel = ViewModelProvider(
            this,
            periodFactory
        )[PeriodViewModel::class.java]

        // Obtain the care history ViewModel.
        careViewModel = ViewModelProvider(
            this
        )[CareViewModel::class.java]

        // Schedule daily affirmations for 8 AM.
        DailyAffirmationScheduler.scheduleNext(this)

        // TEMPORARY: Send a test affirmation notification.
        // Remove this line after confirming that notifications work.

        // Display the app.
        setContent {
            CycleCareTheme {
                CycleCareHomeScreen(
                    periodViewModel = periodViewModel,
                    careViewModel = careViewModel
                )
            }
        }
    }
}

@Composable
fun CycleCareHomeScreen(
    periodViewModel: PeriodViewModel,
    careViewModel: CareViewModel
) {

    val context = LocalContext.current

    // Access preferences used by the older version of CycleCare.
    val preferences = remember {
        context.getSharedPreferences(
            "cyclecare_preferences",
            Context.MODE_PRIVATE
        )
    }

    // Observe the predicted next period date.
    val predictedPeriodDate by
    periodViewModel.nextPredictedPeriodDate.collectAsState()

    // Migrate an older saved period date, if one exists.
    LaunchedEffect(Unit) {

        val oldDate = preferences.getString(
            "last_period_date",
            null
        )

        if (oldDate != null) {
            periodViewModel.migrateLegacyDate(oldDate)
        }
    }

    // Keep the period reminder synchronized with the prediction.
    LaunchedEffect(predictedPeriodDate) {

        // Do nothing if period reminders are disabled.
        if (!PeriodReminderScheduler.areRemindersEnabled(context)) {
            return@LaunchedEffect
        }

        // Check notification permission on Android 13 and later.
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            PeriodReminderScheduler.cancelReminder(context)
            return@LaunchedEffect
        }

        // Reschedule the period reminder.
        PeriodReminderScheduler.rescheduleSavedReminder(
            context = context,
            predictedPeriodDate = predictedPeriodDate
        )
    }

    // Connect all five screens to bottom navigation.
    CycleCareScreen(

        homeContent = {
            HomeScreen(
                periodViewModel = periodViewModel
            )
        },

        calendarContent = {
            CalendarScreen(
                periodViewModel = periodViewModel
            )
        },

        insightsContent = {
            InsightsScreen(
                periodViewModel = periodViewModel
            )
        },

        preciousContent = {
            CareHistoryScreen(
                careViewModel = careViewModel
            )
        },

        profileContent = {
            ProfileScreen(
                periodViewModel = periodViewModel
            )
        }
    )
}