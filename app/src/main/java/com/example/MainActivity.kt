// File: MainActivity.kt
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StackedLineChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.db.AppDatabase
import com.example.data.repository.PortfolioRepository
import com.example.ui.PortfolioViewModel
import com.example.ui.screens.AlertsDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GeminiChatSheet
import com.example.ui.screens.HoldingsScreen
import com.example.ui.screens.OnboardingDialog
import com.example.ui.screens.ProjectionScreen
import com.example.ui.screens.ReferralScreen
import com.example.ui.screens.SettingsAndManualScreen
import com.example.ui.screens.SweepScreen
import com.example.ui.screens.TutorialDialog
import com.example.ui.theme.CyanCore
import com.example.ui.theme.EmeraldYieldBright
import com.example.ui.theme.EmeraldYieldPrimary
import com.example.ui.theme.GoldAlert
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalVoid
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YieldEngineTheme
import com.example.util.configureAppCheck

enum class AppNavDestination(val label: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, "nav_dashboard"),
    SWEEP("Sweep", Icons.Default.FlashOn, "nav_sweep"),
    SCHEDULE("Schedule", Icons.Default.DateRange, "nav_schedule"),
    HOLDINGS("Holdings", Icons.Default.PieChart, "nav_holdings"),
    OUTLOOK("Outlook", Icons.Default.StackedLineChart, "nav_outlook"),
    COPILOT("AI Advisor", Icons.Default.Psychology, "nav_copilot"),
    REFERRALS("Partners", Icons.Default.CardGiftcard, "nav_referrals"),
    SETTINGS("Manual & Settings", Icons.Default.Settings, "nav_settings")
}

class MainActivity : ComponentActivity() {

    private val viewModel: PortfolioViewModel by viewModels {
        val db = AppDatabase.getDatabase(applicationContext)
        val repo = PortfolioRepository(db.holdingDao(), db.cashDao(), db.alertDao())
        viewModelFactory {
            initializer {
                PortfolioViewModel(repo)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mandatory App Check configuration before UI or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            YieldEngineTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: PortfolioViewModel) {
    var currentDestination by remember { mutableStateOf(AppNavDestination.DASHBOARD) }
    var showPresetPrompt by remember { mutableStateOf(false) }
    var showTutorial by remember { mutableStateOf(false) }
    var showAlerts by remember { mutableStateOf(false) }

    val metrics by viewModel.metrics.collectAsStateWithLifecycle()
    val stageBreakdowns by viewModel.stageBreakdowns.collectAsStateWithLifecycle()
    val marketRiskBreakdowns by viewModel.marketRiskBreakdowns.collectAsStateWithLifecycle()
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()
    val settledBuyingPower by viewModel.settledBuyingPower.collectAsStateWithLifecycle()
    val currentSweepResult by viewModel.currentSweepResult.collectAsStateWithLifecycle()
    val sweepHistory by viewModel.sweepRecords.collectAsStateWithLifecycle()
    val canUndoLastSweep by viewModel.canUndoLastSweep.collectAsStateWithLifecycle()
    val lastSweepSummary by viewModel.lastSweepSummary.collectAsStateWithLifecycle()
    val projections by viewModel.projections.collectAsStateWithLifecycle()
    val selectedHorizon by viewModel.selectedHorizon.collectAsStateWithLifecycle()
    val monthlyContribution by viewModel.monthlyContributionInput.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
    val showOnboardingDialog by viewModel.showOnboardingDialog.collectAsStateWithLifecycle()

    val portfolioStanding by viewModel.portfolioStanding.collectAsStateWithLifecycle()
    val alertRules by viewModel.alertRules.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val isRefreshingPrices by viewModel.isRefreshingPrices.collectAsStateWithLifecycle()
    val priceRefreshStatus by viewModel.priceRefreshStatus.collectAsStateWithLifecycle()

    var dashboardConfig by remember { mutableStateOf(com.example.data.model.DashboardModuleConfig()) }
    var prefillSectorForHoldings by remember { mutableStateOf<com.example.data.model.RiskSector?>(null) }

    val unreadAlertsCount = notifications.count { !it.isRead }

    BackHandler(enabled = currentDestination != AppNavDestination.DASHBOARD) {
        currentDestination = AppNavDestination.DASHBOARD
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalVoid),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TerminalSurface)
                    .statusBarsPadding()
                    .border(0.dp, TerminalCardBorder)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
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
                                text = "YIELD ENGINE",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Closed-Loop Portfolio Tracker",
                                fontSize = 10.sp,
                                color = EmeraldYieldBright
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Alerts button
                        IconButton(
                            onClick = { showAlerts = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Alerts",
                                tint = if (unreadAlertsCount > 0) GoldAlert else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Tutorial button
                        IconButton(
                            onClick = { showTutorial = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = "Tutorial",
                                tint = CyanCore,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Referral Bonus Hub
                        IconButton(
                            onClick = { currentDestination = AppNavDestination.REFERRALS },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = "Bonus Hub",
                                tint = if (currentDestination == AppNavDestination.REFERRALS) EmeraldYieldPrimary else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Settings & User Manual Hub
                        IconButton(
                            onClick = { currentDestination = AppNavDestination.SETTINGS },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("top_bar_settings_manual_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Manual & Settings",
                                tint = if (currentDestination == AppNavDestination.SETTINGS) CyanCore else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Setup / Preset
                        IconButton(
                            onClick = { showPresetPrompt = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Setup Portfolio",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = TerminalSurface,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalCardBorder)
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar")
            ) {
                val navItems = listOf(
                    AppNavDestination.DASHBOARD,
                    AppNavDestination.SWEEP,
                    AppNavDestination.SCHEDULE,
                    AppNavDestination.HOLDINGS,
                    AppNavDestination.OUTLOOK,
                    AppNavDestination.COPILOT
                )

                navItems.forEach { item ->
                    val selected = currentDestination == item
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentDestination = item },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 9.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TerminalVoid,
                            selectedTextColor = EmeraldYieldBright,
                            indicatorColor = EmeraldYieldPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag(item.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = TerminalVoid
        ) {
            when (currentDestination) {
                AppNavDestination.DASHBOARD -> {
                    DashboardScreen(
                        metrics = metrics,
                        stageBreakdowns = stageBreakdowns,
                        marketRiskBreakdowns = marketRiskBreakdowns,
                        standing = portfolioStanding,
                        selectedHorizon = selectedHorizon,
                        monthlyContribution = monthlyContribution,
                        onSelectHorizon = { h -> viewModel.setSelectedHorizon(h) },
                        onConsultAdvisor = { currentDestination = AppNavDestination.COPILOT },
                        dashboardConfig = dashboardConfig,
                        onUpdateDashboardConfig = { dashboardConfig = it },
                        isRefreshingPrices = isRefreshingPrices,
                        priceRefreshStatus = priceRefreshStatus,
                        unreadAlertsCount = unreadAlertsCount,
                        onRefreshPrices = { viewModel.refreshMarketPrices() },
                        onOpenAlerts = { showAlerts = true },
                        onOpenTutorial = { showTutorial = true },
                        onNavigateToSweep = { currentDestination = AppNavDestination.SWEEP },
                        onNavigateToReferrals = { currentDestination = AppNavDestination.REFERRALS },
                        onNavigateToSchedule = { currentDestination = AppNavDestination.SCHEDULE },
                        onOpenPresetModal = { showPresetPrompt = true },
                        onDepositCash = { amt, note -> viewModel.addManualDeposit(amt, note) },
                        onLogDividend = { ticker, amt -> viewModel.logDividendPayout(ticker, amt) },
                        onSectorGapClick = { sector ->
                            prefillSectorForHoldings = sector
                            currentDestination = AppNavDestination.HOLDINGS
                        }
                    )
                }
                AppNavDestination.SWEEP -> {
                    SweepScreen(
                        settledBuyingPower = settledBuyingPower,
                        currentSweepResult = currentSweepResult,
                        sweepHistory = sweepHistory,
                        canUndoLastSweep = canUndoLastSweep,
                        lastSweepSummary = lastSweepSummary,
                        onUndoLastSweep = { viewModel.undoLastSweep() },
                        onCalculateSweep = { cash -> viewModel.calculateSweep(cash) },
                        onExecuteSweep = { result -> viewModel.executeSweep(result) }
                    )
                }
                AppNavDestination.SCHEDULE -> {
                    com.example.ui.screens.ScheduleScreen(
                        onConfirmWeekSweep = { ticker, amt ->
                            viewModel.logDividendPayout(ticker, amt)
                            viewModel.calculateSweep(amt)
                            currentDestination = AppNavDestination.SWEEP
                        }
                    )
                }
                AppNavDestination.HOLDINGS -> {
                    HoldingsScreen(
                        holdings = holdings,
                        prefilledSector = prefillSectorForHoldings,
                        onSaveHolding = { h ->
                            viewModel.saveHolding(h)
                            prefillSectorForHoldings = null
                        },
                        onDeleteHolding = { h -> viewModel.deleteHolding(h) },
                        onExportJson = { viewModel.exportJson() },
                        onImportJson = { json -> viewModel.importJson(json) }
                    )
                }
                AppNavDestination.OUTLOOK -> {
                    ProjectionScreen(
                        metrics = metrics,
                        projections = projections,
                        selectedHorizon = selectedHorizon,
                        monthlyContribution = monthlyContribution,
                        onSelectHorizon = { h -> viewModel.setSelectedHorizon(h) },
                        onSetMonthlyContribution = { c -> viewModel.setMonthlyContribution(c) }
                    )
                }
                AppNavDestination.COPILOT -> {
                    GeminiChatSheet(
                        messages = chatMessages,
                        isThinking = isAiThinking,
                        standing = portfolioStanding,
                        onSendMessage = { text, think -> viewModel.sendChatMessage(text, think) },
                        onExecuteRoute = { route ->
                            viewModel.applyRouteToSweep(route)
                            currentDestination = AppNavDestination.SWEEP
                        }
                    )
                }
                AppNavDestination.REFERRALS -> {
                    ReferralScreen()
                }
                AppNavDestination.SETTINGS -> {
                    SettingsAndManualScreen(
                        dashboardConfig = dashboardConfig,
                        onUpdateDashboardConfig = { dashboardConfig = it },
                        canUndoLastSweep = canUndoLastSweep,
                        lastSweepSummary = lastSweepSummary,
                        onUndoLastSweep = { viewModel.undoLastSweep() },
                        onOpenTutorial = { showTutorial = true },
                        onOpenPresetModal = { showPresetPrompt = true },
                        onParseTelemetry = { raw -> viewModel.parseTelemetryBatch(raw) },
                        onApplyTelemetry = { events -> viewModel.applyTelemetryBatch(events) },
                        onExportJson = { viewModel.exportJson() },
                        onImportJson = { json -> viewModel.importJson(json) }
                    )
                }
            }
        }
    }

    // Onboarding Setup Dialog
    if (showOnboardingDialog || showPresetPrompt) {
        OnboardingDialog(
            onCustomPortfolioSelected = { customList, cash ->
                viewModel.seedCustomPortfolio(customList, cash)
                showPresetPrompt = false
            },
            onStartFresh = { cash ->
                viewModel.startFresh(cash)
                showPresetPrompt = false
            },
            onOpenTutorial = { showTutorial = true },
            onDismiss = {
                viewModel.dismissOnboardingDialog()
                showPresetPrompt = false
            }
        )
    }

    // Interactive Onboarding Tutorial Dialog
    if (showTutorial) {
        TutorialDialog(onDismiss = { showTutorial = false })
    }

    // Advanced Alerts Dialog
    if (showAlerts) {
        AlertsDialog(
            rules = alertRules,
            notifications = notifications,
            onAddRule = { rule -> viewModel.addAlertRule(rule) },
            onToggleRule = { rule -> viewModel.toggleAlertRule(rule) },
            onDeleteRule = { rule -> viewModel.deleteAlertRule(rule) },
            onClearNotifications = { viewModel.clearNotifications() },
            onDismiss = { showAlerts = false }
        )
    }
}
