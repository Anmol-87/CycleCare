package com.anmol.cyclecare.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anmol.cyclecare.data.local.PeriodEntity
import com.anmol.cyclecare.ui.components.PeriodCalendar
import com.anmol.cyclecare.viewmodel.PeriodViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val DEFAULT_PREDICTED_PERIOD_DAYS = 5

private val CalendarPink = Color(0xFFB83D68)
private val CalendarDarkPink = Color(0xFF963052)
private val CalendarLightPink = Color(0xFFFCE8EF)
private val CalendarPurple = Color(0xFF8660B5)
private val CalendarBackground = Color(0xFFFFF8FA)
private val CalendarText = Color(0xFF49323D)
private val CalendarMuted = Color(0xFF8D7882)

private val calendarDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy")

// ----------------------------------------------------
// PREMIUM STATIC FLORAL TEXTURE
// ----------------------------------------------------

private fun Modifier.calendarTexture(
    progress: Float = 0f,
    accent: Color = CalendarPink
): Modifier = this.drawBehind {

    val width = size.width
    val height = size.height

    if (width <= 0f || height <= 0f) {
        return@drawBehind
    }

    // Soft decorative corner gradients
    drawCircle(
        color = accent.copy(alpha = 0.045f),
        radius = width * 0.32f,
        center = Offset(width * 0.95f, height * 0.05f)
    )

    drawCircle(
        color = accent.copy(alpha = 0.035f),
        radius = width * 0.25f,
        center = Offset(width * 0.02f, height * 0.95f)
    )

    // Elegant diagonal lines
    var x = -height

    while (x < width) {

        drawLine(
            color = accent.copy(alpha = 0.025f),
            start = Offset(x, 0f),
            end = Offset(x + height, height),
            strokeWidth = 1f
        )

        x += 34f
    }

    // Delicate decorative dots
    var dotX = 18f

    while (dotX < width) {

        var dotY = 18f

        while (dotY < height) {

            drawCircle(
                color = accent.copy(alpha = 0.055f),
                radius = 1.5f,
                center = Offset(dotX, dotY)
            )

            dotY += 38f
        }

        dotX += 38f
    }
}

// ----------------------------------------------------
// MAIN CALENDAR SCREEN
// ----------------------------------------------------

@Composable
fun CalendarScreen(
    periodViewModel: PeriodViewModel
) {

    val periods by periodViewModel.periods.collectAsState()

    val nextPredictedPeriodDate by
    periodViewModel.nextPredictedPeriodDate.collectAsState()

    val selectedCycleLength by
    periodViewModel.selectedCycleLength.collectAsState()

    var selectedDate by remember {
        mutableStateOf<LocalDate?>(null)
    }

    // Flower animation only
    val infiniteTransition = rememberInfiniteTransition(
        label = "calendar_animation"
    )

    val flowerScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flower_scale"
    )

    // Recorded start dates
    val startDates: Set<LocalDate> = remember(periods) {
        periods.mapNotNull { period ->
            parseCalendarDate(period.startDate)
        }.toSet()
    }

    // Recorded end dates
    val endDates: Set<LocalDate> = remember(periods) {
        periods.mapNotNull { period ->
            period.endDate?.let {
                parseCalendarDate(it)
            }
        }.toSet()
    }

    // All recorded period days
    val periodDates: Set<LocalDate> = remember(periods) {
        buildSet {

            periods.forEach { period ->

                val start = parseCalendarDate(period.startDate)
                    ?: return@forEach

                val end = period.endDate?.let {
                    parseCalendarDate(it)
                } ?: start

                var date = start

                while (!date.isAfter(end)) {
                    add(date)
                    date = date.plusDays(1)
                }
            }
        }
    }

    val predictedStartDate: LocalDate? =
        nextPredictedPeriodDate

    // Predicted period days
    val predictedDates: Set<LocalDate> =
        remember(predictedStartDate, periodDates) {

            if (predictedStartDate == null) {
                emptySet()
            } else {

                buildSet {

                    repeat(DEFAULT_PREDICTED_PERIOD_DAYS) { offset ->

                        val date =
                            predictedStartDate.plusDays(offset.toLong())

                        if (date !in periodDates) {
                            add(date)
                        }
                    }
                }
            }
        }

    // Main screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFEEF4),
                        Color(0xFFFFF8FA),
                        Color(0xFFFFF1F6),
                        Color(0xFFFFF8FA)
                    )
                )
            )
            .calendarTexture(accent = CalendarPink)
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            item {

                Spacer(modifier = Modifier.height(8.dp))

                CalendarHeader(
                    flowerScale = flowerScale
                )
            }

            item {

                PeriodCalendar(
                    periodDates = periodDates,
                    startDates = startDates,
                    endDates = endDates,
                    modifier = Modifier.fillMaxWidth(),
                    onDateSelected = { date ->
                        selectedDate = date
                    },
                    predictedDates = predictedDates
                )
            }

            item {
                CalendarLegend()
            }

            item {

                PredictionCard(
                    predictedStartDate = predictedStartDate,
                    cycleLength = selectedCycleLength
                )
            }

            item {

                SectionHeading(
                    title = "Period History",
                    subtitle = "Your previously recorded cycles"
                )
            }

            if (periods.isEmpty()) {

                item {
                    EmptyHistoryCard()
                }

            } else {

                items(
                    items = periods,
                    key = { it.id }
                ) { period ->

                    PeriodHistoryCard(
                        period = period,
                        onEdit = {
                            selectedDate =
                                parseCalendarDate(period.startDate)
                        },
                        onDelete = {
                            periodViewModel.deletePeriod(period)
                        }
                    )
                }
            }

            item {

                DisclaimerCard()

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Period editing dialog
    val dateForEditing = selectedDate

    if (dateForEditing != null) {

        val selectedPeriod = periods.firstOrNull { period ->
            period.startDate == dateForEditing.toString()
        }

        if (selectedPeriod != null) {

            PeriodDetailsEditor(
                period = selectedPeriod,
                onDismiss = {
                    selectedDate = null
                },
                onSaveEndDate = { endDate, onResult ->

                    periodViewModel.savePeriodEndDate(
                        periodId = selectedPeriod.id,
                        endDate = endDate,
                        onResult = onResult
                    )
                },
                onSaveDetails = { symptoms, notes, onResult ->

                    periodViewModel.updatePeriodDetails(
                        periodId = selectedPeriod.id,
                        symptoms = symptoms,
                        notes = notes,
                        onResult = onResult
                    )
                }
            )

        } else {

            AlertDialog(
                onDismissRequest = {
                    selectedDate = null
                },
                title = {
                    Text("No period recorded")
                },
                text = {
                    Text(
                        "This date does not have a saved period record. " +
                                "You can add a period from the Home screen."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedDate = null
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

// ----------------------------------------------------
// HEADER
// ----------------------------------------------------

@Composable
private fun CalendarHeader(
    flowerScale: Float
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = CalendarPink.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF9F315A),
                            Color(0xFFBF4D76),
                            Color(0xFFE18EAB),
                            Color(0xFFEBA7BE)
                        )
                    )
                )
                .calendarTexture(accent = Color.White)
                .padding(22.dp)
        ) {

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = "YOUR PERSONAL",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = "Cycle Calendar",
                            color = Color.White,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "🌸",
                        fontSize = 43.sp,
                        modifier = Modifier.graphicsLayer {
                            scaleX = flowerScale
                            scaleY = flowerScale
                        }
                    )
                }

                Text(
                    text = "Every cycle tells a story. Keep track of yours.",
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(5.dp))

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(
                            horizontal = 13.dp,
                            vertical = 7.dp
                        )
                ) {

                    Text(
                        text = "♡  Your health, your understanding",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// SECTION HEADING
// ----------------------------------------------------

@Composable
private fun SectionHeading(
    title: String,
    subtitle: String
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Text(
            text = title,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = CalendarDarkPink
        )

        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = CalendarMuted
        )
    }
}

// ----------------------------------------------------
// CALENDAR LEGEND
// ----------------------------------------------------

@Composable
private fun CalendarLegend() {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = CalendarPink.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .calendarTexture(accent = CalendarPink)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {

            Text(
                text = "Calendar Guide",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CalendarText
            )

            LegendRow(
                color = CalendarPink,
                label = "Period start date"
            )

            LegendRow(
                color = CalendarLightPink,
                label = "Recorded period days"
            )

            LegendRow(
                color = CalendarPurple,
                label = "Period end date"
            )

            LegendRow(
                color = Color(0xFFE9E0FA),
                label = "Predicted period days"
            )
        }
    }
}

@Composable
private fun LegendRow(
    color: Color,
    label: String
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Box(
            modifier = Modifier
                .size(17.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(color)
                .border(
                    width = 1.dp,
                    color = CalendarMuted.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                )
        )

        Text(
            text = label,
            fontSize = 13.sp,
            color = CalendarText
        )
    }
}

// ----------------------------------------------------
// PREDICTION CARD
// ----------------------------------------------------

@Composable
private fun PredictionCard(
    predictedStartDate: LocalDate?,
    cycleLength: Int
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 7.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = CalendarPurple.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF0E8FC),
                            Color(0xFFF9F2FF),
                            Color(0xFFFFF5FA)
                        )
                    )
                )
                .calendarTexture(accent = CalendarPurple)
                .padding(20.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "🌷",
                        fontSize = 23.sp
                    )
                }

                Column {

                    Text(
                        text = "Next Period Prediction",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CalendarPurple
                    )

                    Text(
                        text = "Your upcoming cycle estimate",
                        fontSize = 11.sp,
                        color = CalendarMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (predictedStartDate == null) {

                Text(
                    text = "Your prediction is waiting 💗",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CalendarText
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Record a period start date to see your next prediction.",
                    fontSize = 13.sp,
                    color = CalendarMuted
                )

            } else {

                Text(
                    text = predictedStartDate.format(calendarDateFormatter),
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = CalendarPurple
                )

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = "Based on your selected $cycleLength-day cycle.",
                    fontSize = 13.sp,
                    color = CalendarText
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.75f))
                        .padding(10.dp)
                ) {

                    Text(
                        text = "♡ Predictions are estimates. Actual period dates may vary.",
                        fontSize = 11.sp,
                        color = CalendarMuted
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// EMPTY HISTORY CARD
// ----------------------------------------------------

@Composable
private fun EmptyHistoryCard() {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .calendarTexture(accent = CalendarPink)
                .padding(25.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "🌸",
                fontSize = 42.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Your story starts here",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = CalendarDarkPink
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = "No periods recorded yet. Add a period start date " +
                        "on the Home screen to begin tracking your cycle.",
                fontSize = 13.sp,
                color = CalendarMuted
            )
        }
    }
}

// ----------------------------------------------------
// PERIOD HISTORY CARD
// ----------------------------------------------------

@Composable
private fun PeriodHistoryCard(
    period: PeriodEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    var showDeleteConfirmation by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = CalendarPink.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Column(
            modifier = Modifier
                .padding(17.dp)
                .calendarTexture(accent = CalendarPink)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CalendarLightPink),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "🌸",
                        fontSize = 22.sp
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Period Record",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CalendarDarkPink
                    )

                    Text(
                        text = parseCalendarDate(period.startDate)
                            ?.format(calendarDateFormatter)
                            ?: period.startDate,
                        fontSize = 12.sp,
                        color = CalendarMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFFFF0F5))
                        .padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        )
                ) {

                    Text(
                        text = "Saved",
                        color = CalendarPink,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HistoryInfoRow(
                label = "Start date",
                value = parseCalendarDate(period.startDate)
                    ?.format(calendarDateFormatter)
                    ?: period.startDate
            )

            Spacer(modifier = Modifier.height(9.dp))

            HistoryInfoRow(
                label = "End date",
                value = if (period.endDate.isNullOrBlank()) {
                    "Not recorded"
                } else {
                    parseCalendarDate(period.endDate)
                        ?.format(calendarDateFormatter)
                        ?: period.endDate
                }
            )

            if (period.symptoms.isNotBlank()) {

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Symptoms",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CalendarDarkPink
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = period.symptoms,
                    fontSize = 13.sp,
                    color = CalendarText
                )
            }

            if (period.notes.isNotBlank()) {

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Personal notes",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CalendarDarkPink
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = period.notes,
                    fontSize = 13.sp,
                    color = CalendarText
                )
            }

            Spacer(modifier = Modifier.height(17.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CalendarPink
                    )
                ) {

                    Text(
                        text = "✎  Edit",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        showDeleteConfirmation = true
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CalendarPink
                    )
                ) {

                    Text(
                        text = "Delete",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    if (showDeleteConfirmation) {

        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
            },
            title = {

                Text(
                    text = "Delete period record?",
                    color = CalendarDarkPink,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {

                Text(
                    "This will permanently delete this period record " +
                            "and its saved symptoms and notes."
                )
            },
            confirmButton = {

                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    }
                ) {

                    Text(
                        "Delete",
                        color = CalendarPink
                    )
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }
}

// ----------------------------------------------------
// HISTORY INFORMATION ROW
// ----------------------------------------------------

@Composable
private fun HistoryInfoRow(
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
            color = CalendarMuted
        )

        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = CalendarText
        )
    }
}

// ----------------------------------------------------
// DISCLAIMER
// ----------------------------------------------------

@Composable
private fun DisclaimerCard() {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFEDF3))
            .calendarTexture(accent = CalendarPink)
            .padding(14.dp)
    ) {

        Text(
            text = "♡ Cycle predictions are estimates, not guarantees. " +
                    "Cycle length and period dates can naturally vary.",
            fontSize = 11.sp,
            color = CalendarDarkPink
        )
    }
}

// ----------------------------------------------------
// PERIOD DETAILS EDITOR
// ----------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodDetailsEditor(
    period: PeriodEntity,
    onDismiss: () -> Unit,
    onSaveEndDate: (
        endDate: String,
        onResult: (Boolean) -> Unit
    ) -> Unit,
    onSaveDetails: (
        symptoms: String,
        notes: String,
        onResult: (Boolean) -> Unit
    ) -> Unit
) {

    var symptoms by remember(period.id) {
        mutableStateOf(period.symptoms)
    }

    var notes by remember(period.id) {
        mutableStateOf(period.notes)
    }

    var endDate by remember(period.id) {
        mutableStateOf(period.endDate ?: "")
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    var isSaving by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) onDismiss()
        },
        title = {

            Text(
                text = "Edit Period Details",
                color = CalendarDarkPink,
                fontWeight = FontWeight.Bold
            )
        },
        text = {

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                item {

                    Text(
                        text = "Start date: ${
                            parseCalendarDate(period.startDate)
                                ?.format(calendarDateFormatter)
                                ?: period.startDate
                        }",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                item {

                    Text(
                        text = "Period end date",
                        style = MaterialTheme.typography.titleSmall,
                        color = CalendarDarkPink
                    )

                    OutlinedButton(
                        onClick = {
                            showDatePicker = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(13.dp)
                    ) {

                        Text(
                            text = if (endDate.isBlank()) {
                                "Select end date"
                            } else {
                                parseCalendarDate(endDate)
                                    ?.format(calendarDateFormatter)
                                    ?: endDate
                            },
                            color = CalendarPink
                        )
                    }
                }

                item {

                    OutlinedTextField(
                        value = symptoms,
                        onValueChange = {
                            symptoms = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Symptoms")
                        },
                        placeholder = {
                            Text("e.g. cramps, headache, tiredness")
                        },
                        minLines = 2,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                item {

                    OutlinedTextField(
                        value = notes,
                        onValueChange = {
                            notes = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Personal notes")
                        },
                        placeholder = {
                            Text("How are you feeling?")
                        },
                        minLines = 3,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                if (errorMessage != null) {

                    item {

                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {

            TextButton(
                enabled = !isSaving,
                onClick = {

                    isSaving = true
                    errorMessage = null

                    val originalEndDate = period.endDate

                    fun saveDetailsAfterEndDate() {

                        onSaveDetails(symptoms, notes) { detailsSaved ->

                            isSaving = false

                            if (detailsSaved) {
                                onDismiss()
                            } else {
                                errorMessage =
                                    "Could not save the period details."
                            }
                        }
                    }

                    if (endDate.isBlank()) {

                        if (originalEndDate == null) {

                            saveDetailsAfterEndDate()

                        } else {

                            onSaveEndDate("") { endSaved ->

                                if (endSaved) {

                                    saveDetailsAfterEndDate()

                                } else {

                                    isSaving = false
                                    errorMessage =
                                        "Could not update the end date."
                                }
                            }
                        }

                    } else if (endDate == originalEndDate) {

                        saveDetailsAfterEndDate()

                    } else {

                        onSaveEndDate(endDate) { endSaved ->

                            if (endSaved) {

                                saveDetailsAfterEndDate()

                            } else {

                                isSaving = false
                                errorMessage =
                                    "Choose a valid end date on or after " +
                                            "the start date, and not in the future."
                            }
                        }
                    }
                }
            ) {

                Text(
                    text = if (isSaving) "Saving..." else "Save",
                    color = CalendarPink
                )
            }
        },
        dismissButton = {

            TextButton(
                enabled = !isSaving,
                onClick = onDismiss
            ) {

                Text("Cancel")
            }
        }
    )

    // Date picker
    if (showDatePicker) {

        val initialMillis = remember(endDate, period.startDate) {

            val selected = parseCalendarDate(endDate)
                ?: parseCalendarDate(period.startDate)
                ?: LocalDate.now()

            selected.atStartOfDay(ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli()
        }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis
        )

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {

                TextButton(
                    onClick = {

                        val millis =
                            datePickerState.selectedDateMillis

                        if (millis != null) {

                            endDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                                .toString()
                        }

                        showDatePicker = false
                    }
                ) {

                    Text(
                        "Select",
                        color = CalendarPink
                    )
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {

                    Text("Cancel")
                }
            }
        ) {

            DatePicker(
                state = datePickerState
            )
        }
    }
}

// ----------------------------------------------------
// DATE PARSER
// ----------------------------------------------------

private fun parseCalendarDate(
    value: String?
): LocalDate? {

    if (value.isNullOrBlank()) {
        return null
    }

    return runCatching {
        LocalDate.parse(value)
    }.getOrNull()
}