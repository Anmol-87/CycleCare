package com.anmol.cyclecare.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CycleCarePink = Color(0xFFB83D68)
private val CycleCareBackground = Color(0xFFFFF8FA)
private val CycleCareText = Color(0xFF796B73)
private val CycleCareIndicator = Color(0xFFFCE8EF)

enum class CycleCareTab(val label: String) {
    Home("Home"),
    Calendar("Calendar"),
    Insights("Insights"),
    Precious("Precious ❤️"),
    Profile("Profile")
}

@Composable
fun BottomNavigationBar(
    selectedTab: CycleCareTab,
    onTabSelected: (CycleCareTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = CycleCareBackground,
        contentColor = CycleCarePink,
        tonalElevation = 8.dp
    ) {
        AnimatedNavigationItem(
            selected = selectedTab == CycleCareTab.Home,
            icon = Icons.Default.Home,
            label = "Home",
            contentDescription = "Home",
            onClick = { onTabSelected(CycleCareTab.Home) }
        )
        AnimatedNavigationItem(
            selected = selectedTab == CycleCareTab.Calendar,
            icon = Icons.Default.CalendarMonth,
            label = "Calendar",
            contentDescription = "Calendar",
            onClick = { onTabSelected(CycleCareTab.Calendar) }
        )
        AnimatedNavigationItem(
            selected = selectedTab == CycleCareTab.Insights,
            icon = Icons.Default.Insights,
            label = "Insights",
            contentDescription = "Insights",
            onClick = { onTabSelected(CycleCareTab.Insights) }
        )
        AnimatedNavigationItem(
            selected = selectedTab == CycleCareTab.Precious,
            icon = Icons.Default.Favorite,
            label = "Precious",
            contentDescription = "Precious Feature",
            heartbeatOnSelect = true,
            onClick = { onTabSelected(CycleCareTab.Precious) }
        )
        AnimatedNavigationItem(
            selected = selectedTab == CycleCareTab.Profile,
            icon = Icons.Default.Person,
            label = "Profile",
            contentDescription = "Profile",
            onClick = { onTabSelected(CycleCareTab.Profile) }
        )
    }
}

@Composable
private fun RowScope.AnimatedNavigationItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    contentDescription: String,
    onClick: () -> Unit,
    heartbeatOnSelect: Boolean = false
) {
    // Press feedback: icon shrinks slightly while the finger is down
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "PressScale"
    )

    // One-shot heartbeat when the tab becomes selected (only if enabled)
    val heartbeatScale = remember { Animatable(1f) }
    LaunchedEffect(selected) {
        if (heartbeatOnSelect && selected) {
            heartbeatScale.animateTo(1.3f, tween(120))
            heartbeatScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    // Smooth spring-based icon scaling
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "IconScale"
    )

    // Subtle upward movement for selected icon
    val iconOffset by animateFloatAsState(
        targetValue = if (selected) -3f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "IconOffset"
    )

    // Smooth icon color transition
    val iconColor by animateColorAsState(
        targetValue = if (selected) CycleCarePink else CycleCareText,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "IconColor"
    )

    // Smooth label color transition
    val labelColor by animateColorAsState(
        targetValue = if (selected) CycleCarePink else CycleCareText,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "LabelColor"
    )

    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        interactionSource = interactionSource,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.graphicsLayer {
                    val combinedScale = iconScale * pressScale * heartbeatScale.value
                    scaleX = combinedScale
                    scaleY = combinedScale
                    translationY = iconOffset.dp.toPx()
                }
            )
        },
        label = {
            Text(
                text = label,
                color = labelColor,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = CycleCarePink,
            selectedTextColor = CycleCarePink,
            indicatorColor = CycleCareIndicator,
            unselectedIconColor = CycleCareText,
            unselectedTextColor = CycleCareText
        )
    )
}