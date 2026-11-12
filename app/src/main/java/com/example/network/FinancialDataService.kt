// File: FinancialDataService.kt
package com.example.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val TAG = "FinancialDataService"

data class MarketQuote(
    val ticker: String,
    val currentPrice: Double,
    val changePercent: Double,
    val timestamp: Long = System.currentTimeMillis()
)

sealed interface PriceRefreshResult {
    data class Success(val quotes: Map<String, MarketQuote>, val timestamp: Long) : PriceRefreshResult
    data class RateLimited(val message: String, val quotes: Map<String, MarketQuote>) : PriceRefreshResult
    data class Error(val message: String) : PriceRefreshResult
}

class FinancialDataService {

    companion object {
        private val VALID_TICKER_REGEX = Regex("^[A-Z][A-Z0-9.\\-]{0,9}$")
        private const val MAX_TICKERS_PER_SWEEP = 25
        private const val MAX_RESPONSE_CHARS = 131_072 // 128 KB response guard

        fun isValidTicker(rawTicker: String): Boolean {
            val normalized = rawTicker.trim().uppercase()
            return VALID_TICKER_REGEX.matches(normalized) && !normalized.contains("..")
        }
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    // In-memory rate-limit tracking: minimum 15 seconds between live network sweeps to prevent 429 errors
    @Volatile
    private var lastRequestTime = 0L
    private val rateLimitIntervalMs = 15_000L

    /**
     * Fetches real-time price quotes for a list of ticker symbols.
     * Enforces strict regex sanitization against URL injection, bounds iteration count,
     * and guarantees socket closure via .use {}.
     */
    suspend fun fetchQuotes(tickers: List<String>): PriceRefreshResult = withContext(Dispatchers.IO) {
        val sanitizedTickers = tickers
            .map { it.trim().uppercase() }
            .filter { VALID_TICKER_REGEX.matches(it) }
            .distinct()
            .take(MAX_TICKERS_PER_SWEEP)

        if (sanitizedTickers.isEmpty()) {
            return@withContext PriceRefreshResult.Success(emptyMap(), System.currentTimeMillis())
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRequestTime < rateLimitIntervalMs) {
            val waitSec = ((rateLimitIntervalMs - (currentTime - lastRequestTime)) / 1000).coerceAtLeast(1)
            Log.w(TAG, "Rate limit guard active. Rate limit interval $waitSec seconds remaining.")
            return@withContext PriceRefreshResult.RateLimited(
                "Market data refreshed recently. Please wait $waitSec seconds to conserve API quota.",
                emptyMap()
            )
        }

        lastRequestTime = currentTime
        val results = mutableMapOf<String, MarketQuote>()

        try {
            val symbolsParam = sanitizedTickers.joinToString(",")
            val url = "https://query1.finance.yahoo.com/v8/finance/chart/$symbolsParam?interval=1d&range=1d"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; ClosedLoopYieldEngine)")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()?.take(MAX_RESPONSE_CHARS)
                    if (!bodyString.isNullOrBlank()) {
                        val root = JSONObject(bodyString)
                        val chart = root.optJSONObject("chart")
                        val resultArr = chart?.optJSONArray("result")

                        if (resultArr != null && resultArr.length() > 0) {
                            val count = resultArr.length().coerceAtMost(MAX_TICKERS_PER_SWEEP)
                            for (i in 0 until count) {
                                val item = resultArr.getJSONObject(i)
                                val meta = item.optJSONObject("meta")
                                val symbol = meta?.optString("symbol")?.trim()?.uppercase() ?: continue
                                if (!VALID_TICKER_REGEX.matches(symbol)) continue

                                val regularMarketPrice = meta.optDouble("regularMarketPrice", 0.0)
                                val previousClose = meta.optDouble("previousClose", regularMarketPrice)

                                if (regularMarketPrice.isFinite() && regularMarketPrice > 0.0) {
                                    val changePct = if (previousClose.isFinite() && previousClose > 0.0) {
                                        ((regularMarketPrice - previousClose) / previousClose) * 100.0
                                    } else 0.0

                                    results[symbol] = MarketQuote(
                                        ticker = symbol,
                                        currentPrice = regularMarketPrice,
                                        changePercent = if (changePct.isFinite()) changePct else 0.0
                                    )
                                }
                            }
                        }
                    }
                } else if (response.code == 429) {
                    return@withContext PriceRefreshResult.RateLimited(
                        "Financial API rate limit exceeded (HTTP 429). Using cached local baseline prices.",
                        emptyMap()
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching live financial data", e)
        }

        if (results.isNotEmpty()) {
            PriceRefreshResult.Success(results, System.currentTimeMillis())
        } else {
            PriceRefreshResult.Error("Live financial network unavailable. Retaining validated local prices.")
        }
    }
}
