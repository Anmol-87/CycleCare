package com.anmol.cyclecare.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anmol.cyclecare.viewmodel.PeriodViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

private val HomeBackground = Color(0xFFFFF8FA)
private val HomePink = Color(0xFFB83D68)
private val HomeLightPink = Color(0xFFFCE8EF)
private val HomeDarkText = Color(0xFF30252B)
private val HomeSecondaryText = Color(0xFF796B73)

@Composable
fun HomeScreen(
    periodViewModel: PeriodViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val periods by periodViewModel.periods.collectAsState()

    val latestPeriod = periods.firstOrNull()

    val formattedDate = latestPeriod?.startDate?.let { date ->
        runCatching {
            LocalDate.parse(date).format(
                DateTimeFormatter.ofPattern(
                    "dd MMM yyyy",
                    Locale.getDefault()
                )
            )
        }.getOrNull()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HomeBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(HomePink, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "C",
                    color = Color.White,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CycleCare",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = HomeDarkText
                )

                Text(
                    text = "Your health, your understanding",
                    fontSize = 12.sp,
                    color = HomeSecondaryText
                )
            }

            Text(
                text = "♡",
                color = HomePink,
                fontSize = 32.sp
            )
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Your cycle,",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = HomeDarkText
        )

        Text(
            text = "your story. 🌸",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = HomePink
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Take a moment for yourself. Every cycle is different, " +
                    "and understanding yours starts here.",
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = HomeSecondaryText
        )

        Spacer(Modifier.height(24.dp))

        // Cycle overview card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = HomeLightPink
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
                    letterSpacing = 1.3.sp,
                    color = HomePink
                )

                Spacer(Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "♡",
                        fontSize = 36.sp,
                        color = HomePink
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = formattedDate?.let {
                        "Last period started $it"
                    } ?: "Let's get to know your cycle",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = HomeDarkText
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = if (latestPeriod == null) {
                        "Log the first day of your last period to begin " +
                                "building your personal history."
                    } else {
                        "Your recorded period is saved on this device. " +
                                "You can review your dates in the Calendar tab."
                    },
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = HomeSecondaryText
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        showPeriodStartPicker(
                            context = context,
                            periodViewModel = periodViewModel
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HomePink
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

        Spacer(Modifier.height(24.dp))

        // Recent activity
        Text(
            text = "Your recent activity",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = HomeDarkText
        )

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🌷  Period records",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = HomeDarkText
                )

                Text(
                    text = when (periods.size) {
                        0 -> "No periods recorded yet."
                        1 -> "You've recorded 1 period."
                        else -> "You've recorded ${periods.size} periods."
                    },
                    fontSize = 14.sp,
                    color = HomeSecondaryText
                )

                Text(
                    text = "Your records stay available in CycleCare.",
                    fontSize = 12.sp,
                    color = HomeSecondaryText
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Be gentle with yourself. 💗",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = HomePink
        )
    }
}

private fun showPeriodStartPicker(
    context: Context,
    periodViewModel: PeriodViewModel
) {
    val calendar = Calendar.getInstance()

    val picker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance().apply {
                clear()
                set(year, month, dayOfMonth)
            }

            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (selectedCalendar.after(today)) {
                Toast.makeText(
                    context,
                    "A start date cannot be in the future.",
                    Toast.LENGTH_SHORT
                ).show()

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

    picker.datePicker.maxDate = Calendar.getInstance().timeInMillis
    picker.show()
}