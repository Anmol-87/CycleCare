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
import com.anmol.cyclecare.notifications.PeriodReminderScheduler
import com.anmol.cyclecare.ui.screens.CalendarScreen
import com.anmol.cyclecare.ui.screens.CycleCareScreen
import com.anmol.cyclecare.ui.screens.HomeScreen
import com.anmol.cyclecare.ui.screens.InsightsScreen
import com.anmol.cyclecare.ui.screens.ProfileScreen
import com.anmol.cyclecare.ui.theme.CycleCareTheme
import com.anmol.cyclecare.viewmodel.PeriodViewModel
import com.anmol.cyclecare.viewmodel.PeriodViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var periodViewModel: PeriodViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Initialize the local database.
        val database = CycleCareDatabase.getDatabase(
            applicationContext
        )

        // Initialize the repository.
        val repository = PeriodRepository(
            database.periodDao()
        )

        // Create the ViewModel factory.
        val factory = PeriodViewModelFactory(
            repository = repository,
            application = application
        )

        // Obtain the ViewModel.
        periodViewModel = ViewModelProvider(
            this,
            factory
        )[PeriodViewModel::class.java]

        // Display the app.
        setContent {
            CycleCareTheme {
                CycleCareHomeScreen(
                    periodViewModel = periodViewModel
                )
            }
        }
    }
}

@Composable
fun CycleCareHomeScreen(
    periodViewModel: PeriodViewModel
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

    // Automatically keep the scheduled reminder synchronized
    // with the latest predicted period date.
    LaunchedEffect(predictedPeriodDate) {

        // Do nothing if reminders are switched off.
        if (!PeriodReminderScheduler.areRemindersEnabled(context)) {
            return@LaunchedEffect
        }

        // Android 13 and later require notification permission.
        // ProfileScreen handles requesting this permission.
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Remove any existing alarm if permission is unavailable.
            PeriodReminderScheduler.cancelReminder(context)
            return@LaunchedEffect
        }

        // Cancel the old alarm and schedule a new one based
        // on the updated prediction and saved reminder settings.
        PeriodReminderScheduler.rescheduleSavedReminder(
            context = context,
            predictedPeriodDate = predictedPeriodDate
        )
    }

    // Connect all four screens to the bottom navigation.
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
        profileContent = {
            ProfileScreen(
                periodViewModel = periodViewModel
            )
        }
    )
}