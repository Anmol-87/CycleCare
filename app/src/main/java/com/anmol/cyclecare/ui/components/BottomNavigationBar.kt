package com.anmol.cyclecare.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CycleCarePink = Color(0xFFB83D68)
private val CycleCareBackground = Color(0xFFFFF8FA)
private val CycleCareText = Color(0xFF796B73)

enum class CycleCareTab(val label: String) {
    Home("Home"),
    Calendar("Calendar"),
    Insights("Insights"),
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
        NavigationBarItem(
            selected = selectedTab == CycleCareTab.Home,
            onClick = { onTabSelected(CycleCareTab.Home) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home"
                )
            },
            label = {
                Text("Home", fontSize = 11.sp)
            },
            colors = navigationItemColors()
        )

        NavigationBarItem(
            selected = selectedTab == CycleCareTab.Calendar,
            onClick = { onTabSelected(CycleCareTab.Calendar) },
            icon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Calendar"
                )
            },
            label = {
                Text("Calendar", fontSize = 11.sp)
            },
            colors = navigationItemColors()
        )

        NavigationBarItem(
            selected = selectedTab == CycleCareTab.Insights,
            onClick = { onTabSelected(CycleCareTab.Insights) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Insights,
                    contentDescription = "Insights"
                )
            },
            label = {
                Text("Insights", fontSize = 11.sp)
            },
            colors = navigationItemColors()
        )

        NavigationBarItem(
            selected = selectedTab == CycleCareTab.Profile,
            onClick = { onTabSelected(CycleCareTab.Profile) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile"
                )
            },
            label = {
                Text("Profile", fontSize = 11.sp)
            },
            colors = navigationItemColors()
        )
    }
}

@Composable
private fun navigationItemColors() =
    NavigationBarItemDefaults.colors(
        selectedIconColor = CycleCarePink,
        selectedTextColor = CycleCarePink,
        indicatorColor = Color(0xFFFCE8EF),
        unselectedIconColor = CycleCareText,
        unselectedTextColor = CycleCareText
    )