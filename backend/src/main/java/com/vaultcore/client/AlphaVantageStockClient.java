package com.vaultcore.client;

import com.vaultcore.dto.StockQuote;
import com.vaultcore.dto.StockQuoteResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Alpha Vantage real-time stock quote client.
 *
 * API: https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol={symbol}&apikey={key}
 *
 * Free tier: 25 requests/day, 5 requests/minute.
 * Results are cached (Redis) for 60 seconds to stay within limits.
 *
 * API Key is configured via:
 *   Environment variable : ALPHAVANTAGE_API_KEY=PTE51U2RQYRR4U24
 *   application.yml      : app.stock.api.key: ${ALPHAVANTAGE_API_KEY:PTE51U2RQYRR4U24}
 */
@Component
public class AlphaVantageStockClient {

    private static final Logger log = LoggerFactory.getLogger(AlphaVantageStockClient.class);
    private static final String AV_BASE = "https://www.alphavantage.co";

    private final RestClient restClient;
    private final String apiKey;

    public AlphaVantageStockClient(
            @Value("${app.stock.api.key:PTE51U2RQYRR4U24}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl(AV_BASE)
                .build();
        log.info("[STOCK-API] Alpha Vantage client initialised. API key configured: {}", !apiKey.isBlank());
    }

    /**
     * Fetch a real-time stock quote for the given symbol.
     *
     * Caches the result for 60 seconds (Redis) to respect Alpha Vantage's
     * 5 req/min free-tier rate limit.
     *
     * Uses Resilience4j circuit breaker — falls back to realistic reference prices
     * if Alpha Vantage is unreachable or rate-limited.
     *
     * @param symbol  e.g. "AAPL", "GOOGL", "TSLA", "NVDA" — any NYSE/NASDAQ ticker
     * @return real-time {@link StockQuoteResponse}, or fallback with source="FALLBACK"
     */
    @CircuitBreaker(name = "stockApiClient", fallbackMethod = "getQuoteFallback")
    @Cacheable(value = "stock", key = "#symbol")
    public StockQuoteResponse getQuote(String symbol) {
        symbol = symbol.toUpperCase();
        log.info("[STOCK-API] Requesting Alpha Vantage quote for: {}", symbol);

        StockQuote response = restClient.get()
                .uri("/query?function=GLOBAL_QUOTE&symbol={symbol}&apikey={key}", symbol, apiKey)
                .retrieve()
                .body(StockQuote.class);

        // Handle rate-limit / API info messages
        if (response == null) {
            log.warn("[STOCK-API] Null response for {}. Using fallback.", symbol);
            return buildFallback(symbol);
        }
        if (response.isRateLimited()) {
            log.warn("[STOCK-API] Rate limited for {}. Message: {}. Using fallback.",
                    symbol, response.getInformation() != null ? response.getInformation() : response.getNote());
            return buildFallback(symbol);
        }
        if (!response.isValid()) {
            log.warn("[STOCK-API] Empty/invalid quote for {} (symbol may not exist on NYSE/NASDAQ). Using fallback.", symbol);
            return buildFallback(symbol);
        }

        StockQuote.GlobalQuote q = response.getGlobalQuote();
        BigDecimal price      = parse(q.getPrice());
        BigDecimal change     = parse(q.getChange());
        BigDecimal changePct  = parse(q.getChangePercentClean());
        BigDecimal high       = parse(q.getHigh());
        BigDecimal low        = parse(q.getLow());
        BigDecimal open       = parse(q.getOpen());
        BigDecimal prevClose  = parse(q.getPreviousClose());

        log.info("[STOCK-API] ✅ Live price for {}: ${} ({}{} %)",
                symbol, price, change.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "", changePct);

        return StockQuoteResponse.builder()
                .symbol(symbol)
                .currentPrice(price)
                .change(change)
                .changePercent(changePct)
                .high(high)
                .low(low)
                .open(open)
                .previousClose(prevClose)
                .marketOpen(true)
                .source("ALPHA_VANTAGE")
                .build();
    }

    // ── Circuit Breaker Fallback ───────────────────────────────────────────────

    public StockQuoteResponse getQuoteFallback(String symbol, Throwable t) {
        log.error("[STOCK-API] Circuit breaker open for {}. Reason: {}. Using fallback.", symbol, t.getMessage());
        return buildFallback(symbol);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /**
     * Reference prices used as fallback when API is unavailable.
     * Based on approximate real-world values.
     */
    private StockQuoteResponse buildFallback(String symbol) {
        double ref = switch (symbol.toUpperCase()) {
            case "AAPL"  -> 255.92;
            case "GOOGL", "GOOG" -> 156.40;
            case "MSFT"  -> 415.20;
            case "AMZN"  -> 185.60;
            case "TSLA"  -> 262.10;
            case "NVDA"  -> 875.50;
            case "META"  -> 505.30;
            case "NFLX"  -> 615.00;
            case "BRK.B" -> 421.00;
            case "JPM"   -> 214.00;
            case "V"     -> 320.00;
            case "WMT"   -> 95.00;
            default      -> 100.00;
        };
        BigDecimal price = bd(ref);
        return StockQuoteResponse.builder()
                .symbol(symbol.toUpperCase())
                .currentPrice(price)
                .change(BigDecimal.ZERO)
                .changePercent(BigDecimal.ZERO)
                .high(price)
                .low(price)
                .open(price)
                .previousClose(price)
                .marketOpen(false)
                .source("FALLBACK")
                .build();
    }

    private BigDecimal parse(String s) {
        if (s == null || s.isBlank()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(s.trim()).setScale(4, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            log.warn("[STOCK-API] Could not parse value: '{}'", s);
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal bd(double v) {
        return BigDecimal.valueOf(v).setScale(4, RoundingMode.HALF_UP);
    }
}
