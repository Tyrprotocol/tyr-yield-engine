// File: AiStandingCard.kt
package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ActionableTradeRoute
import com.example.ai.PortfolioStanding
import com.example.ui.theme.CyanCore
import com.example.ui.theme.EmeraldYieldBright
import com.example.ui.theme.EmeraldYieldPrimary
import com.example.ui.theme.GoldAlert
import com.example.ui.theme.PurpleTech
import com.example.ui.theme.RedRisk
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TerminalVoid
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiStandingCard(
    standing: PortfolioStanding,
    onQuickPromptClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_standing_card"),
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Grade and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                color = when (standing.defenseStatusGrade) {
                                    "A+", "A" -> EmeraldYieldPrimary.copy(alpha = 0.2f)
                                    "B" -> CyanCore.copy(alpha = 0.2f)
                                    else -> GoldAlert.copy(alpha = 0.2f)
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = standing.defenseStatusGrade,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = when (standing.defenseStatusGrade) {
                                "A+", "A" -> EmeraldYieldBright
                                "B" -> CyanCore
                                else -> GoldAlert
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "PORTFOLIO STANDING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Efficiency Score: ${standing.capitalEfficiencyScore}/100",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                // Defense Ratio Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TerminalSurfaceVariant)
                        .border(
                            1.dp,
                            if (standing.defenseToRiskRatio >= 1.25) EmeraldYieldPrimary.copy(alpha = 0.4f) else GoldAlert.copy(alpha = 0.4f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (standing.defenseToRiskRatio >= 1.25) Icons.Default.Shield else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (standing.defenseToRiskRatio >= 1.25) EmeraldYieldBright else GoldAlert,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${String.format(Locale.US, "%.2f", standing.defenseToRiskRatio)}x Defense",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (standing.defenseToRiskRatio >= 1.25) EmeraldYieldBright else GoldAlert
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3-Metric Quick Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricPill(
                    label = "Blended Yield",
                    value = String.format(Locale.US, "%.2f%%", standing.blendedYieldPercent),
                    subtext = "+$" + String.format(Locale.US, "%.2f", standing.monthlyIncome) + "/mo",
                    color = EmeraldYieldBright,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                MetricPill(
                    label = "Sectors Covered",
                    value = "${standing.coveredSectorsCount} / ${standing.totalSectorsCount}",
                    subtext = if (standing.unheldSectors.isEmpty()) "Full 9-Vector" else "${standing.unheldSectors.size} Open Gaps",
                    color = if (standing.unheldSectors.isEmpty()) EmeraldYieldBright else CyanCore,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                MetricPill(
                    label = "Settled Cash",
                    value = String.format(Locale.US, "$%.2f", standing.settledBuyingPower),
                    subtext = "DRIP = OFF",
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Týr Protocol Financial Expert Rating Strip
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TerminalSurfaceVariant)
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TÝR PROTOCOL AUDIT RATING",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = standing.financialExpertGrade,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = EmeraldYieldBright
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Sustainability: ${standing.yieldSustainabilityScore}/100",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanCore
                        )
                        Text(
                            text = "Tax: ${String.format(Locale.US, "%.0f%%", standing.taxEfficiencyScore)}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PurpleTech
                        )
                    }
                }
            }

            // Gaps alert pills
            if (standing.unheldSectors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "UNHELD RISK VECTOR GAPS:",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    standing.unheldSectors.take(4).forEach { sector ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldAlert.copy(alpha = 0.12f))
                                .border(1.dp, GoldAlert.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⚠ ${sector.shortName}",
                                fontSize = 10.sp,
                                color = GoldAlert,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(TerminalSurfaceVariant)
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtext,
                fontSize = 9.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun ActionableRouteCard(
    route: ActionableTradeRoute,
    onExecuteRoute: (ActionableTradeRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("actionable_route_card"),
        colors = CardDefaults.cardColors(containerColor = TerminalSurfaceVariant),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleTech.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(PurpleTech.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = PurpleTech,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = route.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = String.format(Locale.US, "$%.2f", route.totalCapitalToDeploy),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = EmeraldYieldBright
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Step-by-Step Allocations
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                route.targetAllocations.forEachIndexed { idx, alloc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TerminalSurface)
                            .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${idx + 1}.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = alloc.ticker,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Stage ${alloc.stage}",
                                        fontSize = 9.sp,
                                        color = CyanCore
                                    )
                                    if (alloc.isGapFiller) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "🛡 GAP",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldYieldBright
                                        )
                                    }
                                }
                                Text(
                                    text = alloc.sectorName,
                                    fontSize = 9.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = String.format(Locale.US, "$%.2f", alloc.dollarAmount),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = String.format(Locale.US, "%.2f sh @ %.1f%%", alloc.estimatedShares, alloc.annualYieldPercent),
                                fontSize = 9.sp,
                                color = EmeraldYieldBright,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Plain English Trajectory
            Text(
                text = route.plainEnglishRationale,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Apply / Execute Button
            Button(
                onClick = { onExecuteRoute(route) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldYieldPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("apply_route_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = TerminalVoid,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Load Route into Sweep Engine",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TerminalVoid
                )
            }
        }
    }
}
