package com.anmol.cyclecare.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CycleCareScreen(
    homeContent: @Composable () -> Unit,
    calendarContent: @Composable () -> Unit,
    insightsContent: @Composable () -> Unit,
    profileContent: @Composable () -> Unit,
    preciousContent: @Composable () -> Unit
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
        AnimatedContent(
            targetState = currentTab,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    (
                            slideInHorizontally(
                                animationSpec = tween(350),
                                initialOffsetX = { it }
                            ) +
                                    fadeIn(animationSpec = tween(350)) +
                                    scaleIn(
                                        initialScale = 0.96f,
                                        animationSpec = tween(350)
                                    )
                            ) togetherWith (
                            slideOutHorizontally(
                                animationSpec = tween(350),
                                targetOffsetX = { -it / 3 }
                            ) +
                                    fadeOut(animationSpec = tween(250)) +
                                    scaleOut(
                                        targetScale = 0.98f,
                                        animationSpec = tween(350)
                                    )
                            )
                } else {
                    (
                            slideInHorizontally(
                                animationSpec = tween(350),
                                initialOffsetX = { -it }
                            ) +
                                    fadeIn(animationSpec = tween(350)) +
                                    scaleIn(
                                        initialScale = 0.96f,
                                        animationSpec = tween(350)
                                    )
                            ) togetherWith (
                            slideOutHorizontally(
                                animationSpec = tween(350),
                                targetOffsetX = { it / 3 }
                            ) +
                                    fadeOut(animationSpec = tween(250)) +
                                    scaleOut(
                                        targetScale = 0.98f,
                                        animationSpec = tween(350)
                                    )
                            )
                }
            },
            label = "CycleCareScreenTransition"
        ) { targetTab ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (targetTab) {
                    CycleCareTab.Home -> homeContent()
                    CycleCareTab.Calendar -> calendarContent()
                    CycleCareTab.Insights -> insightsContent()
                    CycleCareTab.Precious -> preciousContent()
                    CycleCareTab.Profile -> profileContent()
                }
            }
        }
    }
}