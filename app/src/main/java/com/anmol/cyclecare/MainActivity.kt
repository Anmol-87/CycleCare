package com.anmol.cyclecare

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import com.anmol.cyclecare.data.local.CycleCareDatabase
import com.anmol.cyclecare.data.repository.PeriodRepository
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

        val database = CycleCareDatabase.getDatabase(applicationContext)
        val repository = PeriodRepository(database.periodDao())
        val factory = PeriodViewModelFactory(repository)

        periodViewModel = ViewModelProvider(
            this,
            factory
        )[PeriodViewModel::class.java]

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

    val preferences = remember {
        context.getSharedPreferences(
            "cyclecare_preferences",
            Context.MODE_PRIVATE
        )
    }

    // Migrate a date saved by the older version of CycleCare.
    LaunchedEffect(Unit) {
        val oldDate = preferences.getString(
            "last_period_date",
            null
        )

        if (oldDate != null) {
            periodViewModel.migrateLegacyDate(oldDate)
        }
    }

    // Connect the four screens to the bottom navigation.
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
            ProfileScreen()
        }
    )
}