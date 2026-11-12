// File: DashboardConfig.kt
package com.example.data.model

/**
 * User configurable dashboard modules to declutter the home view.
 */
data class DashboardModuleConfig(
    val showRiskRadar: Boolean = true,
    val showPerformanceChart: Boolean = true, // Interactive 60fps trajectory canvas with 3 views & 6 horizons
    val showTyrFinancialExpertCard: Boolean = true, // Týr Protocol Financial Expert card & auditor
    val showScheduleTracker: Boolean = false, // Offloaded to dedicated sub-page by default
    val showStageBreakdowns: Boolean = true,
    val showMarketDiversity: Boolean = false, // Offloaded to Holdings / Analysis sub-page by default
    val showReferralCard: Boolean = false     // Offloaded to Partners sub-page by default
)
