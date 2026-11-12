// File: CashRecord.kt
package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks settled buying power cash transactions (deposits, dividend cash collected with DRIP=OFF,
 * and multi-vector redistribution sweep purchases).
 */
@Entity(tableName = "cash_records")
data class CashRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val amount: Double,
    val type: String, // "DEPOSIT", "DIVIDEND", "SWEEP_BUY", "WITHDRAWAL"
    val sourceTicker: String? = null,
    val note: String = ""
)

/**
 * Audit log of an executed Multi-Vector Redistribution Sweep.
 */
@Entity(tableName = "sweep_records")
data class SweepRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val totalCashDeployed: Double,
    val targetsSummary: String, // e.g. "JEPQ (4.2 sh), SCHD (3.1 sh)"
    val blendedYieldAchieved: Double,
    val rationale: String
)
