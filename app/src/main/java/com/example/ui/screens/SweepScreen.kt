// File: SweepScreen.kt
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StageRegistry
import com.example.data.model.SweepRecord
import com.example.engine.SweepOptimizationResult
import com.example.engine.SweepTarget
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SweepScreen(
    settledBuyingPower: Double,
    currentSweepResult: SweepOptimizationResult?,
    sweepHistory: List<SweepRecord>,
    canUndoLastSweep: Boolean = false,
    lastSweepSummary: String? = null,
    onUndoLastSweep: () -> Unit = {},
    onCalculateSweep: (Double) -> Unit,
    onExecuteSweep: (SweepOptimizationResult) -> Unit,
    modifier: Modifier = Modifier
) {
    var cashToDeployInput by remember {
        mutableStateOf(if (settledBuyingPower > 0.0) String.format(Locale.US, "%.2f", settledBuyingPower) else "100.00")
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalVoid)
            .padding(horizontal = 16.dp)
            .testTag("sweep_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(TerminalSurfaceVariant)
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(EmeraldYieldPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = EmeraldYieldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Multi-Vector Redistribution Sweep",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Concentrates cash into 1 to 3 optimal funds per pass",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Available Settled Buying Power:",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = String.format(Locale.US, "$%,.2f", settledBuyingPower),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = EmeraldYieldBright
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = cashToDeployInput,
                        onValueChange = { cashToDeployInput = it },
                        label = { Text("Cash Capital to Deploy ($)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldYieldPrimary,
                            unfocusedBorderColor = TerminalCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = EmeraldYieldPrimary,
                            unfocusedLabelColor = TextSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cash_to_deploy_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // What-If Simulation Slider
                    val currentVal = cashToDeployInput.toDoubleOrNull() ?: 0.0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "What-If Payout Simulation:",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = String.format(Locale.US, "$%.0f", currentVal),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanCore,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    androidx.compose.material3.Slider(
                        value = currentVal.toFloat().coerceIn(0f, 1000f),
                        onValueChange = {
                            cashToDeployInput = String.format(Locale.US, "%.2f", it)
                            onCalculateSweep(it.toDouble())
                        },
                        valueRange = 0f..1000f,
                        steps = 19,
                        colors = androidx.compose.material3.SliderDefaults.colors(
                            thumbColor = EmeraldYieldPrimary,
                            activeTrackColor = EmeraldYieldPrimary,
                            inactiveTrackColor = TerminalSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 1-Tap Quick-Deploy Cash Chips (QoL)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickPresets = listOf(
                            "$25" to 25.0,
                            "$50" to 50.0,
                            "$100" to 100.0,
                            "50% Pool" to (settledBuyingPower * 0.5).coerceAtLeast(10.0),
                            "100% Pool" to settledBuyingPower.coerceAtLeast(10.0)
                        )
                        quickPresets.forEach { (label, amountVal) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(TerminalSurface)
                                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        cashToDeployInput = String.format(Locale.US, "%.2f", amountVal)
                                        onCalculateSweep(amountVal)
                                    }
                                    .padding(vertical = 6.dp)
                                    .testTag("quick_sweep_chip_${label.replace(" ", "_").lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanCore,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val amt = cashToDeployInput.toDoubleOrNull() ?: 0.0
                            if (amt > 0.0) {
                                onCalculateSweep(amt)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldYieldPrimary,
                            contentColor = TerminalVoid
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("calculate_sweep_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Optimize Concentrated Allocation",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // 1-Tap Rollback Banner if a Sweep was Executed in this Session
        if (canUndoLastSweep && lastSweepSummary != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoldAlert.copy(alpha = 0.12f))
                        .border(1.dp, GoldAlert.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                        .testTag("sweep_rollback_banner")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ROLLBACK CHECKPOINT ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAlert
                            )
                            Text(
                                text = lastSweepSummary,
                                fontSize = 11.sp,
                                color = TextPrimary,
                                maxLines = 2
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onUndoLastSweep,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAlert,
                                contentColor = TerminalVoid
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("undo_last_sweep_button")
                        ) {
                            Text("Undo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Active Sweep Recommendation Results
        if (currentSweepResult != null && currentSweepResult.targets.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECOMMENDED CONCENTRATED TARGETS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = TextSecondary
                        )

                        Text(
                            text = String.format(Locale.US, "Blended Yield: %.2f%%", currentSweepResult.blendedYieldAchieved),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = EmeraldYieldBright
                        )
                    }

                    // Plain-English Summary Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EmeraldYieldPrimary.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                            .border(1.dp, EmeraldYieldPrimary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = EmeraldYieldPrimary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentSweepResult.plainEnglishSummary,
                                fontSize = 12.sp,
                                color = TextPrimary,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    // Multi-Vector Market Diversity Shield Banner
                    if (currentSweepResult.coveredRiskSectors.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyanCore.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                .border(1.dp, CyanCore.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(CyanCore.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = CyanCore,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "MULTI-VECTOR & MULTI-TIER SHIELD",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                        color = CyanCore
                                    )
                                    val stageListStr = if (currentSweepResult.coveredStages.isNotEmpty()) {
                                        " across Stage ${currentSweepResult.coveredStages.joinToString(", ")}"
                                    } else ""
                                    Text(
                                        text = "Covering ${currentSweepResult.coveredRiskSectors.joinToString(" + ")}$stageListStr to eliminate single-sector failure.",
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    // Target Cards
                    currentSweepResult.targets.forEachIndexed { index, target ->
                        SweepTargetCard(target = target, rank = index + 1)
                    }

                    // Execution CTA
                    Button(
                        onClick = { onExecuteSweep(currentSweepResult) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanCore,
                            contentColor = TerminalVoid
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("execute_sweep_order_button")
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Confirm & Execute Sweep Orders",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Sweep Audit History
        if (sweepHistory.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PREVIOUS SWEEPS AUDIT LOG",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextSecondary
                    )
                }
            }

            items(sweepHistory) { record ->
                val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date(record.timestamp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(TerminalSurface)
                        .border(1.dp, TerminalCardBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = String.format(Locale.US, "$%,.2f Deployed", record.totalCashDeployed),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldYieldBright,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = dateStr,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = record.targetsSummary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = record.rationale,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun SweepTargetCard(target: SweepTarget, rank: Int) {
    val stageMeta = StageRegistry.getStage(target.stage)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TerminalSurface)
            .border(1.dp, stageMeta.accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(14.dp)
            .testTag("sweep_target_card_${target.ticker}")
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
                            .background(stageMeta.accentColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = target.ticker,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = stageMeta.accentColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Stage ${target.stage}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    if (target.riskSectorName.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(TerminalSurfaceVariant, RoundedCornerShape(4.dp))
                                .border(1.dp, TerminalCardBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = target.riskSectorName,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CyanCore
                            )
                        }
                    }
                    if (target.isYieldLeader) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(EmeraldYieldPrimary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .border(1.dp, EmeraldYieldPrimary.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "TOP YIELD",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldYieldBright
                            )
                        }
                    }
                    if (target.isGapFiller) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(GoldAlert.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .border(1.dp, GoldAlert.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "GAP FILLER",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAlert
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format(Locale.US, "$%,.2f", target.allocatedCash),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    Text(
                        text = String.format(Locale.US, "%.3f shares @ $%.2f", target.shareCount, target.currentPrice),
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = target.name,
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format(Locale.US, "Yield: %.1f%%", target.annualYieldPercent),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldYieldBright
                )

                Text(
                    text = String.format(Locale.US, "+$%.2f/mo new income", target.addedMonthlyIncome),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = CyanCore
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = target.strategicRationale,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}
