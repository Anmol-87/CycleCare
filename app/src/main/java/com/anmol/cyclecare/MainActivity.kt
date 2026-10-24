package com.anmol.cyclecare

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.anmol.cyclecare.data.local.CycleCareDatabase
import com.anmol.cyclecare.data.repository.PeriodRepository
import com.anmol.cyclecare.ui.components.PeriodCalendar
import com.anmol.cyclecare.ui.theme.CycleCareTheme
import com.anmol.cyclecare.viewmodel.PeriodViewModel
import com.anmol.cyclecare.viewmodel.PeriodViewModelFactory
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale

private val BackgroundColor = Color(0xFFFFF8FA)
private val PrimaryPink = Color(0xFFB83D68)
private val LightPink = Color(0xFFFCE8EF)
private val DarkText = Color(0xFF30252B)
private val SecondaryText = Color(0xFF796B73)
private val SoftPurple = Color(0xFFF0EBFA)
private val SoftGreen = Color(0xFFE8F4EB)

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

    // Observe the period records saved in Room.
    val periods by periodViewModel.periods.collectAsState()

    val selectedPeriodDate = periods.firstOrNull()?.startDate

    // Convert saved ISO dates into LocalDate values for the calendar.
    val loggedPeriodDates = remember(periods) {
        periods.mapNotNull { period ->
            runCatching {
                LocalDate.parse(period.startDate)
            }.getOrNull()
        }.toSet()
    }

    // Migrate the date saved by the older version of the app.
    LaunchedEffect(Unit) {
        val oldDate = preferences.getString(
            "last_period_date",
            null
        )

        if (oldDate != null) {
            periodViewModel.migrateLegacyDate(oldDate)
        }
    }

    // Format the latest recorded date for the overview card.
    val formattedPeriodDate = selectedPeriodDate?.let { date ->
        try {
            val parser = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            )
            parser.isLenient = false

            val parsedDate = parser.parse(date)

            parsedDate?.let {
                SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.getDefault()
                ).format(it)
            } ?: date
        } catch (_: Exception) {
            date
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundColor
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 28.dp)
        ) {

            // App header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = PrimaryPink,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "C",
                        color = Color.White,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "CycleCare",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )

                    Text(
                        text = "Your health, your understanding",
                        fontSize = 12.sp,
                        color = SecondaryText
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = LightPink,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "♡",
                        fontSize = 25.sp,
                        color = PrimaryPink
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Welcome section
            Text(
                text = "Your cycle,",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Text(
                text = "your story.",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryPink
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Understand your patterns, record how you feel, " +
                        "and learn more about your menstrual health.",
                fontSize = 15.sp,
                lineHeight = 23.sp,
                color = SecondaryText
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Cycle overview card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LightPink
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    Text(
                        text = "YOUR CYCLE OVERVIEW",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = PrimaryPink
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.8f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "♡",
                            fontSize = 38.sp,
                            color = PrimaryPink
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (formattedPeriodDate == null) {
                            "Let's get to know your cycle"
                        } else {
                            "Last period started: $formattedPeriodDate"
                        },
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (selectedPeriodDate == null) {
                            "Record the first day of your last period " +
                                    "to begin building your personal cycle history."
                        } else {
                            "Your period start date has been saved " +
                                    "on this device."
                        },
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = SecondaryText
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Log period button
                    Button(
                        onClick = {
                            val calendar = Calendar.getInstance()

                            val datePicker = DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->

                                    val selectedCalendar =
                                        Calendar.getInstance().apply {
                                            clear()
                                            set(
                                                year,
                                                month,
                                                dayOfMonth
                                            )
                                        }

                                    val today = Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }

                                    // Reject future dates.
                                    if (selectedCalendar.after(today)) {
                                        return@DatePickerDialog
                                    }

                                    val date = String.format(
                                        Locale.US,
                                        "%04d-%02d-%02d",
                                        year,
                                        month + 1,
                                        dayOfMonth
                                    )

                                    periodViewModel.addPeriod(date)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            )

                            datePicker.datePicker.maxDate =
                                Calendar.getInstance().timeInMillis

                            datePicker.show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPink
                        )
                    ) {
                        Text(
                            text = "＋  Log my period",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Tracking section
            Text(
                text = "Take a moment for yourself",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Small observations can help you understand patterns.",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = SecondaryText
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TrackingCard(
                    emoji = "📅",
                    title = "Calendar",
                    description = "View your cycle history",
                    cardColor = SoftPurple,
                    modifier = Modifier.weight(1f)
                )

                TrackingCard(
                    emoji = "🌷",
                    title = "Symptoms",
                    description = "Record how you feel",
                    cardColor = SoftGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            // NEW: Interactive period calendar
            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Your period calendar",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Your recorded period start dates appear in pink.",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = SecondaryText
            )

            Spacer(modifier = Modifier.height(14.dp))

            PeriodCalendar(
                periodDates = loggedPeriodDates,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Health and privacy information
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "♡  Your health matters",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Cycle estimates are not guarantees. " +
                                "We'll use your recorded history to explain " +
                                "patterns and uncertainty rather than present " +
                                "guesses as medical facts.",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = SecondaryText
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Designed with privacy in mind.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryPink
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom note
            Text(
                text = "Made with care, for every cycle.",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 12.sp,
                color = SecondaryText
            )
        }
    }
}

@Composable
fun TrackingCard(
    emoji: String,
    title: String,
    description: String,
    cardColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = emoji,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = SecondaryText
            )
        }
    }
}