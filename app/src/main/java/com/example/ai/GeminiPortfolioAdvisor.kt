// File: GeminiPortfolioAdvisor.kt
package com.example.ai

import android.util.Log
import com.example.data.model.Holding
import com.example.data.model.RiskSector
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.ThinkingConfig
import com.google.firebase.ai.type.ThinkingLevel
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.Locale

private const val TAG = "GeminiAdvisor"

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isThinking: Boolean = false,
    val actionableRoute: ActionableTradeRoute? = null
)

enum class MessageSender {
    USER,
    GEMINI_ARGON,
    GEMINI_FLASH,
    SYSTEM
}

class GeminiPortfolioAdvisor {

    private val systemInstructionContent by lazy {
        content {
            text(
                """
                You are the Týr Protocol Senior Financial Expert & Deep Portfolio Auditor (https://tyrprotocol.pro).
                The user is managing their investment portfolio using a local-first, DRIP=OFF closed-loop engine.
                
                Core Institutional Principles (Týr Protocol Standard - https://tyrprotocol.pro):
                1. DRIP = OFF: Dividend payouts are pooled as cash and redeployed in concentrated sweeps (1 to 3 targets max) into unheld gap assets and yield champions.
                2. Stage Architecture:
                   - Stage 1: Heavy Cash Flow Engines (High-yield income, covered calls e.g. JEPQ, SVOL, JEPI).
                   - Stage 2: Core Market Stability (Broad index anchors, dividend growers e.g. SCHD, VOO).
                   - Stage 3: High-Beta Growth Accelerators (Tech, momentum e.g. QQQM, SMH, IWM).
                   - Stage 4: Tax-Shielded Reserves & Hedging (Treasuries e.g. SGOV, TLT).
                3. Defense-to-Risk Density Ratio: Institutional target is >= 1.25x.
                   - Defense = Stage 2 + Stage 4 + Settled Cash.
                   - Risk = Stage 1 + Stage 3.
                4. Analytical Focus:
                   - Yield Sustainability: Verify dividend payout health, cash-flow coverage, and NAV preservation.
                   - Tax Efficiency: Maximize Section 1256 contracts (60/40 rule), Return of Capital (ROC), and tax-exempt Treasury vehicles (SGOV).
                   - Gap Resolution: Identify blind spots across all 9 market vectors (Tech, Large-Cap, Fixed Income, Energy, REITs, BDCs, Utilities, Hedged Cash Flow, Crypto, Healthcare) and propose highest-yield unheld gap assets.
                5. Recommendation Format:
                   Deliver clear, step-by-step actionable trade routes with plain English rationale.
                   Explain WHY each ticker is selected. Never use corporate fluff or hollow Wall Street platitudes.
                """.trimIndent()
            )
        }
    }

    // Lazy initialization of Gemini 4 Argon with High Thinking enabled
    private val argonThinkingModel by lazy {
        try {
            Firebase.ai.generativeModel(
                modelName = "gemini-4-argon",
                generationConfig = generationConfig {
                    thinkingConfig = ThinkingConfig.Builder().setThinkingLevel(ThinkingLevel.HIGH).build()
                },
                systemInstruction = systemInstructionContent
            )
        } catch (e: Exception) {
            Log.w(TAG, "Argon with thinking init fallback", e)
            Firebase.ai.generativeModel(
                modelName = "gemini-4-argon",
                systemInstruction = systemInstructionContent
            )
        }
    }

    // Lazy initialization of Gemini 3.8 Flash for fast conversational queries
    private val flashModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = systemInstructionContent
        )
    }

    /**
     * Conducts deep portfolio audit, stress testing, and step-by-step route calculations
     * using gemini-4-argon with High Thinking.
     */
    suspend fun auditPortfolioWithThinking(
        holdings: List<Holding>,
        settledCash: Double,
        defenseRatio: Double,
        standing: PortfolioStanding,
        userQuery: String
    ): String = withContext(Dispatchers.IO) {
        val portfolioSummary = formatStandingContext(holdings, settledCash, defenseRatio, standing)
        val prompt = """
            $portfolioSummary
            
            USER INQUIRY:
            $userQuery
            
            Conduct an institutional-grade deep analysis of this closed-loop portfolio. 
            Evaluate:
            1. Defense-to-Risk Density Ratio health (target >= 1.25x) and efficiency grade (${standing.defenseStatusGrade}).
            2. Multi-vector gap coverage (unheld sectors: ${standing.unheldSectors.joinToString { it.displayName }}).
            3. Cash-flow generation efficiency with DRIP=OFF ($${String.format(Locale.US, "%.2f", standing.monthlyIncome)}/mo current run rate).
            4. Step-by-step actionable route: What exact 1 to 3 assets should be swept next to maximize income and close gaps while protecting defense?
            
            Explain everything in plain, accessible English with clear step-by-step rationale.
        """.trimIndent()

        try {
            withTimeout(55_000L) {
                val response = argonThinkingModel.generateContent(prompt)
                val responseText = response.text
                if (!responseText.isNullOrBlank()) {
                    responseText
                } else {
                    fallbackAuditAnalysis(holdings, settledCash, defenseRatio, standing, userQuery)
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Argon thinking generation timed out", e)
            fallbackAuditAnalysis(holdings, settledCash, defenseRatio, standing, userQuery)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI Argon call failed, falling back to local heuristic intelligence", e)
            fallbackAuditAnalysis(holdings, settledCash, defenseRatio, standing, userQuery)
        }
    }

    /**
     * Fast conversational query using gemini-3.8-flash.
     */
    suspend fun askConversational(
        conversationHistory: List<ChatMessage>,
        userMessage: String,
        holdings: List<Holding>,
        settledCash: Double,
        defenseRatio: Double,
        standing: PortfolioStanding
    ): String = withContext(Dispatchers.IO) {
        val contextPrompt = formatStandingContext(holdings, settledCash, defenseRatio, standing)
        val fullPrompt = """
            $contextPrompt
            
            Conversation context:
            ${conversationHistory.takeLast(4).joinToString("\n") { "${it.sender}: ${it.text}" }}
            
            User message: $userMessage
            
            Provide a direct, conversational, and encouraging response as a senior financial copilot.
            Include a step-by-step actionable path with plain English rationale.
        """.trimIndent()

        try {
            withTimeout(40_000L) {
                val response = flashModel.generateContent(fullPrompt)
                response.text ?: fallbackConversational(userMessage, holdings, defenseRatio, standing)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Flash model response failed", e)
            fallbackConversational(userMessage, holdings, defenseRatio, standing)
        }
    }

    private fun formatStandingContext(
        holdings: List<Holding>,
        settledCash: Double,
        defenseRatio: Double,
        standing: PortfolioStanding
    ): String {
        val holdingsList = holdings.joinToString("\n") { h ->
            val sector = RiskSector.resolve(h)
            String.format(
                Locale.US,
                "- %s (%s, Stage %d, Sector: %s): %.2f shares @ $%.2f (Current $%.2f) | Mkt: $%.2f | Yield: %.1f%%",
                h.ticker, h.name, h.stage, sector.shortName, h.shares, h.avgCost, h.currentPrice, h.marketValue, h.annualYieldPercent
            )
        }

        val unheldStr = if (standing.unheldSectors.isNotEmpty()) {
            standing.unheldSectors.joinToString(", ") { it.displayName }
        } else "None (Full 9-Vector Coverage)"

        return """
            CURRENT PORTFOLIO TELEMETRY & STANDING:
            - Total Portfolio Capital: $%.2f
            - Settled Buying Power (Cash): $%.2f
            - Projected Annual Dividend Income: $%.2f ($%.2f/month)
            - Blended Annual Yield: %.2f%%
            - Defense-to-Risk Density Ratio: %.2fx (Target >= 1.25x | Grade: %s)
            - Capital Efficiency Score: %d/100
            - Týr Financial Expert Grade: %s
            - Yield Sustainability Score: %d/100
            - Tax Efficiency Score: %.1f%% (Sec 1256 / ROC / SGOV)
            - Expert Verdict: %s
            - Sector Coverage: %d/%d covered vectors
            - Unheld Sector Gaps: %s
            - Reference Standard: Týr Protocol (https://tyrprotocol.pro)
            
            CURRENT ASSET HOLDINGS:
            $holdingsList
        """.trimIndent().format(
            Locale.US,
            standing.totalCapital,
            standing.settledBuyingPower,
            standing.annualIncome,
            standing.monthlyIncome,
            standing.blendedYieldPercent,
            standing.defenseToRiskRatio,
            standing.defenseStatusGrade,
            standing.capitalEfficiencyScore,
            standing.financialExpertGrade,
            standing.yieldSustainabilityScore,
            standing.taxEfficiencyScore,
            standing.financialExpertVerdict,
            standing.coveredSectorsCount,
            standing.totalSectorsCount,
            unheldStr
        )
    }

    /**
     * Local analytical heuristic engine providing instant, mathematically sound output
     * with step-by-step trade routes.
     */
    fun fallbackAuditAnalysis(
        holdings: List<Holding>,
        settledCash: Double,
        defenseRatio: Double,
        standing: PortfolioStanding,
        query: String
    ): String {
        val totalMkt = standing.totalCapital
        val stage1Val = holdings.filter { it.stage == 1 }.sumOf { it.marketValue }
        val stage2Val = holdings.filter { it.stage == 2 }.sumOf { it.marketValue }
        val stage3Val = holdings.filter { it.stage == 3 }.sumOf { it.marketValue }
        val stage4Val = holdings.filter { it.stage == 4 }.sumOf { it.marketValue } + settledCash

        val unheldList = standing.unheldSectors.take(3).map { it.displayName }
        val gapAdvice = if (unheldList.isNotEmpty()) {
            "Your highest-priority blind spots are **${unheldList.joinToString(", ")}**. Allocating sweeps into high-yielding champions in these sectors broadens income resilience without sacrificing cash flow."
        } else {
            "All core sectors are represented! Your portfolio has broad multi-vector defense across equities, fixed income, and commodities."
        }

        val defenseStatus = when {
            defenseRatio >= 1.25 -> "STRONG (Grade: ${standing.defenseStatusGrade}). Core stability (Stage 2 & 4) comfortably defends against equity or crypto drawdowns."
            defenseRatio >= 1.0 -> "BALANCED EQUILIBRIUM (Grade: ${standing.defenseStatusGrade}). Adequate defense, but a mild high-beta pullback will pressure your ratio."
            else -> "OVER-WEIGHT RISK (Grade: ${standing.defenseStatusGrade}). High-yield option engines and growth vehicles outsize defense anchors. Direct sweeps to Stage 2 (SCHD/VOO) and Stage 4 (SGOV)."
        }

        val suggestedRouteBlock = standing.suggestedRoute?.let { route ->
            val allocLines = route.targetAllocations.joinToString("\n") { a ->
                String.format(
                    Locale.US,
                    "  • **%s** (%s - Stage %d): Deploy **$%.2f** (~%.2f shares @ $%.2f) | %.1f%% Yield%s",
                    a.ticker, a.sectorName, a.stage, a.dollarAmount, a.estimatedShares, a.sharePrice, a.annualYieldPercent,
                    if (a.isGapFiller) " [🛡 Gap Filler]" else if (a.isYieldChampion) " [★ Yield Champion]" else ""
                )
            }

            """
            
            **Step-by-Step Actionable Sweep Route:**
            $allocLines
            - **Estimated Blended Yield:** ${String.format(Locale.US, "%.2f%%", route.expectedBlendedYield)} (+$$${String.format(Locale.US, "%.2f", route.projectedMonthlyCashFlow)}/mo)
            - **Defense Ratio Trajectory:** ${String.format(Locale.US, "%.2fx → %.2fx", route.defenseRatioBefore, route.defenseRatioAfter)}
            - **Rationale:** ${route.plainEnglishRationale}
            """.trimIndent()
        } ?: "\n*Settled cash is $0.00. As weekly dividend payouts arrive via DRIP=OFF, the engine will activate concentrated sweep targets.*"

        return """
            ### Týr Protocol Financial Expert Deep Portfolio Audit
            *Verified Institutional Engine — [https://tyrprotocol.pro](https://tyrprotocol.pro)*

            **1. Portfolio Health, Defense Density & Standing:**
            - Current Defense Ratio: **${String.format(Locale.US, "%.2f", defenseRatio)}x** (Target >= 1.25x | Grade: **${standing.defenseStatusGrade}**)
            - **Týr Expert Rating:** **${standing.financialExpertGrade}** (${standing.financialExpertVerdict})
            - **Yield Sustainability Score:** **${standing.yieldSustainabilityScore}/100**
            - **Tax Efficiency Score:** **${String.format(Locale.US, "%.1f%%", standing.taxEfficiencyScore)}** (Sec 1256 Contracts / ROC / SGOV)
            - Assessment: $defenseStatus

            **2. Multi-Vector Category Diversity & Blind Spots:**
            - Vector Coverage: **${standing.coveredSectorsCount} of ${standing.totalSectorsCount}** sectors held.
            - Gap Resolution: $gapAdvice

            **3. Capital Distribution Across Stages:**
            - **Stage 1 (Cash Flow Engines):** ${String.format(Locale.US, "$%.2f (%.1f%%)", stage1Val, if (totalMkt > 0) (stage1Val/totalMkt)*100 else 0.0)}
            - **Stage 2 (Core Stability):** ${String.format(Locale.US, "$%.2f (%.1f%%)", stage2Val, if (totalMkt > 0) (stage2Val/totalMkt)*100 else 0.0)}
            - **Stage 3 (Growth Beta):** ${String.format(Locale.US, "$%.2f (%.1f%%)", stage3Val, if (totalMkt > 0) (stage3Val/totalMkt)*100 else 0.0)}
            - **Stage 4 & Cash (Reserves):** ${String.format(Locale.US, "$%.2f (%.1f%%)", stage4Val, if (totalMkt > 0) (stage4Val/totalMkt)*100 else 0.0)}
            $suggestedRouteBlock
        """.trimIndent()
    }

    private fun fallbackConversational(
        userMessage: String,
        holdings: List<Holding>,
        defenseRatio: Double,
        standing: PortfolioStanding
    ): String {
        val gapMsg = if (standing.unheldSectors.isNotEmpty()) {
            "We have ${standing.unheldSectors.size} open sector gaps (${standing.unheldSectors.take(2).joinToString { it.shortName }}), but your core defense is holding strong at ${String.format(Locale.US, "%.2f", defenseRatio)}x."
        } else {
            "All market sectors are covered with a robust ${String.format(Locale.US, "%.2f", defenseRatio)}x defense ratio."
        }

        return "Under the Týr Protocol standard (https://tyrprotocol.pro), your portfolio is rated **${standing.financialExpertGrade}** with **${standing.yieldSustainabilityScore}/100** yield sustainability and **${String.format(Locale.US, "%.0f%%", standing.taxEfficiencyScore)}** tax efficiency. $gapMsg With DRIP=OFF, every dividend dollar pools into settled cash, ready to deploy into our highest-yield gap fillers."
    }
}
