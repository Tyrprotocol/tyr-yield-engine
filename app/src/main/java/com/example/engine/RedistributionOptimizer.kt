// File: RedistributionOptimizer.kt
package com.example.engine

import com.example.data.model.Holding
import com.example.data.model.RiskSector
import com.example.data.model.StageRegistry
import java.util.Locale

data class SweepTarget(
    val ticker: String,
    val name: String,
    val stage: Int,
    val currentPrice: Double,
    val allocatedCash: Double,
    val shareCount: Double,
    val annualYieldPercent: Double,
    val addedAnnualIncome: Double,
    val addedMonthlyIncome: Double,
    val strategicRationale: String,
    val riskSectorName: String = "",
    val isYieldLeader: Boolean = false,
    val isGapFiller: Boolean = false
)

data class SweepOptimizationResult(
    val totalCashToDeploy: Double,
    val targets: List<SweepTarget>,
    val blendedYieldAchieved: Double,
    val totalAddedAnnualIncome: Double,
    val totalAddedMonthlyIncome: Double,
    val defenseToRiskRatioBefore: Double,
    val defenseToRiskRatioAfter: Double,
    val plainEnglishSummary: String,
    val coveredRiskSectors: List<String> = emptyList(),
    val coveredStages: List<Int> = emptyList(),
    val filledGaps: List<String> = emptyList(),
    val remainingGaps: List<String> = emptyList()
)

object RedistributionOptimizer {

    /**
     * Curated catalog of champion gap-filling assets across all market risk sectors.
     * When a portfolio has zero exposure to a sector, the optimizer can directly recommend
     * these non-held champion assets to close exposure gaps.
     */
    val SECTOR_CHAMPION_CATALOG: Map<RiskSector, Holding> = mapOf(
        RiskSector.HEALTHCARE_DEFENSIVE to Holding(
            ticker = "HIYY",
            name = "YieldMax HIMS Option Income Strategy ETF",
            shares = 0.0,
            avgCost = 9.84,
            currentPrice = 9.84,
            annualYieldPercent = 75.90,
            stage = 1,
            categoryTag = "Healthcare / Telehealth",
            distributionFrequency = "Weekly - Group 1 (Thu)",
            notes = "Top healthcare synthetic options cash generator (0.07 beta). Closes missing healthcare exposure."
        ),
        RiskSector.NATURAL_RESOURCES_ENERGY to Holding(
            ticker = "XOMO",
            name = "YieldMax XOM Option Income Strategy ETF",
            shares = 0.0,
            avgCost = 10.26,
            currentPrice = 10.26,
            annualYieldPercent = 45.90,
            stage = 3,
            categoryTag = "Energy / Oil & Gas",
            distributionFrequency = "Weekly - Group 1 (Thu)",
            notes = "Top energy & oil/gas synthetic options cash generator (~94% US T-Bill collateral)."
        ),
        RiskSector.COMMODITIES_PRECIOUS_METALS to Holding(
            ticker = "GDXY",
            name = "YieldMax Gold Miners Option Income Strategy ETF",
            shares = 0.0,
            avgCost = 9.63,
            currentPrice = 9.63,
            annualYieldPercent = 38.20,
            stage = 3,
            categoryTag = "Gold Miners (GDX)",
            distributionFrequency = "Weekly - Group 2 (Fri)",
            notes = "Gold & precious metals mining dividend engine. Currency hedge."
        ),
        RiskSector.REAL_ESTATE_INFRASTRUCTURE to Holding(
            ticker = "RIET",
            name = "Hoya Capital High Dividend Yield ETF",
            shares = 0.0,
            avgCost = 13.80,
            currentPrice = 13.80,
            annualYieldPercent = 10.80,
            stage = 2,
            categoryTag = "Real Estate Infrastructure",
            distributionFrequency = "Monthly",
            notes = "High-dividend real estate & infrastructure cash flow."
        ),
        RiskSector.TAX_EXEMPT_STABLE to Holding(
            ticker = "SGOV",
            name = "iShares 0-3 Month Treasury Bond ETF",
            shares = 0.0,
            avgCost = 100.45,
            currentPrice = 100.45,
            annualYieldPercent = 5.15,
            stage = 4,
            categoryTag = "Tax-Exempt T-Bills",
            distributionFrequency = "Monthly",
            notes = "Stage 4 ultimate dry powder. 0% credit risk, 0% state tax."
        ),
        RiskSector.SP500_LARGE_CAP to Holding(
            ticker = "XDTE",
            name = "Roundhill S&P 500 0DTE Covered Call Strategy ETF",
            shares = 0.0,
            avgCost = 36.49,
            currentPrice = 36.49,
            annualYieldPercent = 27.80,
            stage = 2,
            categoryTag = "US Large-Cap Index (S&P 500)",
            distributionFrequency = "Weekly",
            notes = "Stage 2 S&P 500 institutional spine."
        ),
        RiskSector.RUSSELL_SMALL_CAP to Holding(
            ticker = "RDTE",
            name = "Roundhill Russell 2000 0DTE Covered Call Strategy ETF",
            shares = 0.0,
            avgCost = 25.24,
            currentPrice = 25.24,
            annualYieldPercent = 31.93,
            stage = 2,
            categoryTag = "US Small-Cap Index (R2000)",
            distributionFrequency = "Weekly",
            notes = "Domestic small-cap manifold with Section 1256 tax treatment."
        ),
        RiskSector.EQUITIES_THEMATIC_TECH to Holding(
            ticker = "FEPI",
            name = "REX FANG & Innovation Equity Premium Income ETF",
            shares = 0.0,
            avgCost = 36.00,
            currentPrice = 36.00,
            annualYieldPercent = 28.31,
            stage = 2,
            categoryTag = "Big Tech / FANG+ Basket",
            distributionFrequency = "Weekly",
            notes = "Mega-cap tech innovators with high cash velocity."
        ),
        RiskSector.CRYPTO_DIGITAL_ASSETS to Holding(
            ticker = "MSTY",
            name = "YieldMax MSTR Option Income Strategy ETF",
            shares = 0.0,
            avgCost = 12.79,
            currentPrice = 12.79,
            annualYieldPercent = 60.91,
            stage = 1,
            categoryTag = "Crypto / BTC Treasury",
            distributionFrequency = "Weekly",
            notes = "Highest-yield crypto treasury cash engine."
        ),
        RiskSector.FIXED_INCOME_DURATION to Holding(
            ticker = "TLTW",
            name = "iShares 20+ Year Treasury Bond BuyWrite Strategy ETF",
            shares = 0.0,
            avgCost = 21.02,
            currentPrice = 21.02,
            annualYieldPercent = 14.55,
            stage = 3,
            categoryTag = "20+ Yr US Treasury Duration",
            distributionFrequency = "Monthly",
            notes = "Stage 3 duration ballast and deflation hedge."
        )
    )

    /**
     * Extended catalog of secondary gap-filling alternatives across market risk sectors.
     */
    val SECTOR_GAP_ALTERNATIVES: Map<RiskSector, List<Holding>> = mapOf(
        RiskSector.NATURAL_RESOURCES_ENERGY to listOf(
            Holding(
                ticker = "AMLP",
                name = "Alerian MLP ETF (Energy Infrastructure)",
                shares = 0.0,
                avgCost = 44.50,
                currentPrice = 44.50,
                annualYieldPercent = 25.50,
                stage = 3,
                categoryTag = "Natural Resources & Energy",
                distributionFrequency = "Quarterly",
                notes = "Top natural resources & pipeline cash flow engine."
            )
        ),
        RiskSector.COMMODITIES_PRECIOUS_METALS to listOf(
            Holding(
                ticker = "MINY",
                name = "YieldMax Metals & Mining Option Income ETF",
                shares = 0.0,
                avgCost = 10.15,
                currentPrice = 10.15,
                annualYieldPercent = 30.23,
                stage = 3,
                categoryTag = "Strategic Metals / Mining",
                distributionFrequency = "Weekly",
                notes = "Strategic minerals and precious metals inflation ballast."
            )
        ),
        RiskSector.SP500_LARGE_CAP to listOf(
            Holding(
                ticker = "YMAG",
                name = "YieldMax Magnificent 7 Fund of Option Income ETFs",
                shares = 0.0,
                avgCost = 10.25,
                currentPrice = 10.25,
                annualYieldPercent = 32.29,
                stage = 2,
                categoryTag = "Mega-Cap Top 7 (S&P 100)",
                distributionFrequency = "Weekly",
                notes = "Diversified mega-cap top 7 basket with weekly distribution cadence."
            )
        ),
        RiskSector.RUSSELL_SMALL_CAP to listOf(
            Holding(
                ticker = "RDTY",
                name = "Defiance / YieldMax Russell 2000 Income ETF",
                shares = 0.0,
                avgCost = 14.20,
                currentPrice = 14.20,
                annualYieldPercent = 36.50,
                stage = 2,
                categoryTag = "US Small-Cap Index (R2000)",
                distributionFrequency = "Weekly",
                notes = "High-income small-cap index option overlay."
            )
        ),
        RiskSector.EQUITIES_THEMATIC_TECH to listOf(
            Holding(
                ticker = "CVNY",
                name = "YieldMax CVNA Option Income Strategy ETF",
                shares = 0.0,
                avgCost = 18.50,
                currentPrice = 18.50,
                annualYieldPercent = 70.99,
                stage = 1,
                categoryTag = "Consumer Retail / Auto",
                distributionFrequency = "Weekly",
                notes = "Consumer cyclical & automotive retail high-yield engine."
            ),
            Holding(
                ticker = "ULTY",
                name = "YieldMax Ultra Option Income Strategy ETF",
                shares = 0.0,
                avgCost = 9.10,
                currentPrice = 9.10,
                annualYieldPercent = 60.25,
                stage = 1,
                categoryTag = "Multi-Asset Ultra Volatility",
                distributionFrequency = "Weekly",
                notes = "Dynamic basket of high implied volatility innovators."
            ),
            Holding(
                ticker = "CHPY",
                name = "Kurv Semiconductor Innovation Strategy ETF",
                shares = 0.0,
                avgCost = 12.50,
                currentPrice = 12.50,
                annualYieldPercent = 40.17,
                stage = 1,
                categoryTag = "Semiconductors & AI Hardware",
                distributionFrequency = "Weekly",
                notes = "Direct semiconductor innovators and AI infrastructure."
            ),
            Holding(
                ticker = "QDTE",
                name = "Roundhill N-100 0DTE Covered Call Strategy ETF",
                shares = 0.0,
                avgCost = 25.64,
                currentPrice = 25.64,
                annualYieldPercent = 45.75,
                stage = 2,
                categoryTag = "Nasdaq-100 0DTE Index",
                distributionFrequency = "Weekly",
                notes = "Nasdaq-100 0DTE options with Section 1256 tax treatment."
            )
        )
    )

    /**
     * Calculates the optimal cash redistribution sweep into strictly 1 to 3 concentrated targets,
     * targeting non-held gap assets first when gaps exist, and highest-yield champions per category.
     */
    fun optimizeSweep(
        holdings: List<Holding>,
        availableCash: Double,
        currentDefenseRatio: Double
    ): SweepOptimizationResult {
        val safeDefenseRatio = if (currentDefenseRatio.isFinite() && currentDefenseRatio >= 0.0) {
            currentDefenseRatio.coerceAtMost(100.0)
        } else {
            1.25
        }

        if (!availableCash.isFinite() || availableCash <= 0.0) {
            return SweepOptimizationResult(
                totalCashToDeploy = 0.0,
                targets = emptyList(),
                blendedYieldAchieved = 0.0,
                totalAddedAnnualIncome = 0.0,
                totalAddedMonthlyIncome = 0.0,
                defenseToRiskRatioBefore = safeDefenseRatio,
                defenseToRiskRatioAfter = safeDefenseRatio,
                plainEnglishSummary = "Settled buying power is currently $0.00. Log incoming dividend payments or deposit funds to initiate a closed-loop sweep.",
                coveredRiskSectors = emptyList()
            )
        }

        val sanitizedHoldings = holdings.filter {
            it.currentPrice.isFinite() && it.currentPrice > 0.0 &&
                it.shares.isFinite() && it.shares >= 0.0 &&
                it.annualYieldPercent.isFinite() && it.annualYieldPercent >= 0.0
        }

        // Fresh Start ($0.00 base) handling:
        if (sanitizedHoldings.isEmpty() || sanitizedHoldings.all { it.shares == 0.0 }) {
            return generateFreshStartRecommendations(availableCash)
        }

        // Determine number of concentrated targets based on available capital:
        val targetCount = when {
            availableCash < 100.0 -> 1
            availableCash <= 350.0 -> 2
            else -> 3
        }

        val splits = when (targetCount) {
            1 -> listOf(1.0)
            2 -> listOf(0.65, 0.35)
            3 -> listOf(0.50, 0.30, 0.20)
            else -> listOf(1.0)
        }

        // Identify zero-holding vulnerability gaps across all core sectors:
        val unheldSectors = RiskSector.entries
            .filter { it != RiskSector.GENERAL_DIVERSIFIED }
            .filter { sector -> sanitizedHoldings.none { RiskSector.resolve(it) == sector && it.shares > 0.0 } }
            .toSet()

        // Assemble candidate pool: existing holdings + non-held gap champion vehicles from catalog
        val candidatePool = sanitizedHoldings.toMutableList()
        unheldSectors.forEach { unheldSector ->
            SECTOR_CHAMPION_CATALOG[unheldSector]?.let { champ ->
                if (candidatePool.none { it.ticker.equals(champ.ticker, ignoreCase = true) }) {
                    candidatePool.add(champ)
                }
            }
            SECTOR_GAP_ALTERNATIVES[unheldSector]?.forEach { alt ->
                if (candidatePool.none { it.ticker.equals(alt.ticker, ignoreCase = true) }) {
                    candidatePool.add(alt)
                }
            }
        }

        val totalPortfolioMarketVal = sanitizedHoldings.sumOf { it.marketValue }
        val sectorWeights = RiskSector.entries.associateWith { sector ->
            val valInSector = sanitizedHoldings.filter { RiskSector.resolve(it) == sector }.sumOf { it.marketValue }
            if (totalPortfolioMarketVal > 0.0) (valInSector / totalPortfolioMarketVal) * 100.0 else 0.0
        }

        val maxYieldBySector = RiskSector.entries.associateWith { sector ->
            candidatePool.filter { RiskSector.resolve(it) == sector }
                .maxOfOrNull { it.annualYieldPercent } ?: 0.0
        }
        val maxYieldByStage = (1..4).associateWith { stageNum ->
            candidatePool.filter { it.stage == stageNum }
                .maxOfOrNull { it.annualYieldPercent } ?: 0.0
        }

        val scoredHoldings = candidatePool.map { holding ->
            val sector = RiskSector.resolve(holding)
            val currentSectorWeight = sectorWeights[sector] ?: 0.0
            val topSectorYield = maxYieldBySector[sector] ?: 0.0
            val topStageYield = maxYieldByStage[holding.stage] ?: 0.0

            val isSectorYieldLeader = topSectorYield > 0.0 && holding.annualYieldPercent >= (topSectorYield - 0.05)
            val isStageYieldLeader = topStageYield > 0.0 && holding.annualYieldPercent >= (topStageYield - 0.05)
            val isYieldLeader = isSectorYieldLeader || isStageYieldLeader
            val isGapFiller = unheldSectors.contains(sector) || holding.shares == 0.0

            val discountFactor = if (holding.avgCost > 0.0) {
                ((holding.avgCost - holding.currentPrice) / holding.avgCost).coerceIn(-0.25, 0.50)
            } else {
                0.0
            }

            val yieldWeight = (holding.annualYieldPercent / 100.0) * 3.5

            val yieldChampionMultiplier = when {
                isSectorYieldLeader && isStageYieldLeader -> 1.35
                isSectorYieldLeader || isStageYieldLeader -> 1.20
                else -> 1.00
            }

            // Gap Resolution Priority Multiplier (prioritizes closing blind spots without crushing blended yield):
            val gapMultiplier = if (isGapFiller) 1.50 else 1.00

            val diversityFactor = when {
                currentSectorWeight >= sector.maxHealthyWeightPercent -> 0.05 // Hard concentration freeze (>max cap)
                currentSectorWeight <= (sector.maxHealthyWeightPercent * 0.4) -> 1.60
                else -> 1.0
            }

            val stageDefenseFactor = when {
                safeDefenseRatio < 1.25 -> {
                    if (holding.stage == 2 || holding.stage == 4) 1.5 else 0.7
                }
                safeDefenseRatio > 2.0 -> {
                    if (holding.stage == 1 || holding.stage == 3) 1.4 else 0.8
                }
                else -> 1.0
            }

            val baseScore = (0.5 + discountFactor * 1.5 + yieldWeight) * yieldChampionMultiplier * gapMultiplier
            val totalScore = baseScore * diversityFactor * stageDefenseFactor

            ScoredCandidate(
                holding = holding,
                sector = sector,
                totalScore = totalScore,
                isYieldLeader = isYieldLeader,
                isGapFiller = isGapFiller,
                topSectorYield = topSectorYield
            )
        }.sortedByDescending { it.totalScore }

        // Selection Process:
        // Priority 1: When unheld sector gaps exist, the engine MUST prioritize Gap Asset identification
        // over existing held assets, specifically surfacing new, unheld tickers to satisfy missing category
        // and risk requirements across all available target slots.
        // Priority 2: Properly assess highest yield across gap assets so that high-yield champions
        // (like HIYY at 75.9% or XOMO at 45.9%) are surfaced ahead of lower-yielding gap assets (like RIET at 10.8%),
        // while respecting defense-to-risk balance when defense ratio is below 1.25x.
        val selectedCandidates = mutableListOf<ScoredCandidate>()
        val selectedSectors = mutableSetOf<RiskSector>()
        val selectedStages = mutableSetOf<Int>()

        // 1. Identify and rank all available gap filler assets across unheld sectors:
        // For each unheld sector, pick the candidate asset with the highest annualYieldPercent.
        val rankedGapCandidates = unheldSectors.mapNotNull { unheldSector ->
            scoredHoldings
                .filter { it.isGapFiller && it.sector == unheldSector }
                .maxByOrNull { it.holding.annualYieldPercent }
        }.sortedWith(
            compareByDescending<ScoredCandidate> { candidate ->
                // When portfolio defense ratio is critically low (< 1.0x), give defensive gap stages (2 & 4) a boost
                val defAdjust = if (safeDefenseRatio < 1.0 && (candidate.holding.stage == 2 || candidate.holding.stage == 4)) 1.35 else 1.0
                candidate.holding.annualYieldPercent * defAdjust
            }
        )

        // Fill up to all available targetCount slots with unheld Gap Assets first:
        val gapSlotsToFill = minOf(targetCount, rankedGapCandidates.size)

        var gapIdx = 0
        while (selectedCandidates.size < gapSlotsToFill && gapIdx < rankedGapCandidates.size) {
            val gapCandidate = rankedGapCandidates[gapIdx++]
            if (!selectedSectors.contains(gapCandidate.sector)) {
                // Ensure stage diversity if filling the final slot and all previous slots share the same stage
                val needsDifferentStage = selectedCandidates.size == targetCount - 1 &&
                    selectedStages.size == 1 &&
                    selectedStages.contains(gapCandidate.holding.stage) &&
                    rankedGapCandidates.drop(gapIdx).any { !selectedStages.contains(it.holding.stage) }

                if (!needsDifferentStage) {
                    selectedCandidates.add(gapCandidate)
                    selectedSectors.add(gapCandidate.sector)
                    selectedStages.add(gapCandidate.holding.stage)
                }
            }
        }

        // 2. Complete remaining slots prioritizing distinct sectors and ensuring at least 2 distinct stages/tiers:
        for (candidate in scoredHoldings) {
            if (selectedCandidates.size >= targetCount) break
            if (selectedCandidates.any { it.holding.ticker == candidate.holding.ticker }) continue
            if (!selectedSectors.contains(candidate.sector)) {
                if (selectedStages.size < 2) {
                    if (!selectedStages.contains(candidate.holding.stage)) {
                        selectedCandidates.add(candidate)
                        selectedSectors.add(candidate.sector)
                        selectedStages.add(candidate.holding.stage)
                    }
                } else {
                    selectedCandidates.add(candidate)
                    selectedSectors.add(candidate.sector)
                    selectedStages.add(candidate.holding.stage)
                }
            }
        }

        // Pass 2: Relax stage constraint (require distinct sectors)
        if (selectedCandidates.size < targetCount) {
            for (candidate in scoredHoldings) {
                if (selectedCandidates.size >= targetCount) break
                if (selectedCandidates.any { it.holding.ticker == candidate.holding.ticker }) continue
                if (!selectedSectors.contains(candidate.sector)) {
                    selectedCandidates.add(candidate)
                    selectedSectors.add(candidate.sector)
                    selectedStages.add(candidate.holding.stage)
                }
            }
        }

        // Pass 3: Fallback (any highest scoring remaining)
        if (selectedCandidates.size < targetCount) {
            for (candidate in scoredHoldings) {
                if (selectedCandidates.size >= targetCount) break
                if (selectedCandidates.none { it.holding.ticker == candidate.holding.ticker }) {
                    selectedCandidates.add(candidate)
                }
            }
        }

        // Order selected candidates: Prioritize high cash yield for primary split
        val orderedCandidates = selectedCandidates.sortedByDescending { it.holding.annualYieldPercent }

        val targets = mutableListOf<SweepTarget>()
        var totalAddedAnnual = 0.0

        orderedCandidates.forEachIndexed { index, candidate ->
            val holding = candidate.holding
            val sector = candidate.sector
            val currentSectorWeight = sectorWeights[sector] ?: 0.0
            val allocated = availableCash * splits[index]
            val shares = if (holding.currentPrice > 0.0) allocated / holding.currentPrice else 0.0
            val addedAnnual = allocated * (holding.annualYieldPercent / 100.0)
            val addedMonthly = addedAnnual / 12.0
            totalAddedAnnual += addedAnnual

            val rationale = buildRationale(
                holding = holding,
                sector = sector,
                currentSectorWeight = currentSectorWeight,
                defenseRatio = currentDefenseRatio,
                rankIndex = index,
                isYieldLeader = candidate.isYieldLeader,
                isGapFiller = candidate.isGapFiller
            )

            targets.add(
                SweepTarget(
                    ticker = holding.ticker,
                    name = holding.name,
                    stage = holding.stage,
                    currentPrice = holding.currentPrice,
                    allocatedCash = allocated,
                    shareCount = shares,
                    annualYieldPercent = holding.annualYieldPercent,
                    addedAnnualIncome = addedAnnual,
                    addedMonthlyIncome = addedMonthly,
                    strategicRationale = rationale,
                    riskSectorName = sector.shortName,
                    isYieldLeader = candidate.isYieldLeader,
                    isGapFiller = candidate.isGapFiller
                )
            )
        }

        val blendedYield = if (availableCash > 0.0) (totalAddedAnnual / availableCash) * 100.0 else 0.0
        val totalAddedMonthly = totalAddedAnnual / 12.0

        val currentDefenseVal = sanitizedHoldings.filter { it.stage == 2 || it.stage == 4 }.sumOf { it.marketValue } + availableCash
        val currentRiskVal = sanitizedHoldings.filter { it.stage == 1 || it.stage == 3 }.sumOf { it.marketValue }

        val addedDefense = targets.filter { it.stage == 2 || it.stage == 4 }.sumOf { it.allocatedCash }
        val addedRisk = targets.filter { it.stage == 1 || it.stage == 3 }.sumOf { it.allocatedCash }

        val newDefenseVal = (currentDefenseVal - availableCash) + addedDefense
        val newRiskVal = currentRiskVal + addedRisk
        val defenseRatioAfter = if (newRiskVal > 0.0) newDefenseVal / newRiskVal else 1.25

        val coveredSectorNames = targets.map { it.riskSectorName }.distinct()
        val coveredStagesList = targets.map { it.stage }.distinct().sorted()
        val targetedGaps = targets.filter { it.isGapFiller }.map { it.riskSectorName }.distinct()
        val remainingGapsList = unheldSectors.map { it.shortName }.filter { it !in targetedGaps }

        val gapNote = if (targetedGaps.isNotEmpty()) {
            " (Closing exposure gaps in: ${targetedGaps.joinToString(", ")})"
        } else ""

        val summary = String.format(
            Locale.US,
            "Multi-Vector Sweep: Deploying $%.2f into %d vector %s (%s across Stage %s)%s captures a %.2f%% blended cash yield (+$$%.2f/mo) targeted at gap elimination and high income.",
            availableCash,
            targets.size,
            if (targets.size == 1) "leader" else "leaders",
            coveredSectorNames.joinToString(" + "),
            coveredStagesList.joinToString(", "),
            gapNote,
            blendedYield,
            totalAddedMonthly
        )

        return SweepOptimizationResult(
            totalCashToDeploy = availableCash,
            targets = targets,
            blendedYieldAchieved = blendedYield,
            totalAddedAnnualIncome = totalAddedAnnual,
            totalAddedMonthlyIncome = totalAddedMonthly,
            defenseToRiskRatioBefore = safeDefenseRatio,
            defenseToRiskRatioAfter = defenseRatioAfter,
            plainEnglishSummary = summary,
            coveredRiskSectors = coveredSectorNames,
            coveredStages = coveredStagesList,
            filledGaps = targetedGaps,
            remainingGaps = remainingGapsList
        )
    }

    private data class ScoredCandidate(
        val holding: Holding,
        val sector: RiskSector,
        val totalScore: Double,
        val isYieldLeader: Boolean,
        val isGapFiller: Boolean,
        val topSectorYield: Double
    )

    private fun generateFreshStartRecommendations(availableCash: Double): SweepOptimizationResult {
        val targets = if (availableCash <= 75.0) {
            listOf(
                SweepTarget(
                    ticker = "SCHD",
                    name = "Schwab U.S. Dividend Equity ETF",
                    stage = 2,
                    currentPrice = 29.20,
                    allocatedCash = availableCash,
                    shareCount = availableCash / 29.20,
                    annualYieldPercent = 3.5,
                    addedAnnualIncome = availableCash * 0.035,
                    addedMonthlyIncome = (availableCash * 0.035) / 12.0,
                    strategicRationale = "Stage 2 Starter Pillar: Bedrock dividend fortress with high-quality aristocrats.",
                    riskSectorName = "S&P 500 Large-Cap",
                    isYieldLeader = true,
                    isGapFiller = true
                )
            )
        } else {
            val half1 = availableCash * 0.60
            val half2 = availableCash * 0.40
            listOf(
                SweepTarget(
                    ticker = "SCHD",
                    name = "Schwab U.S. Dividend Equity ETF",
                    stage = 2,
                    currentPrice = 29.20,
                    allocatedCash = half1,
                    shareCount = half1 / 29.20,
                    annualYieldPercent = 3.5,
                    addedAnnualIncome = half1 * 0.035,
                    addedMonthlyIncome = (half1 * 0.035) / 12.0,
                    strategicRationale = "Stage 2 Foundation (60%): Stabilizes core principal with growing quarterly dividends.",
                    riskSectorName = "S&P 500 Large-Cap",
                    isYieldLeader = true,
                    isGapFiller = true
                ),
                SweepTarget(
                    ticker = "JEPQ",
                    name = "JPMorgan Nasdaq Equity Premium Income ETF",
                    stage = 1,
                    currentPrice = 55.80,
                    allocatedCash = half2,
                    shareCount = half2 / 55.80,
                    annualYieldPercent = 9.6,
                    addedAnnualIncome = half2 * 0.096,
                    addedMonthlyIncome = (half2 * 0.096) / 12.0,
                    strategicRationale = "Stage 1 Cash Flow Engine (40%): Monthly dividend cash to pool for future sweeps.",
                    riskSectorName = "Thematic Tech",
                    isYieldLeader = true,
                    isGapFiller = true
                )
            )
        }

        val totalAnnual = targets.sumOf { it.addedAnnualIncome }
        val blended = if (availableCash > 0.0) (totalAnnual / availableCash) * 100.0 else 0.0

        return SweepOptimizationResult(
            totalCashToDeploy = availableCash,
            targets = targets,
            blendedYieldAchieved = blended,
            totalAddedAnnualIncome = totalAnnual,
            totalAddedMonthlyIncome = totalAnnual / 12.0,
            defenseToRiskRatioBefore = 1.5,
            defenseToRiskRatioAfter = 1.5,
            plainEnglishSummary = String.format(
                Locale.US,
                "Starter foundation: Directing $%.2f into %d core anchor %s sets up a balanced %.2f%% blended yield without diluting small capital.",
                availableCash,
                targets.size,
                if (targets.size == 1) "fund" else "funds",
                blended
            ),
            coveredRiskSectors = targets.map { it.riskSectorName }.distinct(),
            coveredStages = targets.map { it.stage }.distinct().sorted(),
            filledGaps = targets.map { it.riskSectorName }.distinct(),
            remainingGaps = emptyList()
        )
    }

    private fun buildRationale(
        holding: Holding,
        sector: RiskSector,
        currentSectorWeight: Double,
        defenseRatio: Double,
        rankIndex: Int,
        isYieldLeader: Boolean,
        isGapFiller: Boolean
    ): String {
        val stageMeta = StageRegistry.getStage(holding.stage)
        val rankLabel = when (rankIndex) {
            0 -> "Primary Sweep Focus (Highest Priority)"
            1 -> "Secondary Multi-Vector Target"
            else -> "Tertiary Diversified Booster"
        }

        val gapTag = if (isGapFiller) "🛡 GAP RESOLUTION (Non-Held): Closes missing exposure in ${sector.displayName}. " else ""

        val yieldLeaderTag = if (isYieldLeader) {
            String.format(Locale.US, "★ Category Yield Champion (%.1f%% annual yield). ", holding.annualYieldPercent)
        } else ""

        val discountNote = if (holding.shares > 0.0 && holding.currentPrice < holding.avgCost) {
            String.format(
                Locale.US,
                "Trading at a %.1f%% discount below your cost basis ($%.2f vs $%.2f).",
                ((holding.avgCost - holding.currentPrice) / holding.avgCost) * 100.0,
                holding.currentPrice,
                holding.avgCost
            )
        } else {
            String.format(
                Locale.US,
                "Priced at $%.2f with high cash velocity (%.1f%% yield).",
                holding.currentPrice,
                holding.annualYieldPercent
            )
        }

        val defenseNote = if (defenseRatio < 1.25 && (holding.stage == 2 || holding.stage == 4)) {
            "Reinforces defensive ballast toward the >= 1.25x target."
        } else {
            "Maximizes compounding cash flow engines."
        }

        val diversityNote = String.format(
            Locale.US,
            "Market Sector: [%s] (weight: %.1f%% vs %.0f%% cap). Enforces multi-vector protection.",
            sector.displayName,
            currentSectorWeight,
            sector.maxHealthyWeightPercent
        )

        return "$rankLabel: Stage ${holding.stage} (${stageMeta.title}). $gapTag$yieldLeaderTag$discountNote $defenseNote $diversityNote"
    }

    /**
     * Resolves catalog metadata for a newly swept gap asset so its categoryTag,
     * distributionFrequency, and notes are preserved upon insertion into Room.
     */
    fun findCatalogTemplate(ticker: String): Holding? {
        SECTOR_CHAMPION_CATALOG.values.find { it.ticker.equals(ticker, ignoreCase = true) }?.let { return it }
        SECTOR_GAP_ALTERNATIVES.values.flatten().find { it.ticker.equals(ticker, ignoreCase = true) }?.let { return it }
        return null
    }
}
