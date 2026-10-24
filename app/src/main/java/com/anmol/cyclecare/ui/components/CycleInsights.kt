package com.anmol.cyclecare.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.anmol.cyclecare.data.local.PeriodEntity

private val InsightPink = Color(0xFFB83D68)
private val InsightBackground = Color(0xFFFFF3F7)
private val InsightPurple = Color(0xFF8660B5)
private val InsightText = Color(0xFF49343D)

@Composable
fun CycleInsights(
    periods: List<PeriodEntity>,
    modifier: Modifier = Modifier
) {
    val sortedPeriods = periods
        .mapNotNull { period ->
            val start = runCatching {
                LocalDate.parse(period.startDate)
            }.getOrNull() ?: return@mapNotNull null

            val end = period.endDate?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()
            }

            period to (start to end)
        }
        .sortedByDescending { it.second.first }

    val averageCycleLength = sortedPeriods
        .map { it.second.first }
        .sorted()
        .zipWithNext { previous, next ->
            java.time.temporal.ChronoUnit.DAYS
                .between(previous, next)
        }
        .filter { it > 0 }
        .takeIf { it.isNotEmpty() }
        ?.average()
        ?.let { kotlin.math.round(it).toInt() }

    val completedDurations = sortedPeriods.mapNotNull { (_, dates) ->
        val start = dates.first
        val end = dates.second ?: return@mapNotNull null

        if (end.isBefore(start) || end.isAfter(LocalDate.now())) {
            return@mapNotNull null
        }

        java.time.temporal.ChronoUnit.DAYS
            .between(start, end)
            .toInt() + 1
    }

    val averagePeriodDuration = completedDurations
        .takeIf { it.isNotEmpty() }
        ?.average()
        ?.let { kotlin.math.round(it).toInt() }

    val latestStartDate = sortedPeriods.firstOrNull()?.second?.first

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Cycle Insights ✨",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = InsightText
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InsightCard(
                title = "Average Cycle",
                value = averageCycleLength?.let { "$it days" } ?: "Not enough data",
                description = if (averageCycleLength != null) {
                    "Average time between period starts"
                } else {
                    "Log at least two period start dates"
                },
                modifier = Modifier.weight(1f)
            )

            InsightCard(
                title = "Average Period",
                value = averagePeriodDuration?.let { "$it days" } ?: "Not enough data",
                description = if (averagePeriodDuration != null) {
                    "Based on completed periods"
                } else {
                    "Add an end date to a period"
                },
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = InsightBackground
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                InsightRow(
                    label = "Last period started",
                    value = latestStartDate?.format(
                        DateTimeFormatter.ofPattern("dd MMM yyyy")
                    ) ?: "No periods logged"
                )

                InsightRow(
                    label = "Periods tracked",
                    value = "${sortedPeriods.size}"
                )
            }
        }

        Text(
            text = "Insights are based only on logged records. They are estimates, not medical predictions.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
private fun InsightCard(
    title: String,
    value: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = InsightBackground
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = InsightPurple
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = InsightPink
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = InsightText
            )
        }
    }
}

@Composable
private fun InsightRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = InsightText
        )

        Spacer(modifier = Modifier.height(0.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = InsightPink
        )
    }
}