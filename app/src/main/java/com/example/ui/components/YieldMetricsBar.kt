// File: YieldMetricsBar.kt
package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DefenseStatus
import com.example.ui.PortfolioMetrics
import com.example.ui.theme.CyanCore
import com.example.ui.theme.EmeraldYieldBright
import com.example.ui.theme.EmeraldYieldPrimary
import com.example.ui.theme.GoldAlert
import com.example.ui.theme.RedRisk
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun YieldMetricsBar(
    metrics: PortfolioMetrics,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("yield_metrics_bar"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Hero Portfolio Net Capital Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalSurfaceVariant, RoundedCornerShape(16.dp))
                .border(1.dp, TerminalCardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL PORTFOLIO ASSETS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = TextSecondary
                    )

                    // DRIP Status Indicator Badge
                    Box(
                        modifier = Modifier
                            .background(TerminalSurface, RoundedCornerShape(6.dp))
                            .border(1.dp, EmeraldYieldPrimary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Autorenew,
                                contentDescription = null,
                                tint = EmeraldYieldPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "DRIP = OFF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldYieldBright,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = String.format(Locale.US, "$%,.2f", metrics.totalPortfolioValue),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary,
                    modifier = Modifier.testTag("total_portfolio_value_text")
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val gainPrefix = if (metrics.totalUnrealizedGainLoss >= 0.0) "+$" else "-$"
                    val gainColor = if (metrics.totalUnrealizedGainLoss >= 0.0) EmeraldYieldPrimary else RedRisk
                    Text(
                        text = String.format(
                            Locale.US,
                            "%s%,.2f (%.2f%%)",
                            gainPrefix,
                            kotlin.math.abs(metrics.totalUnrealizedGainLoss),
                            metrics.totalGainLossPercent
                        ),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = gainColor,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• Cost: " + String.format(Locale.US, "$%,.2f", metrics.totalCostBasis),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Secondary Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricSubCard(
                modifier = Modifier.weight(1f),
                title = "SETTLED CASH",
                value = String.format(Locale.US, "$%,.2f", metrics.settledBuyingPower),
                subtitle = "Ready to sweep",
                icon = Icons.Default.AccountBalanceWallet,
                accentColor = CyanCore,
                testTag = "settled_cash_card"
            )

            MetricSubCard(
                modifier = Modifier.weight(1f),
                title = "MONTHLY CASH FLOW",
                value = String.format(Locale.US, "$%,.2f", metrics.monthlyDividendIncome),
                subtitle = String.format(Locale.US, "%.2f%% Blended Yield", metrics.blendedYieldPercent),
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                accentColor = EmeraldYieldPrimary,
                testTag = "monthly_cash_card"
            )
        }

        // Defense-to-Risk Density Ratio Banner
        val (badgeTitle, badgeColor, badgeBg) = when (metrics.defenseStatus) {
            DefenseStatus.STRONG -> Triple(
                "DEFENSIVE SHIELD: STRONG (Better-Than-Good >= 1.25x)",
                EmeraldYieldBright,
                EmeraldYieldPrimary.copy(alpha = 0.15f)
            )
            DefenseStatus.BALANCED -> Triple(
                "DEFENSIVE DENSITY: BALANCED (1.00x - 1.24x)",
                GoldAlert,
                GoldAlert.copy(alpha = 0.15f)
            )
            DefenseStatus.HIGH_RISK -> Triple(
                "DEFENSIVE DENSITY: OVER-WEIGHT RISK (< 1.00x)",
                RedRisk,
                RedRisk.copy(alpha = 0.15f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(badgeBg, RoundedCornerShape(12.dp))
                .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("defense_density_badge")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = badgeTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }

                Text(
                    text = String.format(Locale.US, "%.2fx", metrics.defenseToRiskDensityRatio),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = badgeColor
                )
            }
        }
    }
}

@Composable
private fun MetricSubCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Box(
        modifier = modifier
            .background(TerminalSurface, RoundedCornerShape(14.dp))
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
            .testTag(testTag)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = TextSecondary
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = accentColor
            )
        }
    }
}
