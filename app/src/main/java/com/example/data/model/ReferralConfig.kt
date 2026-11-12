// File: ReferralConfig.kt
package com.example.data.model

data class ReferralEndpoint(
    val id: String, // REF-01 through REF-12
    val title: String,
    val category: String, // "Brokerage Partner", "Automated Tools", "Tax & Research", "Author Support"
    val bonusDescription: String,
    val directUrl: String,
    val tag: String,
    val isPrimaryPartner: Boolean = false
)

object ReferralRegistry {
    const val PINNED_FTC_DISCLOSURE =
        "FTC SPONSORSHIP & AFFILIATE DISCLOSURE: Certain links listed below are verified promotional referral or affiliate endpoints. If you open a new brokerage account or sign up using these links, the author may receive compensation or promotional bonus shares at zero extra cost to you. Tyr's Technical Consultation operates independently and does not custody client funds. Always perform your own due diligence before depositing funds."

    val REFERRAL_CONFIG: List<ReferralEndpoint> = listOf(
        ReferralEndpoint(
            id = "REF-01",
            title = "Robinhood Financial",
            category = "Brokerage Partner",
            bonusDescription = "Get up to $200 in free fractional stock upon account approval & first deposit. Zero commission on stocks and ETFs.",
            directUrl = "https://join.robinhood.com",
            tag = "FREE STOCKS",
            isPrimaryPartner = true
        ),
        ReferralEndpoint(
            id = "REF-02",
            title = "Webull Brokerage",
            category = "Brokerage Partner",
            bonusDescription = "Earn up to 12 free fractional shares with extended-hours trading, advanced charting, and cash management yields.",
            directUrl = "https://www.webull.com",
            tag = "UP TO 12 SHARES",
            isPrimaryPartner = true
        ),
        ReferralEndpoint(
            id = "REF-03",
            title = "M1 Finance",
            category = "Automated Tools",
            bonusDescription = "Automated dynamic pie-slice investing. Perfect for hands-off closed-loop dividend rebalancing sweeps.",
            directUrl = "https://m1.com",
            tag = "PIE REBALANCING"
        ),
        ReferralEndpoint(
            id = "REF-04",
            title = "Interactive Brokers (IBKR)",
            category = "Brokerage Partner",
            bonusDescription = "Institutional execution quality, lowest global margin borrowing rates, and multi-currency global access.",
            directUrl = "https://www.interactivebrokers.com",
            tag = "PRO EXECUTION"
        ),
        ReferralEndpoint(
            id = "REF-05",
            title = "Fundrise Real Estate",
            category = "Alternative Income",
            bonusDescription = "Private commercial real estate and venture debt cash flow funds with low minimums ($10) for non-accredited investors.",
            directUrl = "https://fundrise.com",
            tag = "PRIVATE DEBT"
        ),
        ReferralEndpoint(
            id = "REF-06",
            title = "Acorns Micro-Investing",
            category = "Automated Tools",
            bonusDescription = "Automate spare-change round-ups from daily purchases directly into a diversified core dividend portfolio.",
            directUrl = "https://www.acorns.com",
            tag = "ROUND-UPS"
        ),
        ReferralEndpoint(
            id = "REF-07",
            title = "Public.com Treasury & Stocks",
            category = "Brokerage Partner",
            bonusDescription = "Direct access to high-yield Treasury accounts (exempt from state/local taxes), bond yields, and stocks.",
            directUrl = "https://public.com",
            tag = "T-BILLS & BONDS"
        ),
        ReferralEndpoint(
            id = "REF-08",
            title = "SoFi Invest",
            category = "Brokerage Partner",
            bonusDescription = "Zero-fee automated robo-investing and active trading with a welcome deposit bonus for new members.",
            directUrl = "https://www.sofi.com/invest",
            tag = "CASH BONUS"
        ),
        ReferralEndpoint(
            id = "REF-09",
            title = "CoinLedger Tax Suite",
            category = "Tax & Research",
            bonusDescription = "Automated 1099-B, Schedule D, and crypto capital gains tax calculation engine. Export directly to TurboTax.",
            directUrl = "https://coinledger.io",
            tag = "TAX REPORTING"
        ),
        ReferralEndpoint(
            id = "REF-10",
            title = "TradingView Premium",
            category = "Tax & Research",
            bonusDescription = "Frontier charting platform with real-time price alerts, volume profiling, and institutional ETF screeners.",
            directUrl = "https://www.tradingview.com",
            tag = "CHARTING"
        ),
        ReferralEndpoint(
            id = "REF-11",
            title = "Seeking Alpha Premium",
            category = "Tax & Research",
            bonusDescription = "Detailed dividend safety grades, payout history ratings, Wall Street consensus, and earnings call transcripts.",
            directUrl = "https://seekingalpha.com",
            tag = "DIVIDEND GRADES"
        ),
        ReferralEndpoint(
            id = "REF-12",
            title = "Týr Protocol Open Engineering",
            category = "Author Support",
            bonusDescription = "Support $0-cost open systems engineering, sovereign closed-loop yield research, and local-first financial tooling.",
            directUrl = "https://tyrprotocol.pro",
            tag = "SUPPORT DEV"
        )
    )
}
