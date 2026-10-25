package com.anmol.cyclecare.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

private val PeriodPink = Color(0xFFB83D68)
private val LightPeriodPink = Color(0xFFFCE8EF)
private val EndDatePurple = Color(0xFF8660B5)
private val PredictedLavender = Color(0xFFE9E0FA)
private val DarkText = Color(0xFF30252B)
private val MutedText = Color(0xFF9A8490)

@Composable
fun PeriodCalendar(
    periodDates: Set<LocalDate>,
    startDates: Set<LocalDate>,
    endDates: Set<LocalDate>,
    modifier: Modifier = Modifier,
    onDateSelected: (LocalDate) -> Unit = {},
    predictedDates: Set<LocalDate> = emptySet()
) {
    var displayedMonth by remember {
        mutableStateOf(YearMonth.now())
    }

    var selectedDate by remember {
        mutableStateOf<LocalDate?>(null)
    }

    val today = LocalDate.now()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        displayedMonth = displayedMonth.minusMonths(1)
                        selectedDate = null
                    }
                ) {
                    Text(
                        text = "‹",
                        fontSize = 30.sp,
                        color = PeriodPink
                    )
                }

                Text(
                    text = displayedMonth.format(
                        DateTimeFormatter.ofPattern(
                            "MMMM yyyy",
                            Locale.getDefault()
                        )
                    ),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                IconButton(
                    onClick = {
                        displayedMonth = displayedMonth.plusMonths(1)
                        selectedDate = null
                    }
                ) {
                    Text(
                        text = "›",
                        fontSize = 30.sp,
                        color = PeriodPink
                    )
                }
            }

            // Weekday headings
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    "Mon", "Tue", "Wed", "Thu",
                    "Fri", "Sat", "Sun"
                ).forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        color = MutedText,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.padding(top = 4.dp))

            val firstDay = displayedMonth.atDay(1)
            val daysInMonth = displayedMonth.lengthOfMonth()

            val startingOffset =
                firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value

            val totalCells =
                ((startingOffset + daysInMonth + 6) / 7) * 7

            // Calendar dates
            for (weekStart in 0 until totalCells step 7) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (column in 0 until 7) {
                        val dayNumber =
                            weekStart + column - startingOffset + 1

                        if (dayNumber in 1..daysInMonth) {
                            val date = displayedMonth.atDay(dayNumber)

                            val isPeriodDay = date in periodDates
                            val isStartDate = date in startDates
                            val isEndDate = date in endDates
                            val isPredictedDate =
                                date in predictedDates && !isPeriodDay
                            val isToday = date == today
                            val isSelected = date == selectedDate

                            // Recorded dates take priority over predictions.
                            val backgroundColor = when {
                                isStartDate -> PeriodPink
                                isEndDate -> EndDatePurple
                                isPeriodDay -> LightPeriodPink
                                isPredictedDate -> PredictedLavender
                                else -> Color.Transparent
                            }

                            val textColor = when {
                                isStartDate || isEndDate -> Color.White
                                isPeriodDay -> DarkText
                                isPredictedDate -> Color(0xFF68469A)
                                isToday -> PeriodPink
                                else -> DarkText
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 3.dp)
                                    .aspectRatio(0.82f)
                                    .background(
                                        color = backgroundColor,
                                        shape = CircleShape
                                    )
                                    .then(
                                        if (isToday || isSelected) {
                                            Modifier.border(
                                                width = if (isSelected) {
                                                    2.dp
                                                } else {
                                                    1.dp
                                                },
                                                color = if (isSelected) {
                                                    DarkText
                                                } else {
                                                    PeriodPink
                                                },
                                                shape = CircleShape
                                            )
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .clickable {
                                        selectedDate = date
                                        onDateSelected(date)
                                    },
                                horizontalAlignment =
                                    Alignment.CenterHorizontally,
                                verticalArrangement =
                                    Arrangement.Center
                            ) {
                                Text(
                                    text = dayNumber.toString(),
                                    color = textColor,
                                    fontSize = 14.sp,
                                    fontWeight = if (
                                        isToday ||
                                        isPeriodDay ||
                                        isPredictedDate ||
                                        isSelected
                                    ) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    }
                                )

                                when {
                                    isPeriodDay -> {
                                        Text(
                                            text = when {
                                                isStartDate && isEndDate -> "●"
                                                isStartDate -> "S"
                                                isEndDate -> "E"
                                                else -> "•"
                                            },
                                            color = textColor,
                                            fontSize = 8.sp,
                                            lineHeight = 9.sp
                                        )
                                    }

                                    isPredictedDate -> {
                                        Text(
                                            text = "•",
                                            color = Color(0xFF8660B5),
                                            fontSize = 8.sp,
                                            lineHeight = 9.sp
                                        )
                                    }
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

            Spacer(modifier = Modifier.padding(top = 8.dp))

            // Calendar legend
            CalendarLegend(
                color = PeriodPink,
                label = "Period start"
            )

            CalendarLegend(
                color = LightPeriodPink,
                label = "Recorded period days"
            )

            CalendarLegend(
                color = EndDatePurple,
                label = "Period end"
            )

            CalendarLegend(
                color = PredictedLavender,
                label = "Predicted period days"
            )

            Spacer(modifier = Modifier.padding(top = 4.dp))

            // Selected date information
            selectedDate?.let { date ->
                val message = when {
                    date in startDates && date in endDates ->
                        "Period started and ended on this date"

                    date in startDates ->
                        "Recorded period start date"

                    date in endDates ->
                        "Recorded period end date"

                    date in periodDates ->
                        "Recorded period day"

                    date in predictedDates ->
                        "Predicted period day — this is only an estimate"

                    else ->
                        "No period recorded on this date"
                }

                Text(
                    text = "$message • ${
                        date.format(
                            DateTimeFormatter.ofPattern("d MMM yyyy")
                        )
                    }",
                    modifier = Modifier.padding(top = 12.dp),
                    color = DarkText,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun CalendarLegend(
    color: Color,
    label: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier
                .background(color, CircleShape)
                .padding(6.dp)
        )

        Text(
            text = "  $label",
            color = MutedText,
            fontSize = 12.sp
        )
    }
}