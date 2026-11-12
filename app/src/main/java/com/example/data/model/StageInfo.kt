// File: StageInfo.kt
package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CyanCore
import com.example.ui.theme.EmeraldYieldPrimary
import com.example.ui.theme.GoldAlert
import com.example.ui.theme.PurpleTech

data class StageMetadata(
    val stageNumber: Int,
    val title: String,
    val subtitle: String,
    val plainEnglishDescription: String,
    val roleType: StageRoleType,
    val accentColor: Color
)

enum class StageRoleType {
    RISK_EXPOSURE,
    CORE_DEFENSE,
    GROWTH_BETA,
    TAX_SHIELD_DEFENSE
}

object StageRegistry {
    val stages = listOf(
        StageMetadata(
            stageNumber = 1,
            title = "Highest Yield / High Volatility",
            subtitle = "Least Stable | Single-Stock & Crypto Synthetics",
            plainEnglishDescription = "Highest cash yield engines (30%-70%+). Generates aggressive incoming dividend flow, but exhibits the highest price volatility and underlying asset swings.",
            roleType = StageRoleType.RISK_EXPOSURE,
            accentColor = EmeraldYieldPrimary
        ),
        StageMetadata(
            stageNumber = 2,
            title = "Moderate Yield / Moderate Volatility",
            subtitle = "Index Manifolds & Diversified Baskets",
            plainEnglishDescription = "Broad market index overlays (S&P 500 XDTE, Russell 2000 RDTE, FANG+ FEPI). Delivers 25%-35% cash yield with dampened volatility across diversified baskets.",
            roleType = StageRoleType.CORE_DEFENSE,
            accentColor = CyanCore
        ),
        StageMetadata(
            stageNumber = 3,
            title = "Low Yield / Minor Fluctuation",
            subtitle = "Duration Ballast & Macro Hedges",
            plainEnglishDescription = "Low-yield, minor fluctuation vehicles (e.g. TLTW Treasury duration BuyWrite, GDXY gold hedge). Functions as portfolio shock absorbers and rate cycle buffers.",
            roleType = StageRoleType.GROWTH_BETA,
            accentColor = PurpleTech
        ),
        StageMetadata(
            stageNumber = 4,
            title = "Tax-Exempt / Verified Stable",
            subtitle = "Zero Risk | Capital Preservation",
            plainEnglishDescription = "Strictly tax-exempt, verified stable, zero-risk cash reserves (e.g. SGOV 0-3 month T-bills, municipal bonds). Zero capital risk, zero taxable drag, serving as ultimate dry powder.",
            roleType = StageRoleType.TAX_SHIELD_DEFENSE,
            accentColor = GoldAlert
        )
    )

    fun getStage(stageNumber: Int): StageMetadata {
        return stages.find { it.stageNumber == stageNumber } ?: stages[0]
    }
}
