// File: StageBreakdownCard.kt
package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketRiskBreakdown
import com.example.data.model.RiskSector
import com.example.data.model.StageMetadata
import com.example.data.model.StageRegistry
import com.example.ui.StageBreakdown
import com.example.ui.theme.CyanCore
import com.example.ui.theme.EmeraldYieldBright
import com.example.ui.theme.EmeraldYieldPrimary
import com.example.ui.theme.GoldAlert
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun StageBreakdownSection(
    breakdowns: List<StageBreakdown>,
    modifier: Modifier = Modifier,
    onStageClick: ((Int) -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STAGE ALLOCATION ARCHITECTURE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextSecondary
            )
            Text(
                text = "4-Tier System",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        breakdowns.forEach { breakdown ->
            val stageMeta = StageRegistry.getStage(breakdown.stage)
            StageItemCard(
                breakdown = breakdown,
                meta = stageMeta,
                onClick = { onStageClick?.invoke(breakdown.stage) }
            )
        }
    }
}

@Composable
fun StageItemCard(
    breakdown: StageBreakdown,
    meta: StageMetadata,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TerminalSurface)
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(14.dp))
            .clickable { expanded = !expanded }
            .padding(14.dp)
            .testTag("stage_card_${breakdown.stage}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(meta.accentColor.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, meta.accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${breakdown.stage}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = meta.accentColor
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = meta.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = meta.subtitle,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format(Locale.US, "$%,.2f", breakdown.totalValue),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f%% of port", breakdown.percentageOfPortfolio),
                        fontSize = 11.sp,
                        color = meta.accentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Allocation progress bar
            LinearProgressIndicator(
                progress = { (breakdown.percentageOfPortfolio / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = meta.accentColor,
                trackColor = TerminalSurfaceVariant,
            )

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TerminalSurfaceVariant, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = meta.accentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Plain-English Role:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = meta.accentColor
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = meta.plainEnglishDescription,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Active Vehicles: ${breakdown.holdingsCount}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = String.format(Locale.US, "Est. Annual Flow: $%,.2f", breakdown.annualIncome),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = meta.accentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MarketRiskDiversitySection(
    sectors: List<MarketRiskBreakdown>,
    modifier: Modifier = Modifier,
    onFillGapClick: ((RiskSector) -> Unit)? = null
) {
    if (sectors.isEmpty()) return

    val coveredSectors = sectors.filter { it.holdingsCount > 0 }
    val gapSectors = sectors.filter { it.holdingsCount == 0 }
    val totalSectorsCount = sectors.size

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header with Coverage Telemetry
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = CyanCore,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "MARKET & RISK DIVERSITY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = TextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        if (gapSectors.isEmpty()) EmeraldYieldPrimary.copy(alpha = 0.2f) else CyanCore.copy(alpha = 0.15f),
                        RoundedCornerShape(6.dp)
                    )
                    .border(
                        1.dp,
                        if (gapSectors.isEmpty()) EmeraldYieldPrimary.copy(alpha = 0.5f) else CyanCore.copy(alpha = 0.4f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${coveredSectors.size} of $totalSectorsCount Covered (${gapSectors.size} Gaps)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (gapSectors.isEmpty()) EmeraldYieldBright else CyanCore
                )
            }
        }

        // Subheader: Covered Vectors (Active Exposure)
        Text(
            text = "COVERED CATEGORIES & EXPOSURE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = TextSecondary
        )

        coveredSectors.forEach { item ->
            var expanded by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TerminalSurface)
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(12.dp))
                    .clickable { expanded = !expanded }
                    .padding(12.dp)
                    .testTag("sector_card_${item.sector.categoryId}")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(item.sector.accentColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.sector.displayName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        // Status Badge
                        Box(
                            modifier = Modifier
                                .background(
                                    when {
                                        item.isOverConcentrated -> GoldAlert.copy(alpha = 0.2f)
                                        item.isUnderRepresented -> CyanCore.copy(alpha = 0.2f)
                                        else -> EmeraldYieldPrimary.copy(alpha = 0.2f)
                                    },
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when {
                                    item.isOverConcentrated -> "CONCENTRATED (Capped)"
                                    item.isUnderRepresented -> "EXPANSION FOCUS"
                                    else -> "BALANCED"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    item.isOverConcentrated -> GoldAlert
                                    item.isUnderRepresented -> CyanCore
                                    else -> EmeraldYieldBright
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = String.format(Locale.US, "Weight: %.1f%% / %.0f%% cap", item.currentWeightPercent, item.sector.maxHealthyWeightPercent),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                        Text(
                            text = String.format(Locale.US, "$%,.2f", item.totalValue),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val progressFraction = (item.currentWeightPercent / item.sector.maxHealthyWeightPercent)
                        .toFloat().coerceIn(0f, 1f)

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (item.isOverConcentrated) GoldAlert else item.sector.accentColor,
                        trackColor = TerminalCardBorder
                    )

                    AnimatedVisibility(visible = expanded) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                text = item.sector.description,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Active vehicles in sector: ${item.holdingsCount}",
                                fontSize = 10.sp,
                                color = CyanCore
                            )
                        }
                    }
                }
            }
        }

        // Subheader: Gaps & Uncovered Sectors (What isn't covered or held)
        if (gapSectors.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "UNCOVERED SECTOR GAPS (${gapSectors.size} UNHELD)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = GoldAlert
                )
                Text(
                    text = "Vulnerability Defense",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }

            gapSectors.forEach { gap ->
                var expandedGap by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TerminalSurfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, GoldAlert.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { expandedGap = !expandedGap }
                        .padding(12.dp)
                        .testTag("gap_card_${gap.sector.categoryId}")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(gap.sector.accentColor.copy(alpha = 0.4f), CircleShape)
                                        .border(1.dp, gap.sector.accentColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = gap.sector.displayName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(GoldAlert.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .border(1.dp, GoldAlert.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "0% HELD • GAP",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAlert
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val suggestedTicker = when (gap.sector) {
                            RiskSector.HEALTHCARE_DEFENSIVE -> "HIYY (Healthcare 75.9%) or XLV"
                            RiskSector.NATURAL_RESOURCES_ENERGY -> "XOMO (Oil & Gas 45.9%) or AMLP (Pipelines)"
                            RiskSector.REAL_ESTATE_INFRASTRUCTURE -> "VNQ (REITs) or UTG (Utilities)"
                            RiskSector.TAX_EXEMPT_STABLE -> "SGOV (Tax-Free T-Bills) or MUB (Munis)"
                            RiskSector.COMMODITIES_PRECIOUS_METALS -> "GDXY (Gold Miners) or GLD"
                            RiskSector.RUSSELL_SMALL_CAP -> "RDTE (R2000 0DTE)"
                            RiskSector.SP500_LARGE_CAP -> "XDTE (S&P 500 0DTE) or VOO"
                            RiskSector.EQUITIES_THEMATIC_TECH -> "FEPI (FANG+) or TSLY"
                            RiskSector.FIXED_INCOME_DURATION -> "TLTW (20+ Yr Duration)"
                            RiskSector.CRYPTO_DIGITAL_ASSETS -> "MSTY or HOOY"
                            RiskSector.GENERAL_DIVERSIFIED -> "JEPI or BAL"
                        }

                        Text(
                            text = "Exposure Gap: $0.00 allocated. Adding $suggestedTicker provides immediate balance.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )

                        AnimatedVisibility(visible = expandedGap) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                Text(
                                    text = gap.sector.description,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Recommended target cap: up to ${gap.sector.maxHealthyWeightPercent.toInt()}% of portfolio",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyanCore
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

