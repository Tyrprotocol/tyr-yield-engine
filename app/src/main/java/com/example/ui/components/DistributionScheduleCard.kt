// File: DistributionScheduleCard.kt
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DistributionScheduleRegistry
import com.example.data.model.DistributionScheduleWeek
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

/**
 * Interactive 24-Week Distribution Allocation & Gap-Filling Schedule Tracker
 * Implements the user's strategy schedule (Nov 9, 2026 – Apr 23, 2027) with $0.00 out-of-pocket deposits,
 * tracking primary orders, overflow sweeps, and cumulative income run rates.
 */
@Composable
fun DistributionScheduleCard(
    onConfirmWeekSweep: (ticker: String, amount: Double) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val weeks = DistributionScheduleRegistry.SCHEDULE_24_WEEKS
    var selectedWeekIndex by remember { mutableIntStateOf(0) }
    var executedWeeksSet by remember { mutableStateOf(setOf<Int>()) }

    val currentWeek = weeks.getOrElse(selectedWeekIndex) { weeks.first() }
    val isCurrentExecuted = executedWeeksSet.contains(currentWeek.weekNumber)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TerminalSurface)
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("distribution_schedule_card")
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
                            .background(EmeraldYieldPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "24-Week Schedule",
                            tint = EmeraldYieldBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "24-WEEK GAP-FILLING SCHEDULE",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Closed-Loop Compounding ($0.00 Out-of-Pocket)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Completed Tally
                Box(
                    modifier = Modifier
                        .background(TerminalSurfaceVariant, RoundedCornerShape(6.dp))
                        .border(1.dp, TerminalCardBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${executedWeeksSet.size}/24 Swept",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (executedWeeksSet.size == 24) EmeraldYieldBright else CyanCore
                    )
                }
            }

            // Week Selector Scroller
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(weeks) { index, week ->
                    val isSelected = index == selectedWeekIndex
                    val isDone = executedWeeksSet.contains(week.weekNumber)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isSelected -> CyanCore.copy(alpha = 0.25f)
                                    isDone -> EmeraldYieldPrimary.copy(alpha = 0.2f)
                                    else -> TerminalSurfaceVariant
                                }
                            )
                            .border(
                                1.dp,
                                when {
                                    isSelected -> CyanCore
                                    isDone -> EmeraldYieldPrimary
                                    else -> TerminalCardBorder
                                },
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedWeekIndex = index }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isDone) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldYieldBright,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "Wk ${week.weekNumber}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CyanCore else if (isDone) EmeraldYieldBright else TextPrimary
                            )
                        }
                    }
                }
            }

            // Selected Week Detail Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TerminalSurfaceVariant)
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Week Title & Date Range
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "WEEK ${currentWeek.weekNumber} ALLOCATION PLAN",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = CyanCore
                            )
                            Text(
                                text = currentWeek.dateWindow,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        // Cash Pool Available
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Cash Pool: $${String.format(Locale.US, "%.2f", currentWeek.incomingCashPool)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = EmeraldYieldBright
                            )
                            Text(
                                text = "From settled DRIP off",
                                fontSize = 9.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Primary Order Action (75-85% of cash)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TerminalSurface, RoundedCornerShape(8.dp))
                            .border(1.dp, CyanCore.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "PRIMARY EX-DIV ALLOCATION (75-85%)",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = CyanCore
                                )
                                Text(
                                    text = "10:30 AM EST Dip",
                                    fontSize = 9.sp,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentWeek.primaryOrderAction,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    // Overflow Gap Sweep Order (15-25% of cash)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TerminalSurface, RoundedCornerShape(8.dp))
                            .border(1.dp, GoldAlert.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "OVERFLOW GAP SWEEP (15-25%)",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = GoldAlert
                                )
                                Text(
                                    text = "Complementary Index",
                                    fontSize = 9.sp,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentWeek.overflowSweepAction,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    // Risk Categories & Schedule Gaps Filled
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Gaps Covered:",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = currentWeek.riskCategoriesFilled,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            currentWeek.weekdaySlotsFilled.forEach { slot ->
                                Box(
                                    modifier = Modifier
                                        .background(PurpleTech.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = slot,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PurpleTech
                                    )
                                }
                            }
                        }
                    }

                    // Run Rate Telemetry Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format(Locale.US, "+$%.4f/wk Added", currentWeek.weeklyRunRateAdded),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldYieldBright
                        )
                        Text(
                            text = String.format(Locale.US, "New Total: $%.2f/wk ($%,.2f/yr)", currentWeek.newWeeklyTotalRunRate, currentWeek.newAnnualRunRate),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CyanCore
                        )
                    }

                    // Stepper Navigation & Mark Executed Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            IconButton(
                                onClick = {
                                    if (selectedWeekIndex > 0) selectedWeekIndex--
                                },
                                enabled = selectedWeekIndex > 0,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Week", tint = if (selectedWeekIndex > 0) CyanCore else TextSecondary.copy(alpha = 0.3f))
                            }

                            IconButton(
                                onClick = {
                                    if (selectedWeekIndex < weeks.size - 1) selectedWeekIndex++
                                },
                                enabled = selectedWeekIndex < weeks.size - 1,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next Week", tint = if (selectedWeekIndex < weeks.size - 1) CyanCore else TextSecondary.copy(alpha = 0.3f))
                            }
                        }

                        Button(
                            onClick = {
                                executedWeeksSet = if (isCurrentExecuted) {
                                    executedWeeksSet - currentWeek.weekNumber
                                } else {
                                    // Parse primary ticker symbol e.g. "HIYY" from action
                                    val tickerSymbol = currentWeek.primaryOrderAction.split(" ").firstOrNull { it.all { char -> char.isLetter() } } ?: "HIYY"
                                    onConfirmWeekSweep(tickerSymbol, currentWeek.incomingCashPool)
                                    executedWeeksSet + currentWeek.weekNumber
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCurrentExecuted) EmeraldYieldPrimary.copy(alpha = 0.2f) else EmeraldYieldPrimary,
                                contentColor = if (isCurrentExecuted) EmeraldYieldBright else TerminalVoid
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isCurrentExecuted) "Swept ✓" else "Confirm & Sweep ($" + String.format(Locale.US, "%.2f", currentWeek.incomingCashPool) + ")",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
