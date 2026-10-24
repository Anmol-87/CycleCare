package com.anmol.cyclecare.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anmol.cyclecare.ui.components.PeriodCalendar
import com.anmol.cyclecare.viewmodel.PeriodViewModel
import java.time.LocalDate
private val CalendarBackground = Color(0xFFFFF8FA)
private val CalendarPrimaryPink = Color(0xFFB83D68)
private val CalendarSecondaryText = Color(0xFF796B73)
private val CalendarDarkText = Color(0xFF30252B)
@Composable
fun CalendarScreen(
    periodViewModel: PeriodViewModel,
    modifier: Modifier = Modifier
) {
    val periods by periodViewModel.periods.collectAsState()
    val startDates = remember(periods) {
        periods.mapNotNull { period ->
            runCatching {
                LocalDate.parse(period.startDate)
            }.getOrNull()
        }.toSet()
    }
    val endDates = remember(periods) {
        periods.mapNotNull { period ->
            period.endDate?.let { date ->
                runCatching {
                    LocalDate.parse(date)
                }.getOrNull()
            }
        }.toSet()
    }
    val periodDates = remember(periods) {
        buildSet {
            periods.forEach { period ->
                val start = runCatching {
                    LocalDate.parse(period.startDate)
                }.getOrNull() ?: return@forEach
                val end = period.endDate?.let { date ->
                    runCatching {
                        LocalDate.parse(date)
                    }.getOrNull()
                } ?: start
                // Ignore an invalid end date rather than marking
                // dates before the period start.
                val validEnd = if (end.isBefore(start)) start else end
                var date = start
                while (!date.isAfter(validEnd)) {
                    add(date)
                    date = date.plusDays(1)
                }
            }
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CalendarBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 24.dp)
    ) {
        Text(
            text = "Your Calendar 🌸",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = CalendarDarkText
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Your cycle history, all in one place.",
            fontSize = 14.sp,
            color = CalendarSecondaryText
        )
        Spacer(Modifier.height(24.dp))
        PeriodCalendar(
            periodDates = periodDates,
            startDates = startDates,
            endDates = endDates,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Understanding your calendar",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = CalendarDarkText
        )
        Spacer(Modifier.height(12.dp))
        CalendarExplanation(
            marker = "S",
            title = "Period started",
            description = "The first day of a recorded period.",
            markerColor = CalendarPrimaryPink
        )
        Spacer(Modifier.height(10.dp))
        CalendarExplanation(
            marker = "•",
            title = "Period day",
            description = "A day included in a recorded period.",
            markerColor = Color(0xFFF0A8C0)
        )
        Spacer(Modifier.height(10.dp))
        CalendarExplanation(
            marker = "E",
            title = "Period ended",
            description = "The end date saved for a period.",
            markerColor = Color(0xFF8660B5)
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Only dates you've recorded are marked. " +
                    "Unrecorded dates are not predictions.",
            fontSize = 13.sp,
            color = CalendarSecondaryText,
            lineHeight = 20.sp
        )
    }
}
@Composable
private fun CalendarExplanation(
    marker: String,
    title: String,
    description: String,
    markerColor: Color
) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .background(
                        color = markerColor,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = marker,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(start = 14.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    color = CalendarDarkText,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = description,
                    color = CalendarSecondaryText,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}