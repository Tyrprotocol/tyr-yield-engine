// File: ExampleRobolectricTest.kt
package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AlertRule
import com.example.data.model.AlertType
import com.example.engine.AlertEvaluator
import com.example.engine.ProjectionEngine
import com.example.engine.RedistributionOptimizer
import com.example.ui.DefenseStatus
import com.example.ui.PortfolioMetrics
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun readStringFromContext() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Yield Engine", appName)
  }

  @Test
  fun verifyFreshStartOptimizationAllocatesStarterAnchors() {
    val result = RedistributionOptimizer.optimizeSweep(
        holdings = emptyList(),
        availableCash = 50.0,
        currentDefenseRatio = 1.5
    )
    assertTrue("Targets must not be empty", result.targets.isNotEmpty())
    assertTrue("Blended yield should be greater than zero", result.blendedYieldAchieved > 0.0)
    assertEquals(50.0, result.totalCashToDeploy, 0.01)
  }

  @Test
  fun verifyTwentyYearProjectionEngineGeneratesSixMilestones() {
    val projections = ProjectionEngine.calculateProjections(
        startingBalance = 1000.0,
        blendedYieldPercent = 8.0,
        monthlyContribution = 100.0
    )
    assertEquals(6, projections.size)
    val twentyYear = projections.last()
    assertEquals("20Y", twentyYear.horizonKey)
    assertTrue("Twenty year balance should exceed 1000", twentyYear.projectedPortfolioBalance > 1000.0)
    assertTrue("Cumulative dividends should be substantial", twentyYear.cumulativeDividendsEarned > 0.0)
  }

  @Test
  fun verifyAlertEvaluatorTriggersDefenseRatioNotification() {
    val rule = AlertRule(
        type = AlertType.DEFENSE_RATIO_TARGET,
        thresholdValue = 1.25,
        comparison = "GREATER_THAN",
        label = "Defense Shield Reached"
    )
    val currentMetrics = PortfolioMetrics(
        totalPortfolioValue = 10000.0,
        defenseToRiskDensityRatio = 1.30,
        defenseStatus = DefenseStatus.STRONG
    )
    val triggered = AlertEvaluator.evaluateRules(
        rules = listOf(rule),
        currentMetrics = currentMetrics,
        previousMetrics = null,
        holdings = emptyList()
    )
    assertEquals(1, triggered.size)
    assertEquals("SUCCESS", triggered[0].second.severity)
  }

  private fun createTestSampleHoldings(): List<com.example.data.model.Holding> {
    return listOf(
        com.example.data.model.Holding(
            ticker = "TSLY",
            name = "YieldMax TSLA Option Income Strategy ETF",
            shares = 4.1652,
            avgCost = 22.2270,
            currentPrice = 20.28,
            annualYieldPercent = 50.28,
            stage = 1,
            categoryTag = "EV / Autonomy (TSLA)",
            distributionFrequency = "Weekly - Group 2 (Fri)"
        ),
        com.example.data.model.Holding(
            ticker = "NVDY",
            name = "YieldMax NVDA Option Income Strategy ETF",
            shares = 9.8529,
            avgCost = 12.6562,
            currentPrice = 10.33,
            annualYieldPercent = 30.79,
            stage = 1,
            categoryTag = "AI / Semiconductors (NVDA)",
            distributionFrequency = "Weekly - Group 2 (Fri)"
        ),
        com.example.data.model.Holding(
            ticker = "HOOY",
            name = "YieldMax HOOD Option Income Strategy ETF",
            shares = 12.7086,
            avgCost = 25.5032,
            currentPrice = 18.50,
            annualYieldPercent = 60.38,
            stage = 1,
            categoryTag = "Crypto / Retail Brokerage (HOOD)",
            distributionFrequency = "Weekly - Group 2 (Fri)"
        ),
        com.example.data.model.Holding(
            ticker = "MSTY",
            name = "YieldMax MSTR Option Income Strategy ETF",
            shares = 22.1665,
            avgCost = 15.8843,
            currentPrice = 12.79,
            annualYieldPercent = 60.91,
            stage = 1,
            categoryTag = "Crypto / BTC Treasury (MSTR)",
            distributionFrequency = "Weekly - Group 2 (Fri)"
        ),
        com.example.data.model.Holding(
            ticker = "CONY",
            name = "YieldMax COIN Option Income Strategy ETF",
            shares = 3.8287,
            avgCost = 19.7038,
            currentPrice = 14.24,
            annualYieldPercent = 57.05,
            stage = 1,
            categoryTag = "Crypto Exchange (COIN)",
            distributionFrequency = "Weekly - Group 2 (Fri)"
        ),
        com.example.data.model.Holding(
            ticker = "CRCO",
            name = "YieldMax CRCL Option Income Strategy ETF",
            shares = 7.9184,
            avgCost = 13.1529,
            currentPrice = 10.30,
            annualYieldPercent = 70.41,
            stage = 1,
            categoryTag = "Crypto / Stablecoin (CRCL)",
            distributionFrequency = "Weekly - Group 2 (Fri)"
        ),
        com.example.data.model.Holding(
            ticker = "RDTE",
            name = "Roundhill Russell 2000 0DTE Covered Call Strategy ETF",
            shares = 3.1169,
            avgCost = 26.0419,
            currentPrice = 25.24,
            annualYieldPercent = 31.93,
            stage = 2,
            categoryTag = "US Small-Cap Index (R2000)",
            distributionFrequency = "Weekly (Fri)"
        ),
        com.example.data.model.Holding(
            ticker = "XDTE",
            name = "Roundhill S&P 500 0DTE Covered Call Strategy ETF",
            shares = 1.8260,
            avgCost = 38.6911,
            currentPrice = 36.49,
            annualYieldPercent = 27.80,
            stage = 2,
            categoryTag = "US Large-Cap Index (S&P 500)",
            distributionFrequency = "Weekly (Fri)"
        ),
        com.example.data.model.Holding(
            ticker = "FEPI",
            name = "REX FANG & Innovation Equity Premium Income ETF",
            shares = 1.1814,
            avgCost = 40.4266,
            currentPrice = 36.00,
            annualYieldPercent = 28.31,
            stage = 2,
            categoryTag = "Big Tech / FANG+ Basket",
            distributionFrequency = "Weekly (Wed/Thu)"
        ),
        com.example.data.model.Holding(
            ticker = "GDXY",
            name = "YieldMax Gold Miners Option Income Strategy ETF",
            shares = 5.1288,
            avgCost = 9.8405,
            currentPrice = 9.63,
            annualYieldPercent = 38.20,
            stage = 3,
            categoryTag = "Gold Miners (GDX)",
            distributionFrequency = "Weekly - Group 2 (Fri)"
        ),
        com.example.data.model.Holding(
            ticker = "TLTW",
            name = "iShares 20+ Year Treasury Bond BuyWrite Strategy ETF",
            shares = 1.2483,
            avgCost = 20.5479,
            currentPrice = 21.02,
            annualYieldPercent = 14.55,
            stage = 3,
            categoryTag = "20+ Yr US Treasury Duration (TLT)",
            distributionFrequency = "Monthly (Early Mo)"
        )
    )
  }

  @Test
  fun verifyNewInstallRepositoryStartsCompletelyEmpty() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = androidx.room.Room.inMemoryDatabaseBuilder(
        context,
        com.example.data.db.AppDatabase::class.java
    ).allowMainThreadQueries().build()

    val repository = com.example.data.repository.PortfolioRepository(
        database.holdingDao(),
        database.cashDao(),
        database.alertDao()
    )

    val initialHoldings = repository.allHoldings.first()
    val initialCash = repository.settledBuyingPower.first()
    assertTrue("New install must have 0 holdings", initialHoldings.isEmpty())
    assertEquals("New install must have $0.00 settled cash", 0.0, initialCash, 0.001)

    database.close()
  }

  @Test
  fun verifySweepOptimizationEnforcesMultiVectorDiversity() {
    val baseline = createTestSampleHoldings()

    // Test sweep with $250 (selects 2 targets)
    val sweepResult2 = RedistributionOptimizer.optimizeSweep(
        holdings = baseline,
        availableCash = 250.0,
        currentDefenseRatio = 1.25
    )
    assertEquals(2, sweepResult2.targets.size)
    val sectors2 = sweepResult2.targets.map { it.riskSectorName }.distinct()
    assertEquals("Targets must not share the same risk sector", 2, sectors2.size)

    // Test sweep with $400 (selects 3 targets)
    val sweepResult3 = RedistributionOptimizer.optimizeSweep(
        holdings = baseline,
        availableCash = 400.0,
        currentDefenseRatio = 1.25
    )
    assertEquals(3, sweepResult3.targets.size)
    val sectors3 = sweepResult3.targets.map { it.riskSectorName }.distinct()
    assertEquals("3 targets must cover 3 distinct market risk sectors", 3, sectors3.size)
    assertTrue(sweepResult3.plainEnglishSummary.contains("Multi-Vector Sweep"))
  }

  @Test
  fun verifyDiversityResolvesCommoditiesCryptoAndInfrastructure() {
    val naturalResourceHolding = com.example.data.model.Holding(
        ticker = "XLE",
        name = "Energy Select Sector SPDR Fund",
        shares = 10.0,
        avgCost = 85.0,
        currentPrice = 90.0,
        annualYieldPercent = 3.2,
        stage = 3,
        categoryTag = "Natural Resources & Energy"
    )
    val commodityHolding = com.example.data.model.Holding(
        ticker = "GDXY",
        name = "YieldMax Gold Miners ETF",
        shares = 5.0,
        avgCost = 9.8,
        currentPrice = 9.6,
        annualYieldPercent = 38.2,
        stage = 3,
        categoryTag = "Gold Miners (GDX)"
    )
    val cryptoHolding = com.example.data.model.Holding(
        ticker = "MSTY",
        name = "YieldMax MSTR Option Strategy ETF",
        shares = 20.0,
        avgCost = 15.0,
        currentPrice = 12.8,
        annualYieldPercent = 60.9,
        stage = 1,
        categoryTag = "Crypto / BTC Treasury"
    )
    val reitHolding = com.example.data.model.Holding(
        ticker = "VNQ",
        name = "Vanguard Real Estate ETF",
        shares = 15.0,
        avgCost = 82.0,
        currentPrice = 85.0,
        annualYieldPercent = 4.1,
        stage = 2,
        categoryTag = "Real Estate Infrastructure"
    )

    assertEquals(com.example.data.model.RiskSector.NATURAL_RESOURCES_ENERGY, com.example.data.model.RiskSector.resolve(naturalResourceHolding))
    assertEquals(com.example.data.model.RiskSector.COMMODITIES_PRECIOUS_METALS, com.example.data.model.RiskSector.resolve(commodityHolding))
    assertEquals(com.example.data.model.RiskSector.CRYPTO_DIGITAL_ASSETS, com.example.data.model.RiskSector.resolve(cryptoHolding))
    assertEquals(com.example.data.model.RiskSector.REAL_ESTATE_INFRASTRUCTURE, com.example.data.model.RiskSector.resolve(reitHolding))
  }

  @Test
  fun verifyOptimizerTargetsHighestYieldAssetsInEachCategory() {
    val baseline = createTestSampleHoldings()

    // Test sweep with $400: generates 3 concentrated targets
    val sweepResult = RedistributionOptimizer.optimizeSweep(
        holdings = baseline,
        availableCash = 400.0,
        currentDefenseRatio = 1.35
    )

    // Verify 3 distinct targets are picked
    assertEquals(3, sweepResult.targets.size)

    // Verify at least one target is flagged as a category/stage yield leader
    assertTrue("At least one target should be a yield leader", sweepResult.targets.any { it.isYieldLeader })

    // Verify blended yield is high (income focused)
    assertTrue("Blended yield should be >= 25% across diverse targets", sweepResult.blendedYieldAchieved >= 25.0)

    // Check that rationales indicate yield champion status and multi-vector coverage
    val leaderTarget = sweepResult.targets.first { it.isYieldLeader }
    assertTrue(leaderTarget.strategicRationale.contains("Category Yield Champion") || leaderTarget.strategicRationale.contains("Priced at") || leaderTarget.strategicRationale.contains("discount"))

    // Verify coveredStages spans multiple tiers
    assertTrue("Targets must span multiple distinct tiers", sweepResult.coveredStages.size >= 2)
    // Verify coveredRiskSectors covers distinct sectors
    assertEquals(3, sweepResult.coveredRiskSectors.size)
  }

  @Test
  fun verifyCoveredCategoriesAndExposureGapsAreDetected() {
    val baseline = createTestSampleHoldings()
    val totalVal = baseline.sumOf { it.marketValue }

    val breakdowns = com.example.data.model.RiskSector.entries
        .filter { it != com.example.data.model.RiskSector.GENERAL_DIVERSIFIED }
        .map { sector ->
            val sectorHoldings = baseline.filter { com.example.data.model.RiskSector.resolve(it) == sector }
            val sectorVal = sectorHoldings.sumOf { it.marketValue }
            val pct = (sectorVal / totalVal) * 100.0
            com.example.data.model.MarketRiskBreakdown(
                sector = sector,
                totalValue = sectorVal,
                currentWeightPercent = pct,
                holdingsCount = sectorHoldings.size,
                isOverConcentrated = pct > sector.maxHealthyWeightPercent,
                isUnderRepresented = sectorHoldings.isEmpty() || pct < (sector.maxHealthyWeightPercent * 0.3)
            )
        }

    val covered = breakdowns.filter { it.holdingsCount > 0 }
    val gaps = breakdowns.filter { it.holdingsCount == 0 }

    // Baseline has 6 covered sectors (Crypto, Commodities, S&P 500, Russell 2000, Thematic Tech, Treasury Duration)
    assertTrue("At least 5 sectors should be covered in baseline", covered.size >= 5)
    // Baseline has 3 gaps (Natural Resources, Real Estate, Tax-Exempt T-Bills)
    assertTrue("Gaps should be detected for unheld sectors", gaps.isNotEmpty())
    val gapSectors = gaps.map { it.sector }
    assertTrue("Tax-Exempt is an unheld gap in baseline", gapSectors.contains(com.example.data.model.RiskSector.TAX_EXEMPT_STABLE))
    assertTrue("Natural Resources is an unheld gap in baseline", gapSectors.contains(com.example.data.model.RiskSector.NATURAL_RESOURCES_ENERGY))
    assertTrue("Real Estate is an unheld gap in baseline", gapSectors.contains(com.example.data.model.RiskSector.REAL_ESTATE_INFRASTRUCTURE))
  }

  @Test
  fun verifyTwentyFourWeekDistributionScheduleIntegrity() {
    val schedule = com.example.data.model.DistributionScheduleRegistry.SCHEDULE_24_WEEKS
    assertEquals(24, schedule.size)

    val week1 = schedule.first()
    assertEquals(1, week1.weekNumber)
    assertEquals("Nov 09 – Nov 13", week1.dateWindow)
    assertEquals(11.38, week1.incomingCashPool, 0.01)
    assertTrue(week1.primaryOrderAction.contains("HIYY"))
    assertTrue(week1.overflowSweepAction.contains("CHPY"))

    val week24 = schedule.last()
    assertEquals(24, week24.weekNumber)
    assertEquals(14.03, week24.incomingCashPool, 0.01)
    assertEquals(14.21, week24.newWeeklyTotalRunRate, 0.01)
    assertEquals(739.07, week24.newAnnualRunRate, 0.01)

    // Verify all 24 weeks have valid positive cash pools and actions
    assertTrue(schedule.all { it.incomingCashPool > 0.0 })
    assertTrue(schedule.all { it.primaryOrderAction.isNotBlank() })
    assertTrue(schedule.all { it.overflowSweepAction.isNotBlank() })

    // Verify weekly run rate increases monotonically over the 24 weeks
    for (i in 0 until schedule.size - 1) {
      assertTrue(
        "Run rate should grow or hold steady week-over-week",
        schedule[i + 1].newWeeklyTotalRunRate >= schedule[i].newWeeklyTotalRunRate
      )
    }
  }

  @Test
  fun verifySweepAlgorithmConsidersGapsAndUnheldSectors() {
    val baseline = createTestSampleHoldings().toMutableList()

    // Add a candidate with 0 shares in an unheld sector (e.g. Natural Resources XLE)
    val unheldXLE = com.example.data.model.Holding(
        ticker = "XLE",
        name = "Energy Select Sector SPDR",
        shares = 0.0,
        avgCost = 88.0,
        currentPrice = 88.50,
        annualYieldPercent = 3.40,
        stage = 3,
        categoryTag = "Natural Resources & Energy"
    )
    baseline.add(unheldXLE)

    val result = RedistributionOptimizer.optimizeSweep(
        holdings = baseline,
        availableCash = 300.0,
        currentDefenseRatio = 1.30
    )

    assertTrue("Targets must be selected", result.targets.isNotEmpty())
    val hasGapFiller = result.targets.any { it.isGapFiller }
    if (hasGapFiller) {
      val gapTarget = result.targets.first { it.isGapFiller }
      assertTrue(gapTarget.strategicRationale.contains("GAP RESOLUTION"))
    }
    assertTrue("Should record remaining unheld sectors", result.remainingGaps.isNotEmpty())
  }

  @Test
  fun verifyPortfolioStandingEvaluationAndActionableRoute() {
    val baseline = createTestSampleHoldings()

    val sweep = RedistributionOptimizer.optimizeSweep(
        holdings = baseline,
        availableCash = 150.0,
        currentDefenseRatio = 1.30
    )

    val standing = com.example.ai.PortfolioStandingEvaluator.evaluate(
        holdings = baseline,
        settledCash = 150.0,
        defenseRatio = 1.30,
        sweepResult = sweep
    )

    assertTrue("Efficiency score must be >= 60", standing.capitalEfficiencyScore >= 60)
    assertTrue("Grade must be A or A+", standing.defenseStatusGrade == "A" || standing.defenseStatusGrade == "A+")
    assertEquals(1.30, standing.defenseToRiskRatio, 0.01)
    assertTrue("Must detect unheld sectors", standing.unheldSectors.isNotEmpty())
    assertTrue("Must have suggested route attached", standing.suggestedRoute != null)

    val route = standing.suggestedRoute!!
    assertTrue("Route must have target allocations", route.targetAllocations.isNotEmpty())
    assertEquals(150.0, route.totalCapitalToDeploy, 0.01)
    assertTrue("Blended yield must be positive", route.expectedBlendedYield > 0.0)
  }

  @Test
  fun verifyGapAssetPrioritizationSelectsHighestYieldHiyy() {
    val baseline = createTestSampleHoldings()

    // Test sweep with $300 (selects 2 targets)
    val result = RedistributionOptimizer.optimizeSweep(
        holdings = baseline,
        availableCash = 300.0,
        currentDefenseRatio = 1.30
    )

    assertTrue("Must have targets", result.targets.isNotEmpty())
    val tickers = result.targets.map { it.ticker }

    // Verify HIYY (75.90% yield) is surfaced as a prioritized Gap Asset over RIET (10.80% yield)
    assertTrue("HIYY must be selected to close the missing Healthcare gap", tickers.contains("HIYY"))
    val hiyyTarget = result.targets.first { it.ticker == "HIYY" }
    assertTrue("HIYY must be flagged as a Gap Filler", hiyyTarget.isGapFiller)
    assertTrue("HIYY rationale must indicate GAP RESOLUTION", hiyyTarget.strategicRationale.contains("GAP RESOLUTION"))

    // Ensure RIET was not selected ahead of much higher yielding gap assets like HIYY or XOMO
    assertTrue("RIET (10.8% yield) should not be chosen when HIYY (75.9%) fills gaps", !tickers.contains("RIET"))

    // Blended yield should be well above 40% due to high-velocity gap champions
    assertTrue("Blended yield must exceed 40%", result.blendedYieldAchieved >= 40.0)
  }

  @Test
  fun verifyDetailedTrajectoryCalculationAcrossSixHorizons() {
    val horizons = listOf("30D", "90D", "1Y", "5Y", "10Y", "20Y")
    val startingBalance = 25000.0
    val yieldPercent = 8.5
    val defenseRatio = 1.30
    val monthlyContribution = 200.0

    for (h in horizons) {
      val trajectory = com.example.engine.ProjectionEngine.calculateDetailedTrajectory(
        startingBalance = startingBalance,
        blendedYieldPercent = yieldPercent,
        defenseRatio = defenseRatio,
        horizonKey = h,
        monthlyContribution = monthlyContribution
      )

      assertTrue("Trajectory for $h must have points", trajectory.isNotEmpty())
      val first = trajectory.first()
      val last = trajectory.last()

      assertEquals("First point balance matches starting balance", startingBalance, first.totalBalance, 0.01)
      assertTrue("Last point balance must exceed starting balance for $h", last.totalBalance > startingBalance)
      assertTrue("Cumulative dividends must be >= 0", last.cumulativeDividends >= 0.0)
      assertTrue("Cumulative contributions must be >= 0", last.cumulativeContributions >= 0.0)
      assertTrue("Defensive value must be positive", last.defensiveValue > 0.0)
      assertTrue("Growth value must be positive", last.growthValue > 0.0)
      assertEquals("Defensive + Growth must equal Total Balance", last.totalBalance, last.defensiveValue + last.growthValue, 0.1)
    }
  }

  @Test
  fun verifyTyrProtocolFinancialExpertMetricsAndAudit() {
    val baseline = createTestSampleHoldings()
    val standing = com.example.ai.PortfolioStandingEvaluator.evaluate(
        holdings = baseline,
        settledCash = 150.0,
        defenseRatio = 1.32
    )

    // Check financial expert scores
    assertTrue("Yield sustainability score should be >= 80", standing.yieldSustainabilityScore >= 80)
    assertTrue("Tax efficiency score should be within 10% to 100%", standing.taxEfficiencyScore in 10.0..100.0)
    assertEquals("Sovereign Fortress A+", standing.financialExpertGrade)
    assertTrue("Verdict must reference DRIP=OFF and Tyr Protocol", standing.financialExpertVerdict.contains("DRIP=OFF") && standing.financialExpertVerdict.contains("Týr Protocol"))
  }

  @Test
  fun verifyThreeTargetSweepFillsUnheldGapsAndFreezesCrypto() {
    val baseline = createTestSampleHoldings()
    val heldTickers = baseline.map { it.ticker.uppercase() }.toSet()

    val result = RedistributionOptimizer.optimizeSweep(
        holdings = baseline,
        availableCash = 400.0,
        currentDefenseRatio = 1.30
    )

    assertEquals(3, result.targets.size)

    // 1. All 3 targets must be unheld Gap Assets (not existing held positions)
    assertTrue("All 3 targets must be flagged as gap fillers", result.targets.all { it.isGapFiller })
    assertTrue("None of the 3 targets should be an already-held ticker", result.targets.none { it.ticker.uppercase() in heldTickers })

    // 2. Verify yield ordering: HIYY (75.90%) first, XOMO (45.90%) second
    assertEquals("HIYY", result.targets[0].ticker)
    assertEquals("XOMO", result.targets[1].ticker)

    // 3. Verify over-concentrated Crypto sector (>25% cap) is completely frozen
    assertTrue("Crypto must not receive allocation when over-concentrated", result.targets.none { it.riskSectorName.contains("Crypto", ignoreCase = true) })

    // 4. Verify catalog template resolution preserves exact sector mapping after sweep execution
    result.targets.forEach { target ->
      val template = RedistributionOptimizer.findCatalogTemplate(target.ticker)
      assertTrue("Template must exist for ${target.ticker}", template != null)
      val resolvedSector = com.example.data.model.RiskSector.resolve(template!!)
      assertEquals(target.riskSectorName, resolvedSector.shortName)
    }
  }

  @Test
  fun verifySecurityGuardsBlockTickerUrlInjectionAndPoisonedJson() = kotlinx.coroutines.test.runTest {
    // 1. Ticker regex allowlist blocks path traversal and query injection
    assertTrue(com.example.network.FinancialDataService.isValidTicker("HIYY"))
    assertTrue(com.example.network.FinancialDataService.isValidTicker("BRK.B"))
    assertTrue(!com.example.network.FinancialDataService.isValidTicker("../etc/passwd"))
    assertTrue(!com.example.network.FinancialDataService.isValidTicker("TSLY?interval=1m& evil=1"))
    assertTrue(!com.example.network.FinancialDataService.isValidTicker("A".repeat(25)))

    val database = androidx.room.Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        com.example.data.db.AppDatabase::class.java
    ).allowMainThreadQueries().build()

    val repository = com.example.data.repository.PortfolioRepository(
        database.holdingDao(),
        database.cashDao(),
        database.alertDao()
    )

    repository.seedCustomHoldings(createTestSampleHoldings(), 200.0)

    // 2. Poisoned JSON with negative price or invalid stage must be rejected without wiping portfolio
    val poisonedStageJson = """
      {"holdings":[{"ticker":"HIYY","name":"Test","shares":10.0,"avgCost":9.0,"currentPrice":9.5,"annualYieldPercent":50.0,"stage":99}]}
    """.trimIndent()
    assertTrue("Invalid stage 99 must be rejected", !repository.importFromJson(poisonedStageJson))

    val poisonedTickerJson = """
      {"holdings":[{"ticker":"../EVIL","name":"Test","shares":10.0,"avgCost":9.0,"currentPrice":9.5,"annualYieldPercent":50.0,"stage":1}]}
    """.trimIndent()
    assertTrue("Path traversal ticker must be rejected", !repository.importFromJson(poisonedTickerJson))

    val negativePriceJson = """
      {"holdings":[{"ticker":"HIYY","name":"Test","shares":10.0,"avgCost":9.0,"currentPrice":-5.0,"annualYieldPercent":50.0,"stage":1}]}
    """.trimIndent()
    assertTrue("Negative price must be rejected", !repository.importFromJson(negativePriceJson))

    database.close()
  }

  @Test
  fun stressTestScenario1ExtremeMarketFlashCrashAndDefenseDeficit() {
    // Simulate a severe -75% flash crash across high-beta Stage 1 assets and a 0.25x defense deficit crisis
    val crashedHoldings = createTestSampleHoldings().map { h ->
      val crashMultiplier = if (h.stage == 1) 0.25 else 0.55 // -75% crash in Stage 1, -45% in Stage 2/3
      h.copy(currentPrice = (h.currentPrice * crashMultiplier).coerceAtLeast(0.01))
    }

    val sweepUnderCrisis = RedistributionOptimizer.optimizeSweep(
        holdings = crashedHoldings,
        availableCash = 500.0,
        currentDefenseRatio = 0.25
    )

    assertTrue("Crisis sweep must still produce valid targets", sweepUnderCrisis.targets.isNotEmpty())
    assertEquals(500.0, sweepUnderCrisis.totalCashToDeploy, 0.01)
    assertTrue("All allocated dollars and share counts must be finite and positive", sweepUnderCrisis.targets.all {
      it.allocatedCash.isFinite() && it.allocatedCash > 0.0 &&
      it.shareCount.isFinite() && it.shareCount > 0.0
    })

    val crisisStanding = com.example.ai.PortfolioStandingEvaluator.evaluate(
        holdings = crashedHoldings,
        settledCash = 500.0,
        defenseRatio = 0.25,
        sweepResult = sweepUnderCrisis
    )

    assertEquals("C", crisisStanding.defenseStatusGrade)
    assertTrue("Crisis verdict must flag Defensive Deficit", crisisStanding.financialExpertGrade.contains("Deficit"))
    assertTrue("Yield sustainability score must remain bounded 0..100", crisisStanding.yieldSustainabilityScore in 0..100)

    // Verify AlertEvaluator triggers price movement and defense deficit rules without NaN
    val rules = listOf(
        AlertRule(type = AlertType.DEFENSE_RATIO_TARGET, thresholdValue = 1.0, comparison = "LESS_THAN", label = "Shield Broken"),
        AlertRule(type = AlertType.ASSET_PRICE_MOVEMENT, targetTicker = "MSTY", thresholdValue = 10.0, comparison = "LESS_THAN", label = "MSTY Crash")
    )
    val metrics = PortfolioMetrics(
        totalPortfolioValue = crashedHoldings.sumOf { it.marketValue },
        defenseToRiskDensityRatio = 0.25,
        defenseStatus = DefenseStatus.HIGH_RISK
    )
    val triggered = AlertEvaluator.evaluateRules(rules, metrics, null, crashedHoldings)
    assertEquals("Both crisis alerts must fire", 2, triggered.size)
  }

  @Test
  fun stressTestScenario2SequentialSweepsFillingAllTenSectors() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = androidx.room.Room.inMemoryDatabaseBuilder(
        context,
        com.example.data.db.AppDatabase::class.java
    ).allowMainThreadQueries().build()

    val repository = com.example.data.repository.PortfolioRepository(
        database.holdingDao(),
        database.cashDao(),
        database.alertDao()
    )

    // Start from empty Fresh Start (0 holdings) and run 8 consecutive $350 sweeps
    var currentHoldings = repository.allHoldings.first()
    assertTrue(currentHoldings.isEmpty())

    for (cycle in 1..8) {
      val sweep = RedistributionOptimizer.optimizeSweep(
          holdings = currentHoldings,
          availableCash = 350.0,
          currentDefenseRatio = 1.30
      )
      assertTrue("Cycle $cycle must produce valid targets", sweep.targets.isNotEmpty())
      assertEquals(350.0, sweep.totalCashToDeploy, 0.05)

      // Commit each target into Room just like executeSweepAllocation
      for (target in sweep.targets) {
        val existing = currentHoldings.find { it.ticker.equals(target.ticker, ignoreCase = true) }
        if (existing != null) {
          val totalShares = existing.shares + target.shareCount
          val newAvg = ((existing.shares * existing.avgCost) + target.allocatedCash) / totalShares
          repository.updateHolding(existing.copy(shares = totalShares, avgCost = newAvg))
        } else {
          val template = RedistributionOptimizer.findCatalogTemplate(target.ticker)
          repository.insertHolding(
              com.example.data.model.Holding(
                  ticker = target.ticker,
                  name = target.name,
                  shares = target.shareCount,
                  avgCost = target.currentPrice,
                  currentPrice = target.currentPrice,
                  annualYieldPercent = target.annualYieldPercent,
                  stage = target.stage,
                  categoryTag = template?.categoryTag ?: target.riskSectorName,
                  distributionFrequency = template?.distributionFrequency ?: "Monthly"
              )
          )
        }
      }
      currentHoldings = repository.allHoldings.first()
    }

    // After 8 multi-vector sweeps from scratch, all 10 core risk sectors should be covered (0 unheld gaps)
    val coveredSectors = currentHoldings
        .filter { it.shares > 0.0 }
        .map { com.example.data.model.RiskSector.resolve(it) }
        .toSet()
    val allCoreSectors = com.example.data.model.RiskSector.entries
        .filter { it != com.example.data.model.RiskSector.GENERAL_DIVERSIFIED }
    val remainingGaps = allCoreSectors.filter { it !in coveredSectors }

    assertEquals("All 10 core sectors must be saturated after 8 multi-vector sweeps", 0, remainingGaps.size)

    // Run a 9th sweep when 0 gaps remain to verify smooth post-gap multi-sector optimization
    val postSaturationSweep = RedistributionOptimizer.optimizeSweep(
        holdings = currentHoldings,
        availableCash = 400.0,
        currentDefenseRatio = 1.30
    )
    assertEquals(3, postSaturationSweep.targets.size)
    assertTrue("Post-saturation sweep must have 0 gap fillers", postSaturationSweep.targets.none { it.isGapFiller })
    assertEquals(3, postSaturationSweep.coveredRiskSectors.size)

    database.close()
  }

  @Test
  fun stressTestScenario3HyperConcentrationSingleSectorOverload() {
    // 98% of capital locked in a single Crypto asset ($98,000 MSTY vs $2,000 SGOV)
    val overloadedHoldings = listOf(
        com.example.data.model.Holding(
            ticker = "MSTY",
            name = "YieldMax MSTR Option Income Strategy ETF",
            shares = 4900.0,
            avgCost = 20.0,
            currentPrice = 20.0,
            annualYieldPercent = 65.0,
            stage = 1,
            categoryTag = "Crypto / BTC Treasury (MSTR)"
        ),
        com.example.data.model.Holding(
            ticker = "SGOV",
            name = "iShares 0-3 Month Treasury Bond ETF",
            shares = 20.0,
            avgCost = 100.0,
            currentPrice = 100.0,
            annualYieldPercent = 5.1,
            stage = 4,
            categoryTag = "Tax-Exempt Stable T-Bill"
        )
    )

    val sweep = RedistributionOptimizer.optimizeSweep(
        holdings = overloadedHoldings,
        availableCash = 1000.0,
        currentDefenseRatio = 0.02
    )

    assertEquals(3, sweep.targets.size)
    assertTrue("Over-concentrated Crypto sector (98% > 25% cap) must be 100% frozen from new capital",
        sweep.targets.none { it.riskSectorName.contains("Crypto", ignoreCase = true) })
    assertTrue("All 3 targets must fill unheld gap sectors", sweep.targets.all { it.isGapFiller })

    val standing = com.example.ai.PortfolioStandingEvaluator.evaluate(
        holdings = overloadedHoldings,
        settledCash = 1000.0,
        defenseRatio = 0.02,
        sweepResult = sweep
    )
    assertEquals("C", standing.defenseStatusGrade)
    assertTrue("Must detect remaining unheld sectors", standing.unheldSectors.size >= 7)
  }

  @Test
  fun stressTestScenario4PathologicalNumericFuzzingNanInfinity() {
    val corruptHoldings = listOf(
        com.example.data.model.Holding(
            ticker = "ZERO",
            name = "Zero Price Corrupt Asset",
            shares = 100.0,
            avgCost = 0.0,
            currentPrice = 0.0,
            annualYieldPercent = Double.NaN,
            stage = 1,
            categoryTag = "Tech"
        ),
        com.example.data.model.Holding(
            ticker = "INF",
            name = "Infinite Price Corrupt Asset",
            shares = Double.POSITIVE_INFINITY,
            avgCost = -50.0,
            currentPrice = Double.POSITIVE_INFINITY,
            annualYieldPercent = Double.POSITIVE_INFINITY,
            stage = 2,
            categoryTag = "S&P 500"
        ),
        com.example.data.model.Holding(
            ticker = "XDTE",
            name = "Roundhill S&P 500 0DTE ETF",
            shares = 10.0,
            avgCost = 40.0,
            currentPrice = 38.0,
            annualYieldPercent = 27.8,
            stage = 2,
            categoryTag = "US Large-Cap Index (S&P 500)"
        )
    )

    // 1. Fuzz Optimizer with NaN, Negative, $0.01 micro-sweep, and $10,000,000 whale sweep
    val nanSweep = RedistributionOptimizer.optimizeSweep(corruptHoldings, Double.NaN, Double.NaN)
    assertTrue("NaN cash sweep must return empty targets safely", nanSweep.targets.isEmpty())

    val negSweep = RedistributionOptimizer.optimizeSweep(corruptHoldings, -500.0, -2.0)
    assertTrue("Negative cash sweep must return empty targets safely", negSweep.targets.isEmpty())

    val microSweep = RedistributionOptimizer.optimizeSweep(corruptHoldings, 0.01, 1.30)
    assertTrue("Micro $0.01 sweep must not produce NaN or crash", microSweep.targets.all {
      it.allocatedCash.isFinite() && it.shareCount.isFinite()
    })

    val whaleSweep = RedistributionOptimizer.optimizeSweep(corruptHoldings, 10_000_000.0, Double.POSITIVE_INFINITY)
    assertEquals(3, whaleSweep.targets.size)
    assertTrue("Whale $10M sweep with Infinity defenseRatio must remain finite", whaleSweep.targets.all {
      it.allocatedCash.isFinite() && it.shareCount.isFinite() && it.allocatedCash > 0.0
    })

    // 2. Fuzz ProjectionEngine with NaN, Infinity, and Extreme Yields
    val nanProjections = ProjectionEngine.calculateProjections(
        startingBalance = Double.NaN,
        blendedYieldPercent = Double.POSITIVE_INFINITY,
        monthlyContribution = -999.0,
        annualCapitalAppreciation = Double.NEGATIVE_INFINITY
    )
    assertEquals(6, nanProjections.size)
    assertTrue("Fuzzed projections must never emit NaN or Infinity", nanProjections.all {
      it.projectedPortfolioBalance.isFinite() &&
      it.projectedMonthlyDividendIncome.isFinite() &&
      it.cumulativeDividendsEarned.isFinite()
    })

    val nanTrajectory = ProjectionEngine.calculateDetailedTrajectory(
        startingBalance = Double.POSITIVE_INFINITY,
        blendedYieldPercent = Double.NaN,
        defenseRatio = Double.POSITIVE_INFINITY,
        horizonKey = "20Y",
        monthlyContribution = Double.NaN
    )
    assertTrue("Fuzzed 20Y trajectory must never emit NaN or Infinity", nanTrajectory.all {
      it.totalBalance.isFinite() &&
      it.defensiveValue.isFinite() &&
      it.growthValue.isFinite() &&
      it.monthlyDividend.isFinite()
    })
  }

  @Test
  fun stressTestScenario5HighVolumeTelemetryBatchParserFuzzing() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = androidx.room.Room.inMemoryDatabaseBuilder(
        context,
        com.example.data.db.AppDatabase::class.java
    ).allowMainThreadQueries().build()

    val repository = com.example.data.repository.PortfolioRepository(
        database.holdingDao(),
        database.cashDao(),
        database.alertDao()
    )
    repository.seedCustomHoldings(createTestSampleHoldings(), 100.0)

    val viewModel = com.example.ui.PortfolioViewModel(repository = repository)

    // Build a 480-line stress payload mixing valid telemetry, natural language alerts, and malicious/overflow lines
    val batchLines = buildString {
      appendLine("# Stress Batch Header")
      for (i in 1..50) {
        appendLine("DIVIDEND MSTY 2.50")
      }
      for (i in 1..25) {
        appendLine("Dividend received: $1.00 from XDTE")
      }
      appendLine("DEPOSIT 250.00")
      appendLine("BUY HIYY 10.0 @ 9.35")
      appendLine("PRICE TSLY 21.50")
      // Fuzz / invalid lines that must be rejected cleanly
      for (i in 1..80) {
        appendLine("DIVIDEND ../EVIL 50.00")
        appendLine("DIVIDEND MSTY -999.00")
        appendLine("DEPOSIT 999999999999999999999999999999.00")
        appendLine("BUY INVALID_TICKER_NAME_TOO_LONG 5 @ 10.0")
        appendLine("RANDOM GARBAGE LINE $i DROP TABLE holdings;")
      }
    }

    val events = viewModel.parseTelemetryBatch(batchLines)
    // 50 MSTY ($125) + 25 XDTE ($25) + 1 DEPOSIT ($250) + 1 BUY HIYY + 1 PRICE TSLY = 78 valid events
    assertEquals("Must stage exactly 78 valid events and reject all 400+ malformed lines", 78, events.size)
    val dividendAndDepositSum = events
        .filter { it.type == com.example.ui.TelemetryEventType.DIVIDEND || it.type == com.example.ui.TelemetryEventType.DEPOSIT }
        .sumOf { it.amountOrShares }
    assertEquals("Total staged cash inflows must equal +$400.00", 400.0, dividendAndDepositSum, 0.01)

    database.close()
  }
}



