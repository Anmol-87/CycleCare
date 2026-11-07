package com.anmol.cyclecare.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anmol.cyclecare.data.local.PeriodEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

// --------------------------------------------------
// COLORS
// --------------------------------------------------

private val Pink = Color(0xFFB83D68)
private val DarkPink = Color(0xFF963153)
private val LightPink = Color(0xFFF5B8CB)
private val Purple = Color(0xFF8660B5)
private val TextDark = Color(0xFF49343D)
private val TextMuted = Color(0xFF9B8991)
private val GridColor = Color(0xFFF0E4E9)

// --------------------------------------------------
// DATA MODELS
// --------------------------------------------------

private data class PeriodRecord(
    val start: LocalDate,
    val end: LocalDate?
)

private data class CycleRecord(
    val label: String,
    val days: Int
)

private data class DurationRecord(
    val label: String,
    val days: Int
)

// --------------------------------------------------
// MAIN INSIGHTS
// --------------------------------------------------

@Composable
fun CycleInsights(
    periods: List<PeriodEntity>,
    modifier: Modifier = Modifier
) {

    val sortedPeriods = periods.mapNotNull { period ->

        val start = runCatching {
            LocalDate.parse(period.startDate)
        }.getOrNull() ?: return@mapNotNull null

        val end = period.endDate?.let {
            runCatching {
                LocalDate.parse(it)
            }.getOrNull()
        }

        if (end != null &&
            (end.isBefore(start) || end.isAfter(LocalDate.now()))
        ) {
            return@mapNotNull null
        }

        PeriodRecord(start, end)

    }.sortedBy { it.start }

    // Cycle lengths
    val cycleRecords = sortedPeriods
        .zipWithNext { previous, next ->

            val days = ChronoUnit.DAYS.between(
                previous.start,
                next.start
            ).toInt()

            CycleRecord(
                label = previous.start.format(
                    DateTimeFormatter.ofPattern("MMM")
                ),
                days = days
            )

        }
        .filter { it.days > 0 }
        .takeLast(6)

    // Period durations
    val durationRecords = sortedPeriods.mapNotNull { period ->

        val end = period.end ?: return@mapNotNull null

        val days = ChronoUnit.DAYS.between(
            period.start,
            end
        ).toInt() + 1

        DurationRecord(
            label = period.start.format(
                DateTimeFormatter.ofPattern("MMM")
            ),
            days = days
        )

    }.takeLast(6)

    val cycleValues = cycleRecords.map { it.days }

    val averageCycle = cycleValues
        .takeIf { it.isNotEmpty() }
        ?.average()
        ?.roundToInt()

    val averagePeriod = durationRecords
        .map { it.days }
        .takeIf { it.isNotEmpty() }
        ?.average()
        ?.roundToInt()

    val shortestCycle = cycleValues.minOrNull()
    val longestCycle = cycleValues.maxOrNull()

    val latestDate = sortedPeriods.lastOrNull()?.start

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {

        // HEADER
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            Text(
                text = "YOUR HEALTH AT A GLANCE",
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
                color = Pink
            )

            Text(
                text = "Your Statistics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )

            Text(
                text = "Small insights that help you understand your body.",
                fontSize = 13.sp,
                color = TextMuted,
                lineHeight = 20.sp
            )
        }

        // STATISTICS
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                PremiumStatCard(
                    title = "Average Cycle",
                    value = averageCycle?.let { "$it" } ?: "--",
                    unit = if (averageCycle != null) "days" else "",
                    description = "Between period starts",
                    icon = "🌸",
                    background = Color(0xFFFFEDF3),
                    accent = Pink,
                    modifier = Modifier.weight(1f)
                )

                PremiumStatCard(
                    title = "Average Period",
                    value = averagePeriod?.let { "$it" } ?: "--",
                    unit = if (averagePeriod != null) "days" else "",
                    description = "Completed periods",
                    icon = "🌷",
                    background = Color(0xFFF4EEFF),
                    accent = Purple,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                PremiumStatCard(
                    title = "Shortest Cycle",
                    value = shortestCycle?.toString() ?: "--",
                    unit = if (shortestCycle != null) "days" else "",
                    description = "Recorded minimum",
                    icon = "✨",
                    background = Color(0xFFFFF5E9),
                    accent = Color(0xFFC18443),
                    modifier = Modifier.weight(1f)
                )

                PremiumStatCard(
                    title = "Longest Cycle",
                    value = longestCycle?.toString() ?: "--",
                    unit = if (longestCycle != null) "days" else "",
                    description = "Recorded maximum",
                    icon = "💗",
                    background = Color(0xFFECF6F4),
                    accent = Color(0xFF57968A),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // CYCLE TREND
        PremiumChartCard(
            eyebrow = "MONTHLY OVERVIEW",
            title = "Cycle Length",
            subtitle = "Your cycle pattern over time",
            icon = "📈",
            badge = if (cycleRecords.isNotEmpty()) {
                "${cycleRecords.size} intervals"
            } else {
                "Waiting for data"
            }
        ) {

            if (cycleRecords.size >= 2) {
                CycleTrendChart(cycleRecords)
            } else {
                ChartEmptyState(
                    icon = "🌸",
                    title = "Your story starts here",
                    message = "Record at least three period start dates to see your cycle trend."
                )
            }
        }

        // PERIOD DURATION
        PremiumChartCard(
            eyebrow = "PERIOD HISTORY",
            title = "Period Duration",
            subtitle = "How long each recorded period lasted",
            icon = "🌷",
            badge = "${durationRecords.size} completed"
        ) {

            if (durationRecords.isNotEmpty()) {
                PeriodDurationChart(durationRecords)
            } else {
                ChartEmptyState(
                    icon = "💗",
                    title = "No completed periods yet",
                    message = "Add end dates to your recorded periods to see your duration chart."
                )
            }
        }

        // TRACKING SUMMARY
        TrackingSummary(
            latestDate = latestDate,
            totalPeriods = sortedPeriods.size,
            completedPeriods = durationRecords.size
        )

        // DISCLAIMER
        Text(
            text = "Your insights are calculated from your own recorded dates. " +
                    "Cycle patterns naturally vary, and these statistics are not medical predictions.",
            fontSize = 11.sp,
            color = TextMuted,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

// --------------------------------------------------
// PREMIUM STATISTICS CARD
// --------------------------------------------------

@Composable
private fun PremiumStatCard(
    title: String,
    value: String,
    unit: String,
    description: String,
    icon: String,
    background: Color,
    accent: Color,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(23.dp),
        colors = CardDefaults.cardColors(
            containerColor = background
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        fontSize = 15.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                Text(
                    text = value,
                    fontSize = 29.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accent
                )

                if (unit.isNotEmpty()) {
                    Text(
                        text = unit,
                        fontSize = 11.sp,
                        color = accent,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }
            }

            Text(
                text = description,
                fontSize = 10.sp,
                color = TextMuted,
                lineHeight = 15.sp
            )
        }
    }
}

// --------------------------------------------------
// PREMIUM CHART CARD
// --------------------------------------------------

@Composable
private fun PremiumChartCard(
    eyebrow: String,
    title: String,
    subtitle: String,
    icon: String,
    badge: String,
    content: @Composable () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {

                    Text(
                        text = eyebrow,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.7.sp,
                        color = Pink
                    )

                    Text(
                        text = title,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Text(
                    text = icon,
                    fontSize = 25.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFFFFF0F5))
                    .padding(horizontal = 11.dp, vertical = 6.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Pink
                )
            }

            content()
        }
    }
}

// --------------------------------------------------
// SMOOTH CYCLE TREND CHART
// --------------------------------------------------

@Composable
private fun CycleTrendChart(
    records: List<CycleRecord>
) {

    var selectedIndex by remember(records) {
        mutableIntStateOf(records.lastIndex)
    }

    val values = records.map { it.days }

    val minValue = ((values.minOrNull() ?: 0) - 5)
        .coerceAtLeast(0)

    val maxValue = (values.maxOrNull() ?: 1) + 5

    val range = (maxValue - minValue)
        .coerceAtLeast(1)
        .toFloat()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // SELECTED VALUE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {
                Text(
                    text = "SELECTED CYCLE",
                    fontSize = 9.sp,
                    letterSpacing = 1.2.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    Text(
                        text = "${records[selectedIndex].days}",
                        fontSize = 27.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Pink
                    )

                    Text(
                        text = "days",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            Text(
                text = records[selectedIndex].label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )
        }

        // CHART
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
        ) {

            val left = 12.dp.toPx()
            val right = size.width - 12.dp.toPx()
            val top = 15.dp.toPx()
            val bottom = size.height - 15.dp.toPx()

            val chartWidth = right - left
            val chartHeight = bottom - top

            // Grid lines
            for (i in 0..4) {
                val y = top + chartHeight * i / 4f

                drawLine(
                    color = GridColor,
                    start = Offset(left, y),
                    end = Offset(right, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Points
            val points = values.mapIndexed { index, value ->

                val x = if (values.size == 1) {
                    left + chartWidth / 2f
                } else {
                    left + chartWidth * index /
                            (values.size - 1).toFloat()
                }

                val y = bottom -
                        ((value - minValue) / range) * chartHeight

                Offset(x, y)
            }

            // Smooth curved path
            val linePath = Path().apply {

                moveTo(points.first().x, points.first().y)

                for (i in 0 until points.size - 1) {

                    val current = points[i]
                    val next = points[i + 1]

                    val controlX = (current.x + next.x) / 2f

                    cubicTo(
                        controlX,
                        current.y,
                        controlX,
                        next.y,
                        next.x,
                        next.y
                    )
                }
            }

            // Gradient below the curve
            val fillPath = Path().apply {

                addPath(linePath)

                lineTo(points.last().x, bottom)
                lineTo(points.first().x, bottom)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        LightPink.copy(alpha = 0.38f),
                        LightPink.copy(alpha = 0.02f)
                    ),
                    startY = top,
                    endY = bottom
                )
            )

            // Main curve
            drawPath(
                path = linePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        LightPink,
                        Pink,
                        DarkPink
                    )
                ),
                style = Stroke(
                    width = 3.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Data point circles
            points.forEachIndexed { index, point ->

                if (index == selectedIndex) {
                    drawCircle(
                        color = LightPink.copy(alpha = 0.3f),
                        radius = 12.dp.toPx(),
                        center = point
                    )
                }

                drawCircle(
                    color = Color.White,
                    radius = 7.dp.toPx(),
                    center = point
                )

                drawCircle(
                    color = if (index == selectedIndex) {
                        DarkPink
                    } else {
                        Pink
                    },
                    radius = 4.dp.toPx(),
                    center = point
                )
            }
        }

        // SELECTABLE DATA POINTS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            records.forEachIndexed { index, record ->

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (index == selectedIndex) {
                                Color(0xFFFFEDF3)
                            } else {
                                Color.Transparent
                            }
                        )
                        .padding(horizontal = 9.dp, vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = record.label,
                        fontSize = 10.sp,
                        fontWeight = if (index == selectedIndex) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        },
                        color = if (index == selectedIndex) {
                            Pink
                        } else {
                            TextMuted
                        }
                    )

                    Text(
                        text = "${record.days}",
                        fontSize = 10.sp,
                        color = TextDark
                    )
                }
            }
        }

        Text(
            text = "Tap a month label to view its cycle length.",
            fontSize = 10.sp,
            color = TextMuted
        )
    }
}

// --------------------------------------------------
// PERIOD DURATION CHART
// --------------------------------------------------

@Composable
private fun PeriodDurationChart(
    records: List<DurationRecord>
) {

    val maxValue = (records.maxOfOrNull { it.days } ?: 1)
        .coerceAtLeast(1)

    var selectedIndex by remember(records) {
        mutableIntStateOf(records.lastIndex)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {

        // Highlight selected duration
        Column {
            Text(
                text = "SELECTED PERIOD",
                fontSize = 9.sp,
                letterSpacing = 1.2.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                Text(
                    text = "${records[selectedIndex].days}",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Pink
                )

                Text(
                    text = "days",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }

        // BAR CHART
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(155.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {

            records.forEachIndexed { index, record ->

                val fraction = (
                        record.days.toFloat() / maxValue
                        ).coerceIn(0f, 1f)

                val barColor = if (index == selectedIndex) {
                    Pink
                } else {
                    LightPink
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {

                    Text(
                        text = "${record.days}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = barColor
                    )

                    Spacer(modifier = Modifier.height(7.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.68f)
                            .height(
                                (105f * fraction)
                                    .coerceAtLeast(7f)
                                    .dp
                            )
                            .clip(
                                RoundedCornerShape(
                                    topStart = 11.dp,
                                    topEnd = 11.dp
                                )
                            )
                            .background(
                                Brush.verticalGradient(
                                    colors = if (index == selectedIndex) {
                                        listOf(
                                            Color(0xFFE88BA9),
                                            Pink,
                                            DarkPink
                                        )
                                    } else {
                                        listOf(
                                            Color(0xFFF7D1DE),
                                            LightPink
                                        )
                                    }
                                )
                            )
                    )
                }
            }
        }

        // MONTH SELECTORS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            records.forEachIndexed { index, record ->

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (index == selectedIndex) {
                                Color(0xFFFFEDF3)
                            } else {
                                Color.Transparent
                            }
                        )
                        .padding(horizontal = 9.dp, vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = record.label,
                        fontSize = 10.sp,
                        fontWeight = if (index == selectedIndex) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        },
                        color = if (index == selectedIndex) {
                            Pink
                        } else {
                            TextMuted
                        }
                    )

                    Text(
                        text = "${record.days}d",
                        fontSize = 10.sp,
                        color = TextDark
                    )
                }
            }
        }

        Text(
            text = "Tap a month label to view its period duration.",
            fontSize = 10.sp,
            color = TextMuted
        )
    }
}

// --------------------------------------------------
// EMPTY CHART
// --------------------------------------------------

@Composable
private fun ChartEmptyState(
    icon: String,
    title: String,
    message: String
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFFF5F8))
            .padding(horizontal = 18.dp, vertical = 25.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {

        Text(
            text = icon,
            fontSize = 32.sp
        )

        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Text(
            text = message,
            fontSize = 11.sp,
            color = TextMuted,
            lineHeight = 18.sp
        )
    }
}

// --------------------------------------------------
// TRACKING SUMMARY
// --------------------------------------------------

@Composable
private fun TrackingSummary(
    latestDate: LocalDate?,
    totalPeriods: Int,
    completedPeriods: Int
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF0F5)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(19.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {

                Text(
                    text = "💗",
                    fontSize = 20.sp
                )

                Text(
                    text = "Your Tracking Summary",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }

            SummaryRow(
                label = "Last period started",
                value = latestDate?.format(
                    DateTimeFormatter.ofPattern("dd MMM yyyy")
                ) ?: "Not recorded"
            )

            SummaryRow(
                label = "Total periods tracked",
                value = "$totalPeriods"
            )

            SummaryRow(
                label = "Completed periods",
                value = "$completedPeriods"
            )
        }
    }
}

@Composable
private fun SummaryRow(
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
            fontSize = 12.sp,
            color = TextMuted
        )

        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Pink
        )
    }
}