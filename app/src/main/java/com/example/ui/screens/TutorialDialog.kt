// File: TutorialDialog.kt
package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

data class TutorialStep(
    val stepNumber: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val highlightColor: androidx.compose.ui.graphics.Color
)

@Composable
fun TutorialDialog(
    onDismiss: () -> Unit
) {
    val steps = listOf(
        TutorialStep(
            stepNumber = 1,
            title = "The Closed-Loop Yield Strategy",
            subtitle = "Hardcoded DRIP = OFF",
            description = "Traditional apps blindly drip dividend payouts back into whichever fund paid them out, regardless of price or balance. Here, DRIP is permanently OFF: all cash payouts accumulate into your Settled Buying Power pool so you can redeploy capital with intention.",
            icon = Icons.Default.Autorenew,
            highlightColor = EmeraldYieldPrimary
        ),
        TutorialStep(
            stepNumber = 2,
            title = "Dual Starting Architectures",
            subtitle = "Fresh Start vs Existing Portfolio",
            description = "Start Fresh ($0.00 Base) lets you begin cleanly with whatever cash you have ready ($25 or $50) without dilution. Existing Portfolio lets you enter or paste your own current holdings, shares, and cost basis.",
            icon = Icons.Default.PieChart,
            highlightColor = CyanCore
        ),
        TutorialStep(
            stepNumber = 3,
            title = "Plain-English 4 Stages",
            subtitle = "Every Fund Has a Purpose",
            description = "Stage 1: Heavy Cash Flow Engines (Covered calls like JEPQ)\nStage 2: Core Market Stability (Index anchors like SCHD, VOO)\nStage 3: Growth Accelerators (Tech momentum like QQQM)\nStage 4: Tax-Shielded Reserves (Treasury ballast like SGOV)",
            icon = Icons.Default.Security,
            highlightColor = GoldAlert
        ),
        TutorialStep(
            stepNumber = 4,
            title = "Defense-to-Risk Density Ratio",
            subtitle = "Target >= 1.25x 'Better-Than-Good'",
            description = "We divide your defensive ballast (Stage 2 + Stage 4 + Cash) by your high-beta risk (Stage 1 + Stage 3). When your ratio stays >= 1.25x, your capital is shielded from steep market drawdowns while still harvesting double-digit yield.",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            highlightColor = EmeraldYieldBright
        ),
        TutorialStep(
            stepNumber = 5,
            title = "Multi-Vector Sweeps",
            subtitle = "Concentrated Allocation (1-3 Targets)",
            description = "Instead of 'peanut-butter' spreading cash across 15 funds at once, each sweep concentrates into the top 1 to 3 positions trading at the biggest discount or offering the best yield capture. One tap executes the sweep straight to your ledger.",
            icon = Icons.Default.FlashOn,
            highlightColor = PurpleTech
        )
    )

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val step = steps[currentStepIndex]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, TerminalCardBorder, RoundedCornerShape(20.dp)),
            color = TerminalVoid
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .testTag("tutorial_dialog")
            ) {
                // Top Header & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(step.highlightColor.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${step.stepNumber}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = step.highlightColor
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Tutorial (Step ${step.stepNumber} of ${steps.size})",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card Body
                AnimatedContent(targetState = step, label = "tutorial_anim") { current ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(TerminalSurface)
                            .border(1.dp, current.highlightColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = current.icon,
                                    contentDescription = null,
                                    tint = current.highlightColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = current.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = current.subtitle,
                                        fontSize = 11.sp,
                                        color = current.highlightColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = current.description,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Navigation Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStepIndex > 0) {
                        TextButton(
                            onClick = { currentStepIndex-- }
                        ) {
                            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back", color = TextSecondary)
                        }
                    } else {
                        TextButton(onClick = onDismiss) {
                            Text("Skip", color = TextSecondary)
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStepIndex < steps.size - 1) {
                                currentStepIndex++
                            } else {
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = step.highlightColor,
                            contentColor = TerminalVoid
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (currentStepIndex < steps.size - 1) "Next Step" else "Got It!",
                            fontWeight = FontWeight.Bold
                        )
                        if (currentStepIndex < steps.size - 1) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
