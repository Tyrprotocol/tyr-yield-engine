// File: OnboardingDialog.kt
package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Holding
import com.example.engine.RedistributionOptimizer
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
import java.util.Locale

@Composable
fun OnboardingDialog(
    onCustomPortfolioSelected: (List<Holding>, Double) -> Unit,
    onStartFresh: (Double) -> Unit,
    onOpenTutorial: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMode by remember { mutableStateOf("MODE_A") }
    var startingCashInput by remember { mutableStateOf("50.00") }

    // Custom portfolio state starts clean and empty on a new install
    val customHoldings = remember {
        mutableStateListOf<Holding>()
    }

    var showCustomEditor by remember { mutableStateOf(true) }
    var showAddAssetForm by remember { mutableStateOf(true) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    // Form fields for adding or editing a custom asset
    var formTicker by remember { mutableStateOf("") }
    var formName by remember { mutableStateOf("") }
    var formShares by remember { mutableStateOf("") }
    var formAvgCost by remember { mutableStateOf("") }
    var formCurrentPrice by remember { mutableStateOf("") }
    var formYieldPct by remember { mutableStateOf("") }
    var formStage by remember { mutableStateOf(2) }
    var formCategoryTag by remember { mutableStateOf("Core Index / Equity") }
    var formFrequency by remember { mutableStateOf("Monthly") }
    var formError by remember { mutableStateOf<String?>(null) }

    var pasteTextInput by remember { mutableStateOf("") }
    var showPasteBox by remember { mutableStateOf(false) }

    val catalogQuickTemplates = remember {
        RedistributionOptimizer.SECTOR_CHAMPION_CATALOG.values.toList()
    }

    fun resetAssetForm() {
        editingIndex = null
        formTicker = ""
        formName = ""
        formShares = ""
        formAvgCost = ""
        formCurrentPrice = ""
        formYieldPct = ""
        formStage = 2
        formCategoryTag = "Core Index / Equity"
        formFrequency = "Monthly"
        formError = null
    }

    fun loadHoldingIntoForm(index: Int, holding: Holding) {
        editingIndex = index
        formTicker = holding.ticker
        formName = holding.name
        formShares = holding.shares.toString()
        formAvgCost = holding.avgCost.toString()
        formCurrentPrice = holding.currentPrice.toString()
        formYieldPct = holding.annualYieldPercent.toString()
        formStage = holding.stage
        formCategoryTag = holding.categoryTag
        formFrequency = holding.distributionFrequency
        formError = null
        showAddAssetForm = true
    }

    fun applyCatalogTemplate(template: Holding) {
        formTicker = template.ticker
        formName = template.name
        formCurrentPrice = String.format(Locale.US, "%.2f", template.currentPrice)
        if (formAvgCost.isBlank()) {
            formAvgCost = String.format(Locale.US, "%.2f", template.currentPrice)
        }
        formYieldPct = String.format(Locale.US, "%.2f", template.annualYieldPercent)
        formStage = template.stage
        formCategoryTag = template.categoryTag
        formFrequency = template.distributionFrequency
        formError = null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, TerminalCardBorder, RoundedCornerShape(20.dp)),
            color = TerminalVoid
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .testTag("onboarding_dialog")
            ) {
                // Header with Tutorial Help Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(EmeraldYieldPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = EmeraldYieldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Closed-Loop Yield Engine",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Select Starting Architecture",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    TextButton(onClick = onOpenTutorial) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = null,
                            tint = CyanCore,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tutorial", fontSize = 11.sp, color = CyanCore)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Mode A: Fresh Start ($0.00 Base)
                    item {
                        val isModeA = selectedMode == "MODE_A"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isModeA) TerminalSurfaceVariant else TerminalSurface)
                                .border(
                                    1.dp,
                                    if (isModeA) EmeraldYieldPrimary else TerminalCardBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedMode = "MODE_A" }
                                .padding(14.dp)
                                .testTag("mode_a_fresh_start_card")
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Savings,
                                            contentDescription = null,
                                            tint = if (isModeA) EmeraldYieldBright else TextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Mode A: Start Fresh ($0.00 Base)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isModeA) EmeraldYieldBright else TextPrimary
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(EmeraldYieldPrimary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "BEGINNER",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldYieldPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Starts cleanly with $0.00 and 0 shares. Enter your available starting cash (even $25 or $50) to build a concentrated 1-2 fund foundation without mathematical dilution.",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )

                                if (isModeA) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = startingCashInput,
                                        onValueChange = { startingCashInput = it },
                                        label = { Text("Starting Cash Ready to Deploy ($)") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.AddCard,
                                                contentDescription = null,
                                                tint = EmeraldYieldPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
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
                                            .testTag("starting_cash_input")
                                    )
                                }
                            }
                        }
                    }

                    // Mode B: Import or Enter Existing Portfolio
                    item {
                        val isModeB = selectedMode == "MODE_B"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isModeB) TerminalSurfaceVariant else TerminalSurface)
                                .border(
                                    1.dp,
                                    if (isModeB) CyanCore else TerminalCardBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    selectedMode = "MODE_B"
                                    showCustomEditor = true
                                }
                                .padding(14.dp)
                                .testTag("mode_b_existing_portfolio_card")
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Dataset,
                                            contentDescription = null,
                                            tint = if (isModeB) CyanCore else TextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Mode B: Existing Portfolio",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isModeB) CyanCore else TextPrimary
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(CyanCore.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "CUSTOM IMPORT",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanCore
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Already hold dividend or index funds? Add your own tickers with your exact share count and cost basis, or paste a CSV list.",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )

                                if (isModeB) {
                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = startingCashInput,
                                        onValueChange = { startingCashInput = it },
                                        label = { Text("Settled Buying Power Cash ($)") },
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
                                }
                            }
                        }
                    }

                    // Interactive Tailored Holdings Customizer & Full Asset Builder
                    if (selectedMode == "MODE_B" && showCustomEditor) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(TerminalSurface)
                                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "YOUR ASSETS (${customHoldings.size})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyanCore
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            TextButton(
                                                onClick = { showPasteBox = !showPasteBox },
                                                modifier = Modifier.testTag("onboarding_toggle_paste_button")
                                            ) {
                                                Text(
                                                    text = if (showPasteBox) "Hide CSV" else "Paste CSV",
                                                    fontSize = 11.sp,
                                                    color = CyanCore
                                                )
                                            }
                                            TextButton(
                                                onClick = {
                                                    resetAssetForm()
                                                    showAddAssetForm = !showAddAssetForm
                                                },
                                                modifier = Modifier.testTag("onboarding_open_add_asset_button")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    tint = EmeraldYieldBright,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = if (showAddAssetForm && editingIndex == null) "Hide Form" else "New Asset",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = EmeraldYieldBright
                                                )
                                            }
                                        }
                                    }

                                    // Inline Add / Edit Asset Composer (Replaces hardcoded 10 @ $50 placeholder)
                                    if (showAddAssetForm) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(TerminalVoid)
                                                .border(
                                                    1.dp,
                                                    if (editingIndex != null) GoldAlert else EmeraldYieldPrimary.copy(alpha = 0.6f),
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .padding(12.dp)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = if (editingIndex != null) {
                                                            "EDIT ASSET #${editingIndex!! + 1} ($formTicker)"
                                                        } else {
                                                            "ADD CUSTOM ASSET"
                                                        },
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (editingIndex != null) GoldAlert else EmeraldYieldBright
                                                    )
                                                    Text(
                                                        text = "Tap chip or type any ticker",
                                                        fontSize = 10.sp,
                                                        color = TextSecondary
                                                    )
                                                }

                                                // 1-Tap Quick-Fill Catalog Chips
                                                LazyRow(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    items(catalogQuickTemplates) { tmpl ->
                                                        val isSelected = formTicker.equals(tmpl.ticker, ignoreCase = true)
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(
                                                                    if (isSelected) CyanCore.copy(alpha = 0.22f)
                                                                    else TerminalSurfaceVariant
                                                                )
                                                                .border(
                                                                    1.dp,
                                                                    if (isSelected) CyanCore else TerminalCardBorder,
                                                                    RoundedCornerShape(6.dp)
                                                                )
                                                                .clickable { applyCatalogTemplate(tmpl) }
                                                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                                        ) {
                                                            Text(
                                                                text = "${tmpl.ticker} (S${tmpl.stage})",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isSelected) CyanCore else TextPrimary
                                                            )
                                                        }
                                                    }
                                                }

                                                // Row 1: Ticker + Name
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    OutlinedTextField(
                                                        value = formTicker,
                                                        onValueChange = { raw ->
                                                            val clean = raw.uppercase(Locale.US).trim()
                                                            formTicker = clean
                                                            formError = null
                                                            // Auto-fill metadata if ticker matches a known catalog template
                                                            val tmpl = RedistributionOptimizer.findCatalogTemplate(clean)
                                                            if (tmpl != null) {
                                                                formName = tmpl.name
                                                                formCurrentPrice = String.format(Locale.US, "%.2f", tmpl.currentPrice)
                                                                if (formAvgCost.isBlank()) {
                                                                    formAvgCost = String.format(Locale.US, "%.2f", tmpl.currentPrice)
                                                                }
                                                                formYieldPct = String.format(Locale.US, "%.2f", tmpl.annualYieldPercent)
                                                                formStage = tmpl.stage
                                                                formCategoryTag = tmpl.categoryTag
                                                                formFrequency = tmpl.distributionFrequency
                                                            }
                                                        },
                                                        label = { Text("Ticker (e.g. VOO, MSTY)") },
                                                        singleLine = true,
                                                        modifier = Modifier
                                                            .weight(0.95f)
                                                            .testTag("onboarding_ticker_input")
                                                    )
                                                    OutlinedTextField(
                                                        value = formName,
                                                        onValueChange = { formName = it },
                                                        label = { Text("Asset Name (Optional)") },
                                                        singleLine = true,
                                                        modifier = Modifier
                                                            .weight(1.35f)
                                                            .testTag("onboarding_name_input")
                                                    )
                                                }

                                                // Row 2: Shares + Avg Cost / Share
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    OutlinedTextField(
                                                        value = formShares,
                                                        onValueChange = {
                                                            formShares = it
                                                            formError = null
                                                        },
                                                        label = { Text("Shares Held") },
                                                        placeholder = { Text("e.g. 12.5") },
                                                        singleLine = true,
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .testTag("onboarding_shares_input")
                                                    )
                                                    OutlinedTextField(
                                                        value = formAvgCost,
                                                        onValueChange = {
                                                            formAvgCost = it
                                                            formError = null
                                                        },
                                                        label = { Text("Avg Cost / Sh ($)") },
                                                        placeholder = { Text("e.g. 24.50") },
                                                        singleLine = true,
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .testTag("onboarding_cost_input")
                                                    )
                                                }

                                                // Row 3: Current Price + Annual Yield %
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    OutlinedTextField(
                                                        value = formCurrentPrice,
                                                        onValueChange = { formCurrentPrice = it },
                                                        label = { Text("Current Price ($)") },
                                                        placeholder = { Text("Defaults to Cost") },
                                                        singleLine = true,
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .testTag("onboarding_price_input")
                                                    )
                                                    OutlinedTextField(
                                                        value = formYieldPct,
                                                        onValueChange = { formYieldPct = it },
                                                        label = { Text("Annual Yield (%)") },
                                                        placeholder = { Text("e.g. 12.0") },
                                                        singleLine = true,
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .testTag("onboarding_yield_input")
                                                    )
                                                }

                                                // Row 4: Stage 1-4 Selector
                                                Text(
                                                    text = "Portfolio Stage Tier:",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextSecondary
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    val stageLabels = listOf(
                                                        1 to "S1 Cash",
                                                        2 to "S2 Core",
                                                        3 to "S3 Macro",
                                                        4 to "S4 T-Bill"
                                                    )
                                                    stageLabels.forEach { (stNum, label) ->
                                                        val isSelected = formStage == stNum
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(
                                                                    if (isSelected) EmeraldYieldPrimary.copy(alpha = 0.22f)
                                                                    else TerminalSurfaceVariant
                                                                )
                                                                .border(
                                                                    1.dp,
                                                                    if (isSelected) EmeraldYieldPrimary else TerminalCardBorder,
                                                                    RoundedCornerShape(6.dp)
                                                                )
                                                                .clickable { formStage = stNum }
                                                                .padding(vertical = 6.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = label,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isSelected) EmeraldYieldBright else TextSecondary
                                                            )
                                                        }
                                                    }
                                                }

                                                if (formError != null) {
                                                    Text(
                                                        text = formError!!,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = GoldAlert
                                                    )
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.End,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    if (editingIndex != null || customHoldings.isNotEmpty()) {
                                                        TextButton(
                                                            onClick = {
                                                                resetAssetForm()
                                                                if (customHoldings.isNotEmpty()) {
                                                                    showAddAssetForm = false
                                                                }
                                                            }
                                                        ) {
                                                            Text("Cancel", fontSize = 11.sp, color = TextSecondary)
                                                        }
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                    }

                                                    Button(
                                                        onClick = {
                                                            val cleanTicker = formTicker.trim().uppercase(Locale.US)
                                                            val parsedShares = formShares.trim().toDoubleOrNull()
                                                            val parsedCost = formAvgCost.trim().toDoubleOrNull()

                                                            if (cleanTicker.isBlank()) {
                                                                formError = "Please enter a ticker symbol (e.g. VOO, MSTY, SGOV)."
                                                                return@Button
                                                            }
                                                            if (parsedShares == null || parsedShares <= 0.0 || !parsedShares.isFinite()) {
                                                                formError = "Please enter a valid positive share count (e.g. 10 or 4.5)."
                                                                return@Button
                                                            }
                                                            if (parsedCost == null || parsedCost <= 0.0 || !parsedCost.isFinite()) {
                                                                formError = "Please enter your average cost per share (e.g. 25.00)."
                                                                return@Button
                                                            }

                                                            val parsedPrice = formCurrentPrice.trim().toDoubleOrNull()
                                                                ?.takeIf { it > 0.0 && it.isFinite() }
                                                                ?: parsedCost
                                                            val parsedYield = formYieldPct.trim().toDoubleOrNull()
                                                                ?.takeIf { it >= 0.0 && it.isFinite() }
                                                                ?: 5.0

                                                            val builtHolding = Holding(
                                                                ticker = cleanTicker,
                                                                name = formName.trim().ifBlank { "$cleanTicker Holding" },
                                                                shares = parsedShares,
                                                                avgCost = parsedCost,
                                                                currentPrice = parsedPrice,
                                                                annualYieldPercent = parsedYield,
                                                                stage = formStage.coerceIn(1, 4),
                                                                categoryTag = formCategoryTag.ifBlank { "Stage $formStage Asset" },
                                                                distributionFrequency = formFrequency.ifBlank { "Monthly" }
                                                            )

                                                            val targetIdx = editingIndex
                                                            if (targetIdx != null && targetIdx in customHoldings.indices) {
                                                                customHoldings[targetIdx] = builtHolding
                                                            } else {
                                                                customHoldings.add(builtHolding)
                                                            }
                                                            resetAssetForm()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = EmeraldYieldPrimary,
                                                            contentColor = TerminalVoid
                                                        ),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.testTag("onboarding_confirm_add_asset_button")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = if (editingIndex != null) "Update Asset" else "Save Asset to List",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (showPasteBox) {
                                        OutlinedTextField(
                                            value = pasteTextInput,
                                            onValueChange = { pasteTextInput = it },
                                            placeholder = {
                                                Text(
                                                    "Paste format:\nTICKER, SHARES, COST, [PRICE], [STAGE], [YIELD%]\ne.g. VOO, 10, 510, 545, 2, 1.4\nMSTY, 25, 15.50, 13.20, 1, 60.9",
                                                    fontSize = 10.sp,
                                                    color = TextSecondary
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(110.dp)
                                        )
                                        Button(
                                            onClick = {
                                                val parsed = parseHoldingsFromText(pasteTextInput)
                                                if (parsed.isNotEmpty()) {
                                                    customHoldings.addAll(parsed)
                                                    showPasteBox = false
                                                    pasteTextInput = ""
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = CyanCore,
                                                contentColor = TerminalVoid
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Parse & Append Holdings",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        itemsIndexed(customHoldings) { index, holding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TerminalSurface)
                                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { loadHoldingIntoForm(index, holding) }
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${holding.ticker} • ${holding.name} (Stage ${holding.stage})",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = String.format(
                                                Locale.US,
                                                "%.3f sh @ $%.2f (Cur: $%.2f | Val: $%.2f) • %.1f%% yield",
                                                holding.shares,
                                                holding.avgCost,
                                                holding.currentPrice,
                                                holding.marketValue,
                                                holding.annualYieldPercent
                                            ),
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { loadHoldingIntoForm(index, holding) },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit ${holding.ticker}",
                                                tint = CyanCore,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                customHoldings.removeAt(index)
                                                if (editingIndex == index) resetAssetForm()
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete ${holding.ticker}",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (selectedMode == "MODE_A") {
                                val cash = startingCashInput.toDoubleOrNull() ?: 50.0
                                onStartFresh(cash)
                            } else {
                                // If the user typed a ticker/shares in the form and immediately tapped Launch without tapping Save Asset first, save it automatically!
                                val pendingTicker = formTicker.trim().uppercase(Locale.US)
                                val pendingShares = formShares.trim().toDoubleOrNull()
                                val pendingCost = formAvgCost.trim().toDoubleOrNull()
                                if (pendingTicker.isNotBlank() && pendingShares != null && pendingShares > 0.0 && pendingCost != null && pendingCost > 0.0) {
                                    val pendingPrice = formCurrentPrice.trim().toDoubleOrNull()?.takeIf { it > 0.0 } ?: pendingCost
                                    val pendingYield = formYieldPct.trim().toDoubleOrNull()?.takeIf { it >= 0.0 } ?: 5.0
                                    val autoHolding = Holding(
                                        ticker = pendingTicker,
                                        name = formName.trim().ifBlank { "$pendingTicker Holding" },
                                        shares = pendingShares,
                                        avgCost = pendingCost,
                                        currentPrice = pendingPrice,
                                        annualYieldPercent = pendingYield,
                                        stage = formStage.coerceIn(1, 4),
                                        categoryTag = formCategoryTag.ifBlank { "Stage $formStage Asset" },
                                        distributionFrequency = formFrequency.ifBlank { "Monthly" }
                                    )
                                    val targetIdx = editingIndex
                                    if (targetIdx != null && targetIdx in customHoldings.indices) {
                                        customHoldings[targetIdx] = autoHolding
                                    } else {
                                        customHoldings.add(autoHolding)
                                    }
                                }
                                val cash = startingCashInput.toDoubleOrNull() ?: 50.0
                                onCustomPortfolioSelected(customHoldings.toList(), cash)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMode == "MODE_A") EmeraldYieldPrimary else CyanCore,
                            contentColor = TerminalVoid
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("confirm_onboarding_button")
                    ) {
                        Text(
                            text = if (selectedMode == "MODE_A") {
                                "Launch Fresh Start"
                            } else {
                                "Launch Tailored Portfolio (${customHoldings.size})"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

internal fun parseHoldingsFromText(text: String): List<Holding> {
    val list = mutableListOf<Holding>()
    text.lines().forEach { line ->
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEach
        val parts = trimmed.split(",").map { it.trim() }
        if (parts.size >= 3) {
            val ticker = parts[0].uppercase(Locale.US)
            val shares = parts[1].toDoubleOrNull() ?: return@forEach
            val cost = parts[2].toDoubleOrNull() ?: return@forEach
            if (shares <= 0.0 || cost <= 0.0) return@forEach

            val template = RedistributionOptimizer.findCatalogTemplate(ticker)
            val price = if (parts.size >= 4) {
                parts[3].toDoubleOrNull()?.takeIf { it > 0.0 } ?: template?.currentPrice ?: cost
            } else {
                template?.currentPrice ?: cost
            }
            val stage = if (parts.size >= 5) {
                parts[4].toIntOrNull()?.coerceIn(1, 4) ?: template?.stage ?: 2
            } else {
                template?.stage ?: 2
            }
            val yieldPct = if (parts.size >= 6) {
                parts[5].toDoubleOrNull()?.takeIf { it >= 0.0 } ?: template?.annualYieldPercent ?: 4.5
            } else {
                template?.annualYieldPercent ?: 4.5
            }

            list.add(
                Holding(
                    ticker = ticker,
                    name = template?.name ?: "$ticker Fund",
                    shares = shares,
                    avgCost = cost,
                    currentPrice = price,
                    annualYieldPercent = yieldPct,
                    stage = stage,
                    categoryTag = template?.categoryTag ?: "Stage $stage Holding",
                    distributionFrequency = template?.distributionFrequency ?: "Monthly"
                )
            )
        }
    }
    return list
}
