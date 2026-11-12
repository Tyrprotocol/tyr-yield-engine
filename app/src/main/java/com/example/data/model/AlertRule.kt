// File: AlertRule.kt
package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AlertType {
    PORTFOLIO_CHANGE_PCT,   // e.g. Portfolio moves +/- 5%
    ASSET_PRICE_MOVEMENT,   // e.g. JEPQ drops below $52.00 or crosses $58.00
    DIVIDEND_PAYMENT,       // e.g. Dividend received with DRIP=OFF
    DEFENSE_RATIO_TARGET    // e.g. Defense-to-Risk ratio >= 1.25x achieved
}

@Entity(tableName = "alert_rules")
data class AlertRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: AlertType,
    val targetTicker: String? = null,
    val thresholdValue: Double,
    val comparison: String = "GREATER_THAN", // "GREATER_THAN", "LESS_THAN"
    val isEnabled: Boolean = true,
    val label: String,
    val lastTriggeredTimestamp: Long = 0L
)

@Entity(tableName = "alert_notifications")
data class AlertNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val message: String,
    val severity: String = "INFO", // "INFO", "SUCCESS", "WARNING", "ALERT"
    val isRead: Boolean = false
)
