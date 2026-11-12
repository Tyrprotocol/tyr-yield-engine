// File: AlertEvaluator.kt
package com.example.engine

import com.example.data.model.AlertNotification
import com.example.data.model.AlertRule
import com.example.data.model.AlertType
import com.example.data.model.Holding
import com.example.ui.PortfolioMetrics
import java.util.Locale

object AlertEvaluator {

    /**
     * Evaluates active alert rules against current portfolio telemetry and returns new notifications.
     */
    fun evaluateRules(
        rules: List<AlertRule>,
        currentMetrics: PortfolioMetrics,
        previousMetrics: PortfolioMetrics?,
        holdings: List<Holding>
    ): List<Pair<AlertRule, AlertNotification>> {
        val triggered = mutableListOf<Pair<AlertRule, AlertNotification>>()

        rules.filter { it.isEnabled }.forEach { rule ->
            when (rule.type) {
                AlertType.PORTFOLIO_CHANGE_PCT -> {
                    if (previousMetrics != null && previousMetrics.totalPortfolioValue > 0.0) {
                        val pctChange = ((currentMetrics.totalPortfolioValue - previousMetrics.totalPortfolioValue) / previousMetrics.totalPortfolioValue) * 100.0
                        val isTriggered = when (rule.comparison) {
                            "GREATER_THAN" -> pctChange >= rule.thresholdValue
                            "LESS_THAN" -> pctChange <= -kotlin.math.abs(rule.thresholdValue)
                            else -> kotlin.math.abs(pctChange) >= rule.thresholdValue
                        }
                        if (isTriggered) {
                            val notif = AlertNotification(
                                title = "Portfolio Value Shift (${String.format(Locale.US, "%.1f%%", pctChange)})",
                                message = String.format(
                                    Locale.US,
                                    "Total portfolio value shifted to $%,.2f, triggering your %s threshold.",
                                    currentMetrics.totalPortfolioValue,
                                    rule.label
                                ),
                                severity = if (pctChange >= 0.0) "SUCCESS" else "WARNING"
                            )
                            triggered.add(Pair(rule, notif))
                        }
                    }
                }

                AlertType.ASSET_PRICE_MOVEMENT -> {
                    if (!rule.targetTicker.isNullOrBlank()) {
                        val holding = holdings.find { it.ticker.equals(rule.targetTicker, ignoreCase = true) }
                        if (holding != null) {
                            val isTriggered = when (rule.comparison) {
                                "GREATER_THAN" -> holding.currentPrice >= rule.thresholdValue
                                "LESS_THAN" -> holding.currentPrice <= rule.thresholdValue
                                else -> false
                            }
                            if (isTriggered) {
                                val notif = AlertNotification(
                                    title = "${holding.ticker} Price Target Triggered",
                                    message = String.format(
                                        Locale.US,
                                        "%s reached $%.2f (%s $%.2f threshold).",
                                        holding.ticker,
                                        holding.currentPrice,
                                        if (rule.comparison == "GREATER_THAN") "above" else "below",
                                        rule.thresholdValue
                                    ),
                                    severity = "INFO"
                                )
                                triggered.add(Pair(rule, notif))
                            }
                        }
                    }
                }

                AlertType.DEFENSE_RATIO_TARGET -> {
                    val ratio = currentMetrics.defenseToRiskDensityRatio
                    val isTriggered = when (rule.comparison) {
                        "GREATER_THAN" -> ratio >= rule.thresholdValue
                        "LESS_THAN" -> ratio <= rule.thresholdValue
                        else -> false
                    }
                    if (isTriggered) {
                        val notif = AlertNotification(
                            title = "Defense-to-Risk Target (${String.format(Locale.US, "%.2fx", ratio)})",
                            message = String.format(
                                Locale.US,
                                "Defense-to-Risk Density Ratio achieved %.2fx (Target %s %.2fx). Defense status: %s.",
                                ratio,
                                if (rule.comparison == "GREATER_THAN") ">=" else "<=",
                                rule.thresholdValue,
                                currentMetrics.defenseStatus.name
                            ),
                            severity = if (ratio >= 1.25) "SUCCESS" else "WARNING"
                        )
                        triggered.add(Pair(rule, notif))
                    }
                }

                AlertType.DIVIDEND_PAYMENT -> {
                    // Triggered when dividend is logged
                }
            }
        }

        return triggered
    }
}
