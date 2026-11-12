// File: Holding.kt
package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an individual investment vehicle within the Closed-Loop Yield Engine.
 * Supports Stage 1 (Cash Flow), Stage 2 (Stability), Stage 3 (Growth), Stage 4 (Reserves).
 */
@Entity(tableName = "holdings")
data class Holding(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ticker: String,
    val name: String,
    val shares: Double,
    val avgCost: Double,
    val currentPrice: Double,
    val annualYieldPercent: Double,
    val stage: Int, // 1, 2, 3, or 4
    val categoryTag: String,
    val distributionFrequency: String = "Monthly", // "Monthly", "Quarterly", "Weekly"
    val notes: String = ""
) {
    val marketValue: Double
        get() = shares * currentPrice

    val totalCostBasis: Double
        get() = shares * avgCost

    val unrealizedGainLoss: Double
        get() = marketValue - totalCostBasis

    val unrealizedGainLossPercent: Double
        get() = if (totalCostBasis > 0.0) ((marketValue - totalCostBasis) / totalCostBasis) * 100.0 else 0.0

    val annualDividendIncome: Double
        get() = marketValue * (annualYieldPercent / 100.0)

    val monthlyDividendIncome: Double
        get() = annualDividendIncome / 12.0
}
