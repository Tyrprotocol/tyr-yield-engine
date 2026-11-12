// File: HoldingsScreen.kt
package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Holding
import com.example.data.model.RiskSector
import com.example.data.model.StageRegistry
import com.example.ui.theme.CyanCore
import com.example.ui.theme.EmeraldYieldBright
import com.example.ui.theme.EmeraldYieldPrimary
import com.example.ui.theme.RedRisk
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TerminalVoid
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun HoldingsScreen(
    holdings: List<Holding>,
    prefilledSector: RiskSector? = null,
    onSaveHolding: (Holding) -> Unit,
    onDeleteHolding: (Holding) -> Unit,
    onExportJson: suspend () -> String,
    onImportJson: suspend (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var selectedStageFilter by remember { mutableStateOf<Int?>(null) }
    var selectedSectorFilter by remember { mutableStateOf<RiskSector?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("VALUE") } // VALUE, YIELD, GAIN, TICKER
    var editingHolding by remember { mutableStateOf<Holding?>(null) }
    var isAddingNew by remember { mutableStateOf(prefilledSector != null) }
    var showImportDialog by remember { mutableStateOf(false) }

    val filteredHoldings = remember(holdings, selectedStageFilter, selectedSectorFilter, searchQuery, sortBy) {
        val base = holdings.filter { holding ->
            val matchesStage = selectedStageFilter == null || holding.stage == selectedStageFilter
            val matchesSector = selectedSectorFilter == null || RiskSector.resolve(holding) == selectedSectorFilter
            val matchesSearch = searchQuery.isBlank() ||
                holding.ticker.contains(searchQuery.trim(), ignoreCase = true) ||
                holding.name.contains(searchQuery.trim(), ignoreCase = true) ||
                holding.categoryTag.contains(searchQuery.trim(), ignoreCase = true)
            matchesStage && matchesSector && matchesSearch
        }
        when (sortBy) {
            "YIELD" -> base.sortedByDescending { it.annualYieldPercent }
            "GAIN" -> base.sortedByDescending { it.unrealizedGainLossPercent }
            "TICKER" -> base.sortedBy { it.ticker }
            else -> base.sortedByDescending { it.marketValue }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = TerminalVoid,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddingNew = true },
                containerColor = EmeraldYieldPrimary,
                contentColor = TerminalVoid,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_holding_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Holding")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("holdings_screen"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Header & Backup Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PORTFOLIO HOLDINGS",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${holdings.size} active vehicles across 4 stages",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    val json = onExportJson()
                                    clipboardManager.setText(AnnotatedString(json))
                                    Toast.makeText(context, "Portfolio JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Export JSON",
                                tint = CyanCore
                            )
                        }

                        IconButton(onClick = { showImportDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = "Import JSON",
                                tint = EmeraldYieldPrimary
                            )
                        }
                    }
                }
            }

            // Search & Sort Controls (QoL) + Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text("Search ticker, name, or sector...", fontSize = 12.sp, color = TextSecondary)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Holdings",
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
                            .testTag("holdings_search_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SORT:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        listOf(
                            "VALUE" to "Value $",
                            "YIELD" to "Yield %",
                            "GAIN" to "Gain %",
                            "TICKER" to "A-Z"
                        ).forEach { (key, label) ->
                            FilterChip(
                                label = label,
                                isSelected = sortBy == key,
                                onClick = { sortBy = key }
                            )
                        }
                    }

                    Text(
                        text = "TIER FILTER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = TextSecondary
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                label = "All Tiers (${holdings.size})",
                                isSelected = selectedStageFilter == null,
                                onClick = { selectedStageFilter = null }
                            )
                        }
                        items(listOf(1, 2, 3, 4)) { stageNum ->
                            val count = holdings.count { it.stage == stageNum }
                            FilterChip(
                                label = "Stage $stageNum ($count)",
                                isSelected = selectedStageFilter == stageNum,
                                onClick = { selectedStageFilter = stageNum }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "MARKET & RISK VECTOR FILTER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = CyanCore
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                label = "All Vectors (${holdings.size})",
                                isSelected = selectedSectorFilter == null,
                                onClick = { selectedSectorFilter = null }
                            )
                        }
                        items(RiskSector.entries) { sector ->
                            val count = holdings.count { RiskSector.resolve(it) == sector }
                            if (count > 0 || sector != RiskSector.GENERAL_DIVERSIFIED) {
                                FilterChip(
                                    label = "${sector.shortName} ($count)",
                                    isSelected = selectedSectorFilter == sector,
                                    onClick = { selectedSectorFilter = if (selectedSectorFilter == sector) null else sector }
                                )
                            }
                        }
                    }
                }
            }

            if (filteredHoldings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No Holdings in this Category",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap the + button to add a new ticker or load the preset.",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            items(filteredHoldings, key = { it.id }) { holding ->
                HoldingItemCard(
                    holding = holding,
                    onEdit = { editingHolding = holding },
                    onDelete = { onDeleteHolding(holding) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) // FAB spacing
            }
        }
    }

    // Add / Edit Holding Dialog
    if (isAddingNew || editingHolding != null) {
        val initialForAdd = if (editingHolding == null && prefilledSector != null) {
            when (prefilledSector) {
                RiskSector.HEALTHCARE_DEFENSIVE -> Holding(ticker = "HIYY", name = "YieldMax HIMS Option Income Strategy ETF", shares = 10.0, avgCost = 9.84, currentPrice = 9.84, annualYieldPercent = 75.90, stage = 1, categoryTag = "Healthcare / Telehealth", notes = "Weekly Thu Payer")
                RiskSector.NATURAL_RESOURCES_ENERGY -> Holding(ticker = "XOMO", name = "YieldMax XOM Option Income Strategy ETF", shares = 10.0, avgCost = 10.26, currentPrice = 10.26, annualYieldPercent = 45.90, stage = 3, categoryTag = "Energy / Oil & Gas", notes = "Weekly Thu Payer")
                RiskSector.REAL_ESTATE_INFRASTRUCTURE -> Holding(ticker = "VNQ", name = "Vanguard Real Estate ETF", shares = 10.0, avgCost = 86.20, currentPrice = 86.20, annualYieldPercent = 4.10, stage = 2, categoryTag = "Real Estate Infrastructure")
                RiskSector.COMMODITIES_PRECIOUS_METALS -> Holding(ticker = "GDXY", name = "YieldMax Gold Miners ETF", shares = 20.0, avgCost = 9.63, currentPrice = 9.63, annualYieldPercent = 38.20, stage = 3, categoryTag = "Gold Miners (GDX)")
                RiskSector.TAX_EXEMPT_STABLE -> Holding(ticker = "SGOV", name = "iShares 0-3 Month Treasury Bond", shares = 5.0, avgCost = 100.45, currentPrice = 100.45, annualYieldPercent = 5.15, stage = 4, categoryTag = "Tax-Exempt T-Bills")
                RiskSector.FIXED_INCOME_DURATION -> Holding(ticker = "TLTW", name = "iShares 20+ Year Treasury BuyWrite", shares = 15.0, avgCost = 21.02, currentPrice = 21.02, annualYieldPercent = 14.55, stage = 3, categoryTag = "Treasury Duration")
                RiskSector.CRYPTO_DIGITAL_ASSETS -> Holding(ticker = "MSTY", name = "YieldMax MSTR Option Strategy", shares = 20.0, avgCost = 12.79, currentPrice = 12.79, annualYieldPercent = 60.91, stage = 1, categoryTag = "Crypto / BTC Treasury")
                else -> null
            }
        } else editingHolding

        HoldingEditDialog(
            initialHolding = initialForAdd,
            onDismiss = {
                isAddingNew = false
                editingHolding = null
            },
            onSave = { updated ->
                onSaveHolding(updated)
                isAddingNew = false
                editingHolding = null
            }
        )
    }

    // Import JSON Dialog
    if (showImportDialog) {
        var importText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Portfolio Backup JSON", color = TextPrimary) },
            text = {
                Column {
                    Text(
                        text = "Paste your previously exported portfolio JSON string below. This will restore all holdings.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldYieldPrimary,
                            unfocusedBorderColor = TerminalCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val success = onImportJson(importText)
                            if (success) {
                                Toast.makeText(context, "Portfolio imported successfully!", Toast.LENGTH_SHORT).show()
                                showImportDialog = false
                            } else {
                                Toast.makeText(context, "Invalid JSON format.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldYieldPrimary, contentColor = TerminalVoid)
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = TerminalSurface
        )
    }
}

@Composable
fun HoldingItemCard(
    holding: Holding,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val stageMeta = StageRegistry.getStage(holding.stage)
    val sector = RiskSector.resolve(holding)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TerminalSurface)
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onEdit)
            .padding(14.dp)
            .testTag("holding_item_${holding.ticker}")
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
                            text = holding.ticker,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = stageMeta.accentColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Stage ${holding.stage}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(sector.accentColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .border(1.dp, sector.accentColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = sector.shortName,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = sector.accentColor
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format(Locale.US, "$%,.2f", holding.marketValue),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    val gainPrefix = if (holding.unrealizedGainLoss >= 0.0) "+$" else "-$"
                    val gainColor = if (holding.unrealizedGainLoss >= 0.0) EmeraldYieldBright else RedRisk
                    Text(
                        text = String.format(
                            Locale.US,
                            "%s%,.2f (%.1f%%)",
                            gainPrefix,
                            kotlin.math.abs(holding.unrealizedGainLoss),
                            holding.unrealizedGainLossPercent
                        ),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = gainColor,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = holding.name,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format(
                        Locale.US,
                        "%.2f sh @ $%.2f (Cur: $%.2f)",
                        holding.shares,
                        holding.avgCost,
                        holding.currentPrice
                    ),
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = String.format(
                        Locale.US,
                        "%.1f%% ($%.2f/mo)",
                        holding.annualYieldPercent,
                        holding.monthlyDividendIncome
                    ),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = EmeraldYieldBright
                )
            }
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) EmeraldYieldPrimary.copy(alpha = 0.2f) else TerminalSurface)
            .border(
                1.dp,
                if (isSelected) EmeraldYieldPrimary else TerminalCardBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) EmeraldYieldBright else TextSecondary
        )
    }
}

@Composable
fun HoldingEditDialog(
    initialHolding: Holding?,
    onDismiss: () -> Unit,
    onSave: (Holding) -> Unit
) {
    var ticker by remember { mutableStateOf(initialHolding?.ticker ?: "") }
    var name by remember { mutableStateOf(initialHolding?.name ?: "") }
    var shares by remember { mutableStateOf(initialHolding?.shares?.toString() ?: "") }
    var avgCost by remember { mutableStateOf(initialHolding?.avgCost?.toString() ?: "") }
    var currentPrice by remember { mutableStateOf(initialHolding?.currentPrice?.toString() ?: "") }
    var yieldPct by remember { mutableStateOf(initialHolding?.annualYieldPercent?.toString() ?: "") }
    var stage by remember { mutableStateOf(initialHolding?.stage?.toString() ?: "2") }
    var categoryTag by remember { mutableStateOf(initialHolding?.categoryTag ?: "Core Market Index") }
    var frequency by remember { mutableStateOf(initialHolding?.distributionFrequency ?: "Monthly") }
    var notes by remember { mutableStateOf(initialHolding?.notes ?: "") }

    data class SectorPreset(
        val category: String,
        val defaultTicker: String,
        val defaultName: String,
        val defaultPrice: Double,
        val defaultYield: Double,
        val defaultStage: Int,
        val defaultFreq: String
    )

    val quickAddPresets = listOf(
        SectorPreset("Healthcare & Telehealth", "HIYY", "YieldMax HIMS Option Income Strategy ETF", 9.84, 75.90, 1, "Weekly"),
        SectorPreset("Natural Resources & Energy", "XOMO", "YieldMax XOM Option Income Strategy ETF", 10.26, 45.90, 3, "Weekly"),
        SectorPreset("Real Estate & Infra", "RIET", "Hoya Capital High Dividend Yield ETF", 13.80, 10.80, 2, "Monthly"),
        SectorPreset("Commodities & Gold", "GDXY", "YieldMax Gold Miners ETF", 9.63, 38.20, 3, "Weekly"),
        SectorPreset("S&P 500 Large-Cap", "XDTE", "Roundhill S&P 500 0DTE ETF", 36.49, 27.80, 2, "Weekly"),
        SectorPreset("Russell 2000 Small-Cap", "RDTE", "Roundhill Russell 2000 0DTE", 25.24, 31.93, 2, "Weekly"),
        SectorPreset("Big Tech & FANG+", "FEPI", "REX FANG & Innovation Income", 36.00, 28.31, 2, "Weekly"),
        SectorPreset("Crypto Cash Engine", "MSTY", "YieldMax MSTR Option Strategy", 12.79, 60.91, 1, "Weekly"),
        SectorPreset("Treasury Duration", "TLTW", "iShares 20+ Year Treasury BuyWrite", 21.02, 14.55, 3, "Monthly"),
        SectorPreset("Tax-Exempt T-Bills", "SGOV", "iShares 0-3 Month Treasury Bond", 100.45, 5.15, 4, "Monthly")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialHolding == null) "Add Diversified Asset" else "Edit ${initialHolding.ticker}",
                color = TextPrimary
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (initialHolding == null) {
                    item {
                        Text(
                            text = "QUICK-PRESET VECTOR EXPANSION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = CyanCore
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap a preset vector below to automatically populate ticker, stage, and verified underlying metrics:",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            lineHeight = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(quickAddPresets) { preset ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(TerminalSurfaceVariant)
                                        .border(1.dp, CyanCore.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                        .clickable {
                                            ticker = preset.defaultTicker
                                            name = preset.defaultName
                                            currentPrice = preset.defaultPrice.toString()
                                            if (avgCost.isBlank()) {
                                                avgCost = preset.defaultPrice.toString()
                                            }
                                            yieldPct = preset.defaultYield.toString()
                                            stage = preset.defaultStage.toString()
                                            categoryTag = preset.category
                                            frequency = preset.defaultFreq
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "+ ${preset.defaultTicker} (${preset.category.take(14)}...)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanCore
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                item {
                    OutlinedTextField(
                        value = ticker,
                        onValueChange = { raw ->
                            val clean = raw.uppercase(Locale.US).trim()
                            ticker = clean
                            val tmpl = com.example.engine.RedistributionOptimizer.findCatalogTemplate(clean)
                            if (tmpl != null) {
                                name = tmpl.name
                                currentPrice = String.format(Locale.US, "%.2f", tmpl.currentPrice)
                                if (avgCost.isBlank()) {
                                    avgCost = String.format(Locale.US, "%.2f", tmpl.currentPrice)
                                }
                                yieldPct = String.format(Locale.US, "%.2f", tmpl.annualYieldPercent)
                                stage = tmpl.stage.toString()
                                categoryTag = tmpl.categoryTag
                                frequency = tmpl.distributionFrequency
                            }
                        },
                        label = { Text("Ticker (e.g. VOO, MSTY, SGOV)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Fund / Asset Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text(
                        text = "Diversity Asset Category",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanCore
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(quickAddPresets) { preset ->
                            val isSelected = categoryTag.contains(preset.category, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) EmeraldYieldPrimary.copy(alpha = 0.2f) else TerminalSurfaceVariant)
                                    .border(1.dp, if (isSelected) EmeraldYieldPrimary else TerminalCardBorder, RoundedCornerShape(6.dp))
                                    .clickable { categoryTag = preset.category }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = preset.category,
                                    fontSize = 11.sp,
                                    color = if (isSelected) EmeraldYieldBright else TextPrimary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = categoryTag,
                        onValueChange = { categoryTag = it },
                        label = { Text("Category Tag / Underlying") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = shares,
                            onValueChange = { shares = it },
                            label = { Text("Shares") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = stage,
                            onValueChange = { stage = it },
                            label = { Text("Stage (1-4)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = avgCost,
                            onValueChange = { avgCost = it },
                            label = { Text("Avg Cost ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = currentPrice,
                            onValueChange = { currentPrice = it },
                            label = { Text("Price ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = yieldPct,
                            onValueChange = { yieldPct = it },
                            label = { Text("Yield (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = frequency,
                            onValueChange = { frequency = it },
                            label = { Text("Frequency") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Strategy") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sCount = shares.toDoubleOrNull() ?: 0.0
                    val aCost = avgCost.toDoubleOrNull() ?: 0.0
                    val cPrice = currentPrice.toDoubleOrNull() ?: aCost
                    val yPct = yieldPct.toDoubleOrNull() ?: 0.0
                    val st = stage.toIntOrNull()?.coerceIn(1, 4) ?: 1
                    val resolvedTag = categoryTag.ifBlank { "General Diversified" }

                    if (ticker.isNotBlank()) {
                        val updated = (initialHolding ?: Holding(
                            ticker = ticker,
                            name = name.ifBlank { ticker },
                            shares = sCount,
                            avgCost = aCost,
                            currentPrice = cPrice,
                            annualYieldPercent = yPct,
                            stage = st,
                            categoryTag = resolvedTag,
                            distributionFrequency = frequency,
                            notes = notes
                        )).copy(
                            ticker = ticker,
                            name = name.ifBlank { ticker },
                            shares = sCount,
                            avgCost = aCost,
                            currentPrice = cPrice,
                            annualYieldPercent = yPct,
                            stage = st,
                            categoryTag = resolvedTag,
                            distributionFrequency = frequency,
                            notes = notes
                        )
                        onSave(updated)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldYieldPrimary, contentColor = TerminalVoid)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = TerminalSurface
    )
}
