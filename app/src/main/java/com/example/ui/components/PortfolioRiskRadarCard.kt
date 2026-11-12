// File: PortfolioRiskRadarCard.kt
package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketRiskBreakdown
import com.example.data.model.RiskSector
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Native Jetpack Compose Canvas Radar / Spider Web Chart
 * Visualizes portfolio weight % vs healthy risk caps across all 9 market vectors and 4 tiers,
 * directly highlighting coverage gaps for beginner and uneducated users.
 */
@Composable
fun PortfolioRiskRadarCard(
    sectors: List<MarketRiskBreakdown>,
    onSectorGapClick: (RiskSector) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (sectors.isEmpty()) return

    var animationTarget by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationTarget,
        animationSpec = tween(durationMillis = 1000),
        label = "radar_anim"
    )

    LaunchedEffect(sectors) {
        animationTarget = 1f
    }

    val coveredCount = sectors.count { it.holdingsCount > 0 }
    val gapCount = sectors.count { it.holdingsCount == 0 }
    val gaps = sectors.filter { it.holdingsCount == 0 }

    var selectedSector by remember { mutableStateOf<MarketRiskBreakdown?>(null) }
    var showPlainEnglishInfo by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TerminalSurface)
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("portfolio_risk_radar_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(CyanCore.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = "Radar Chart",
                            tint = CyanCore,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "PORTFOLIO RISK RADAR",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Visual Multi-Vector Exposure & Gap Web",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Coverage Status Pill
                Box(
                    modifier = Modifier
                        .background(
                            if (gapCount == 0) EmeraldYieldPrimary.copy(alpha = 0.2f) else GoldAlert.copy(alpha = 0.2f),
                            RoundedCornerShape(6.dp)
                        )
                        .border(
                            1.dp,
                            if (gapCount == 0) EmeraldYieldPrimary else GoldAlert.copy(alpha = 0.5f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$coveredCount / ${sectors.size} Covered",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (gapCount == 0) EmeraldYieldBright else GoldAlert
                    )
                }
            }

            // Plain-English Beginner Banner Toggle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(TerminalSurfaceVariant)
                    .clickable { showPlainEnglishInfo = !showPlainEnglishInfo }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CyanCore,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (gapCount > 0) {
                                "How to read this: Wider web = safer portfolio. Flat points = $gapCount exposure gaps."
                            } else {
                                "Full Protection: Every core category is defended against market shocks."
                            },
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = if (showPlainEnglishInfo) "Hide" else "Learn",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanCore
                    )
                }
            }

            AnimatedVisibility(visible = showPlainEnglishInfo) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyanCore.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .border(1.dp, CyanCore.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "A radar chart works like an umbrella. If one side is collapsed, market rain hits you there. If you hold only crypto or tech, you are vulnerable if tech drops. Balancing with commodities, energy, and government paper shields your weekly cash flow.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Radar Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(240.dp)
                        .testTag("radar_chart_canvas")
                ) {
                    drawRadarWeb(
                        sectors = sectors,
                        progress = animatedProgress
                    )
                }
            }

            // Legend Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(CyanCore, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Your Current Weight", fontSize = 10.sp, color = TextPrimary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(GoldAlert.copy(alpha = 0.6f), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Max Healthy Cap", fontSize = 10.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(RedRisk, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Unheld Gap", fontSize = 10.sp, color = RedRisk)
                }
            }

            // Beginner Diagnostic Gap Pills
            if (gapCount > 0) {
                Text(
                    text = "UNCOVERED EXPOSURE GAPS (${gapCount} NEEDED)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = GoldAlert
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    gaps.take(3).forEach { gap ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldAlert.copy(alpha = 0.12f))
                                .border(1.dp, GoldAlert.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable { onSectorGapClick(gap.sector) }
                                .padding(8.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = GoldAlert,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = gap.sector.shortName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAlert,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "0% Held • $0.00",
                                    fontSize = 9.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom DrawScope extension for rendering the multi-axis spider radar
 */
private fun DrawScope.drawRadarWeb(
    sectors: List<MarketRiskBreakdown>,
    progress: Float
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = (size.minDimension / 2f) * 0.72f
    val axisCount = sectors.size
    val angleStep = (2 * PI / axisCount).toFloat()

    // 1. Draw Concentric Grid Polygons (25%, 50%, 75%, 100% of max scale)
    val ringLevels = listOf(0.25f, 0.50f, 0.75f, 1.0f)
    ringLevels.forEach { level ->
        val ringPath = Path()
        for (i in 0 until axisCount) {
            val angle = -PI.toFloat() / 2f + (i * angleStep)
            val r = radius * level
            val x = center.x + r * cos(angle)
            val y = center.y + r * sin(angle)
            if (i == 0) ringPath.moveTo(x, y) else ringPath.lineTo(x, y)
        }
        ringPath.close()
        drawPath(
            path = ringPath,
            color = Color(0xFF1E2838),
            style = Stroke(width = 1.dp.toPx())
        )
    }

    // 2. Draw Spokes / Axis Lines and Sector Dot Markers
    for (i in 0 until axisCount) {
        val angle = -PI.toFloat() / 2f + (i * angleStep)
        val endX = center.x + radius * cos(angle)
        val endY = center.y + radius * sin(angle)

        drawLine(
            color = Color(0xFF223147),
            start = center,
            end = Offset(endX, endY),
            strokeWidth = 1.dp.toPx()
        )

        // Draw axis gap warning dot if weight is 0
        val sector = sectors[i]
        val labelRadius = radius + 18.dp.toPx()
        val labelX = center.x + labelRadius * cos(angle)
        val labelY = center.y + labelRadius * sin(angle)

        if (sector.holdingsCount == 0) {
            drawCircle(
                color = GoldAlert,
                radius = 3.dp.toPx(),
                center = Offset(labelX, labelY)
            )
        }
    }

    // Max display scale: normalize against 45% (highest sector healthy cap)
    val maxScale = 45.0

    // 3. Draw Recommended Cap Polygon (Amber outline)
    val capPath = Path()
    for (i in 0 until axisCount) {
        val angle = -PI.toFloat() / 2f + (i * angleStep)
        val capFraction = (sectors[i].sector.maxHealthyWeightPercent / maxScale).coerceIn(0.1, 1.0)
        val r = (radius * capFraction).toFloat()
        val x = center.x + r * cos(angle)
        val y = center.y + r * sin(angle)
        if (i == 0) capPath.moveTo(x, y) else capPath.lineTo(x, y)
    }
    capPath.close()
    drawPath(
        path = capPath,
        color = GoldAlert.copy(alpha = 0.35f),
        style = Stroke(width = 1.5.dp.toPx())
    )

    // 4. Draw Current Portfolio Weight Polygon (Filled Cyan/Emerald Web)
    val currentPath = Path()
    for (i in 0 until axisCount) {
        val angle = -PI.toFloat() / 2f + (i * angleStep)
        val weight = sectors[i].currentWeightPercent * progress
        val weightFraction = (weight / maxScale).coerceIn(0.0, 1.0)
        // If 0% held, snap to a small anchor near center so gap is visually distinct
        val r = if (sectors[i].holdingsCount == 0) 4.dp.toPx() else (radius * weightFraction).toFloat()
        val x = center.x + r * cos(angle)
        val y = center.y + r * sin(angle)
        if (i == 0) currentPath.moveTo(x, y) else currentPath.lineTo(x, y)
    }
    currentPath.close()

    // Fill area
    drawPath(
        path = currentPath,
        color = CyanCore.copy(alpha = 0.30f),
        style = Fill
    )
    // Perimeter outline
    drawPath(
        path = currentPath,
        color = CyanCore,
        style = Stroke(width = 2.dp.toPx())
    )

    // 5. Draw Vertex Points
    for (i in 0 until axisCount) {
        val angle = -PI.toFloat() / 2f + (i * angleStep)
        val weight = sectors[i].currentWeightPercent * progress
        val weightFraction = (weight / maxScale).coerceIn(0.0, 1.0)
        val r = if (sectors[i].holdingsCount == 0) 4.dp.toPx() else (radius * weightFraction).toFloat()
        val x = center.x + r * cos(angle)
        val y = center.y + r * sin(angle)

        val pointColor = when {
            sectors[i].holdingsCount == 0 -> RedRisk
            sectors[i].isOverConcentrated -> GoldAlert
            else -> EmeraldYieldBright
        }
        drawCircle(
            color = pointColor,
            radius = if (sectors[i].holdingsCount == 0) 3.5.dp.toPx() else 4.5.dp.toPx(),
            center = Offset(x, y)
        )
    }
}
