package com.anmol.cyclecare.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PeriodPink = Color(0xFFFF6B9A)
private val LightPink = Color(0xFFFFE1EB)
private val DarkText = Color(0xFF49333D)
private val MutedText = Color(0xFF9A8490)

@Composable
fun PeriodCalendar(
    periodDates: Set<LocalDate>,
    modifier: Modifier = Modifier,
    onDateSelected: (LocalDate) -> Unit = {}
) {
    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val today = LocalDate.now()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    displayedMonth = displayedMonth.minusMonths(1)
                    selectedDate = null
                }) {
                    Text("‹", fontSize = 30.sp, color = PeriodPink)
                }

                Text(
                    text = displayedMonth.format(
                        DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
                    ),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                IconButton(onClick = {
                    displayedMonth = displayedMonth.plusMonths(1)
                    selectedDate = null
                }) {
                    Text("›", fontSize = 30.sp, color = PeriodPink)
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    .forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            color = MutedText,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
            }

            val firstDay = displayedMonth.atDay(1)
            val daysInMonth = displayedMonth.lengthOfMonth()
            val startingOffset = firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value
            val totalCells = ((startingOffset + daysInMonth + 6) / 7) * 7

            for (weekStart in 0 until totalCells step 7) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (column in 0 until 7) {
                        val dayNumber = weekStart + column - startingOffset + 1

                        if (dayNumber in 1..daysInMonth) {
                            val date = displayedMonth.atDay(dayNumber)
                            val isPeriodDay = date in periodDates
                            val isToday = date == today
                            val isSelected = date == selectedDate

                            val backgroundColor = when {
                                isSelected -> DarkText
                                isPeriodDay -> PeriodPink
                                else -> Color.Transparent
                            }

                            val textColor = when {
                                isSelected || isPeriodDay -> Color.White
                                isToday -> PeriodPink
                                else -> DarkText
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 3.dp)
                                    .aspectRatio(0.82f)
                                    .background(backgroundColor, CircleShape)
                                    .clickable {
                                        selectedDate = date
                                        onDateSelected(date)
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = dayNumber.toString(),
                                    color = textColor,
                                    fontSize = 14.sp,
                                    fontWeight = if (
                                        isToday || isPeriodDay || isSelected
                                    ) FontWeight.Bold else FontWeight.Normal
                                )

                                if (isPeriodDay) {
                                    Text(
                                        text = "•",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        lineHeight = 9.sp
                                    )
                                }
                            }
                        } else {
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.82f)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(
                    modifier = Modifier
                        .background(PeriodPink, CircleShape)
                        .padding(5.dp)
                )

                Text(
                    text = "  Logged period date",
                    color = MutedText,
                    fontSize = 12.sp
                )
            }

            selectedDate?.let { date ->
                Text(
                    text = if (date in periodDates) {
                        "💗 Period recorded on ${
                            date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
                        }"
                    } else {
                        "Selected: ${
                            date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
                        }"
                    },
                    modifier = Modifier.padding(top = 10.dp),
                    color = DarkText,
                    fontSize = 14.sp
                )
            }
        }
    }
}