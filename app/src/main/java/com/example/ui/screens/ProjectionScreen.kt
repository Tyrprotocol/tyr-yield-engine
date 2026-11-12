// File: ProjectionScreen.kt
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.StackedLineChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ProjectionEngine
import com.example.engine.ProjectionMilestone
import com.example.ui.PortfolioMetrics
import com.example.ui.theme.CyanCore
import com.example.ui.theme.EmeraldYieldBright
import com.example.ui.theme.EmeraldYieldPrimary
import com.example.ui.theme.GoldAlert
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TerminalVoid
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun ProjectionScreen(
    metrics: PortfolioMetrics,
    projections: List<ProjectionMilestone>,
    selectedHorizon: String,
    monthlyContribution: Double,
    onSelectHorizon: (String) -> Unit,
    onSetMonthlyContribution: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeMilestone = projections.find { it.horizonKey == selectedHorizon } ?: projections.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalVoid)
            .padding(horizontal = 16.dp)
            .testTag("projection_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(EmeraldYieldPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.StackedLineChart,
                        contentDescription = null,
                        tint = EmeraldYieldBright,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "20-Year Closed-Loop Horizon Outlook",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Closed-loop compounding with DRIP=OFF sweeps",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Horizon Selector Tabs (30D, 90D, 1Y, 5Y, 10Y, 20Y)
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(projections) { milestone ->
                    val isSelected = milestone.horizonKey == selectedHorizon
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) EmeraldYieldPrimary.copy(alpha = 0.25f) else TerminalSurface)
                            .border(
                                1.dp,
                                if (isSelected) EmeraldYieldPrimary else TerminalCardBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectHorizon(milestone.horizonKey) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("horizon_tab_${milestone.horizonKey}")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = milestone.horizonKey,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) EmeraldYieldBright else TextPrimary
                            )
                            Text(
                                text = milestone.horizonLabel,
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Interactive Performance Trajectory Chart with 3 View Toggles & 6 Horizons
        item {
            com.example.ui.components.InteractivePerformanceChart(
                startingBalance = metrics.totalPortfolioValue,
                blendedYieldPercent = metrics.blendedYieldPercent,
                defenseRatio = metrics.defenseToRiskDensityRatio,
                selectedHorizon = selectedHorizon,
                monthlyContribution = monthlyContribution,
                onSelectHorizon = onSelectHorizon
            )
        }

        // Active Milestone Hero Focus
        if (activeMilestone != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(TerminalSurfaceVariant)
                        .border(1.dp, TerminalCardBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                        .testTag("milestone_hero_card")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "PROJECTED VALUE AT ${activeMilestone.horizonLabel.uppercase()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "${activeMilestone.months} Months",
                                fontSize = 11.sp,
                                color = EmeraldYieldBright,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = String.format(Locale.US, "$%,.2f", activeMilestone.projectedPortfolioBalance),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Secondary Stat Tiles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MilestoneStatTile(
                                label = "CUMULATIVE DIVIDENDS",
                                value = String.format(Locale.US, "$%,.2f", activeMilestone.cumulativeDividendsEarned),
                                accentColor = EmeraldYieldBright,
                                modifier = Modifier.weight(1f)
                            )

                            MilestoneStatTile(
                                label = "MONTHLY RUN-RATE",
                                value = String.format(Locale.US, "$%,.2f/mo", activeMilestone.projectedMonthlyDividendIncome),
                                accentColor = CyanCore,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        MilestoneStatTile(
                            label = "OUT-OF-POCKET CASH DEPOSITS",
                            value = String.format(Locale.US, "$%,.2f ($%.0f/mo)", activeMilestone.cumulativeContributions, monthlyContribution),
                            accentColor = GoldAlert,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Monthly Contribution Slider Control
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TerminalSurface)
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Monthly Added Contribution:",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = String.format(Locale.US, "$%.0f / month", monthlyContribution),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CyanCore
                        )
                    }

                    Slider(
                        value = monthlyContribution.toFloat(),
                        onValueChange = { onSetMonthlyContribution(it.toDouble()) },
                        valueRange = 0f..1000f,
                        steps = 19,
                        colors = SliderDefaults.colors(
                            thumbColor = CyanCore,
                            activeTrackColor = CyanCore,
                            inactiveTrackColor = TerminalSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$0/mo", fontSize = 10.sp, color = TextSecondary)
                        Text("$250/mo", fontSize = 10.sp, color = TextSecondary)
                        Text("$500/mo", fontSize = 10.sp, color = TextSecondary)
                        Text("$1,000/mo", fontSize = 10.sp, color = TextSecondary)
                    }
                }
            }
        }

        // Pinned Non-Liability Legal Notice
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TerminalSurface)
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
                    .testTag("pinned_legal_notice")
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        tint = GoldAlert,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = ProjectionEngine.PINNED_NON_LIABILITY_NOTICE,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MilestoneStatTile(
    label: String,
    value: String,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(TerminalSurface, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = accentColor
            )
        }
    }
}
