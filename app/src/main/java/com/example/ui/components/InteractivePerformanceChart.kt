// File: InteractivePerformanceChart.kt
package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ProjectionEngine
import com.example.engine.TrajectoryPoint
import com.example.ui.theme.CyanCore
import com.example.ui.theme.EmeraldYieldBright
import com.example.ui.theme.EmeraldYieldPrimary
import com.example.ui.theme.GoldAlert
import com.example.ui.theme.PurpleTech
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TerminalVoid
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.roundToInt

enum class PerformanceMetricView(
    val title: String,
    val subtitle: String,
    val primaryColor: Color
) {
    TOTAL_BALANCE(
        title = "Total Balance",
        subtitle = "Snowball Portfolio Growth",
        primaryColor = EmeraldYieldBright
    ),
    DIVIDEND_INCOME(
        title = "Dividend Income",
        subtitle = "Monthly Run-Rate & Cumulative Payouts",
        primaryColor = CyanCore
    ),
    RISK_EXPOSURE(
        title = "Risk Exposure",
        subtitle = "Defensive Fortress vs Growth Engines",
        primaryColor = PurpleTech
    )
}

/**
 * Interactive touch-scrubbable Canvas chart visualizing portfolio trajectory across
 * Total Balance, Dividend Income, and Risk Exposure for 30D, 90D, 1Y, 5Y, 10Y, and 20Y.
 * Specially designed with clear labels and beginner-friendly explainers.
 */
@Composable
fun InteractivePerformanceChart(
    startingBalance: Double,
    blendedYieldPercent: Double,
    defenseRatio: Double,
    selectedHorizon: String = "1Y",
    monthlyContribution: Double = 100.0,
    annualCapitalAppreciation: Double = 4.0,
    onSelectHorizon: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedMetricView by remember { mutableStateOf(PerformanceMetricView.TOTAL_BALANCE) }
    var scrubFraction by remember { mutableFloatStateOf(1.0f) }
    var isScrubbing by remember { mutableStateOf(false) }

    val horizons = listOf("30D", "90D", "1Y", "5Y", "10Y", "20Y")

    // Generate granular trajectory points
    val trajectoryPoints = remember(
        startingBalance,
        blendedYieldPercent,
        defenseRatio,
        selectedHorizon,
        monthlyContribution,
        annualCapitalAppreciation
    ) {
        ProjectionEngine.calculateDetailedTrajectory(
            startingBalance = startingBalance,
            blendedYieldPercent = blendedYieldPercent,
            defenseRatio = defenseRatio,
            horizonKey = selectedHorizon,
            monthlyContribution = monthlyContribution,
            annualCapitalAppreciation = annualCapitalAppreciation
        )
    }

    val activeIndex = ((scrubFraction * (trajectoryPoints.size - 1)).roundToInt())
        .coerceIn(0, (trajectoryPoints.size - 1).coerceAtLeast(0))
    val activePoint = trajectoryPoints.getOrNull(activeIndex) ?: trajectoryPoints.lastOrNull()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TerminalSurface)
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("interactive_performance_chart_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // Header: Title & Metric View Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(selectedMetricView.primaryColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (selectedMetricView) {
                                PerformanceMetricView.TOTAL_BALANCE -> Icons.AutoMirrored.Filled.TrendingUp
                                PerformanceMetricView.DIVIDEND_INCOME -> Icons.AutoMirrored.Filled.ShowChart
                                PerformanceMetricView.RISK_EXPOSURE -> Icons.Default.Security
                            },
                            contentDescription = null,
                            tint = selectedMetricView.primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "PORTFOLIO TRAJECTORY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = selectedMetricView.subtitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                // Drag Scrub indicator hint
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TerminalSurfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = "Touch to scrub",
                        tint = CyanCore,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isScrubbing) "Scrubbing" else "Drag to inspect",
                        fontSize = 9.sp,
                        color = CyanCore,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // View Toggles: Total Balance | Dividend Income | Risk Exposure
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(TerminalSurfaceVariant)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PerformanceMetricView.entries.forEach { view ->
                    val isSelected = selectedMetricView == view
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) view.primaryColor.copy(alpha = 0.25f)
                                else Color.Transparent
                            )
                            .border(
                                1.dp,
                                if (isSelected) view.primaryColor else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                selectedMetricView = view
                                scrubFraction = 1.0f
                            }
                            .padding(vertical = 6.dp)
                            .testTag("metric_view_tab_${view.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = view.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) view.primaryColor else TextSecondary
                        )
                    }
                }
            }

            // Horizon Selector Chips: 30D, 90D, 1Y, 5Y, 10Y, 20Y
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(horizons) { horizonKey ->
                    val isSelected = horizonKey == selectedHorizon
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) EmeraldYieldPrimary.copy(alpha = 0.25f)
                                else TerminalSurfaceVariant
                            )
                            .border(
                                1.dp,
                                if (isSelected) EmeraldYieldPrimary else TerminalCardBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onSelectHorizon(horizonKey)
                                scrubFraction = 1.0f
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("horizon_chip_$horizonKey")
                    ) {
                        Text(
                            text = horizonKey,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) EmeraldYieldBright else TextSecondary
                        )
                    }
                }
            }

            // Dynamic Active Tooltip Header
            if (activePoint != null) {
                ActiveScrubTooltip(
                    view = selectedMetricView,
                    point = activePoint,
                    horizonKey = selectedHorizon,
                    monthlyContribution = monthlyContribution
                )
            }

            // Interactive Canvas Drawing Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .testTag("interactive_canvas_view")
                        .pointerInput(trajectoryPoints) {
                            detectTapGestures { offset ->
                                scrubFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                isScrubbing = true
                            }
                        }
                        .pointerInput(trajectoryPoints) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    scrubFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                    isScrubbing = true
                                },
                                onDragEnd = { isScrubbing = false },
                                onDragCancel = { isScrubbing = false },
                                onDrag = { change, _ ->
                                    scrubFraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                                    isScrubbing = true
                                }
                            )
                        }
                ) {
                    if (trajectoryPoints.isEmpty()) return@Canvas

                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    // 1. Grid reference lines
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = canvasHeight * (i.toFloat() / gridLines)
                        drawLine(
                            color = Color(0xFF1E2838),
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    when (selectedMetricView) {
                        PerformanceMetricView.TOTAL_BALANCE -> {
                            drawTotalBalanceCurve(
                                points = trajectoryPoints,
                                canvasWidth = canvasWidth,
                                canvasHeight = canvasHeight,
                                activeIndex = activeIndex
                            )
                        }
                        PerformanceMetricView.DIVIDEND_INCOME -> {
                            drawDividendIncomeCurve(
                                points = trajectoryPoints,
                                canvasWidth = canvasWidth,
                                canvasHeight = canvasHeight,
                                activeIndex = activeIndex
                            )
                        }
                        PerformanceMetricView.RISK_EXPOSURE -> {
                            drawRiskExposureCurve(
                                points = trajectoryPoints,
                                canvasWidth = canvasWidth,
                                canvasHeight = canvasHeight,
                                activeIndex = activeIndex
                            )
                        }
                    }

                    // 2. Vertical Scrubbing Crosshair Cursor
                    val scrubX = scrubFraction * canvasWidth
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(scrubX, 0f),
                        end = Offset(scrubX, canvasHeight),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
            }

            // Legend Strip
            ChartLegendRow(view = selectedMetricView)

            // Beginner Investor Friendly Explainer
            BeginnerExplainerCard(view = selectedMetricView)
        }
    }
}

@Composable
private fun ActiveScrubTooltip(
    view: PerformanceMetricView,
    point: TrajectoryPoint,
    horizonKey: String,
    monthlyContribution: Double
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TerminalSurfaceVariant)
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("scrub_tooltip_box")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TIMELINE: ${point.timeLabel.uppercase()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "[$horizonKey Horizon]",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldYieldBright
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                when (view) {
                    PerformanceMetricView.TOTAL_BALANCE -> {
                        Text(
                            text = String.format(Locale.US, "$%,.2f", point.totalBalance),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = EmeraldYieldBright
                        )
                    }
                    PerformanceMetricView.DIVIDEND_INCOME -> {
                        Text(
                            text = String.format(Locale.US, "$%,.2f/mo", point.monthlyDividend),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = CyanCore
                        )
                    }
                    PerformanceMetricView.RISK_EXPOSURE -> {
                        val ratio = if (point.growthValue > 0) point.defensiveValue / point.growthValue else 1.25
                        Text(
                            text = String.format(Locale.US, "%.2fx Defense", ratio),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = PurpleTech
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                when (view) {
                    PerformanceMetricView.TOTAL_BALANCE -> {
                        Text(
                            text = String.format(Locale.US, "Dividends: +$%,.0f", point.cumulativeDividends),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CyanCore
                        )
                        Text(
                            text = String.format(Locale.US, "Deposits: $%,.0f", point.cumulativeContributions),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = GoldAlert
                        )
                    }
                    PerformanceMetricView.DIVIDEND_INCOME -> {
                        Text(
                            text = String.format(Locale.US, "Total Paid: $%,.0f", point.cumulativeDividends),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = EmeraldYieldBright
                        )
                        Text(
                            text = String.format(Locale.US, "$%.0f/yr velocity", point.monthlyDividend * 12.0),
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                    PerformanceMetricView.RISK_EXPOSURE -> {
                        Text(
                            text = String.format(Locale.US, "Shields: $%,.0f", point.defensiveValue),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = EmeraldYieldBright
                        )
                        Text(
                            text = String.format(Locale.US, "Growth: $%,.0f", point.growthValue),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = PurpleTech
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartLegendRow(view: PerformanceMetricView) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (view) {
            PerformanceMetricView.TOTAL_BALANCE -> {
                LegendItem(color = EmeraldYieldBright, label = "Total Value")
                Spacer(modifier = Modifier.width(14.dp))
                LegendItem(color = CyanCore, label = "Dividends Reinvested")
                Spacer(modifier = Modifier.width(14.dp))
                LegendItem(color = GoldAlert, label = "Your Deposits")
            }
            PerformanceMetricView.DIVIDEND_INCOME -> {
                LegendItem(color = CyanCore, label = "Monthly Dividend Run-Rate")
                Spacer(modifier = Modifier.width(14.dp))
                LegendItem(color = EmeraldYieldBright, label = "Cumulative Cash Earned")
            }
            PerformanceMetricView.RISK_EXPOSURE -> {
                LegendItem(color = EmeraldYieldBright, label = "Defensive Shields (Stage 2, 4 & Cash)")
                Spacer(modifier = Modifier.width(14.dp))
                LegendItem(color = PurpleTech, label = "Yield & Beta Engines (Stage 1 & 3)")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun BeginnerExplainerCard(view: PerformanceMetricView) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TerminalSurfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
            .testTag("beginner_explainer_card")
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Beginner Tip",
                tint = CyanCore,
                modifier = Modifier
                    .size(16.dp)
                    .padding(top = 1.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "BEGINNER INVESTOR GUIDE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = CyanCore
                )
                Spacer(modifier = Modifier.height(2.dp))
                val explainer = when (view) {
                    PerformanceMetricView.TOTAL_BALANCE ->
                        "The 'Snowball Effect': Under DRIP=OFF, weekly dividend payouts are pooled and swept into unheld gap assets and high-yield leaders. Notice how the green curve steepens over time as dividends do more heavy lifting than your out-of-pocket deposits!"
                    PerformanceMetricView.DIVIDEND_INCOME ->
                        "Your Dividend Paycheck: Shows how much cash your portfolio generates each month without selling any shares. This stream provides living income or fuel to buy undervalued assets during market dips."
                    PerformanceMetricView.RISK_EXPOSURE ->
                        "The 1.25x Defense Shield: Týr Protocol requires at least $1.25 in defensive stability (Treasuries, bond proxies, index options) for every $1.00 in growth engines to cushion market crashes."
                }
                Text(
                    text = explainer,
                    fontSize = 11.sp,
                    color = TextPrimary,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

// Canvas Helpers for Curves

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTotalBalanceCurve(
    points: List<TrajectoryPoint>,
    canvasWidth: Float,
    canvasHeight: Float,
    activeIndex: Int
) {
    val maxVal = points.maxOfOrNull { it.totalBalance }?.coerceAtLeast(100.0) ?: 100.0
    val widthStep = if (points.size > 1) canvasWidth / (points.size - 1) else canvasWidth

    val balancePath = Path()
    val contribPath = Path()
    val pointsOffsets = mutableListOf<Offset>()

    points.forEachIndexed { i, p ->
        val x = i * widthStep
        val normBal = (p.totalBalance / maxVal).coerceIn(0.0, 1.0).toFloat()
        val normContrib = (p.cumulativeContributions / maxVal).coerceIn(0.0, 1.0).toFloat()

        val yBal = canvasHeight - (normBal * canvasHeight * 0.85f) - 8.dp.toPx()
        val yContrib = canvasHeight - (normContrib * canvasHeight * 0.85f) - 8.dp.toPx()

        pointsOffsets.add(Offset(x, yBal))

        if (i == 0) {
            balancePath.moveTo(x, yBal)
            contribPath.moveTo(x, yContrib)
        } else {
            val prev = pointsOffsets[i - 1]
            val cx = (prev.x + x) / 2f
            balancePath.cubicTo(cx, prev.y, cx, yBal, x, yBal)

            val prevContribY = canvasHeight - ((points[i - 1].cumulativeContributions / maxVal).coerceIn(0.0, 1.0).toFloat() * canvasHeight * 0.85f) - 8.dp.toPx()
            contribPath.cubicTo(cx, prevContribY, cx, yContrib, x, yContrib)
        }
    }

    // Gradient Fill under Balance Curve
    val fillPath = Path().apply {
        addPath(balancePath)
        lineTo(canvasWidth, canvasHeight)
        lineTo(0f, canvasHeight)
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

    // Draw Contribution Line (Gold)
    drawPath(
        path = contribPath,
        color = GoldAlert.copy(alpha = 0.7f),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )

    // Draw Balance Line (Emerald)
    drawPath(
        path = balancePath,
        color = EmeraldYieldBright,
        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
    )

    // Active Vertex Highlight
    val activeOffset = pointsOffsets.getOrNull(activeIndex) ?: pointsOffsets.lastOrNull()
    if (activeOffset != null) {
        drawCircle(
            color = EmeraldYieldBright,
            radius = 6.dp.toPx(),
            center = activeOffset
        )
        drawCircle(
            color = TerminalVoid,
            radius = 3.dp.toPx(),
            center = activeOffset
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDividendIncomeCurve(
    points: List<TrajectoryPoint>,
    canvasWidth: Float,
    canvasHeight: Float,
    activeIndex: Int
) {
    val maxIncome = points.maxOfOrNull { it.monthlyDividend }?.coerceAtLeast(10.0) ?: 10.0
    val widthStep = if (points.size > 1) canvasWidth / (points.size - 1) else canvasWidth

    val incomePath = Path()
    val pointsOffsets = mutableListOf<Offset>()

    points.forEachIndexed { i, p ->
        val x = i * widthStep
        val normIncome = (p.monthlyDividend / maxIncome).coerceIn(0.0, 1.0).toFloat()
        val y = canvasHeight - (normIncome * canvasHeight * 0.85f) - 8.dp.toPx()
        pointsOffsets.add(Offset(x, y))

        if (i == 0) {
            incomePath.moveTo(x, y)
        } else {
            val prev = pointsOffsets[i - 1]
            val cx = (prev.x + x) / 2f
            incomePath.cubicTo(cx, prev.y, cx, y, x, y)
        }
    }

    val fillPath = Path().apply {
        addPath(incomePath)
        lineTo(canvasWidth, canvasHeight)
        lineTo(0f, canvasHeight)
        close()
    }

    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                CyanCore.copy(alpha = 0.35f),
                EmeraldYieldPrimary.copy(alpha = 0.05f),
                Color.Transparent
            )
        ),
        style = Fill
    )

    drawPath(
        path = incomePath,
        color = CyanCore,
        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
    )

    val activeOffset = pointsOffsets.getOrNull(activeIndex) ?: pointsOffsets.lastOrNull()
    if (activeOffset != null) {
        drawCircle(
            color = CyanCore,
            radius = 6.dp.toPx(),
            center = activeOffset
        )
        drawCircle(
            color = TerminalVoid,
            radius = 3.dp.toPx(),
            center = activeOffset
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRiskExposureCurve(
    points: List<TrajectoryPoint>,
    canvasWidth: Float,
    canvasHeight: Float,
    activeIndex: Int
) {
    val maxVal = points.maxOfOrNull { it.totalBalance }?.coerceAtLeast(100.0) ?: 100.0
    val widthStep = if (points.size > 1) canvasWidth / (points.size - 1) else canvasWidth

    val defPath = Path()
    val growthPath = Path()
    val defOffsets = mutableListOf<Offset>()

    points.forEachIndexed { i, p ->
        val x = i * widthStep
        val normDef = (p.defensiveValue / maxVal).coerceIn(0.0, 1.0).toFloat()
        val normGrowth = (p.growthValue / maxVal).coerceIn(0.0, 1.0).toFloat()

        val yDef = canvasHeight - (normDef * canvasHeight * 0.85f) - 8.dp.toPx()
        val yGrowth = canvasHeight - (normGrowth * canvasHeight * 0.85f) - 8.dp.toPx()

        defOffsets.add(Offset(x, yDef))

        if (i == 0) {
            defPath.moveTo(x, yDef)
            growthPath.moveTo(x, yGrowth)
        } else {
            val prevDef = defOffsets[i - 1]
            val cx = (prevDef.x + x) / 2f
            defPath.cubicTo(cx, prevDef.y, cx, yDef, x, yDef)

            val prevGrowthY = canvasHeight - ((points[i - 1].growthValue / maxVal).coerceIn(0.0, 1.0).toFloat() * canvasHeight * 0.85f) - 8.dp.toPx()
            growthPath.cubicTo(cx, prevGrowthY, cx, yGrowth, x, yGrowth)
        }
    }

    drawPath(
        path = growthPath,
        color = PurpleTech,
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )

    drawPath(
        path = defPath,
        color = EmeraldYieldBright,
        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
    )

    val activeDef = defOffsets.getOrNull(activeIndex) ?: defOffsets.lastOrNull()
    if (activeDef != null) {
        drawCircle(
            color = EmeraldYieldBright,
            radius = 6.dp.toPx(),
            center = activeDef
        )
        drawCircle(
            color = TerminalVoid,
            radius = 3.dp.toPx(),
            center = activeDef
        )
    }
}
