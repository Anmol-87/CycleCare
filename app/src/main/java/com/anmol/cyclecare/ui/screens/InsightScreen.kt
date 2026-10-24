package com.anmol.cyclecare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anmol.cyclecare.ui.components.CycleInsights
import com.anmol.cyclecare.viewmodel.PeriodViewModel

private val InsightsBackground = Color(0xFFFFF8FA)
private val InsightsText = Color(0xFF30252B)
private val InsightsMutedText = Color(0xFF796B73)

@Composable
fun InsightsScreen(
    periodViewModel: PeriodViewModel,
    modifier: Modifier = Modifier
) {
    val periods by periodViewModel.periods.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(InsightsBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "Your Cycle Insights 🌸",
            style = MaterialTheme.typography.headlineMedium,
            color = InsightsText
        )

        Text(
            text = "Understand your cycle patterns, one month at a time.",
            style = MaterialTheme.typography.bodyLarge,
            color = InsightsMutedText,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        CycleInsights(periods = periods)

        Text(
            text = "A little reminder 💗",
            style = MaterialTheme.typography.titleMedium,
            color = InsightsText
        )

        Text(
            text = "Cycle predictions are estimates, not guarantees. " +
                "Cycles can naturally vary from month to month. " +
                "Keep tracking your dates to build a more useful history.",
            style = MaterialTheme.typography.bodyMedium,
            color = InsightsMutedText,
            lineHeight = 22.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )
    }
}
