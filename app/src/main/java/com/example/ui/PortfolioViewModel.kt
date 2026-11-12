// File: PortfolioViewModel.kt
package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ChatMessage
import com.example.ai.GeminiPortfolioAdvisor
import com.example.ai.MessageSender
import com.example.data.model.AlertNotification
import com.example.data.model.AlertRule
import com.example.data.model.CashRecord
import com.example.data.model.Holding
import com.example.data.model.MarketRiskBreakdown
import com.example.data.model.RiskSector
import com.example.data.model.SweepRecord
import com.example.data.repository.PortfolioRepository
import com.example.engine.AlertEvaluator
import com.example.engine.ProjectionEngine
import com.example.engine.ProjectionMilestone
import com.example.engine.RedistributionOptimizer
import com.example.engine.SweepOptimizationResult
import com.example.network.FinancialDataService
import com.example.network.PriceRefreshResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

data class PortfolioMetrics(
    val totalPortfolioValue: Double = 0.0,
    val holdingsMarketValue: Double = 0.0,
    val totalCostBasis: Double = 0.0,
    val totalUnrealizedGainLoss: Double = 0.0,
    val totalGainLossPercent: Double = 0.0,
    val annualDividendIncome: Double = 0.0,
    val monthlyDividendIncome: Double = 0.0,
    val blendedYieldPercent: Double = 0.0,
    val settledBuyingPower: Double = 0.0,
    val defenseToRiskDensityRatio: Double = 1.25,
    val defenseStatus: DefenseStatus = DefenseStatus.BALANCED
)

enum class DefenseStatus {
    STRONG,    // >= 1.25x
    BALANCED,  // 1.00x - 1.24x
    HIGH_RISK  // < 1.00x
}

data class StageBreakdown(
    val stage: Int,
    val totalValue: Double,
    val percentageOfPortfolio: Double,
    val annualIncome: Double,
    val holdingsCount: Int
)

enum class TelemetryEventType {
    DIVIDEND,
    DEPOSIT,
    BUY,
    PRICE
}

data class ParsedTelemetryEvent(
    val type: TelemetryEventType,
    val ticker: String = "",
    val amountOrShares: Double = 0.0,
    val unitPrice: Double = 0.0,
    val note: String = "",
    val summary: String = ""
)

data class SweepUndoSnapshot(
    val previousHoldings: List<Holding>,
    val previousSettledCash: Double,
    val description: String
)

class PortfolioViewModel(
    private val repository: PortfolioRepository
) : ViewModel() {

    private val advisor = GeminiPortfolioAdvisor()
    private val financialService = FinancialDataService()

    val holdings: StateFlow<List<Holding>> = repository.allHoldings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settledBuyingPower: StateFlow<Double> = repository.settledBuyingPower
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cashRecords: StateFlow<List<CashRecord>> = repository.allCashRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sweepRecords: StateFlow<List<SweepRecord>> = repository.allSweepRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alertRules: StateFlow<List<AlertRule>> = repository.allAlertRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<AlertNotification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined Portfolio Metrics
    val metrics: StateFlow<PortfolioMetrics> = combine(
        holdings,
        settledBuyingPower
    ) { holdingsList, cash ->
        calculateMetrics(holdingsList, cash)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PortfolioMetrics())

    // Stage Breakdowns
    val stageBreakdowns: StateFlow<List<StageBreakdown>> = combine(
        holdings,
        metrics
    ) { holdingsList, met ->
        (1..4).map { stageNum ->
            val stageHoldings = holdingsList.filter { it.stage == stageNum }
            val stageVal = stageHoldings.sumOf { it.marketValue }
            val stageIncome = stageHoldings.sumOf { it.annualDividendIncome }
            val pct = if (met.totalPortfolioValue > 0.0) (stageVal / met.totalPortfolioValue) * 100.0 else 0.0
            StageBreakdown(
                stage = stageNum,
                totalValue = stageVal,
                percentageOfPortfolio = pct,
                annualIncome = stageIncome,
                holdingsCount = stageHoldings.size
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Market Risk & Sector Diversity Breakdowns (Evaluates all 9 sectors to detect coverage and gaps)
    val marketRiskBreakdowns: StateFlow<List<MarketRiskBreakdown>> = combine(
        holdings,
        metrics
    ) { holdingsList, met ->
        RiskSector.entries.filter { it != RiskSector.GENERAL_DIVERSIFIED }.map { sector ->
            val sectorHoldings = holdingsList.filter { RiskSector.resolve(it) == sector }
            val totalVal = sectorHoldings.sumOf { it.marketValue }
            val pct = if (met.totalPortfolioValue > 0.0) (totalVal / met.totalPortfolioValue) * 100.0 else 0.0
            MarketRiskBreakdown(
                sector = sector,
                totalValue = totalVal,
                currentWeightPercent = pct,
                holdingsCount = sectorHoldings.size,
                isOverConcentrated = pct > sector.maxHealthyWeightPercent,
                isUnderRepresented = sectorHoldings.isEmpty() || pct < (sector.maxHealthyWeightPercent * 0.3)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Sweep Result
    private val _currentSweepResult = MutableStateFlow<SweepOptimizationResult?>(null)
    val currentSweepResult: StateFlow<SweepOptimizationResult?> = _currentSweepResult.asStateFlow()

    // Rollback Checkpoint for Last Executed Sweep
    private var lastSweepSnapshot: SweepUndoSnapshot? = null
    private val _canUndoLastSweep = MutableStateFlow(false)
    val canUndoLastSweep: StateFlow<Boolean> = _canUndoLastSweep.asStateFlow()

    private val _lastSweepSummary = MutableStateFlow<String?>(null)
    val lastSweepSummary: StateFlow<String?> = _lastSweepSummary.asStateFlow()

    // Financial Live Price Refresh State
    private val _isRefreshingPrices = MutableStateFlow(false)
    val isRefreshingPrices: StateFlow<Boolean> = _isRefreshingPrices.asStateFlow()

    private val _priceRefreshStatus = MutableStateFlow("Local Data Verified")
    val priceRefreshStatus: StateFlow<String> = _priceRefreshStatus.asStateFlow()

    // 20-Year Projections State
    private val _monthlyContributionInput = MutableStateFlow(150.0)
    val monthlyContributionInput: StateFlow<Double> = _monthlyContributionInput.asStateFlow()

    private val _selectedHorizon = MutableStateFlow("1Y")
    val selectedHorizon: StateFlow<String> = _selectedHorizon.asStateFlow()

    val projections: StateFlow<List<ProjectionMilestone>> = combine(
        metrics,
        _monthlyContributionInput
    ) { met, contribution ->
        ProjectionEngine.calculateProjections(
            startingBalance = met.totalPortfolioValue,
            blendedYieldPercent = met.blendedYieldPercent.coerceAtLeast(3.5),
            monthlyContribution = contribution
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Portfolio Standing & Live Telemetry Evaluation
    val portfolioStanding: StateFlow<com.example.ai.PortfolioStanding> = combine(
        holdings,
        settledBuyingPower,
        metrics,
        currentSweepResult
    ) { holdingsList, cash, met, sweep ->
        com.example.ai.PortfolioStandingEvaluator.evaluate(
            holdings = holdingsList,
            settledCash = cash,
            defenseRatio = met.defenseToRiskDensityRatio,
            sweepResult = sweep
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        com.example.ai.PortfolioStandingEvaluator.evaluate(emptyList(), 0.0, 1.25)
    )

    // Gemini Chat State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.GEMINI_ARGON,
                text = "Your closed-loop yield engine is online with DRIP=OFF hard-coded. Ask for a deep high-thinking audit, test an allocation scenario, or inspect your next concentrated sweep."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Onboarding Dialog Visibility
    private val _showOnboardingDialog = MutableStateFlow(false)
    val showOnboardingDialog: StateFlow<Boolean> = _showOnboardingDialog.asStateFlow()

    private var previousMetrics: PortfolioMetrics? = null

    init {
        viewModelScope.launch {
            // Seed default alert rules if empty
            repository.seedDefaultAlertRulesIfEmpty()

            // Check if initial database is empty on first launch
            repository.allHoldings.collect { currentList ->
                if (currentList.isEmpty()) {
                    _showOnboardingDialog.value = true
                }
            }
        }

        // Automatic Alert Evaluation
        viewModelScope.launch {
            metrics.collect { current ->
                val prev = previousMetrics
                if (prev != null && prev.totalPortfolioValue > 0.0) {
                    val rules = repository.allAlertRules.first()
                    val hList = holdings.value
                    val triggered = AlertEvaluator.evaluateRules(rules, current, prev, hList)
                    triggered.forEach { (_, notif) ->
                        repository.insertNotification(notif)
                    }
                }
                previousMetrics = current
            }
        }
    }

    fun dismissOnboardingDialog() {
        _showOnboardingDialog.value = false
    }

    fun seedCustomPortfolio(customHoldings: List<Holding>, startingCash: Double) {
        viewModelScope.launch {
            repository.seedCustomHoldings(customHoldings, startingCash)
            _showOnboardingDialog.value = false
        }
    }

    fun startFresh(startingCash: Double) {
        viewModelScope.launch {
            repository.seedFreshStart(startingCash)
            _showOnboardingDialog.value = false
        }
    }

    fun refreshMarketPrices() {
        viewModelScope.launch {
            _isRefreshingPrices.value = true
            val currentList = holdings.value
            val tickers = currentList.map { it.ticker }

            when (val result = financialService.fetchQuotes(tickers)) {
                is PriceRefreshResult.Success -> {
                    repository.updateHoldingPrices(result.quotes)
                    _priceRefreshStatus.value = "Live Quotes Updated (${result.quotes.size} assets)"
                }
                is PriceRefreshResult.RateLimited -> {
                    _priceRefreshStatus.value = result.message
                }
                is PriceRefreshResult.Error -> {
                    _priceRefreshStatus.value = result.message
                }
            }
            _isRefreshingPrices.value = false
        }
    }

    fun addAlertRule(rule: AlertRule) {
        viewModelScope.launch {
            repository.insertAlertRule(rule)
        }
    }

    fun toggleAlertRule(rule: AlertRule) {
        viewModelScope.launch {
            repository.updateAlertRule(rule.copy(isEnabled = !rule.isEnabled))
        }
    }

    fun deleteAlertRule(rule: AlertRule) {
        viewModelScope.launch {
            repository.deleteAlertRule(rule)
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    fun calculateSweep(availableCash: Double) {
        val currentList = holdings.value
        val ratio = metrics.value.defenseToRiskDensityRatio
        val result = RedistributionOptimizer.optimizeSweep(currentList, availableCash, ratio)
        _currentSweepResult.value = result
    }

    fun executeSweep(result: SweepOptimizationResult) {
        viewModelScope.launch {
            if (result.targets.isEmpty() || result.totalCashToDeploy <= 0.0) return@launch

            // 0. Save Rollback Snapshot before mutating state
            val preSweepHoldings = holdings.value.map { it.copy() }
            val preSweepCash = settledBuyingPower.value
            val summaryStr = result.targets.joinToString(", ") {
                String.format(Locale.US, "%s (%.2f sh @ $%.2f)", it.ticker, it.shareCount, it.currentPrice)
            }
            lastSweepSnapshot = SweepUndoSnapshot(
                previousHoldings = preSweepHoldings,
                previousSettledCash = preSweepCash,
                description = String.format(Locale.US, "$%.2f -> %s", result.totalCashToDeploy, summaryStr)
            )
            _canUndoLastSweep.value = true
            _lastSweepSummary.value = lastSweepSnapshot?.description

            // 1. Deduct cash from settled buying power
            repository.addCashTransaction(
                amount = result.totalCashToDeploy,
                type = "SWEEP_BUY",
                note = "Closed-loop sweep into ${result.targets.size} targets."
            )

            // 2. Add purchased shares to holdings
            val currentList = holdings.value.toMutableList()
            result.targets.forEach { target ->
                val existing = currentList.find { it.ticker.equals(target.ticker, ignoreCase = true) }
                if (existing != null) {
                    val newTotalShares = existing.shares + target.shareCount
                    val newTotalCost = existing.totalCostBasis + target.allocatedCash
                    val newAvgCost = if (newTotalShares > 0.0) newTotalCost / newTotalShares else existing.avgCost
                    repository.updateHolding(
                        existing.copy(
                            shares = newTotalShares,
                            avgCost = newAvgCost
                        )
                    )
                } else {
                    val template = RedistributionOptimizer.findCatalogTemplate(target.ticker)
                    repository.insertHolding(
                        Holding(
                            ticker = target.ticker,
                            name = target.name,
                            shares = target.shareCount,
                            avgCost = target.currentPrice,
                            currentPrice = target.currentPrice,
                            annualYieldPercent = target.annualYieldPercent,
                            stage = target.stage,
                            categoryTag = template?.categoryTag ?: target.riskSectorName.ifBlank { "Sweep Allocation" },
                            distributionFrequency = template?.distributionFrequency ?: "Weekly",
                            notes = template?.notes ?: "DRIP=OFF Closed-Loop Gap Sweep"
                        )
                    )
                }
            }

            // 3. Record audit log
            repository.recordSweep(
                SweepRecord(
                    totalCashDeployed = result.totalCashToDeploy,
                    targetsSummary = summaryStr,
                    blendedYieldAchieved = result.blendedYieldAchieved,
                    rationale = result.plainEnglishSummary
                )
            )

            // Trigger notification
            repository.insertNotification(
                AlertNotification(
                    title = "Sweep Executed ($" + String.format(Locale.US, "%.2f", result.totalCashToDeploy) + ")",
                    message = "Redistributed into ${result.targets.size} targets at ${String.format(Locale.US, "%.2f%%", result.blendedYieldAchieved)} blended yield.",
                    severity = "SUCCESS"
                )
            )

            _currentSweepResult.value = null
        }
    }

    fun undoLastSweep() {
        val snapshot = lastSweepSnapshot ?: return
        viewModelScope.launch {
            repository.seedCustomHoldings(
                customHoldings = snapshot.previousHoldings,
                startingCash = snapshot.previousSettledCash
            )
            repository.insertNotification(
                AlertNotification(
                    title = "Sweep Rolled Back",
                    message = "Restored portfolio state and settled cash ($${String.format(Locale.US, "%.2f", snapshot.previousSettledCash)}) prior to ${snapshot.description}.",
                    severity = "INFO"
                )
            )
            lastSweepSnapshot = null
            _canUndoLastSweep.value = false
            _lastSweepSummary.value = null
        }
    }

    fun parseTelemetryBatch(rawText: String): List<ParsedTelemetryEvent> {
        val events = mutableListOf<ParsedTelemetryEvent>()
        val tickerRegex = Regex("^[A-Z0-9.\\-]{1,10}$")

        rawText.lines().take(500).forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#")) return@forEach

            val tokens = line.split(Regex("\\s+"))
            val upperFirst = tokens.firstOrNull()?.uppercase(Locale.US).orEmpty()
            val upperSecond = tokens.getOrNull(1)?.uppercase(Locale.US).orEmpty()

            when {
                upperFirst == "DIVIDEND" && tokens.size == 3 && !upperSecond.startsWith("RECEIVED") && !upperSecond.startsWith("PAID") -> {
                    val ticker = upperSecond
                    val amt = tokens[2].replace("$", "").toDoubleOrNull() ?: 0.0
                    if (tickerRegex.matches(ticker) && !ticker.contains("..") && amt > 0.0 && amt <= 1e9 && amt.isFinite()) {
                        events.add(
                            ParsedTelemetryEvent(
                                type = TelemetryEventType.DIVIDEND,
                                ticker = ticker,
                                amountOrShares = amt,
                                summary = String.format(Locale.US, "Log Dividend: +$%.2f from %s (DRIP=OFF)", amt, ticker)
                            )
                        )
                    }
                }
                upperFirst == "DEPOSIT" && tokens.size >= 2 -> {
                    val amt = tokens[1].replace("$", "").toDoubleOrNull() ?: 0.0
                    val note = if (tokens.size > 2) tokens.drop(2).joinToString(" ").take(80) else "Telemetry Deposit"
                    if (amt > 0.0 && amt <= 1e9 && amt.isFinite()) {
                        events.add(
                            ParsedTelemetryEvent(
                                type = TelemetryEventType.DEPOSIT,
                                amountOrShares = amt,
                                note = note,
                                summary = String.format(Locale.US, "Deposit Cash: +$%.2f (%s)", amt, note)
                            )
                        )
                    }
                }
                upperFirst == "BUY" && tokens.size >= 3 -> {
                    val ticker = tokens[1].uppercase(Locale.US)
                    val shares = tokens[2].toDoubleOrNull() ?: 0.0
                    val priceToken = tokens.drop(3).firstOrNull { it != "@" }?.replace("$", "")
                    val price = priceToken?.toDoubleOrNull() ?: 0.0
                    if (tickerRegex.matches(ticker) && !ticker.contains("..") && shares > 0.0 && shares <= 1e9 && shares.isFinite() && price >= 0.0 && price <= 1e9 && price.isFinite()) {
                        events.add(
                            ParsedTelemetryEvent(
                                type = TelemetryEventType.BUY,
                                ticker = ticker,
                                amountOrShares = shares,
                                unitPrice = price,
                                summary = if (price > 0.0) {
                                    String.format(Locale.US, "Buy Shares: +%.3f %s @ $%.2f", shares, ticker, price)
                                } else {
                                    String.format(Locale.US, "Buy Shares: +%.3f %s", shares, ticker)
                                }
                            )
                        )
                    }
                }
                upperFirst == "PRICE" && tokens.size >= 3 -> {
                    val ticker = tokens[1].uppercase(Locale.US)
                    val price = tokens[2].replace("$", "").toDoubleOrNull() ?: 0.0
                    if (tickerRegex.matches(ticker) && !ticker.contains("..") && price > 0.0 && price <= 1e9 && price.isFinite()) {
                        events.add(
                            ParsedTelemetryEvent(
                                type = TelemetryEventType.PRICE,
                                ticker = ticker,
                                unitPrice = price,
                                summary = String.format(Locale.US, "Update Price: %s -> $%.2f", ticker, price)
                            )
                        )
                    }
                }
                line.contains("dividend", ignoreCase = true) && !line.contains("..") && !line.contains("/") && !line.contains("\\") -> {
                    val amtMatch = Regex("(?<![0-9.\\-])\\$?([0-9]{1,9}\\.[0-9]{1,2})(?![0-9])").find(line)
                    val tickerMatch = Regex("\\b([A-Z]{3,5})\\b").findAll(line)
                        .map { it.groupValues[1] }
                        .firstOrNull { it !in setOf("FROM", "PAID", "CASH", "DRIP", "USD", "DIVIDEND") }
                    val amt = amtMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                    if (tickerMatch != null && amt > 0.0 && amt <= 1e9 && amt.isFinite()) {
                        events.add(
                            ParsedTelemetryEvent(
                                type = TelemetryEventType.DIVIDEND,
                                ticker = tickerMatch,
                                amountOrShares = amt,
                                summary = String.format(Locale.US, "Log Dividend: +$%.2f from %s (DRIP=OFF)", amt, tickerMatch)
                            )
                        )
                    }
                }
            }
        }
        return events
    }

    fun applyTelemetryBatch(events: List<ParsedTelemetryEvent>) {
        if (events.isEmpty()) return
        viewModelScope.launch {
            val currentList = holdings.value.toMutableList()

            events.forEach { ev ->
                when (ev.type) {
                    TelemetryEventType.DIVIDEND -> {
                        repository.addCashTransaction(
                            amount = ev.amountOrShares,
                            type = "DIVIDEND",
                            ticker = ev.ticker,
                            note = "Telemetry Dividend from ${ev.ticker} (DRIP=OFF)"
                        )
                    }
                    TelemetryEventType.DEPOSIT -> {
                        repository.addCashTransaction(
                            amount = ev.amountOrShares,
                            type = "DEPOSIT",
                            note = ev.note.ifBlank { "Telemetry Deposit" }
                        )
                    }
                    TelemetryEventType.BUY -> {
                        val existing = currentList.find { it.ticker.equals(ev.ticker, ignoreCase = true) }
                        if (existing != null) {
                            val buyPrice = if (ev.unitPrice > 0.0) ev.unitPrice else existing.currentPrice
                            val newShares = existing.shares + ev.amountOrShares
                            val newTotalCost = existing.totalCostBasis + (ev.amountOrShares * buyPrice)
                            val newAvgCost = if (newShares > 0.0) newTotalCost / newShares else existing.avgCost
                            val updated = existing.copy(
                                shares = newShares,
                                avgCost = newAvgCost,
                                currentPrice = buyPrice
                            )
                            repository.updateHolding(updated)
                            val idx = currentList.indexOfFirst { it.id == existing.id }
                            if (idx >= 0) currentList[idx] = updated
                        } else {
                            val template = RedistributionOptimizer.findCatalogTemplate(ev.ticker)
                            val buyPrice = when {
                                ev.unitPrice > 0.0 -> ev.unitPrice
                                template != null -> template.currentPrice
                                else -> 25.0
                            }
                            val inserted = Holding(
                                ticker = ev.ticker,
                                name = template?.name ?: "${ev.ticker} Holding",
                                shares = ev.amountOrShares,
                                avgCost = buyPrice,
                                currentPrice = buyPrice,
                                annualYieldPercent = template?.annualYieldPercent ?: 12.0,
                                stage = template?.stage ?: 2,
                                categoryTag = template?.categoryTag ?: "Telemetry Import",
                                distributionFrequency = template?.distributionFrequency ?: "Monthly",
                                notes = "Imported via Local Telemetry Sync"
                            )
                            repository.insertHolding(inserted)
                        }
                    }
                    TelemetryEventType.PRICE -> {
                        val existing = currentList.find { it.ticker.equals(ev.ticker, ignoreCase = true) }
                        if (existing != null && ev.unitPrice > 0.0) {
                            val updated = existing.copy(currentPrice = ev.unitPrice)
                            repository.updateHolding(updated)
                            val idx = currentList.indexOfFirst { it.id == existing.id }
                            if (idx >= 0) currentList[idx] = updated
                        }
                    }
                }
            }
        }
    }

    fun addManualDeposit(amount: Double, note: String = "Cash Deposit") {
        viewModelScope.launch {
            if (amount > 0.0) {
                repository.addCashTransaction(amount, "DEPOSIT", note = note)
            }
        }
    }

    fun logDividendPayout(ticker: String, amount: Double) {
        viewModelScope.launch {
            if (amount > 0.0) {
                repository.addCashTransaction(
                    amount = amount,
                    type = "DIVIDEND",
                    ticker = ticker,
                    note = "Dividend payout from $ticker (DRIP=OFF)"
                )
                repository.insertNotification(
                    AlertNotification(
                        title = "Dividend Received: $ticker (+$" + String.format(Locale.US, "%.2f", amount) + ")",
                        message = "Cash pooled into settled buying power with DRIP=OFF.",
                        severity = "SUCCESS"
                    )
                )
            }
        }
    }

    fun saveHolding(holding: Holding) {
        viewModelScope.launch {
            if (holding.id == 0L) {
                repository.insertHolding(holding)
            } else {
                repository.updateHolding(holding)
            }
        }
    }

    fun deleteHolding(holding: Holding) {
        viewModelScope.launch {
            repository.deleteHolding(holding)
        }
    }

    fun setMonthlyContribution(value: Double) {
        _monthlyContributionInput.value = value
    }

    fun setSelectedHorizon(key: String) {
        _selectedHorizon.value = key
    }

    // Gemini AI Chat Actions
    fun sendChatMessage(userText: String, useHighThinking: Boolean = false) {
        if (userText.isBlank()) return

        val userMsg = ChatMessage(sender = MessageSender.USER, text = userText)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiThinking.value = true

        viewModelScope.launch {
            val hList = holdings.value
            val cash = settledBuyingPower.value
            val ratio = metrics.value.defenseToRiskDensityRatio
            val standing = portfolioStanding.value

            val responseText = if (useHighThinking || userText.contains("audit", ignoreCase = true) || userText.contains("risk", ignoreCase = true) || userText.contains("stress", ignoreCase = true) || userText.contains("route", ignoreCase = true)) {
                advisor.auditPortfolioWithThinking(hList, cash, ratio, standing, userText)
            } else {
                advisor.askConversational(_chatMessages.value, userText, hList, cash, ratio, standing)
            }

            val replyMsg = ChatMessage(
                sender = if (useHighThinking) MessageSender.GEMINI_ARGON else MessageSender.GEMINI_FLASH,
                text = responseText,
                isThinking = useHighThinking,
                actionableRoute = standing.suggestedRoute
            )
            _chatMessages.value = _chatMessages.value + replyMsg
            _isAiThinking.value = false
        }
    }

    fun applyRouteToSweep(route: com.example.ai.ActionableTradeRoute) {
        calculateSweep(route.totalCapitalToDeploy)
    }

    suspend fun exportJson(): String {
        return repository.exportToJson(holdings.value, cashRecords.value)
    }

    suspend fun importJson(json: String): Boolean {
        return repository.importFromJson(json)
    }

    private fun calculateMetrics(holdingsList: List<Holding>, cash: Double): PortfolioMetrics {
        val holdingsVal = holdingsList.sumOf { it.marketValue }
        val totalVal = holdingsVal + cash
        val costBasis = holdingsList.sumOf { it.totalCostBasis }
        val gainLoss = holdingsVal - costBasis
        val gainLossPct = if (costBasis > 0.0) (gainLoss / costBasis) * 100.0 else 0.0

        val annualIncome = holdingsList.sumOf { it.annualDividendIncome }
        val monthlyIncome = annualIncome / 12.0
        val blendedYield = if (holdingsVal > 0.0) (annualIncome / holdingsVal) * 100.0 else 0.0

        val defenseVal = holdingsList.filter { it.stage == 2 || it.stage == 4 }.sumOf { it.marketValue } + cash
        val riskVal = holdingsList.filter { it.stage == 1 || it.stage == 3 }.sumOf { it.marketValue }

        val ratio = when {
            riskVal <= 0.0 -> if (defenseVal > 0.0) 2.5 else 1.25
            else -> defenseVal / riskVal
        }

        val status = when {
            ratio >= 1.25 -> DefenseStatus.STRONG
            ratio >= 1.0 -> DefenseStatus.BALANCED
            else -> DefenseStatus.HIGH_RISK
        }

        return PortfolioMetrics(
            totalPortfolioValue = totalVal,
            holdingsMarketValue = holdingsVal,
            totalCostBasis = costBasis,
            totalUnrealizedGainLoss = gainLoss,
            totalGainLossPercent = gainLossPct,
            annualDividendIncome = annualIncome,
            monthlyDividendIncome = monthlyIncome,
            blendedYieldPercent = blendedYield,
            settledBuyingPower = cash,
            defenseToRiskDensityRatio = ratio,
            defenseStatus = status
        )
    }
}
