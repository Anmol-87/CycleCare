package com.anmol.cyclecare.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anmol.cyclecare.ui.components.CycleInsights
import com.anmol.cyclecare.viewmodel.PeriodViewModel

private val InsightsBackground = Color(0xFFFFF8FA)
private val InsightsText = Color(0xFF30252B)
private val InsightsMutedText = Color(0xFF796B73)
private val InsightsPink = Color(0xFFB83D68)
private val InsightsLightPink = Color(0xFFFCE8EF)

@Composable
fun InsightsScreen(
    periodViewModel: PeriodViewModel,
    modifier: Modifier = Modifier
) {
    val periods by periodViewModel.periods.collectAsState()

    // Flower breathing animation
    val infiniteTransition = rememberInfiniteTransition(
        label = "insightsAnimation"
    )

    val flowerScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flowerScale"
    )

    // Animated decorative accent
    val accentAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1600,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "accentAlpha"
    )

    val accentColor by infiniteTransition.animateColor(
        initialValue = Color(0xFFE9A2B9),
        targetValue = Color(0xFFB83D68),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "accentColor"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFEDF3),
                        Color(0xFFFFF8FA),
                        Color(0xFFFFF1F6),
                        Color(0xFFFFF8FA)
                    )
                )
            )
            .drawBehind {
                // Decorative soft circles
                drawCircle(
                    color = Color(0xFFF8DCE7).copy(alpha = 0.35f),
                    radius = 130.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(
                        size.width * 0.95f,
                        size.height * 0.08f
                    )
                )

                drawCircle(
                    color = Color(0xFFF5D5E1).copy(alpha = 0.25f),
                    radius = 170.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(
                        size.width * 0.02f,
                        size.height * 0.55f
                    )
                )

                // Subtle diagonal texture
                val gap = 38.dp.toPx()

                for (x in 0..(size.width / gap).toInt()) {
                    for (y in 0..(size.height / gap).toInt()) {
                        val px = x * gap
                        val py = y * gap

                        drawCircle(
                            color = Color(0xFFE8B8C9).copy(alpha = 0.17f),
                            radius = 1.2.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(
                                px,
                                py
                            )
                        )
                    }
                }
            }
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 26.dp),

        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        // HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Your Cycle Insights",
                    style = MaterialTheme.typography.headlineMedium,
                    color = InsightsText
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Understand your body a little better.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = InsightsMutedText,
                    lineHeight = 24.sp
                )
            }

            Text(
                text = "🌸",
                fontSize = 38.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = flowerScale
                    scaleY = flowerScale
                    rotationZ = flowerScale * 4f - 4f
                }
            )
        }

        // ANIMATED DECORATIVE ACCENT
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xFFE8B4C6)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .graphicsLayer {
                        alpha = accentAlpha
                    }
                    .background(
                        color = accentColor.copy(alpha = 0.15f),
                        shape = CircleShape
                    )
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "✦  YOUR WELLNESS  ✦",
                    color = accentColor,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFE8B4C6),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // EXISTING CYCLE INSIGHTS
        CycleInsights(periods = periods)

        Spacer(modifier = Modifier.height(4.dp))

        // REMINDER HEADING
        Text(
            text = "A little reminder 💗",
            style = MaterialTheme.typography.titleMedium,
            color = InsightsText
        )

        // REMINDER CARD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFFFFF0F5),
                    shape = MaterialTheme.shapes.large
                )
                .drawBehind {
                    drawRoundRect(
                        color = Color(0xFFE8B4C6).copy(alpha = 0.35f),
                        style = Stroke(width = 1.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                            20.dp.toPx()
                        )
                    )
                }
                .padding(18.dp)
        ) {

            Text(
                text = "Your body has its own rhythm 🌷",
                style = MaterialTheme.typography.titleSmall,
                color = InsightsPink
            )

            Spacer(modifier = Modifier.height(9.dp))

            Text(
                text = "Cycle predictions are estimates, not guarantees. " +
                        "Cycles can naturally vary from month to month. " +
                        "Keep tracking your dates to build a more useful history.",
                style = MaterialTheme.typography.bodyMedium,
                color = InsightsMutedText,
                lineHeight = 23.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}