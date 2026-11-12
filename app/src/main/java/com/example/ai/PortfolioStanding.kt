// File: PortfolioStanding.kt
package com.example.ai

import com.example.data.model.Holding
import com.example.data.model.MarketRiskBreakdown
import com.example.data.model.RiskSector
import com.example.engine.SweepOptimizationResult
import java.util.Locale

/**
 * Encapsulates real-time mathematical standing metrics for portfolio health,
 * gap coverage across sectors/tiers, defense-to-risk density, and capital efficiency.
 */
data class PortfolioStanding(
    val totalCapital: Double,
    val settledBuyingPower: Double,
    val annualIncome: Double,
    val monthlyIncome: Double,
    val blendedYieldPercent: Double,
    val defenseToRiskRatio: Double,
    val defenseStatusGrade: String,      // "A+" (>=1.30x), "A" (>=1.25x), "B" (1.00-1.24x), "C" (<1.00x)
    val capitalEfficiencyScore: Int,     // 0 to 100
    val coveredSectorsCount: Int,
    val totalSectorsCount: Int,
    val unheldSectors: List<RiskSector>,
    val stageWeights: Map<Int, Double>,
    val suggestedRoute: ActionableTradeRoute? = null,
    val yieldSustainabilityScore: Int = 92,
    val taxEfficiencyScore: Double = 68.0,
    val financialExpertGrade: String = "Sovereign Fortress A+",
    val financialExpertVerdict: String = "Closed-loop cash recycling active under DRIP=OFF. High yield velocity is armored by defense density."
)

/**
 * Structured actionable trade route proposed by AI or heuristic calculations.
 */
data class ActionableTradeRoute(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val plainEnglishRationale: String,
    val totalCapitalToDeploy: Double,
    val expectedBlendedYield: Double,
    val projectedMonthlyCashFlow: Double,
    val targetAllocations: List<RouteAllocation>,
    val defenseRatioBefore: Double,
    val defenseRatioAfter: Double,
    val filledGaps: List<String>
)

data class RouteAllocation(
    val ticker: String,
    val name: String,
    val stage: Int,
    val sectorName: String,
    val dollarAmount: Double,
    val estimatedShares: Double,
    val sharePrice: Double,
    val annualYieldPercent: Double,
    val isGapFiller: Boolean,
    val isYieldChampion: Boolean
)

object PortfolioStandingEvaluator {

    /**
     * Evaluates live telemetry into a comprehensive standing assessment.
     */
    fun evaluate(
        holdings: List<Holding>,
        settledCash: Double,
        defenseRatio: Double,
        sweepResult: SweepOptimizationResult? = null
    ): PortfolioStanding {
        val holdingsVal = holdings.sumOf { it.marketValue }
        val totalCapital = holdingsVal + settledCash
        val annualIncome = holdings.sumOf { it.annualDividendIncome }
        val monthlyIncome = annualIncome / 12.0
        val blendedYield = if (holdingsVal > 0.0) (annualIncome / holdingsVal) * 100.0 else 0.0

        // Sector gap analysis
        val heldSectors = holdings
            .filter { it.shares > 0.0 }
            .map { RiskSector.resolve(it) }
            .toSet()

        val allValidSectors = RiskSector.entries.filter { it != RiskSector.GENERAL_DIVERSIFIED }
        val unheld = allValidSectors.filter { it !in heldSectors }

        // Stage distribution weights
        val stageWeights = (1..4).associateWith { stageNum ->
            val stageVal = holdings.filter { it.stage == stageNum }.sumOf { it.marketValue }
            if (totalCapital > 0.0) (stageVal / totalCapital) * 100.0 else 0.0
        }

        // Efficiency score: Combines Defense density health (40%), Yield velocity (30%), and Sector diversity (30%)
        val defenseScore = when {
            defenseRatio >= 1.35 -> 40
            defenseRatio >= 1.25 -> 38
            defenseRatio >= 1.10 -> 30
            defenseRatio >= 1.00 -> 24
            else -> 12
        }

        val yieldScore = when {
            blendedYield >= 9.0 -> 30
            blendedYield >= 7.0 -> 27
            blendedYield >= 5.0 -> 22
            blendedYield >= 3.0 -> 16
            else -> 10
        }

        val sectorCoveragePct = if (allValidSectors.isNotEmpty()) {
            (heldSectors.size.toDouble() / allValidSectors.size.toDouble())
        } else 1.0
        val diversityScore = (sectorCoveragePct * 30).toInt().coerceIn(0, 30)

        val totalEfficiency = (defenseScore + yieldScore + diversityScore).coerceIn(10, 100)

        // Financial Expert: Yield sustainability based on defense density and asset stage distribution
        val stage1Val = holdings.filter { it.stage == 1 }.sumOf { it.marketValue }
        val stage2Val = holdings.filter { it.stage == 2 }.sumOf { it.marketValue }
        val stage4Val = holdings.filter { it.stage == 4 }.sumOf { it.marketValue } + settledCash
        val defensiveCapital = stage2Val + stage4Val
        val defensiveRatioFromCapital = if (totalCapital > 0.0) defensiveCapital / totalCapital else 0.5

        val sustainabilityScore = when {
            defenseRatio >= 1.30 && defensiveRatioFromCapital >= 0.50 -> 96
            defenseRatio >= 1.25 -> 92
            defenseRatio >= 1.10 -> 85
            defenseRatio >= 1.00 -> 78
            else -> 64
        }.coerceIn(50, 99)

        // Financial Expert: Tax efficiency score (% of portfolio in Section 1256, ROC, or tax-exempt Treasuries)
        val taxAdvantagedCapital = holdings.filter { h ->
            val ticker = h.ticker.uppercase(Locale.US)
            ticker in setOf("SGOV", "XDTE", "QDTE", "RDTE", "SIOO", "FEPI", "SPYI", "QQQI", "TLT")
        }.sumOf { it.marketValue } + settledCash // Settled cash has no ongoing dividend tax drag

        val taxEfficiency = if (totalCapital > 0.0) {
            ((taxAdvantagedCapital / totalCapital) * 100.0).coerceIn(10.0, 98.0)
        } else 50.0

        val expertGrade = when {
            defenseRatio >= 1.25 && sustainabilityScore >= 88 && totalEfficiency >= 75 -> "Sovereign Fortress A+"
            defenseRatio >= 1.15 && totalEfficiency >= 70 -> "Disciplined Shield A"
            defenseRatio >= 1.00 -> "Balanced Parity B"
            else -> "Defensive Deficit C"
        }

        val expertVerdict = when {
            defenseRatio >= 1.25 -> "Closed-loop cash recycling active under DRIP=OFF. High yield velocity is anchored by dominant defense density (Týr Protocol Standard)."
            defenseRatio >= 1.00 -> "Defense density meets baseline parity. Prioritize gap resolution into Stage 1 & 2 assets."
            else -> "Defense density below 1.25x target. Immediate capital sweep required to reinforce downside buffer."
        }

        val grade = when {
            defenseRatio >= 1.30 && totalEfficiency >= 85 -> "A+"
            defenseRatio >= 1.25 && totalEfficiency >= 75 -> "A"
            defenseRatio >= 1.00 -> "B"
            else -> "C"
        }

        // Map sweep result into ActionableTradeRoute if available
        val route = sweepResult?.let { sweep ->
            if (sweep.targets.isNotEmpty() && sweep.totalCashToDeploy > 0.0) {
                ActionableTradeRoute(
                    title = String.format(
                        Locale.US,
                        "Concentrated %d-Target Cash Sweep",
                        sweep.targets.size
                    ),
                    plainEnglishRationale = sweep.plainEnglishSummary,
                    totalCapitalToDeploy = sweep.totalCashToDeploy,
                    expectedBlendedYield = sweep.blendedYieldAchieved,
                    projectedMonthlyCashFlow = sweep.totalAddedMonthlyIncome,
                    targetAllocations = sweep.targets.map { t ->
                        RouteAllocation(
                            ticker = t.ticker,
                            name = t.name,
                            stage = t.stage,
                            sectorName = t.riskSectorName,
                            dollarAmount = t.allocatedCash,
                            estimatedShares = t.shareCount,
                            sharePrice = t.currentPrice,
                            annualYieldPercent = t.annualYieldPercent,
                            isGapFiller = t.isGapFiller,
                            isYieldChampion = t.isYieldLeader
                        )
                    },
                    defenseRatioBefore = sweep.defenseToRiskRatioBefore,
                    defenseRatioAfter = sweep.defenseToRiskRatioAfter,
                    filledGaps = sweep.filledGaps
                )
            } else null
        }

        return PortfolioStanding(
            totalCapital = totalCapital,
            settledBuyingPower = settledCash,
            annualIncome = annualIncome,
            monthlyIncome = monthlyIncome,
            blendedYieldPercent = blendedYield,
            defenseToRiskRatio = defenseRatio,
            defenseStatusGrade = grade,
            capitalEfficiencyScore = totalEfficiency,
            coveredSectorsCount = heldSectors.size,
            totalSectorsCount = allValidSectors.size,
            unheldSectors = unheld,
            stageWeights = stageWeights,
            suggestedRoute = route,
            yieldSustainabilityScore = sustainabilityScore,
            taxEfficiencyScore = taxEfficiency,
            financialExpertGrade = expertGrade,
            financialExpertVerdict = expertVerdict
        )
    }
}
