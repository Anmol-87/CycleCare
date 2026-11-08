package com.anmol.cyclecare.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anmol.cyclecare.data.local.PeriodEntity
import com.anmol.cyclecare.ui.components.PeriodCalendar
import com.anmol.cyclecare.viewmodel.PeriodViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

private const val DEFAULT_PREDICTED_PERIOD_DAYS = 5

private val CalendarPink = Color(0xFFB83D68)
private val CalendarDarkPink = Color(0xFF963052)
private val CalendarLightPink = Color(0xFFFCE8EF)
private val CalendarPurple = Color(0xFF8660B5)
private val CalendarBackground = Color(0xFFFFF8FA)
private val CalendarText = Color(0xFF49323D)
private val CalendarMuted = Color(0xFF8D7882)
private val CalendarBorder = Color(0xFFF4D5E0)

private val calendarDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy")

// ----------------------------------------------------
// SHARED CARD STYLE
// One corner radius and one soft pink shadow, the
// same as the Home screen.
// ----------------------------------------------------

private val CalendarCardShape = RoundedCornerShape(28.dp)

private fun Modifier.calendarSoftShadow(
    shape: Shape = CalendarCardShape,
    elevation: Dp = 14.dp
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    ambientColor = CalendarPink.copy(alpha = 0.15f),
    spotColor = CalendarPink.copy(alpha = 0.22f)
)

// ----------------------------------------------------
// BACKGROUND TEXTURE
// (used only on the screen background and the header)
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
// STAGGERED ENTRANCE (fade-in + slide-up)
// State is saved per index, so items that scroll away
// and come back do not replay the animation.
// ----------------------------------------------------

@Composable
private fun CalendarEntrance(
    index: Int,
    content: @Composable () -> Unit
) {

    var visible by rememberSaveable(index) {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {

        if (!visible) {
            delay(index * 90L)
            visible = true
        }
    }

    val entranceAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "calendar_entrance_alpha"
    )

    val entranceOffset by animateFloatAsState(
        targetValue = if (visible) 0f else 36f,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "calendar_entrance_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = entranceAlpha
                translationY = entranceOffset.dp.toPx()
            }
    ) {
        content()
    }
}

// ----------------------------------------------------
// ANIMATED COUNTER
// ----------------------------------------------------

@Composable
private fun rememberCalendarCountUp(
    target: Int,
    durationMillis: Int = 900
): Int {

    val animatedValue = remember {
        Animatable(0f)
    }

    LaunchedEffect(target) {
        animatedValue.animateTo(
            targetValue = target.toFloat(),
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            )
        )
    }

    return animatedValue.value.roundToInt()
}

// ----------------------------------------------------
// DAYS-UNTIL-PERIOD RING
// ----------------------------------------------------

@Composable
private fun CalendarDaysRing(
    progress: Float,
    days: Int,
    modifier: Modifier = Modifier
) {

    val animatedProgress = remember {
        Animatable(0f)
    }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(
                durationMillis = 1100,
                easing = FastOutSlowInEasing
            )
        )
    }

    Box(
        modifier = modifier.size(88.dp),
        contentAlignment = Alignment.Center
    ) {

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {

            val strokeWidth = 6.dp.toPx()
            val inset = strokeWidth / 2f

            val arcSize = Size(
                width = size.width - strokeWidth,
                height = size.height - strokeWidth
            )

            // Track
            drawArc(
                color = Color.White.copy(alpha = 0.22f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )

            // Progress
            drawArc(
                color = Color.White,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress.value,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "$days",
                fontSize = 25.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Text(
                text = "DAYS",
                fontSize = 9.sp,
                letterSpacing = 1.4.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
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

                CalendarEntrance(index = 0) {

                    CalendarHeader(
                        flowerScale = flowerScale
                    )
                }
            }

            item {

                CalendarEntrance(index = 1) {

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
            }

            item {

                CalendarEntrance(index = 2) {
                    CalendarLegend()
                }
            }

            item {

                CalendarEntrance(index = 3) {

                    PredictionCard(
                        predictedStartDate = predictedStartDate,
                        cycleLength = selectedCycleLength
                    )
                }
            }

            item {

                CalendarEntrance(index = 4) {

                    SectionHeading(
                        title = "Period History",
                        subtitle = "Your previously recorded cycles"
                    )
                }
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
                containerColor = Color.White,
                shape = CalendarCardShape,
                title = {
                    Text(
                        text = "No period recorded",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        color = CalendarDarkPink
                    )
                },
                text = {
                    Text(
                        text = "This date does not have a saved period record. " +
                                "You can add a period from the Home screen.",
                        color = CalendarText
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedDate = null
                        }
                    ) {
                        Text(
                            text = "OK",
                            color = CalendarPink,
                            fontWeight = FontWeight.SemiBold
                        )
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .calendarSoftShadow()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFFFF4F8)
                    )
                ),
                CalendarCardShape
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        CalendarPink.copy(alpha = 0.30f),
                        CalendarBorder.copy(alpha = 0.60f),
                        CalendarPink.copy(alpha = 0.18f)
                    )
                ),
                shape = CalendarCardShape
            )
            .calendarTexture(accent = CalendarPink)
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
                        color = CalendarPink,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = buildAnnotatedString {

                            withStyle(SpanStyle(color = CalendarText)) {
                                append("Cycle ")
                            }

                            withStyle(SpanStyle(color = CalendarPink)) {
                                append("Calendar")
                            }
                        },
                        fontSize = 27.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.4.sp
                    )
                }

                // Animated flower inside a white seal

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White, CircleShape)
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    CalendarPink.copy(alpha = 0.45f),
                                    CalendarBorder
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "🌸",
                        fontSize = 34.sp,
                        modifier = Modifier.graphicsLayer {
                            scaleX = flowerScale
                            scaleY = flowerScale
                        }
                    )
                }
            }

            Text(
                text = "Every cycle tells a story. Keep track of yours.",
                color = CalendarMuted,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CalendarLightPink)
                    .border(
                        width = 1.dp,
                        color = CalendarBorder,
                        shape = CircleShape
                    )
                    .padding(
                        horizontal = 13.dp,
                        vertical = 7.dp
                    )
            ) {

                Text(
                    text = "Your health, your understanding",
                    color = CalendarPink,
                    fontSize = 11.sp,
                    letterSpacing = 0.3.sp,
                    fontWeight = FontWeight.Medium
                )
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
            fontSize = 22.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            color = CalendarText
        )

        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = CalendarMuted
        )
    }
}

// ----------------------------------------------------
// SMALL HAIRLINE DIVIDER
// ----------------------------------------------------

@Composable
private fun SoftDivider() {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(CalendarBorder.copy(alpha = 0.7f))
    )
}

// ----------------------------------------------------
// CALENDAR LEGEND
// ----------------------------------------------------

@Composable
private fun CalendarLegend() {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .calendarSoftShadow(elevation = 10.dp),
        shape = CalendarCardShape,
        border = BorderStroke(1.dp, CalendarBorder),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {

            Text(
                text = "Calendar Guide",
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
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
                    color = CalendarMuted.copy(alpha = 0.18f),
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
// Pink to match the Home screen, with a count-up
// and a days-until-period ring.
// ----------------------------------------------------

@Composable
private fun PredictionCard(
    predictedStartDate: LocalDate?,
    cycleLength: Int
) {

    val daysUntil: Int? = predictedStartDate?.let {
        ChronoUnit.DAYS.between(LocalDate.now(), it).toInt()
    }

    val animatedDays = rememberCalendarCountUp(
        target = (daysUntil ?: 0).coerceAtLeast(0)
    )

    // 0 = cycle just started, 1 = period due today
    val ringProgress = if (daysUntil != null && cycleLength > 0) {
        (1f - daysUntil.toFloat() / cycleLength.toFloat())
            .coerceIn(0f, 1f)
    } else {
        0f
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .calendarSoftShadow(),
        shape = CalendarCardShape,
        border = BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.35f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = CalendarPink
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFC94E78),
                            Color(0xFFB83D68),
                            Color(0xFFA93461)
                        )
                    )
                )
                .padding(22.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {

                    Text(
                        text = "Next Period Prediction",
                        fontSize = 17.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Text(
                        text = "Your upcoming cycle estimate",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (predictedStartDate == null || daysUntil == null) {

                Text(
                    text = "Your prediction is waiting",
                    fontSize = 23.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Record a period start date to see your next prediction.",
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color.White.copy(alpha = 0.88f)
                )

            } else {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = predictedStartDate.format(
                                calendarDateFormatter
                            ),
                            fontSize = 28.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Based on your selected $cycleLength-day cycle.",
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = when {

                                daysUntil > 1 ->
                                    "About $animatedDays days to go"

                                daysUntil == 1 ->
                                    "Estimated to start tomorrow"

                                daysUntil == 0 ->
                                    "Predicted to start today"

                                else ->
                                    "The predicted date has passed."
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    CalendarDaysRing(
                        progress = ringProgress,
                        days = animatedDays
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                        .padding(10.dp)
                ) {

                    Text(
                        text = "Predictions are estimates. Actual period dates may vary.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f)
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
            .calendarSoftShadow(elevation = 10.dp),
        shape = CalendarCardShape,
        border = BorderStroke(1.dp, CalendarBorder),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(CalendarLightPink),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = CalendarPink,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Your story starts here",
                fontSize = 19.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                color = CalendarText
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = "No periods recorded yet. Add a period start date " +
                        "on the Home screen to begin tracking your cycle.",
                fontSize = 13.sp,
                lineHeight = 20.sp,
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
            .calendarSoftShadow(elevation = 10.dp),
        shape = CalendarCardShape,
        border = BorderStroke(1.dp, CalendarBorder),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CalendarLightPink),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = CalendarPink,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Period Record",
                        fontSize = 17.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        color = CalendarText
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
                        .border(
                            width = 1.dp,
                            color = CalendarBorder,
                            shape = CircleShape
                        )
                        .padding(
                            horizontal = 11.dp,
                            vertical = 5.dp
                        )
                ) {

                    Text(
                        text = "SAVED",
                        color = CalendarPink,
                        fontSize = 9.sp,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SoftDivider()

            Spacer(modifier = Modifier.height(12.dp))

            HistoryInfoRow(
                label = "Start date",
                value = parseCalendarDate(period.startDate)
                    ?.format(calendarDateFormatter)
                    ?: period.startDate
            )

            Spacer(modifier = Modifier.height(12.dp))

            SoftDivider()

            Spacer(modifier = Modifier.height(12.dp))

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

                SoftDivider()

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Symptoms",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CalendarPink
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = period.symptoms,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = CalendarText
                )
            }

            if (period.notes.isNotBlank()) {

                Spacer(modifier = Modifier.height(12.dp))

                SoftDivider()

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Personal notes",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CalendarPink
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = period.notes,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = CalendarText
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                // Edit is the main action (filled)

                Button(
                    onClick = onEdit,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CalendarPink,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {

                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Edit",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Delete is secondary (soft outline)

                OutlinedButton(
                    onClick = {
                        showDeleteConfirmation = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        CalendarPink.copy(alpha = 0.35f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CalendarPink
                    )
                ) {

                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

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
            containerColor = Color.White,
            shape = CalendarCardShape,
            title = {

                Text(
                    text = "Delete period record?",
                    color = CalendarDarkPink,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {

                Text(
                    text = "This will permanently delete this period record " +
                            "and its saved symptoms and notes.",
                    color = CalendarText
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
                        text = "Delete",
                        color = CalendarPink,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                    }
                ) {

                    Text(
                        text = "Cancel",
                        color = CalendarMuted
                    )
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
            fontSize = 13.sp,
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFFEDF3))
            .border(
                width = 1.dp,
                color = CalendarBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = CalendarPink,
            modifier = Modifier.size(18.dp)
        )

        Text(
            text = "Cycle predictions are estimates, not guarantees. " +
                    "Cycle length and period dates can naturally vary.",
            fontSize = 11.sp,
            lineHeight = 16.sp,
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

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CalendarPink,
        focusedLabelColor = CalendarPink,
        cursorColor = CalendarPink,
        unfocusedBorderColor = CalendarBorder
    )

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) onDismiss()
        },
        containerColor = Color.White,
        shape = CalendarCardShape,
        title = {

            Text(
                text = "Edit Period Details",
                color = CalendarDarkPink,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = CalendarText
                    )
                }

                item {

                    Text(
                        text = "Period end date",
                        style = MaterialTheme.typography.titleSmall,
                        color = CalendarDarkPink
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = {
                            showDatePicker = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CalendarBorder)
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
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors
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
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors
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
                    color = CalendarPink,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {

            TextButton(
                enabled = !isSaving,
                onClick = onDismiss
            ) {

                Text(
                    text = "Cancel",
                    color = CalendarMuted
                )
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
                        text = "Select",
                        color = CalendarPink,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {

                    Text(
                        text = "Cancel",
                        color = CalendarMuted
                    )
                }
            }
        ) {

            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White,
                    selectedDayContainerColor = CalendarPink,
                    todayDateBorderColor = CalendarPink,
                    todayContentColor = CalendarPink,
                    selectedYearContainerColor = CalendarPink
                )
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