// File: SettingsAndManualScreen.kt
package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DashboardModuleConfig
import com.example.ui.ParsedTelemetryEvent
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
import kotlinx.coroutines.launch

enum class SettingsTab(val title: String) {
    MANUAL("User Manual & Guide"),
    TELEMETRY("Local Telemetry Sync"),
    PREFERENCES("Settings & QoL")
}

data class ManualSection(
    val id: String,
    val badge: String,
    val title: String,
    val subtitle: String,
    val accentColor: Color,
    val icon: ImageVector,
    val keyPoints: List<String>,
    val formulaOrRule: String
)

private val USER_MANUAL_SECTIONS = listOf(
    ManualSection(
        id = "drip_off",
        badge = "CORE DOCTRINE",
        title = "1. Closed-Loop DRIP = OFF Architecture",
        subtitle = "Why automatic dividend reinvestment is permanently disabled",
        accentColor = EmeraldYieldBright,
        icon = Icons.Default.FlashOn,
        keyPoints = listOf(
            "In standard brokerage accounts, DRIP blindly buys more shares of whichever asset paid the dividend—even if that asset is overpriced or over-concentrated.",
            "With DRIP = OFF, every weekly and monthly cash distribution pools directly into your Settled Buying Power ledger.",
            "You then run the Multi-Vector Sweep to deploy that cash into unheld sector gaps or discounted defensive ballast."
        ),
        formulaOrRule = "Rule: All Dividend Payouts -> Settled Cash Pool -> Intentional Concentrated Sweep"
    ),
    ManualSection(
        id = "four_stages",
        badge = "CAPITAL TIERS",
        title = "2. The 4-Stage Portfolio Architecture",
        subtitle = "How every ticker serves a specific structural role",
        accentColor = CyanCore,
        icon = Icons.AutoMirrored.Filled.MenuBook,
        keyPoints = listOf(
            "Stage 1 (Heavy Cash Flow Engines): High-velocity option-income ETFs (MSTY, NVYY, COYY, TSYY, HIYY) that generate weekly cash flow.",
            "Stage 2 (Core Market Stability): Broad equity index and dividend growth anchors (XDTE, QDTE, RDTE, FEPI, SCHD, VOO, VNQ).",
            "Stage 3 (Growth & Macro Accelerators): Sector momentum, gold miners, energy, and bond duration (AIPI, CEPI, GDXY, XOMO, TLTW).",
            "Stage 4 (Tax-Shielded Reserves): 0-3 Month US Treasury bills (SGOV) providing state-tax-exempt liquidity and crash protection."
        ),
        formulaOrRule = "Target Balance: Stage 2 + Stage 4 + Cash >= 1.25x (Stage 1 + Stage 3)"
    ),
    ManualSection(
        id = "defense_ratio",
        badge = "RISK SHIELD",
        title = "3. Defense-to-Risk Density Ratio (>= 1.25x)",
        subtitle = "Protecting your principal during steep market drawdowns",
        accentColor = GoldAlert,
        icon = Icons.Default.Security,
        keyPoints = listOf(
            "Measures how many dollars of defensive ballast (Stage 2 + Stage 4 + Settled Cash) back every $1.00 of high-beta yield engines (Stage 1 + Stage 3).",
            "STRONG (>= 1.25x): 'Better-Than-Good' sovereign shield. The sweep engine is free to harvest high-yield Stage 1 & Stage 3 opportunities.",
            "BALANCED (1.00x - 1.24x) or HIGH RISK (< 1.00x): The sweep optimizer automatically boosts Stage 2 and Stage 4 defensive weights by up to 2.25x to restore your shield."
        ),
        formulaOrRule = "Defense Ratio = (Stage 2 + Stage 4 + Settled Cash) / (Stage 1 + Stage 3)"
    ),
    ManualSection(
        id = "sweep_algorithm",
        badge = "OPTIMIZER",
        title = "4. Gap-First Sweep & Concentration Freeze",
        subtitle = "How the 6-factor redistribution engine selects 1 to 3 targets",
        accentColor = PurpleTech,
        icon = Icons.AutoMirrored.Filled.TrendingUp,
        keyPoints = listOf(
            "Zero-Dilution Rule: Deployments under $75 focus on 1 target; $75-$249 focus on 2 targets; $250+ focus on 3 targets across distinct risk vectors.",
            "Gap-First Priority: Whenever any of the 9 Market Risk Vectors are unheld (0 holdings), 100% of target slots are dedicated to the highest-yield unheld Gap Champions (e.g., HIYY at 75.9% for Healthcare, XOMO at 45.9% for Energy).",
            "Over-Concentration Freeze: Any sector exceeding its healthy cap (e.g., Crypto > 25%) receives a hard 0.05x freeze multiplier until balance is restored."
        ),
        formulaOrRule = "Score = YieldVelocity * Discount * StageNeed * Diversity * GapBoost * TaxFactor"
    ),
    ManualSection(
        id = "charts_and_schedule",
        badge = "ANALYTICS",
        title = "5. Interactive Charts & 24-Week Schedule",
        subtitle = "Visualizing long-term compounding and weekly payout velocity",
        accentColor = EmeraldYieldPrimary,
        icon = Icons.Default.Info,
        keyPoints = listOf(
            "Touch-Scrubbable Trajectory Chart: Drag your finger across the chart on Dashboard or Outlook to inspect Total Balance, Dividend Income, and Risk Exposure across 30D, 90D, 1Y, 5Y, 10Y, and 20Y horizons.",
            "24-Week Distribution Schedule: Tracks Wednesday/Thursday/Friday weekly payers (XDTE, QDTE, RDTE, YMAX, MSTY, NVYY, COYY, TSYY, GDXY) and lets you log & sweep payouts in 1 tap.",
            "Rollback Safety: Every executed sweep can be undone with 1 tap using the Rollback button in Sweep or Settings."
        ),
        formulaOrRule = "Tip: Toggle between Total Balance, Dividend Income, and Risk Exposure tabs on any chart."
    )
)

@Composable
fun SettingsAndManualScreen(
    dashboardConfig: DashboardModuleConfig,
    onUpdateDashboardConfig: (DashboardModuleConfig) -> Unit,
    canUndoLastSweep: Boolean,
    lastSweepSummary: String?,
    onUndoLastSweep: () -> Unit,
    onOpenTutorial: () -> Unit,
    onOpenPresetModal: () -> Unit,
    onParseTelemetry: (String) -> List<ParsedTelemetryEvent>,
    onApplyTelemetry: (List<ParsedTelemetryEvent>) -> Unit,
    onExportJson: suspend () -> String,
    onImportJson: suspend (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(SettingsTab.MANUAL) }
    var manualSearchQuery by remember { mutableStateOf("") }
    val expandedSectionIds = remember {
        mutableStateListOf("drip_off", "four_stages", "defense_ratio", "sweep_algorithm")
    }

    var rawTelemetryInput by remember { mutableStateOf("") }
    var stagedTelemetryEvents by remember { mutableStateOf<List<ParsedTelemetryEvent>>(emptyList()) }
    var jsonRestoreInput by remember { mutableStateOf("") }
    var showJsonRestoreBox by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalVoid)
            .padding(horizontal = 16.dp)
            .testTag("settings_and_manual_screen")
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(CyanCore.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = CyanCore,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "USER MANUAL & SETTINGS",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Post-Launch Reference, Local Telemetry & QoL Controls",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            // Quick Relaunch Interactive Walkthrough Button
            TextButton(
                onClick = onOpenTutorial,
                modifier = Modifier.testTag("relaunch_tutorial_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = "Walkthrough",
                    tint = EmeraldYieldBright,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Walkthrough",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldYieldBright
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sub-Tab Selector Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(TerminalSurfaceVariant)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SettingsTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) CyanCore.copy(alpha = 0.22f) else Color.Transparent
                        )
                        .border(
                            1.dp,
                            if (isSelected) CyanCore else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedTab = tab }
                        .padding(vertical = 7.dp)
                        .testTag("settings_tab_${tab.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) CyanCore else TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            SettingsTab.MANUAL -> {
                val matchingSections = remember(manualSearchQuery) {
                    if (manualSearchQuery.isBlank()) {
                        USER_MANUAL_SECTIONS
                    } else {
                        val q = manualSearchQuery.trim()
                        USER_MANUAL_SECTIONS.filter { sec ->
                            sec.title.contains(q, ignoreCase = true) ||
                                sec.subtitle.contains(q, ignoreCase = true) ||
                                sec.badge.contains(q, ignoreCase = true) ||
                                sec.keyPoints.any { it.contains(q, ignoreCase = true) } ||
                                sec.formulaOrRule.contains(q, ignoreCase = true)
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        // Interactive Walkthrough Relaunch Card + Search Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(TerminalSurface)
                                .border(1.dp, EmeraldYieldPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "FIRST-LAUNCH INTERACTIVE TUTORIAL",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldYieldBright
                                    )
                                    Text(
                                        text = "Replay the 5-card visual walkthrough anytime to review core concepts.",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = onOpenTutorial,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldYieldPrimary,
                                        contentColor = TerminalVoid
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Replay", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = manualSearchQuery,
                            onValueChange = { manualSearchQuery = it },
                            placeholder = {
                                Text(
                                    "Search manual (e.g. DRIP, 1.25x, HIYY, Stage 4)...",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Manual",
                                    tint = CyanCore,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanCore,
                                unfocusedBorderColor = TerminalCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("manual_search_input")
                        )
                    }

                    items(matchingSections, key = { it.id }) { section ->
                        val isExpanded = expandedSectionIds.contains(section.id)
                        ManualSectionCard(
                            section = section,
                            isExpanded = isExpanded,
                            onToggle = {
                                if (isExpanded) {
                                    expandedSectionIds.remove(section.id)
                                } else {
                                    expandedSectionIds.add(section.id)
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }

            SettingsTab.TELEMETRY -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(TerminalSurface)
                                .border(1.dp, CyanCore.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = null,
                                        tint = CyanCore,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "LOCAL TELEMETRY BATCH PARSER (DRY-RUN FIRST)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanCore
                                        )
                                        Text(
                                            text = "Zero-permission, 100% on-device parser for brokerage dividend alerts, trade confirmations, or quick batch lines.",
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }

                                Text(
                                    text = "Supported Formats (1 per line):\n• DIVIDEND JEPQ 18.45  (or 'Dividend received $18.45 from JEPQ')\n• DEPOSIT 150.00 Weekly Transfer\n• BUY HIYY 10.0 @ 9.84  (adds shares & updates avg cost)\n• PRICE MSTY 13.40  (updates live share price)",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextSecondary,
                                    lineHeight = 14.sp
                                )

                                OutlinedTextField(
                                    value = rawTelemetryInput,
                                    onValueChange = { rawTelemetryInput = it },
                                    placeholder = {
                                        Text(
                                            "DIVIDEND MSTY 24.50\nBUY HIYY 5.0 @ 9.84\nPRICE XDTE 36.80",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanCore,
                                        unfocusedBorderColor = TerminalCardBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(115.dp)
                                        .testTag("telemetry_batch_input")
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val clip = clipboardManager.getText()?.text.orEmpty()
                                            if (clip.isNotBlank()) {
                                                rawTelemetryInput = clip
                                                stagedTelemetryEvents = onParseTelemetry(clip)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = TerminalSurfaceVariant,
                                            contentColor = CyanCore
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Paste Clipboard", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            stagedTelemetryEvents = onParseTelemetry(rawTelemetryInput)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CyanCore,
                                            contentColor = TerminalVoid
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("dry_run_telemetry_button")
                                    ) {
                                        Text("Dry-Run Preview", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Staged Dry-Run Preview Results
                    if (stagedTelemetryEvents.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(TerminalSurfaceVariant)
                                    .border(1.dp, EmeraldYieldPrimary, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                                    .testTag("telemetry_dry_run_preview_card")
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "DRY-RUN PREVIEW (${stagedTelemetryEvents.size} VERIFIED ACTIONS)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldYieldBright
                                    )

                                    stagedTelemetryEvents.forEach { ev ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "• ${ev.summary}",
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = ev.type.name,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CyanCore
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            onApplyTelemetry(stagedTelemetryEvents)
                                            Toast.makeText(
                                                context,
                                                "Applied ${stagedTelemetryEvents.size} telemetry updates!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            stagedTelemetryEvents = emptyList()
                                            rawTelemetryInput = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = EmeraldYieldPrimary,
                                            contentColor = TerminalVoid
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("apply_telemetry_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Commit Telemetry Updates to Ledger",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 1-Tap JSON Backup & Restore Card
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(TerminalSurface)
                                .border(1.dp, TerminalCardBorder, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "PORTFOLIO SNAPSHOT BACKUP (JSON)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Export your complete holdings and settled cash ledger to clipboard or restore from a saved JSON snapshot.",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                val json = onExportJson()
                                                clipboardManager.setText(AnnotatedString(json))
                                                Toast.makeText(
                                                    context,
                                                    "Portfolio JSON copied to clipboard!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = EmeraldYieldPrimary,
                                            contentColor = TerminalVoid
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Copy JSON Backup", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { showJsonRestoreBox = !showJsonRestoreBox },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = TerminalSurfaceVariant,
                                            contentColor = CyanCore
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Upload,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Restore JSON", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (showJsonRestoreBox) {
                                    OutlinedTextField(
                                        value = jsonRestoreInput,
                                        onValueChange = { jsonRestoreInput = it },
                                        placeholder = {
                                            Text("Paste backup JSON here...", fontSize = 11.sp, color = TextSecondary)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                    )
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                val ok = onImportJson(jsonRestoreInput)
                                                if (ok) {
                                                    Toast.makeText(context, "Backup restored!", Toast.LENGTH_SHORT).show()
                                                    showJsonRestoreBox = false
                                                    jsonRestoreInput = ""
                                                } else {
                                                    Toast.makeText(context, "Invalid JSON backup.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CyanCore,
                                            contentColor = TerminalVoid
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Validate & Restore Snapshot", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }

            SettingsTab.PREFERENCES -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Rollback / Undo Last Sweep Card
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(TerminalSurface)
                                .border(
                                    1.dp,
                                    if (canUndoLastSweep) GoldAlert else TerminalCardBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(14.dp)
                                .testTag("settings_rollback_card")
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = if (canUndoLastSweep) GoldAlert else TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "SWEEP ROLLBACK / UNDO SAFETY PAIR",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (canUndoLastSweep) GoldAlert else TextPrimary
                                    )
                                }

                                Text(
                                    text = if (canUndoLastSweep && lastSweepSummary != null) {
                                        "Ready to Rollback: $lastSweepSummary"
                                    } else {
                                        "No recent sweep in this session to undo. Whenever you execute a sweep, an exact rollback checkpoint is saved here."
                                    },
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 15.sp
                                )

                                if (canUndoLastSweep) {
                                    Button(
                                        onClick = {
                                            onUndoLastSweep()
                                            Toast.makeText(
                                                context,
                                                "Rolled back last sweep and restored settled cash!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = GoldAlert,
                                            contentColor = TerminalVoid
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("settings_undo_sweep_button")
                                    ) {
                                        Text("Rollback Last Executed Sweep", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Portfolio Architecture / Baseline Switcher
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(TerminalSurface)
                                .border(1.dp, TerminalCardBorder, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "STARTING ARCHITECTURE & SETUP",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Switch between Mode A ($0.00 Fresh Start) and Mode B (Custom Holdings / CSV Import).",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = onOpenPresetModal,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CyanCore,
                                        contentColor = TerminalVoid
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Setup", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Dashboard Module Toggles
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(TerminalSurface)
                                .border(1.dp, TerminalCardBorder, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "DASHBOARD MODULE VISIBILITY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanCore
                                )
                                SettingToggleItem(
                                    title = "Týr Financial Expert Card",
                                    subtitle = "Live sustainability, tax efficiency & tyrprotocol.pro",
                                    checked = dashboardConfig.showTyrFinancialExpertCard,
                                    onCheckedChange = {
                                        onUpdateDashboardConfig(dashboardConfig.copy(showTyrFinancialExpertCard = it))
                                    }
                                )
                                SettingToggleItem(
                                    title = "Interactive Trajectory Chart",
                                    subtitle = "Touch-scrubbable 30D to 20Y performance canvas",
                                    checked = dashboardConfig.showPerformanceChart,
                                    onCheckedChange = {
                                        onUpdateDashboardConfig(dashboardConfig.copy(showPerformanceChart = it))
                                    }
                                )
                                SettingToggleItem(
                                    title = "Portfolio Risk Radar",
                                    subtitle = "9-vector spider web & gap detector",
                                    checked = dashboardConfig.showRiskRadar,
                                    onCheckedChange = {
                                        onUpdateDashboardConfig(dashboardConfig.copy(showRiskRadar = it))
                                    }
                                )
                                SettingToggleItem(
                                    title = "24-Week Schedule Tracker",
                                    subtitle = "Weekly payout & gap-filling calendar",
                                    checked = dashboardConfig.showScheduleTracker,
                                    onCheckedChange = {
                                        onUpdateDashboardConfig(dashboardConfig.copy(showScheduleTracker = it))
                                    }
                                )
                                SettingToggleItem(
                                    title = "4-Stage Capital Breakdown",
                                    subtitle = "Stage 1-4 allocation cards",
                                    checked = dashboardConfig.showStageBreakdowns,
                                    onCheckedChange = {
                                        onUpdateDashboardConfig(dashboardConfig.copy(showStageBreakdowns = it))
                                    }
                                )
                                SettingToggleItem(
                                    title = "Market & Risk Diversity Grid",
                                    subtitle = "Detailed sector weights & concentration caps",
                                    checked = dashboardConfig.showMarketDiversity,
                                    onCheckedChange = {
                                        onUpdateDashboardConfig(dashboardConfig.copy(showMarketDiversity = it))
                                    }
                                )
                                SettingToggleItem(
                                    title = "Partner Welcome Bonus Banner",
                                    subtitle = "Featured brokerage referral card",
                                    checked = dashboardConfig.showReferralCard,
                                    onCheckedChange = {
                                        onUpdateDashboardConfig(dashboardConfig.copy(showReferralCard = it))
                                    }
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualSectionCard(
    section: ManualSection,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TerminalSurface)
            .border(1.dp, section.accentColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle)
            .padding(14.dp)
            .testTag("manual_section_${section.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(section.accentColor.copy(alpha = 0.18f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = section.icon,
                            contentDescription = null,
                            tint = section.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = section.badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = section.accentColor
                        )
                        Text(
                            text = section.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = section.subtitle,
                fontSize = 11.sp,
                color = TextSecondary
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    section.keyPoints.forEach { point ->
                        Row(
                            modifier = Modifier.padding(bottom = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(5.dp)
                                    .background(section.accentColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = point,
                                fontSize = 11.sp,
                                color = TextPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TerminalSurfaceVariant)
                            .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = section.formulaOrRule,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = section.accentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggleItem(
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
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CyanCore,
                checkedTrackColor = CyanCore.copy(alpha = 0.3f),
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = TerminalSurface
            )
        )
    }
}
