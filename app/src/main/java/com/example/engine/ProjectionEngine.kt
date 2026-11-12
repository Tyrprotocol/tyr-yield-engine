// File: ProjectionEngine.kt
package com.example.engine

data class ProjectionMilestone(
    val horizonKey: String, // "30D", "90D", "1Y", "5Y", "10Y", "20Y"
    val horizonLabel: String,
    val months: Int,
    val projectedPortfolioBalance: Double,
    val cumulativeDividendsEarned: Double,
    val projectedMonthlyDividendIncome: Double,
    val cumulativeContributions: Double
)

object ProjectionEngine {

    const val PINNED_NON_LIABILITY_NOTICE =
        "NON-LIABILITY STATUTORY LEGAL NOTICE: The calculations, projections, and optimization models displayed in this application are for mathematical modeling, educational research, and algorithmic simulation purposes only. They do NOT constitute financial, investment, legal, or tax advice. Market asset values and dividend payout amounts fluctuate; past dividend yields do not guarantee future distributions. Closed-loop DRIP=OFF strategy requires manual execution through your registered broker-dealer."

    /**
     * Calculates closed-loop dividend cash flow and portfolio trajectory across 6 standard horizons.
     * @param startingBalance Current holdings market value + settled cash.
     * @param blendedYieldPercent Current blended annual yield % (e.g. 7.5).
     * @param monthlyContribution Additional cash deposited per month.
     * @param annualCapitalAppreciation Conservative baseline capital appreciation (default 4.0%).
     */
    fun calculateProjections(
        startingBalance: Double,
        blendedYieldPercent: Double,
        monthlyContribution: Double = 100.0,
        annualCapitalAppreciation: Double = 4.0
    ): List<ProjectionMilestone> {
        val horizons = listOf(
            Triple("30D", "30 Days", 1),
            Triple("90D", "90 Days", 3),
            Triple("1Y", "1 Year", 12),
            Triple("5Y", "5 Years", 60),
            Triple("10Y", "10 Years", 120),
            Triple("20Y", "20 Years", 240)
        )

        val safeStart = if (startingBalance.isFinite() && startingBalance >= 0.0) startingBalance.coerceAtMost(1e12) else 0.0
        val safeYield = if (blendedYieldPercent.isFinite() && blendedYieldPercent >= 0.0) blendedYieldPercent.coerceAtMost(200.0) else 0.0
        val safeContrib = if (monthlyContribution.isFinite() && monthlyContribution >= 0.0) monthlyContribution.coerceAtMost(1e9) else 0.0
        val safeGrowth = if (annualCapitalAppreciation.isFinite()) annualCapitalAppreciation.coerceIn(-50.0, 50.0) else 4.0

        val monthlyYieldRate = (safeYield / 100.0) / 12.0
        val monthlyGrowthRate = (safeGrowth / 100.0) / 12.0

        return horizons.map { (key, label, months) ->
            var currentBalance = safeStart
            var cumulativeDividends = 0.0
            var cumulativeDeposits = 0.0

            for (m in 1..months) {
                // Dividend income generated in month m (DRIP=OFF, pooled and redeployed at start of next cycle)
                val monthlyDividend = currentBalance * monthlyYieldRate
                cumulativeDividends += monthlyDividend

                // Re-deploy dividend cash + monthly contribution into the engine
                cumulativeDeposits += safeContrib
                currentBalance += monthlyDividend + safeContrib

                // Modest asset appreciation
                currentBalance = (currentBalance * (1.0 + monthlyGrowthRate)).coerceAtLeast(0.0)
            }

            val finalMonthlyIncome = currentBalance * monthlyYieldRate

            ProjectionMilestone(
                horizonKey = key,
                horizonLabel = label,
                months = months,
                projectedPortfolioBalance = currentBalance,
                cumulativeDividendsEarned = cumulativeDividends,
                projectedMonthlyDividendIncome = finalMonthlyIncome,
                cumulativeContributions = cumulativeDeposits
            )
        }
    }

    /**
     * Calculates granular, sequential trajectory steps for smooth 60fps interactive Canvas charting.
     * Supports scrubbing along the exact timeline with milestone indicators.
     */
    fun calculateDetailedTrajectory(
        startingBalance: Double,
        blendedYieldPercent: Double,
        defenseRatio: Double,
        horizonKey: String,
        monthlyContribution: Double = 100.0,
        annualCapitalAppreciation: Double = 4.0
    ): List<TrajectoryPoint> {
        val (totalMonths, sampleSteps) = when (horizonKey) {
            "30D" -> 1 to 30 // Daily steps
            "90D" -> 3 to 24 // Bi-weekly steps
            "1Y" -> 12 to 24 // Semi-monthly steps
            "5Y" -> 60 to 30 // Bi-monthly steps
            "10Y" -> 120 to 30 // Quarterly steps
            "20Y" -> 240 to 40 // Semi-annual steps
            else -> 12 to 24
        }

        val safeStart = if (startingBalance.isFinite() && startingBalance >= 0.0) startingBalance.coerceAtMost(1e12) else 0.0
        val safeYield = if (blendedYieldPercent.isFinite() && blendedYieldPercent >= 0.0) blendedYieldPercent.coerceAtMost(200.0) else 0.0
        val safeDefenseRatio = if (defenseRatio.isFinite() && defenseRatio >= 0.0) defenseRatio.coerceAtMost(100.0) else 1.25
        val safeContrib = if (monthlyContribution.isFinite() && monthlyContribution >= 0.0) monthlyContribution.coerceAtMost(1e9) else 0.0
        val safeGrowth = if (annualCapitalAppreciation.isFinite()) annualCapitalAppreciation.coerceIn(-50.0, 50.0) else 4.0

        val monthlyYieldRate = (safeYield / 100.0) / 12.0
        val monthlyGrowthRate = (safeGrowth / 100.0) / 12.0

        val points = mutableListOf<TrajectoryPoint>()
        var currentBalance = safeStart
        var cumulativeDividends = 0.0
        var cumulativeDeposits = 0.0

        // Ratio of defensive assets: Defense / (Defense + Risk)
        // With Defense / Risk = defenseRatio, Defense / Total = defenseRatio / (1 + defenseRatio)
        val defenseShare = (safeDefenseRatio / (1.0 + safeDefenseRatio)).coerceIn(0.20, 0.85)

        val monthsPerStep = totalMonths.toDouble() / sampleSteps.toDouble()

        for (step in 0..sampleSteps) {
            val elapsedMonths = step * monthsPerStep
            val timeLabel = when (horizonKey) {
                "30D" -> "Day ${step.coerceAtLeast(1)}"
                "90D" -> "Week ${(step * 13.0 / sampleSteps).toInt().coerceAtLeast(1)}"
                "1Y" -> if (elapsedMonths < 1.0) "Day ${(elapsedMonths * 30).toInt()}" else "Month ${elapsedMonths.toInt()}"
                "5Y" -> String.format(java.util.Locale.US, "%.1f Yrs (Mo %d)", elapsedMonths / 12.0, elapsedMonths.toInt())
                "10Y" -> String.format(java.util.Locale.US, "%.1f Yrs (Mo %d)", elapsedMonths / 12.0, elapsedMonths.toInt())
                "20Y" -> String.format(java.util.Locale.US, "%.1f Yrs (Mo %d)", elapsedMonths / 12.0, elapsedMonths.toInt())
                else -> "Mo ${elapsedMonths.toInt()}"
            }

            if (step > 0) {
                val stepDiv = currentBalance * monthlyYieldRate * monthsPerStep
                cumulativeDividends += stepDiv
                val stepDeposit = safeContrib * monthsPerStep
                cumulativeDeposits += stepDeposit
                currentBalance += stepDiv + stepDeposit
                currentBalance = (currentBalance * (1.0 + monthlyGrowthRate * monthsPerStep)).coerceAtLeast(0.0)
            }

            val monthlyRunRate = currentBalance * monthlyYieldRate
            val defVal = currentBalance * defenseShare
            val growthVal = currentBalance * (1.0 - defenseShare)

            points.add(
                TrajectoryPoint(
                    stepIndex = step,
                    timeLabel = timeLabel,
                    totalBalance = currentBalance,
                    cumulativeDividends = cumulativeDividends,
                    monthlyDividend = monthlyRunRate,
                    defensiveValue = defVal,
                    growthValue = growthVal,
                    cumulativeContributions = cumulativeDeposits
                )
            )
        }

        return points
    }
}

/**
 * High-resolution trajectory data point for interactive canvas rendering & touch scrubbing.
 */
data class TrajectoryPoint(
    val stepIndex: Int,
    val timeLabel: String,
    val totalBalance: Double,
    val cumulativeDividends: Double,
    val monthlyDividend: Double,
    val defensiveValue: Double,
    val growthValue: Double,
    val cumulativeContributions: Double
)
