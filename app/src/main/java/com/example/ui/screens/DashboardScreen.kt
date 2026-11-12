// File: DashboardScreen.kt
package com.example.ui.screens

import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketRiskBreakdown
import com.example.data.model.ReferralRegistry
import com.example.ui.PortfolioMetrics
import com.example.ui.StageBreakdown
import com.example.ui.components.DistributionScheduleCard
import com.example.ui.components.MarketRiskDiversitySection
import com.example.ui.components.PortfolioRiskRadarCard
import com.example.ui.components.StageBreakdownSection
import com.example.ui.components.YieldMetricsBar
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

@Composable
fun DashboardScreen(
    metrics: PortfolioMetrics,
    stageBreakdowns: List<StageBreakdown>,
    marketRiskBreakdowns: List<MarketRiskBreakdown> = emptyList(),
    standing: com.example.ai.PortfolioStanding? = null,
    selectedHorizon: String = "1Y",
    monthlyContribution: Double = 100.0,
    onSelectHorizon: (String) -> Unit = {},
    onConsultAdvisor: () -> Unit = {},
    dashboardConfig: com.example.data.model.DashboardModuleConfig = com.example.data.model.DashboardModuleConfig(),
    onUpdateDashboardConfig: (com.example.data.model.DashboardModuleConfig) -> Unit = {},
    isRefreshingPrices: Boolean,
    priceRefreshStatus: String,
    unreadAlertsCount: Int,
    onRefreshPrices: () -> Unit,
    onOpenAlerts: () -> Unit,
    onOpenTutorial: () -> Unit,
    onNavigateToSweep: () -> Unit,
    onNavigateToReferrals: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onOpenPresetModal: () -> Unit,
    onDepositCash: (Double, String) -> Unit,
    onLogDividend: (String, Double) -> Unit,
    onSectorGapClick: (com.example.data.model.RiskSector) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDepositDialog by remember { mutableStateOf(false) }
    var showDividendDialog by remember { mutableStateOf(false) }
    var showCustomizeDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "spin_anim")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing)),
        label = "angle_anim"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalVoid)
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            YieldMetricsBar(metrics = metrics)
        }

        // Live Market Refresh & Alert Status Strip
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live Price Status Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TerminalSurface)
                        .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
                        .clickable(onClick = onRefreshPrices)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("refresh_prices_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Refresh Quotes",
                        tint = if (isRefreshingPrices) CyanCore else TextSecondary,
                        modifier = Modifier
                            .size(14.dp)
                            .then(if (isRefreshingPrices) Modifier.rotate(angle) else Modifier)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRefreshingPrices) "Fetching Market Prices..." else priceRefreshStatus,
                        fontSize = 11.sp,
                        color = if (isRefreshingPrices) CyanCore else TextSecondary,
                        maxLines = 1
                    )
                }

                // Alerts & Tutorial Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Alert Icon Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (unreadAlertsCount > 0) GoldAlert.copy(alpha = 0.2f) else TerminalSurface)
                            .border(1.dp, if (unreadAlertsCount > 0) GoldAlert else TerminalCardBorder, RoundedCornerShape(8.dp))
                            .clickable(onClick = onOpenAlerts)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("open_alerts_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Alerts",
                                tint = if (unreadAlertsCount > 0) GoldAlert else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            if (unreadAlertsCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$unreadAlertsCount",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAlert
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Tutorial Help Button
                    IconButton(onClick = onOpenTutorial, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Tutorial", tint = CyanCore, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Quick Command Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    title = "Run Sweep",
                    icon = Icons.Default.FlashOn,
                    color = EmeraldYieldPrimary,
                    modifier = Modifier.weight(1f),
                    testTag = "dashboard_run_sweep_button",
                    onClick = onNavigateToSweep
                )

                QuickActionButton(
                    title = "Deposit Cash",
                    icon = Icons.Default.Add,
                    color = CyanCore,
                    modifier = Modifier.weight(1f),
                    testTag = "dashboard_deposit_cash_button",
                    onClick = { showDepositDialog = true }
                )

                QuickActionButton(
                    title = "Log Dividend",
                    icon = Icons.Default.Payments,
                    color = GoldAlert,
                    modifier = Modifier.weight(1f),
                    testTag = "dashboard_log_dividend_button",
                    onClick = { showDividendDialog = true }
                )

                QuickActionButton(
                    title = "Customize",
                    icon = androidx.compose.material.icons.Icons.Default.Tune,
                    color = PurpleTech,
                    modifier = Modifier.weight(1f),
                    testTag = "dashboard_customize_button",
                    onClick = { showCustomizeDialog = true }
                )

                QuickActionButton(
                    title = "Setup",
                    icon = Icons.Default.Refresh,
                    color = TextSecondary,
                    modifier = Modifier.weight(0.8f),
                    testTag = "dashboard_preset_button",
                    onClick = onOpenPresetModal
                )
            }
        }

        // Týr Protocol Financial Expert Card (Live Sustainability & Tax Auditor)
        if (dashboardConfig.showTyrFinancialExpertCard && standing != null) {
            item {
                com.example.ui.components.TyrFinancialExpertCard(
                    standing = standing,
                    onConsultAdvisor = onConsultAdvisor
                )
            }
        }

        // Interactive Trajectory Performance Chart (3 Views, 6 Horizons, Touch Scrubbing)
        if (dashboardConfig.showPerformanceChart) {
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
        }

        // Portfolio Risk Radar Card (Native Compose Spider Web)
        if (dashboardConfig.showRiskRadar) {
            item {
                PortfolioRiskRadarCard(
                    sectors = marketRiskBreakdowns,
                    onSectorGapClick = onSectorGapClick
                )
            }
        }

        // 24-Week Distribution Allocation & Gap-Filling Schedule Tracker (Configurable)
        if (dashboardConfig.showScheduleTracker) {
            item {
                DistributionScheduleCard()
            }
        }

        // 4-Stage Allocation Section
        if (dashboardConfig.showStageBreakdowns) {
            item {
                StageBreakdownSection(breakdowns = stageBreakdowns)
            }
        }

        // Market & Risk Diversity Section (Configurable)
        if (dashboardConfig.showMarketDiversity) {
            item {
                MarketRiskDiversitySection(sectors = marketRiskBreakdowns)
            }
        }

        // Featured Brokerage Welcome Bonus Card (Configurable)
        if (dashboardConfig.showReferralCard) {
            item {
                val primaryPartner = ReferralRegistry.REFERRAL_CONFIG.firstOrNull { it.isPrimaryPartner }
                    ?: ReferralRegistry.REFERRAL_CONFIG.first()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TerminalSurfaceVariant)
                    .border(1.dp, EmeraldYieldPrimary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, primaryPartner.directUrl.toUri())
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            onNavigateToReferrals()
                        }
                    }
                    .padding(14.dp)
                    .testTag("dashboard_referral_promo_card")
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
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(EmeraldYieldPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = null,
                                tint = EmeraldYieldBright,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = primaryPartner.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = primaryPartner.tag,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldYieldPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = primaryPartner.bonusDescription,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 2
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open Link",
                        tint = EmeraldYieldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Manual Deposit Dialog
    if (showDepositDialog) {
        var depositAmount by remember { mutableStateOf("") }
        var depositNote by remember { mutableStateOf("Bank Transfer") }

        AlertDialog(
            onDismissRequest = { showDepositDialog = false },
            title = { Text("Deposit Cash to Settled Buying Power", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = depositAmount,
                        onValueChange = { depositAmount = it },
                        label = { Text("Amount ($)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanCore,
                            unfocusedBorderColor = TerminalCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = depositNote,
                        onValueChange = { depositNote = it },
                        label = { Text("Source Note") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanCore,
                            unfocusedBorderColor = TerminalCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = depositAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0.0) {
                            onDepositCash(amt, depositNote)
                            showDepositDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanCore, contentColor = TerminalVoid)
                ) {
                    Text("Add Cash")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDepositDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = TerminalSurface
        )
    }

    // Log Dividend Dialog
    if (showDividendDialog) {
        var dividendTicker by remember { mutableStateOf("") }
        var dividendAmount by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDividendDialog = false },
            title = { Text("Log Dividend Payout (DRIP=OFF)", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Logs cash dividend directly into your settled buying power without automatic reinvestment.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = dividendTicker,
                        onValueChange = { dividendTicker = it.uppercase() },
                        label = { Text("Ticker Symbol (e.g. JEPQ, SCHD)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAlert,
                            unfocusedBorderColor = TerminalCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dividendAmount,
                        onValueChange = { dividendAmount = it },
                        label = { Text("Net Payout Amount ($)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAlert,
                            unfocusedBorderColor = TerminalCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = dividendAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0.0 && dividendTicker.isNotBlank()) {
                            onLogDividend(dividendTicker, amt)
                            showDividendDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAlert, contentColor = TerminalVoid)
                ) {
                    Text("Log Payout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDividendDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = TerminalSurface
        )
    }

    // Customize Dashboard Modules Dialog
    if (showCustomizeDialog) {
        AlertDialog(
            onDismissRequest = { showCustomizeDialog = false },
            title = { Text("Customize Dashboard", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Toggle modules on or off to declutter your main dashboard. Offloaded modules remain accessible via their dedicated sub-pages.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )

                    ModuleToggleRow(
                        title = "Týr Financial Expert Card",
                        subtitle = "Live audit scores & tyrprotocol.pro",
                        checked = dashboardConfig.showTyrFinancialExpertCard,
                        onCheckedChange = { onUpdateDashboardConfig(dashboardConfig.copy(showTyrFinancialExpertCard = it)) }
                    )

                    ModuleToggleRow(
                        title = "Interactive Performance Chart",
                        subtitle = "60fps canvas, 3 views & 6 horizons",
                        checked = dashboardConfig.showPerformanceChart,
                        onCheckedChange = { onUpdateDashboardConfig(dashboardConfig.copy(showPerformanceChart = it)) }
                    )

                    ModuleToggleRow(
                        title = "Portfolio Risk Radar",
                        subtitle = "60fps Compose Spider Web",
                        checked = dashboardConfig.showRiskRadar,
                        onCheckedChange = { onUpdateDashboardConfig(dashboardConfig.copy(showRiskRadar = it)) }
                    )

                    ModuleToggleRow(
                        title = "24-Week Schedule Tracker",
                        subtitle = "Also in dedicated Schedule tab",
                        checked = dashboardConfig.showScheduleTracker,
                        onCheckedChange = { onUpdateDashboardConfig(dashboardConfig.copy(showScheduleTracker = it)) }
                    )

                    ModuleToggleRow(
                        title = "4-Stage Capital Breakdown",
                        subtitle = "Core stages allocation cards",
                        checked = dashboardConfig.showStageBreakdowns,
                        onCheckedChange = { onUpdateDashboardConfig(dashboardConfig.copy(showStageBreakdowns = it)) }
                    )

                    ModuleToggleRow(
                        title = "Market & Risk Diversity Grid",
                        subtitle = "All 9 vectors & risk caps",
                        checked = dashboardConfig.showMarketDiversity,
                        onCheckedChange = { onUpdateDashboardConfig(dashboardConfig.copy(showMarketDiversity = it)) }
                    )

                    ModuleToggleRow(
                        title = "Brokerage Partner Bonus",
                        subtitle = "Account opening bonus banners",
                        checked = dashboardConfig.showReferralCard,
                        onCheckedChange = { onUpdateDashboardConfig(dashboardConfig.copy(showReferralCard = it)) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCustomizeDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanCore, contentColor = TerminalVoid)
                ) {
                    Text("Done")
                }
            },
            containerColor = TerminalSurface
        )
    }
}

@Composable
private fun ModuleToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalSurfaceVariant)
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = subtitle, fontSize = 10.sp, color = TextSecondary)
        }
        androidx.compose.material3.Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedThumbColor = CyanCore,
                checkedTrackColor = CyanCore.copy(alpha = 0.3f),
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = TerminalSurface
            )
        )
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    testTag: String = "",
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(TerminalSurface)
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1
            )
        }
    }
}
