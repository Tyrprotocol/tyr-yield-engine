// File: ScheduleScreen.kt
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.DistributionScheduleCard
import com.example.ui.theme.TerminalVoid

/**
 * Dedicated sub-page for the 24-Week Distribution Allocation and Closed-Loop Gap-Filling Schedule.
 * Declutters the main dashboard while offering full interactive week-by-week tracking and confirmation.
 */
@Composable
fun ScheduleScreen(
    onConfirmWeekSweep: (ticker: String, amount: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalVoid)
            .padding(horizontal = 16.dp)
            .testTag("schedule_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            DistributionScheduleCard(
                onConfirmWeekSweep = onConfirmWeekSweep
            )
        }
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
