package com.vaultcore.dto;

import java.math.BigDecimal;

/**
 * Frontend-facing real-time stock quote response.
 */
public class StockQuoteResponse {

    private String symbol;
    private BigDecimal currentPrice;
    private BigDecimal change;
    private BigDecimal changePercent;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal open;
    private BigDecimal previousClose;
    private boolean marketOpen;
    private String source; // "FINNHUB" or "FALLBACK"

    private StockQuoteResponse() {}

    // ── Builder ────────────────────────────────────────────────────────────────

    public static class Builder {
        private final StockQuoteResponse obj = new StockQuoteResponse();

        public Builder symbol(String v)              { obj.symbol = v; return this; }
        public Builder currentPrice(BigDecimal v)    { obj.currentPrice = v; return this; }
        public Builder change(BigDecimal v)          { obj.change = v; return this; }
        public Builder changePercent(BigDecimal v)   { obj.changePercent = v; return this; }
        public Builder high(BigDecimal v)            { obj.high = v; return this; }
        public Builder low(BigDecimal v)             { obj.low = v; return this; }
        public Builder open(BigDecimal v)            { obj.open = v; return this; }
        public Builder previousClose(BigDecimal v)   { obj.previousClose = v; return this; }
        public Builder marketOpen(boolean v)         { obj.marketOpen = v; return this; }
        public Builder source(String v)              { obj.source = v; return this; }
        public StockQuoteResponse build()            { return obj; }
    }

    public static Builder builder() { return new Builder(); }

    // ── Getters ────────────────────────────────────────────────────────────────

    public String getSymbol()             { return symbol; }
    public BigDecimal getCurrentPrice()   { return currentPrice; }
    public BigDecimal getChange()         { return change; }
    public BigDecimal getChangePercent()  { return changePercent; }
    public BigDecimal getHigh()           { return high; }
    public BigDecimal getLow()            { return low; }
    public BigDecimal getOpen()           { return open; }
    public BigDecimal getPreviousClose()  { return previousClose; }
    public boolean isMarketOpen()         { return marketOpen; }
    public String getSource()             { return source; }
}
