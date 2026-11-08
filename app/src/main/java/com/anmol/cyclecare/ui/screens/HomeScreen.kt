package com.anmol.cyclecare.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anmol.cyclecare.viewmodel.PeriodViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.delay


// --------------------------------------------------
// COLOUR PALETTE
// --------------------------------------------------

private val HomeBackground = Color(0xFFFFF8FA)
private val HomePink = Color(0xFFB83D68)
private val HomeLightPink = Color(0xFFFCE8EF)
private val HomeDarkText = Color(0xFF30252B)
private val HomeSecondaryText = Color(0xFF796B73)
private val HomeWhite = Color.White
private val CardBorder = Color(0xFFF4D5E0)


// --------------------------------------------------
// SHARED CARD STYLE
// One corner radius and one soft pink shadow
// used by the header and every card.
// --------------------------------------------------

private val CardShape = RoundedCornerShape(28.dp)

private fun Modifier.softShadow(
    shape: Shape = CardShape,
    elevation: Float = 14f
): Modifier = this.shadow(
    elevation = elevation.dp,
    shape = shape,
    ambientColor = HomePink.copy(alpha = 0.15f),
    spotColor = HomePink.copy(alpha = 0.22f)
)


// --------------------------------------------------
// ANIMATED BACKGROUND TEXTURE
// (used only on the screen background and the
// pink prediction card)
// --------------------------------------------------

private fun Modifier.cycleTexture(
    progress: Float,
    accent: Color = HomePink
): Modifier = this.drawBehind {

    val width = size.width
    val height = size.height

    val movement = progress * 2f * PI.toFloat()

    // Large floating circle - top right

    drawCircle(
        color = accent.copy(alpha = 0.045f),
        radius = size.minDimension * 0.45f,
        center = Offset(
            x = width * 0.96f,
            y = height * (0.08f + sin(movement) * 0.025f)
        )
    )

    // Large floating circle - bottom left

    drawCircle(
        color = accent.copy(alpha = 0.035f),
        radius = size.minDimension * 0.25f,
        center = Offset(
            x = width * (0.04f + sin(movement + 1f) * 0.025f),
            y = height * 0.92f
        )
    )

    // Floating decorative particles

    for (i in 0..9) {

        val phase = movement + i * 0.75f

        val xPosition = width * (
                0.10f + (i * 0.085f)
                )

        val yPosition = height * (
                0.12f + (i % 4) * 0.22f
                )

        val floatingY = sin(phase) * 13.dp.toPx()

        val floatingX = sin(phase + 1.5f) * 5.dp.toPx()

        drawCircle(
            color = accent.copy(
                alpha = 0.07f + (sin(phase) + 1f) * 0.025f
            ),
            radius = (2f + (i % 3)).dp.toPx(),
            center = Offset(
                xPosition + floatingX,
                yPosition + floatingY
            )
        )
    }

    // Bottom decorative dots

    for (i in 0..4) {

        val phase = movement + i

        drawCircle(
            color = accent.copy(alpha = 0.06f),
            radius = 1.8.dp.toPx(),
            center = Offset(
                width * (0.12f + i * 0.05f),
                height * 0.85f + sin(phase) * 8.dp.toPx()
            )
        )
    }
}


// --------------------------------------------------
// ANIMATED TULIP LOGO 🌷
// --------------------------------------------------

@Composable
private fun HeartbeatLogo() {

    val transition = rememberInfiniteTransition(
        label = "tulip_animation"
    )

    val flowerScale by transition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tulip_scale"
    )

    val flowerRotation by transition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tulip_rotation"
    )

    Box(
        modifier = Modifier
            .size(58.dp)
            .graphicsLayer {
                scaleX = flowerScale
                scaleY = flowerScale
                rotationZ = flowerRotation
            },
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = "🌷",
            fontSize = 43.sp
        )
    }
}


// --------------------------------------------------
// PREMIUM STAGGERED ENTRANCE ANIMATION
// Fade-in + slide-up, delayed by index so cards
// appear one after another. Uses graphicsLayer so
// the layout keeps its space (no jumping content).
// --------------------------------------------------

@Composable
private fun StaggeredEntrance(
    index: Int,
    content: @Composable () -> Unit
) {
    var visible by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        delay(index * 100L)
        visible = true
    }

    val entranceAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "entrance_alpha"
    )

    val entranceOffset by animateFloatAsState(
        targetValue = if (visible) 0f else 36f,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "entrance_offset"
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


// --------------------------------------------------
// ANIMATED COUNTER
// Counts smoothly from 0 up to the target on first
// display, and animates between values afterwards.
// --------------------------------------------------

@Composable
private fun rememberCountUp(
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


// --------------------------------------------------
// DAYS-UNTIL-PERIOD RING
// Thin circular progress ring. It fills as the
// next period gets closer.
// --------------------------------------------------

@Composable
private fun DaysRing(
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
        modifier = modifier.size(92.dp),
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
                fontSize = 26.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                color = HomeWhite
            )

            Text(
                text = "DAYS",
                fontSize = 9.sp,
                letterSpacing = 1.4.sp,
                color = HomeWhite.copy(alpha = 0.8f)
            )
        }
    }
}


// --------------------------------------------------
// HOME SCREEN
// --------------------------------------------------

@Composable
fun HomeScreen(
    periodViewModel: PeriodViewModel,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    val haptic = LocalHapticFeedback.current

    val periods by periodViewModel.periods.collectAsState()

    val averageCycleLength by
    periodViewModel.averageCycleLength.collectAsState()

    val selectedCycleLength by
    periodViewModel.selectedCycleLength.collectAsState()

    val nextPredictedPeriodDate by
    periodViewModel.nextPredictedPeriodDate.collectAsState()


    // --------------------------------------------------
    // PERIOD DATA
    // --------------------------------------------------

    val latestPeriod = periods.firstOrNull()

    val latestStartDate = latestPeriod?.startDate?.let { date ->

        runCatching {
            LocalDate.parse(date)
        }.getOrNull()
    }

    val formattedDate = latestStartDate?.format(
        DateTimeFormatter.ofPattern(
            "dd MMM yyyy",
            Locale.getDefault()
        )
    )

    val formattedPrediction = nextPredictedPeriodDate?.format(
        DateTimeFormatter.ofPattern(
            "dd MMM yyyy",
            Locale.getDefault()
        )
    )

    val daysUntilNextPeriod = nextPredictedPeriodDate?.let {

        ChronoUnit.DAYS.between(
            LocalDate.now(),
            it
        ).toInt()
    }

    // How close the next period is: 0 = just started a cycle, 1 = due today
    val ringProgress = if (
        daysUntilNextPeriod != null && selectedCycleLength > 0
    ) {
        (1f - daysUntilNextPeriod.toFloat() / selectedCycleLength.toFloat())
            .coerceIn(0f, 1f)
    } else {
        0f
    }


    // --------------------------------------------------
    // ANIMATED STATISTICS (COUNT-UP NUMBERS)
    // --------------------------------------------------

    val animatedSelectedLength = rememberCountUp(
        target = selectedCycleLength,
        durationMillis = 250
    )

    val animatedPeriodCount = rememberCountUp(
        target = periods.size
    )

    val animatedDaysCount = rememberCountUp(
        target = (daysUntilNextPeriod ?: 0).coerceAtLeast(0)
    )


    // --------------------------------------------------
    // ANIMATED BACKGROUND
    // --------------------------------------------------

    val backgroundTransition = rememberInfiniteTransition(
        label = "background_animation"
    )

    val backgroundProgress by backgroundTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 12000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "background_progress"
    )


    // --------------------------------------------------
    // HEART ANIMATION
    // --------------------------------------------------

    val infiniteTransition = rememberInfiniteTransition(
        label = "heart_animation"
    )

    val heartScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heart_scale"
    )


    // --------------------------------------------------
    // SCREEN ENTRANCE ANIMATION
    // --------------------------------------------------

    AnimatedVisibility(
        visible = true,

        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF5F8),
                        Color(0xFFFFF8FA),
                        Color(0xFFFFF1F5)
                    )
                )
            )
            .cycleTexture(
                progress = backgroundProgress
            ),

        enter = fadeIn(
            animationSpec = tween(500)
        ) + slideInVertically(
            initialOffsetY = { it / 18 },
            animationSpec = tween(500)
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(
                    top = 24.dp,
                    bottom = 30.dp
                )
        ) {


            // --------------------------------------------------
            // PREMIUM CYCLECARE HEADER
            // --------------------------------------------------

            StaggeredEntrance(index = 0) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .softShadow()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFFFFF),
                                    Color(0xFFFFF4F8)
                                )
                            ),
                            CardShape
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    HomePink.copy(alpha = 0.30f),
                                    CardBorder.copy(alpha = 0.60f),
                                    HomePink.copy(alpha = 0.18f)
                                )
                            ),
                            shape = CardShape
                        )
                        .padding(
                            horizontal = 16.dp,
                            vertical = 14.dp
                        ),

                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Logo badge: animated tulip inside a white seal with a thin ring

                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .background(Color.White, CircleShape)
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        HomePink.copy(alpha = 0.45f),
                                        CardBorder
                                    )
                                ),
                                shape = CircleShape
                            ),

                        contentAlignment = Alignment.Center
                    ) {

                        HeartbeatLogo()
                    }

                    Spacer(Modifier.width(14.dp))

                    // App name and tagline

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = buildAnnotatedString {

                                withStyle(SpanStyle(color = HomeDarkText)) {
                                    append("Cycle")
                                }

                                withStyle(SpanStyle(color = HomePink)) {
                                    append("Care")
                                }
                            },
                            fontSize = 25.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.6.sp
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "Your health, your understanding",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 0.3.sp,
                            color = HomeSecondaryText
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    // Decorative flower badge

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.White, CircleShape)
                            .border(
                                width = 1.dp,
                                color = CardBorder,
                                shape = CircleShape
                            ),

                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "🌸",
                            fontSize = 20.sp
                        )
                    }
                }
            }


            Spacer(Modifier.height(34.dp))


            // --------------------------------------------------
            // INTRODUCTION (serif headline)
            // --------------------------------------------------

            StaggeredEntrance(index = 1) {

                Column {

                    Text(
                        text = "Your cycle,",
                        fontSize = 34.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        color = HomeDarkText
                    )

                    Text(
                        text = "your story.",
                        fontSize = 34.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        color = HomePink
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "Take a moment for yourself. Every cycle is different, " +
                                "and understanding yours starts here.",
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = HomeSecondaryText
                    )
                }
            }


            Spacer(Modifier.height(28.dp))


            // --------------------------------------------------
            // NEXT PERIOD PREDICTION CARD
            // (keeps the animated texture; adds progress ring)
            // --------------------------------------------------

            StaggeredEntrance(index = 2) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .softShadow(),

                    shape = CardShape,

                    border = BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.35f)
                    ),

                    colors = CardDefaults.cardColors(
                        containerColor = HomePink
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
                            .cycleTexture(
                                progress = backgroundProgress,
                                accent = Color.White
                            )
                            .padding(24.dp)
                    ) {

                        Text(
                            text = "YOUR NEXT PERIOD",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.3.sp,
                            color = HomeWhite
                        )

                        Spacer(Modifier.height(16.dp))

                        if (
                            formattedPrediction != null &&
                            daysUntilNextPeriod != null
                        ) {

                            // Hero date: larger, lighter, serif

                            AnimatedContent(
                                targetState = formattedPrediction,

                                transitionSpec = {
                                    fadeIn(tween(350)) togetherWith
                                            fadeOut(tween(200))
                                },

                                label = "prediction_date"
                            ) { date ->

                                Text(
                                    text = date,
                                    fontSize = 34.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Normal,
                                    color = HomeWhite
                                )
                            }

                            Spacer(Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                AnimatedContent(
                                    targetState = daysUntilNextPeriod,

                                    modifier = Modifier.weight(1f),

                                    transitionSpec = {
                                        fadeIn(tween(350)) togetherWith
                                                fadeOut(tween(200))
                                    },

                                    label = "countdown"
                                ) { days ->

                                    Text(
                                        text = when {

                                            days > 1 ->
                                                "About $animatedDaysCount days to go"

                                            days == 1 ->
                                                "Estimated to start tomorrow"

                                            days == 0 ->
                                                "Your predicted start date is today"

                                            else ->
                                                "The predicted date has passed. Log your next period to update your estimate."
                                        },

                                        fontSize = 14.sp,
                                        lineHeight = 21.sp,
                                        color = HomeWhite
                                    )
                                }

                                Spacer(Modifier.width(16.dp))

                                DaysRing(
                                    progress = ringProgress,
                                    days = animatedDaysCount
                                )
                            }

                        } else {

                            Text(
                                text = "Your prediction starts here",
                                fontSize = 24.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Normal,
                                color = HomeWhite
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "Record your last period's start date to see an estimated next date.",
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                color = HomeWhite
                            )
                        }

                        Spacer(Modifier.height(18.dp))

                        Text(
                            text = "Predictions are estimates, not guarantees.",
                            fontSize = 12.sp,
                            color = HomeWhite.copy(alpha = 0.85f)
                        )
                    }
                }
            }


            Spacer(Modifier.height(22.dp))


            // --------------------------------------------------
            // CYCLE OVERVIEW CARD
            // --------------------------------------------------

            StaggeredEntrance(index = 3) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .softShadow(),

                    shape = CardShape,

                    border = BorderStroke(
                        1.dp,
                        CardBorder
                    ),

                    colors = CardDefaults.cardColors(
                        containerColor = HomeLightPink
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
                                        Color(0xFFFFF0F5),
                                        Color(0xFFFCE8EF),
                                        Color(0xFFFFF4F7)
                                    )
                                )
                            )
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
                                .size(62.dp)
                                .background(
                                    HomeWhite,
                                    CircleShape
                                ),

                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                text = "♡",
                                fontSize = 36.sp,
                                color = HomePink,

                                modifier = Modifier.graphicsLayer {
                                    scaleX = heartScale
                                    scaleY = heartScale
                                }
                            )
                        }

                        Spacer(Modifier.height(17.dp))

                        Text(
                            text = formattedDate?.let {
                                "Last period started $it"
                            } ?: "Let's get to know your cycle",

                            fontSize = 21.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.SemiBold,
                            color = HomeDarkText
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = if (latestPeriod == null) {

                                "Log the first day of your last period to begin building your personal history."

                            } else {

                                "Your recorded period is saved on this device. You can review your dates in the Calendar tab."
                            },

                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = HomeSecondaryText
                        )

                        Spacer(Modifier.height(22.dp))

                        // Polished button: gradient, soft shadow, light haptic tap

                        val buttonShape = RoundedCornerShape(16.dp)

                        Button(
                            onClick = {

                                haptic.performHapticFeedback(
                                    HapticFeedbackType.TextHandleMove
                                )

                                showPeriodStartPicker(
                                    context = context,
                                    periodViewModel = periodViewModel
                                )
                            },

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .shadow(
                                    elevation = 10.dp,
                                    shape = buttonShape,
                                    ambientColor = HomePink.copy(alpha = 0.20f),
                                    spotColor = HomePink.copy(alpha = 0.35f)
                                )
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFC94E78),
                                            Color(0xFFA93461)
                                        )
                                    ),
                                    buttonShape
                                ),

                            shape = buttonShape,

                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            ),

                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp
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
            }


            Spacer(Modifier.height(22.dp))


            // --------------------------------------------------
            // CYCLE LENGTH SETTINGS
            // --------------------------------------------------

            StaggeredEntrance(index = 4) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .softShadow(),

                    shape = CardShape,

                    border = BorderStroke(
                        1.dp,
                        CardBorder
                    ),

                    colors = CardDefaults.cardColors(
                        containerColor = HomeWhite
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
                                        Color(0xFFFFFEFF),
                                        Color(0xFFFFF5F8)
                                    )
                                )
                            )
                            .padding(22.dp)
                    ) {

                        Text(
                            text = "Cycle length settings",
                            fontSize = 20.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.SemiBold,
                            color = HomeDarkText
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Choose your usual cycle length. Your prediction updates automatically.",
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = HomeSecondaryText
                        )

                        Spacer(Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),

                            verticalAlignment = Alignment.CenterVertically,

                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Text(
                                text = "Selected length",
                                fontSize = 14.sp,
                                color = HomeSecondaryText
                            )

                            Text(
                                text = "$animatedSelectedLength days",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = HomePink
                            )
                        }

                        // Pink slider with hidden tick marks for a cleaner look

                        Slider(
                            value = selectedCycleLength.toFloat(),

                            onValueChange = { value ->

                                periodViewModel.setCycleLength(
                                    value.toInt()
                                )
                            },

                            valueRange = 15f..60f,
                            steps = 44,

                            colors = SliderDefaults.colors(
                                thumbColor = HomePink,
                                activeTrackColor = HomePink,
                                inactiveTrackColor = CardBorder,
                                activeTickColor = Color.Transparent,
                                inactiveTickColor = Color.Transparent
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),

                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Text(
                                text = "15 days",
                                fontSize = 12.sp,
                                color = HomeSecondaryText
                            )

                            Text(
                                text = "60 days",
                                fontSize = 12.sp,
                                color = HomeSecondaryText
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        Text(
                            text = "Automatic average",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HomeDarkText
                        )

                        Spacer(Modifier.height(6.dp))

                        AnimatedContent(
                            targetState = averageCycleLength,

                            transitionSpec = {
                                fadeIn(tween(300)) togetherWith
                                        fadeOut(tween(200))
                            },

                            label = "average_length"
                        ) { average ->

                            // Counts up to the calculated average
                            val animatedAverage = rememberCountUp(
                                target = average ?: 0
                            )

                            Text(
                                text = if (average == null) {

                                    "Record at least two period start dates to calculate your average."

                                } else {

                                    "Your recorded cycles average $animatedAverage days."
                                },

                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                color = HomeSecondaryText
                            )
                        }

                        if (averageCycleLength != null) {

                            Spacer(Modifier.height(14.dp))

                            Button(
                                onClick = {

                                    periodViewModel.setCycleLength(
                                        averageCycleLength!!
                                    )
                                },

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),

                                shape = RoundedCornerShape(14.dp),

                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HomeLightPink,
                                    contentColor = HomePink
                                ),

                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp
                                )
                            ) {

                                Text(
                                    text = "Use my calculated average",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }


            Spacer(Modifier.height(28.dp))


            // --------------------------------------------------
            // RECENT ACTIVITY
            // --------------------------------------------------

            StaggeredEntrance(index = 5) {

                Column {

                    Text(
                        text = "Your recent activity",
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        color = HomeDarkText
                    )

                    Spacer(Modifier.height(14.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .softShadow(),

                        shape = CardShape,

                        border = BorderStroke(
                            1.dp,
                            CardBorder
                        ),

                        colors = CardDefaults.cardColors(
                            containerColor = HomeWhite
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
                                            Color(0xFFFFFEFF),
                                            Color(0xFFFFF5F8)
                                        )
                                    )
                                )
                                .padding(22.dp),

                            verticalArrangement = Arrangement.spacedBy(9.dp)
                        ) {

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                // Outline icon instead of an emoji

                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            HomeLightPink,
                                            CircleShape
                                        ),

                                    contentAlignment = Alignment.Center
                                ) {

                                    Icon(
                                        imageVector = Icons.Outlined.CalendarMonth,
                                        contentDescription = null,
                                        tint = HomePink,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(Modifier.width(12.dp))

                                Text(
                                    text = "Period records",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HomeDarkText
                                )
                            }

                            Text(
                                text = when (periods.size) {

                                    0 -> "No periods recorded yet."

                                    1 -> "You've recorded 1 period."

                                    else -> "You've recorded $animatedPeriodCount periods."
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
                }
            }


            Spacer(Modifier.height(28.dp))


            // --------------------------------------------------
            // FOOTER
            // --------------------------------------------------

            StaggeredEntrance(index = 6) {

                Text(
                    text = "Be gentle with yourself.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    color = HomePink
                )
            }
        }
    }
}


// --------------------------------------------------
// PERIOD DATE PICKER
// --------------------------------------------------

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

    picker.datePicker.maxDate =
        Calendar.getInstance().timeInMillis

    picker.show()
}