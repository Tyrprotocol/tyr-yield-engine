// File: CompoundingTrajectoryChart.kt
package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ProjectionMilestone
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

/**
 * Native Jetpack Compose Canvas curved area chart illustrating 20-year exponential compounding:
 * Portfolio Value vs Cumulative Out-of-Pocket Deposits vs Dividend Earnings.
 */
@Composable
fun CompoundingTrajectoryChart(
    milestones: List<ProjectionMilestone>,
    modifier: Modifier = Modifier
) {
    if (milestones.isEmpty()) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TerminalSurface)
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("compounding_chart_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "COMPOUNDING TRAJECTORY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "Dividends Reinvested via DRIP=OFF Sweeps",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                val finalVal = milestones.lastOrNull()?.projectedPortfolioBalance ?: 0.0
                Text(
                    text = String.format(Locale.US, "$%,.0f Max", finalVal),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = EmeraldYieldBright
                )
            }

            // Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("compounding_trajectory_canvas")
                ) {
                    val maxVal = milestones.maxOfOrNull { it.projectedPortfolioBalance } ?: 1000.0
                    val widthStep = if (milestones.size > 1) size.width / (milestones.size - 1) else size.width

                    // 1. Grid reference lines
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = size.height * (i.toFloat() / gridLines)
                        drawLine(
                            color = Color(0xFF1E2838),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // 2. Build Paths for Portfolio Balance and Contributions
                    val balancePath = Path()
                    val contribPath = Path()

                    val balancePoints = mutableListOf<Offset>()
                    val contribPoints = mutableListOf<Offset>()

                    milestones.forEachIndexed { index, m ->
                        val x = index * widthStep
                        val normBalance = (m.projectedPortfolioBalance / maxVal).coerceIn(0.0, 1.0).toFloat()
                        val normContrib = (m.cumulativeContributions / maxVal).coerceIn(0.0, 1.0).toFloat()

                        val yBalance = size.height - (normBalance * size.height * 0.88f) - 6.dp.toPx()
                        val yContrib = size.height - (normContrib * size.height * 0.88f) - 6.dp.toPx()

                        balancePoints.add(Offset(x, yBalance))
                        contribPoints.add(Offset(x, yContrib))

                        if (index == 0) {
                            balancePath.moveTo(x, yBalance)
                            contribPath.moveTo(x, yContrib)
                        } else {
                            val prev = balancePoints[index - 1]
                            val cx = (prev.x + x) / 2f
                            balancePath.cubicTo(cx, prev.y, cx, yBalance, x, yBalance)

                            val prevC = contribPoints[index - 1]
                            val cxC = (prevC.x + x) / 2f
                            contribPath.cubicTo(cxC, prevC.y, cxC, yContrib, x, yContrib)
                        }
                    }

                    // 3. Area Fill for Portfolio Balance
                    val fillPath = Path().apply {
                        addPath(balancePath)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                EmeraldYieldPrimary.copy(alpha = 0.35f),
                                CyanCore.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        ),
                        style = Fill
                    )

                    // 4. Draw Lines
                    drawPath(
                        path = contribPath,
                        color = GoldAlert.copy(alpha = 0.6f),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )

                    drawPath(
                        path = balancePath,
                        color = EmeraldYieldBright,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 5. Draw Vertex Points
                    balancePoints.forEach { pt ->
                        drawCircle(
                            color = EmeraldYieldPrimary,
                            radius = 4.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = Color(0xFF070B09),
                            radius = 2.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(EmeraldYieldBright, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Total Balance", fontSize = 10.sp, color = TextPrimary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(GoldAlert, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Your Cash Deposits", fontSize = 10.sp, color = TextSecondary)
                }
            }
        }
    }
}
