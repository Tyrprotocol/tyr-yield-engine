// File: MarketRiskCategory.kt
package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CyanCore
import com.example.ui.theme.EmeraldYieldPrimary
import com.example.ui.theme.GoldAlert
import com.example.ui.theme.PurpleTech

/**
 * Multi-vector market diversity categories designed to eliminate single-point failure risk.
 * Extends across Crypto, Commodities, Natural Resources/Energy, Real Estate,
 * Broad S&P/Russell Indices, Thematic Tech/Single-Stock, Treasury Duration, and Tax-Exempt Reserves.
 */
enum class RiskSector(
    val categoryId: String,
    val displayName: String,
    val shortName: String,
    val maxHealthyWeightPercent: Double,
    val description: String,
    val accentColor: Color
) {
    HEALTHCARE_DEFENSIVE(
        categoryId = "healthcare_defensive",
        displayName = "Healthcare & Defensive Cash Flow",
        shortName = "Healthcare",
        maxHealthyWeightPercent = 20.0,
        description = "Healthcare, pharmaceuticals, telehealth, and essential wellness cash generators (HIYY, XLV). Inelastic consumer demand with low correlation to tech and crypto beta.",
        accentColor = Color(0xFF00BFA5)
    ),
    CRYPTO_DIGITAL_ASSETS(
        categoryId = "crypto_digital_assets",
        displayName = "Crypto & Digital Assets",
        shortName = "Crypto Assets",
        maxHealthyWeightPercent = 25.0,
        description = "High beta synthetic options on digital assets and Bitcoin treasuries (MSTY, HOOY, CONY, CRCO). High yield velocity, capped to insulate portfolio from crypto drawdowns.",
        accentColor = Color(0xFFFF9800)
    ),
    COMMODITIES_PRECIOUS_METALS(
        categoryId = "commodities_metals",
        displayName = "Commodities & Precious Metals",
        shortName = "Commodities",
        maxHealthyWeightPercent = 20.0,
        description = "Gold, silver, and precious metals mining income (GDXY, GLD, SLV). Counter-cyclical inflation hedge and currency debasement defense.",
        accentColor = Color(0xFFFFD54F)
    ),
    NATURAL_RESOURCES_ENERGY(
        categoryId = "natural_resources_energy",
        displayName = "Natural Resources & Energy",
        shortName = "Natural Resources",
        maxHealthyWeightPercent = 20.0,
        description = "Physical input assets: energy grids, oil/gas infrastructure, uranium, agriculture, and copper (XLE, AMLP, XOMO, URA, DBA, COPX). Tangible real-economy cash flow.",
        accentColor = Color(0xFFD84315)
    ),
    REAL_ESTATE_INFRASTRUCTURE(
        categoryId = "real_estate_infrastructure",
        displayName = "Real Estate & Infrastructure",
        shortName = "Real Estate / Infra",
        maxHealthyWeightPercent = 20.0,
        description = "Commercial real estate, REITs, data centers, and essential utility infrastructure (VNQ, UTG, NFRA, RIET). Contractual rental and utility distribution cash flows.",
        accentColor = Color(0xFF00897B)
    ),
    SP500_LARGE_CAP(
        categoryId = "sp500_large_cap",
        displayName = "S&P 500 Large-Cap Manifolds",
        shortName = "S&P 500",
        maxHealthyWeightPercent = 35.0,
        description = "500 largest US enterprises (XDTE, VOO, SPY). Core institutional spine providing market ballast and zero single-company failure risk.",
        accentColor = CyanCore
    ),
    RUSSELL_SMALL_CAP(
        categoryId = "russell_small_cap",
        displayName = "Russell 2000 Small-Cap Manifolds",
        shortName = "Russell 2000",
        maxHealthyWeightPercent = 25.0,
        description = "2,000 domestic small-cap enterprises (RDTE, IWM). Captures cyclical economic expansion and interest-rate cut tailwinds.",
        accentColor = Color(0xFF64B5F6)
    ),
    EQUITIES_THEMATIC_TECH(
        categoryId = "equities_thematic_tech",
        displayName = "Big Tech & Thematic Single-Stock Beta",
        shortName = "Thematic Tech",
        maxHealthyWeightPercent = 25.0,
        description = "Mega-cap tech innovators and high-beta single-stock option overlays (FEPI, TSLY, NVDY). Aggressive secular upside capture disciplined by strict risk caps.",
        accentColor = PurpleTech
    ),
    FIXED_INCOME_DURATION(
        categoryId = "fixed_income_duration",
        displayName = "US Treasury Duration & Fixed Income",
        shortName = "Treasury Duration",
        maxHealthyWeightPercent = 25.0,
        description = "20+ Year US Treasury duration buy-write and bond ballast (TLTW, TLT). Portfolio EGR valve, deflation insurance, and interest-rate buffer.",
        accentColor = Color(0xFF81C784)
    ),
    TAX_EXEMPT_STABLE(
        categoryId = "tax_exempt_stable",
        displayName = "Tax-Exempt Reserves & Capital (Tier 4)",
        shortName = "Tax-Exempt T-Bills",
        maxHealthyWeightPercent = 40.0,
        description = "State and federal tax-exempt short-duration Treasuries (SGOV) and municipal paper. Strictly zero capital risk, zero taxable drag, ultimate dry powder.",
        accentColor = GoldAlert
    ),
    GENERAL_DIVERSIFIED(
        categoryId = "general_diversified",
        displayName = "Diversified / Hybrid Assets",
        shortName = "Diversified",
        maxHealthyWeightPercent = 25.0,
        description = "Multi-asset, blended allocation, or balanced income instruments.",
        accentColor = Color(0xFFB0BEC5)
    );

    companion object {
        fun resolve(holding: Holding): RiskSector {
            val ticker = holding.ticker.uppercase()
            val tag = holding.categoryTag.lowercase()
            val name = holding.name.lowercase()
            val notes = holding.notes.lowercase()
            val combined = "$tag $name $notes"

            return when {
                // Tax-Exempt & Cash Reserves (Stage 4) - evaluate before general bond/duration checks
                holding.stage == 4 || combined.contains("sgov") ||
                    combined.contains("tax-exempt") || combined.contains("t-bill") ||
                    combined.contains("muni") || combined.contains("municipal") ||
                    ticker in listOf("SGOV", "MUB", "USFR", "BIL") ->
                    TAX_EXEMPT_STABLE

                // Healthcare & Defensive Cash Flow
                combined.contains("health") || combined.contains("pharma") ||
                    combined.contains("medical") || combined.contains("biotech") ||
                    combined.contains("hims") ||
                    ticker in listOf("HIYY", "XLV", "IBB", "XBI") ->
                    HEALTHCARE_DEFENSIVE

                // Commodities & Precious Metals
                ticker in listOf("GDXY", "MINY", "GLD", "SLV", "GDX", "GDXJ", "DBC", "PDBC") ||
                    combined.contains("gold") || combined.contains("silver") ||
                    combined.contains("commodity") || combined.contains("commodities") ||
                    combined.contains("precious metal") || combined.contains("hard-asset") ->
                    COMMODITIES_PRECIOUS_METALS

                // Natural Resources & Energy Infrastructure
                ticker in listOf("XLE", "AMLP", "XOMO", "URA", "DBA", "COPX", "IGE", "GUNR", "CLMT") ||
                    combined.contains("natural resource") || combined.contains("energy") ||
                    combined.contains("oil") || combined.contains("gas") ||
                    combined.contains("uranium") || combined.contains("agriculture") ||
                    combined.contains("timber") || combined.contains("pipeline") ||
                    combined.contains("copper") || combined.contains("mineral") ->
                    NATURAL_RESOURCES_ENERGY

                // Crypto & Digital Assets
                ticker in listOf("MSTY", "HOOY", "CONY", "CRCO", "IBIT", "BITO", "ETHA") ||
                    combined.contains("crypto") || combined.contains("btc") ||
                    combined.contains("bitcoin") || combined.contains("hood") ||
                    combined.contains("coin") || combined.contains("crcl") ||
                    combined.contains("stablecoin") || combined.contains("eth") ||
                    combined.contains("solana") ->
                    CRYPTO_DIGITAL_ASSETS

                // Real Estate & Physical Infrastructure
                ticker in listOf("VNQ", "UTG", "NFRA", "RIET", "KBWY", "XLU") ||
                    combined.contains("reit") || combined.contains("real estate") ||
                    combined.contains("infrastructure") || combined.contains("utility") ||
                    combined.contains("utilities") ->
                    REAL_ESTATE_INFRASTRUCTURE

                // Broad Market Index Manifolds (S&P 500, Russell 2000)
                combined.contains("s&p 500") || combined.contains("s&p 100") ||
                    combined.contains("sp500") || combined.contains("russell") ||
                    combined.contains("r2000") || combined.contains("small-cap index") ||
                    combined.contains("large-cap index") ||
                    ticker in listOf("XDTE", "YMAG", "RDTE", "RDTY", "VOO", "SPY", "IVV", "IWM") -> {
                    if (combined.contains("russell") || combined.contains("r2000") || ticker in listOf("RDTE", "RDTY", "IWM")) {
                        RUSSELL_SMALL_CAP
                    } else {
                        SP500_LARGE_CAP
                    }
                }

                // Thematic Tech & Single-Stock Beta
                ticker in listOf("TSLY", "NVDY", "FEPI", "CVNY", "ULTY", "CHPY", "QDTE", "SMH", "QQQM") ||
                    combined.contains("tsla") || combined.contains("nvda") ||
                    combined.contains("fang") || combined.contains("semiconductor") ||
                    combined.contains("thematic tech") ||
                    combined.contains("ai /") || combined.contains("ev /") ->
                    EQUITIES_THEMATIC_TECH

                // Fixed Income & Treasury Duration
                ticker in listOf("TLTW", "TLT", "AGG", "BND", "HYG") ||
                    combined.contains("treasury duration") || combined.contains("20+ yr") ||
                    combined.contains("duration") || combined.contains("bond") ->
                    FIXED_INCOME_DURATION

                else -> GENERAL_DIVERSIFIED
            }
        }
    }
}

data class MarketRiskBreakdown(
    val sector: RiskSector,
    val totalValue: Double,
    val currentWeightPercent: Double,
    val holdingsCount: Int,
    val isOverConcentrated: Boolean,
    val isUnderRepresented: Boolean
)
