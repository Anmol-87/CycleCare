package com.anmol.cyclecare.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.anmol.cyclecare.ui.components.BottomNavigationBar
import com.anmol.cyclecare.ui.components.CycleCareTab

@Composable
fun CycleCareScreen(
    homeContent: @Composable () -> Unit,
    calendarContent: @Composable () -> Unit,
    insightsContent: @Composable () -> Unit,
    profileContent: @Composable () -> Unit
) {
    var selectedTab by rememberSaveable {
        mutableStateOf(CycleCareTab.Home.name)
    }

    val currentTab = CycleCareTab.valueOf(selectedTab)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                selectedTab = currentTab,
                onTabSelected = { tab ->
                    selectedTab = tab.name
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                CycleCareTab.Home -> homeContent()
                CycleCareTab.Calendar -> calendarContent()
                CycleCareTab.Insights -> insightsContent()
                CycleCareTab.Profile -> profileContent()
            }
        }
    }
}