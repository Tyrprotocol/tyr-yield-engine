// File: PortfolioRepository.kt
package com.example.data.repository

import com.example.data.db.AlertDao
import com.example.data.db.CashDao
import com.example.data.db.HoldingDao
import com.example.data.model.AlertNotification
import com.example.data.model.AlertRule
import com.example.data.model.AlertType
import com.example.data.model.CashRecord
import com.example.data.model.Holding
import com.example.data.model.SweepRecord
import com.example.network.MarketQuote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class PortfolioRepository(
    private val holdingDao: HoldingDao,
    private val cashDao: CashDao,
    private val alertDao: AlertDao
) {
    val allHoldings: Flow<List<Holding>> = holdingDao.getAllHoldings()
    val allCashRecords: Flow<List<CashRecord>> = cashDao.getAllCashRecords()
    val allSweepRecords: Flow<List<SweepRecord>> = cashDao.getAllSweepRecords()
    val allAlertRules: Flow<List<AlertRule>> = alertDao.getAllAlertRules()
    val allNotifications: Flow<List<AlertNotification>> = alertDao.getAllNotifications()

    val settledBuyingPower: Flow<Double> = allCashRecords.map { records ->
        records.sumOf { record ->
            when (record.type) {
                "DEPOSIT", "DIVIDEND" -> record.amount
                "SWEEP_BUY", "WITHDRAWAL" -> -record.amount
                else -> 0.0
            }
        }
    }

    suspend fun insertHolding(holding: Holding) = holdingDao.insertHolding(holding)

    suspend fun updateHolding(holding: Holding) = holdingDao.updateHolding(holding)

    suspend fun deleteHolding(holding: Holding) = holdingDao.deleteHolding(holding)

    suspend fun addCashTransaction(amount: Double, type: String, ticker: String? = null, note: String = "") {
        cashDao.insertCashRecord(
            CashRecord(
                amount = amount,
                type = type,
                sourceTicker = ticker,
                note = note
            )
        )
    }

    suspend fun recordSweep(sweepRecord: SweepRecord) = cashDao.insertSweepRecord(sweepRecord)

    suspend fun insertAlertRule(rule: AlertRule) = alertDao.insertAlertRule(rule)

    suspend fun updateAlertRule(rule: AlertRule) = alertDao.updateAlertRule(rule)

    suspend fun deleteAlertRule(rule: AlertRule) = alertDao.deleteAlertRule(rule)

    suspend fun insertNotification(notification: AlertNotification) = alertDao.insertNotification(notification)

    suspend fun markNotificationAsRead(id: Long) = alertDao.markNotificationAsRead(id)

    suspend fun clearAllNotifications() = alertDao.clearAllNotifications()

    suspend fun clearPortfolio() {
        holdingDao.deleteAllHoldings()
        cashDao.deleteAllCashRecords()
        cashDao.deleteAllSweepRecords()
    }

    suspend fun seedDefaultAlertRulesIfEmpty() {
        val rules = alertDao.getAllAlertRules().first()
        if (rules.isEmpty()) {
            val defaults = listOf(
                AlertRule(
                    type = AlertType.DEFENSE_RATIO_TARGET,
                    thresholdValue = 1.25,
                    comparison = "GREATER_THAN",
                    label = "Defense Shield Reached (>= 1.25x)"
                ),
                AlertRule(
                    type = AlertType.PORTFOLIO_CHANGE_PCT,
                    thresholdValue = 5.0,
                    comparison = "GREATER_THAN",
                    label = "Portfolio Gain Target (+5%)"
                ),
                AlertRule(
                    type = AlertType.PORTFOLIO_CHANGE_PCT,
                    thresholdValue = 5.0,
                    comparison = "LESS_THAN",
                    label = "Drawdown Alert (-5%)"
                ),
                AlertRule(
                    type = AlertType.ASSET_PRICE_MOVEMENT,
                    targetTicker = "JEPQ",
                    thresholdValue = 56.50,
                    comparison = "GREATER_THAN",
                    label = "JEPQ Breakout Target ($56.50)"
                )
            )
            defaults.forEach { alertDao.insertAlertRule(it) }
        }
    }

    suspend fun updateHoldingPrices(quotes: Map<String, MarketQuote>) {
        val currentHoldings = holdingDao.getAllHoldings().first()
        currentHoldings.forEach { holding ->
            val quote = quotes[holding.ticker.uppercase()]
            if (quote != null && quote.currentPrice > 0.0) {
                holdingDao.updateHolding(holding.copy(currentPrice = quote.currentPrice))
            }
        }
    }

    /**
     * Seeds user's tailored custom holdings.
     */
    suspend fun seedCustomHoldings(customHoldings: List<Holding>, startingCash: Double) {
        clearPortfolio()
        if (customHoldings.isNotEmpty()) {
            holdingDao.insertAll(customHoldings)
        }
        if (startingCash > 0.0) {
            cashDao.insertCashRecord(
                CashRecord(
                    amount = startingCash,
                    type = "DEPOSIT",
                    sourceTicker = "STARTER",
                    note = "Initial settled buying power for tailored portfolio start."
                )
            )
        }
    }

    /**
     * Seeds Mode A: Fresh Start ($0.00 Base) with initial cash deposit.
     */
    suspend fun seedFreshStart(initialCash: Double) {
        clearPortfolio()
        if (initialCash > 0.0) {
            cashDao.insertCashRecord(
                CashRecord(
                    amount = initialCash,
                    type = "DEPOSIT",
                    sourceTicker = "STARTER",
                    note = "Initial starter capital for closed-loop engine foundation."
                )
            )
        }
    }

    /**
     * Exports entire portfolio database to JSON string for local backup.
     */
    suspend fun exportToJson(currentHoldings: List<Holding>, cashRecords: List<CashRecord>): String {
        val root = JSONObject()
        val holdingsArray = JSONArray()
        currentHoldings.forEach { h ->
            val obj = JSONObject()
            obj.put("ticker", h.ticker)
            obj.put("name", h.name)
            obj.put("shares", h.shares)
            obj.put("avgCost", h.avgCost)
            obj.put("currentPrice", h.currentPrice)
            obj.put("annualYieldPercent", h.annualYieldPercent)
            obj.put("stage", h.stage)
            obj.put("categoryTag", h.categoryTag)
            obj.put("distributionFrequency", h.distributionFrequency)
            obj.put("notes", h.notes)
            holdingsArray.put(obj)
        }
        root.put("holdings", holdingsArray)
        root.put("exportTimestamp", System.currentTimeMillis())
        return root.toString(2)
    }

    /**
     * Imports portfolio from JSON backup string with strict payload size caps,
     * loop iteration bounds, ticker sanitization, and finite numeric validation.
     */
    suspend fun importFromJson(jsonString: String): Boolean {
        if (jsonString.isBlank() || jsonString.length > 262_144) {
            return false
        }
        return try {
            val tickerRegex = Regex("^[A-Z][A-Z0-9.\\-]{0,9}$")
            val root = JSONObject(jsonString)
            val holdingsArray = root.getJSONArray("holdings")
            val maxItems = holdingsArray.length().coerceAtMost(500)
            if (maxItems == 0) return false

            val importedHoldings = mutableListOf<Holding>()
            for (i in 0 until maxItems) {
                val obj = holdingsArray.getJSONObject(i)
                val rawTicker = obj.getString("ticker").trim().uppercase()
                if (!tickerRegex.matches(rawTicker) || rawTicker.contains("..")) return false

                val rawName = obj.getString("name").trim().take(120)
                if (rawName.isEmpty()) return false

                val shares = obj.getDouble("shares")
                val avgCost = obj.getDouble("avgCost")
                val currentPrice = obj.getDouble("currentPrice")
                val annualYieldPercent = obj.getDouble("annualYieldPercent")
                val stage = obj.getInt("stage")

                if (!shares.isFinite() || shares < 0.0) return false
                if (!avgCost.isFinite() || avgCost < 0.0) return false
                if (!currentPrice.isFinite() || currentPrice <= 0.0) return false
                if (!annualYieldPercent.isFinite() || annualYieldPercent < 0.0 || annualYieldPercent > 500.0) return false
                if (stage !in 1..4) return false

                importedHoldings.add(
                    Holding(
                        ticker = rawTicker,
                        name = rawName,
                        shares = shares,
                        avgCost = avgCost,
                        currentPrice = currentPrice,
                        annualYieldPercent = annualYieldPercent,
                        stage = stage,
                        categoryTag = obj.optString("categoryTag", "General").trim().take(80),
                        distributionFrequency = obj.optString("distributionFrequency", "Monthly").trim().take(40),
                        notes = obj.optString("notes", "").trim().take(240)
                    )
                )
            }
            if (importedHoldings.isNotEmpty()) {
                clearPortfolio()
                holdingDao.insertAll(importedHoldings)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
