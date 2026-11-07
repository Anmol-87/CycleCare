package com.anmol.cyclecare.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anmol.cyclecare.data.local.CareResponseEntity
import com.anmol.cyclecare.viewmodel.CareViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ---------------------------------------------------------------------------
// COLORS
// ---------------------------------------------------------------------------

private val CarePink = Color(0xFFB83D68)
private val CareDarkPink = Color(0xFF963052)
private val CareBackground = Color(0xFFFFF7FA)
private val CareText = Color(0xFF30252B)
private val CareMuted = Color(0xFF897780)
private val CareLightPink = Color(0xFFFCE8EF)
private val CareWhite = Color(0xFFFFFFFF)
private val CareGreen = Color(0xFF388B69)
private val CareLightGreen = Color(0xFFE9F6EF)

// ---------------------------------------------------------------------------
// MODELS
// ---------------------------------------------------------------------------

private enum class HistoryFilter(
    val label: String,
    val emoji: String,
    val type: String?
) {
    ALL("All", "💗", null),
    WATER("Water", "💧", "WATER"),
    FOOD("Food", "🍛", "FOOD"),
    FRUITS("Fruits", "🍎", "FRUITS")
}

private data class ReminderStyle(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val accent: Color,
    val soft: Color
)

private fun reminderStyle(type: String): ReminderStyle = when (type) {
    "WATER" -> ReminderStyle(
        emoji = "💧",
        title = "Water reminder",
        subtitle = "Stay hydrated",
        accent = Color(0xFF3B82C4),
        soft = Color(0xFFE6F1FB)
    )
    "FOOD" -> ReminderStyle(
        emoji = "🍛",
        title = "Food reminder",
        subtitle = "Nourish your body",
        accent = Color(0xFFD9822B),
        soft = Color(0xFFFDF0E1)
    )
    "FRUITS" -> ReminderStyle(
        emoji = "🍎",
        title = "Fruit reminder",
        subtitle = "Healthy eating",
        accent = Color(0xFFD64545),
        soft = Color(0xFFFDE8E8)
    )
    else -> ReminderStyle(
        emoji = "❤️",
        title = "Care reminder",
        subtitle = "A little self-care",
        accent = CarePink,
        soft = CareLightPink
    )
}

// ---------------------------------------------------------------------------
// MAIN SCREEN
// ---------------------------------------------------------------------------

@Composable
fun CareHistoryScreen(
    careViewModel: CareViewModel,
    modifier: Modifier = Modifier
) {
    val responses by careViewModel.careResponses.collectAsState()

    var selectedFilter by remember { mutableStateOf(HistoryFilter.ALL) }

    val doneCount = remember(responses) { responses.count { it.response == "YES" } }
    val skippedCount = responses.size - doneCount

    val grouped = remember(responses, selectedFilter) {
        val filtered = selectedFilter.type
            ?.let { type -> responses.filter { it.reminderType == type } }
            ?: responses

        filtered
            .sortedByDescending { it.answeredAt }
            .groupBy { startOfDay(it.answeredAt) }
            .toList()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF0F5),
                        CareBackground,
                        Color(0xFFFFF9FB)
                    )
                )
            )
    ) {
        // Subtle background texture
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = CarePink.copy(alpha = 0.035f),
                radius = 150.dp.toPx(),
                center = Offset(size.width, size.height * 0.12f)
            )
            drawCircle(
                color = CarePink.copy(alpha = 0.035f),
                radius = 110.dp.toPx(),
                center = Offset(0f, size.height * 0.65f)
            )
            drawCircle(
                color = Color(0xFFE9A8C0).copy(alpha = 0.07f),
                radius = 75.dp.toPx(),
                center = Offset(size.width * 0.9f, size.height * 0.85f)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 26.dp,
                bottom = 28.dp
            )
        ) {
            // HEADER
            item(key = "header") {
                HeaderSection()
                Spacer(modifier = Modifier.height(22.dp))
            }

            // INTRODUCTION CARD
            item(key = "intro") {
                IntroCard()
                Spacer(modifier = Modifier.height(22.dp))
            }

            // STATS
            item(key = "stats") {
                SectionTitle(title = "Your Activity", emoji = "🌷")
                Spacer(modifier = Modifier.height(12.dp))
                StatsCard(
                    total = responses.size,
                    done = doneCount,
                    skipped = skippedCount
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // FILTERS (only when there is something to filter)
            if (responses.isNotEmpty()) {
                item(key = "filters") {
                    FilterRow(
                        selected = selectedFilter,
                        onSelect = { selectedFilter = it }
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            // HISTORY LIST
            when {
                responses.isEmpty() -> {
                    item(key = "empty") { EmptyHistoryState() }
                }

                grouped.isEmpty() -> {
                    item(key = "empty_filter") {
                        EmptyFilterState(filter = selectedFilter)
                    }
                }

                else -> {
                    grouped.forEach { (dayStart, dayItems) ->
                        item(key = "day_$dayStart") {
                            DayHeader(
                                label = dayLabel(dayStart),
                                count = dayItems.size
                            )
                        }

                        items(
                            items = dayItems,
                            key = { it.id }
                        ) { response ->
                            CareResponseCard(response)
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        item(key = "gap_$dayStart") {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// HEADER
// ---------------------------------------------------------------------------

@Composable
private fun HeaderSection() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .shadow(
                    elevation = 7.dp,
                    shape = CircleShape,
                    ambientColor = CarePink.copy(alpha = 0.15f)
                )
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🌸", fontSize = 29.sp)
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = "Care History",
                fontSize = 27.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CareDarkPink,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Every little act of self-care matters.",
                fontSize = 12.sp,
                color = CareMuted
            )
        }
    }
}

// ---------------------------------------------------------------------------
// INTRODUCTION CARD
// ---------------------------------------------------------------------------

@Composable
private fun IntroCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(25.dp),
                ambientColor = CarePink.copy(alpha = 0.12f)
            ),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(containerColor = CareWhite)
    ) {
        Box {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
            ) {
                drawCircle(
                    color = CareLightPink.copy(alpha = 0.7f),
                    radius = 90.dp.toPx(),
                    center = Offset(size.width, 0f)
                )
                drawCircle(
                    color = CareLightPink.copy(alpha = 0.45f),
                    radius = 40.dp.toPx(),
                    center = Offset(size.width * 0.72f, size.height * 0.95f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "YOUR WELLNESS JOURNEY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = CarePink
                )

                Spacer(modifier = Modifier.height(9.dp))

                Text(
                    text = "Small steps,\nbeautiful progress. 💗",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 30.sp,
                    color = CareText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "A personal record of the care you give yourself every day.",
                    fontSize = 12.sp,
                    color = CareMuted,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// SECTION TITLE
// ---------------------------------------------------------------------------

@Composable
private fun SectionTitle(title: String, emoji: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = CareText
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(text = emoji, fontSize = 20.sp)
    }
}

// ---------------------------------------------------------------------------
// STATS CARD
// ---------------------------------------------------------------------------

@Composable
private fun StatsCard(
    total: Int,
    done: Int,
    skipped: Int
) {
    val progress = if (total == 0) 0f else done.toFloat() / total.toFloat()
    val percent = (progress * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = CarePink.copy(alpha = 0.08f)
            )
            .background(CareWhite, RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = CarePink.copy(alpha = 0.10f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(CareLightPink, RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Saved responses",
                    tint = CarePink,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Total Responses",
                    fontSize = 13.sp,
                    color = CareMuted,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "$total saved entries",
                    fontSize = 19.sp,
                    color = CareText,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(text = "💗", fontSize = 27.sp)
        }

        if (total > 0) {
            Spacer(modifier = Modifier.height(18.dp))

            // Completion progress bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Completion",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = CareMuted
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "$percent%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CareGreen
                )
            }

            Spacer(modifier = Modifier.height(7.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(CareLightPink)
            ) {
                if (progress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF5DB38E),
                                        CareGreen
                                    )
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiniStat(
                    modifier = Modifier.weight(1f),
                    label = "Done",
                    value = done,
                    color = CareGreen,
                    background = CareLightGreen
                )
                MiniStat(
                    modifier = Modifier.weight(1f),
                    label = "Skipped",
                    value = skipped,
                    color = CarePink,
                    background = CareLightPink
                )
            }
        }
    }
}

@Composable
private fun MiniStat(
    modifier: Modifier = Modifier,
    label: String,
    value: Int,
    color: Color,
    background: Color
) {
    Row(
        modifier = modifier
            .background(background, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

// ---------------------------------------------------------------------------
// FILTER CHIPS
// ---------------------------------------------------------------------------

@Composable
private fun FilterRow(
    selected: HistoryFilter,
    onSelect: (HistoryFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HistoryFilter.values().forEach { filter ->
            val isSelected = filter == selected

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) CarePink else CareWhite)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) CarePink else CarePink.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = filter.emoji, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = filter.label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Color.White else CareText
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// DAY HEADER
// ---------------------------------------------------------------------------

@Composable
private fun DayHeader(label: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.uppercase(Locale.getDefault()),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = CareMuted
        )

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(CarePink.copy(alpha = 0.12f))
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = if (count == 1) "1 entry" else "$count entries",
            fontSize = 10.sp,
            color = CareMuted
        )
    }
}

// ---------------------------------------------------------------------------
// EMPTY STATES
// ---------------------------------------------------------------------------

@Composable
private fun EmptyHistoryState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp)
    ) {
        Box(
            modifier = Modifier
                .size(105.dp)
                .background(CareLightPink, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🌷", fontSize = 49.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Your story starts here!",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = CareText
        )

        Spacer(modifier = Modifier.height(9.dp))

        Text(
            text = "Your care history will appear here.\n" +
                    "Start responding to your daily care reminders\n" +
                    "and build your wellness journey.",
            fontSize = 13.sp,
            color = CareMuted,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp
        )

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = "Made with love, for your well-being ♡",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = CarePink
        )
    }
}

@Composable
private fun EmptyFilterState(filter: HistoryFilter) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(CareLightPink, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = filter.emoji, fontSize = 36.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No ${filter.label.lowercase(Locale.getDefault())} entries yet",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = CareText
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Try another filter to see the rest of your journey.",
            fontSize = 12.sp,
            color = CareMuted,
            textAlign = TextAlign.Center
        )
    }
}

// ---------------------------------------------------------------------------
// RESPONSE CARD
// ---------------------------------------------------------------------------

@Composable
private fun CareResponseCard(response: CareResponseEntity) {
    val style = reminderStyle(response.reminderType)

    val isYes = response.response == "YES"
    val answerText = if (isYes) "Haan" else "Nahi"
    val answerColor = if (isYes) CareGreen else CarePink
    val answerBackground = if (isYes) CareLightGreen else CareLightPink
    val time = rememberTime(response.answeredAt)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = CarePink.copy(alpha = 0.08f)
            )
            .border(
                width = 1.dp,
                color = CarePink.copy(alpha = 0.09f),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CareWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // EMOJI ICON (tinted per reminder type)
            Box(
                modifier = Modifier
                    .size(53.dp)
                    .background(style.soft, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = style.emoji, fontSize = 27.sp)
            }

            Spacer(modifier = Modifier.width(13.dp))

            // REMINDER DETAILS
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = style.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CareText
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = style.subtitle,
                    fontSize = 11.sp,
                    color = style.accent,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = time,
                    fontSize = 10.sp,
                    color = CareMuted
                )
            }

            Spacer(modifier = Modifier.width(7.dp))

            // ANSWER BADGE
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .background(answerBackground, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = answerText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = answerColor
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = if (isYes) "Done ✓" else "Skipped",
                    fontSize = 9.sp,
                    color = answerColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// DATE HELPERS
// ---------------------------------------------------------------------------

@Composable
private fun rememberTime(timestamp: Long): String {
    return remember(timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
    }
}

private fun startOfDay(timestamp: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun dayLabel(dayStart: Long): String {
    val today = startOfDay(System.currentTimeMillis())
    val yesterday = Calendar.getInstance().apply {
        timeInMillis = today
        add(Calendar.DAY_OF_YEAR, -1)
    }.timeInMillis

    return when (dayStart) {
        today -> "Today"
        yesterday -> "Yesterday"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            .format(Date(dayStart))
    }
}