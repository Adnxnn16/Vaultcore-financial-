package com.vaultcore.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Alpha Vantage GLOBAL_QUOTE API response structure.
 *
 * API endpoint:
 *   GET https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol={symbol}&apikey={key}
 *
 * Example response:
 * {
 *   "Global Quote": {
 *     "01. symbol": "AAPL",
 *     "05. price": "255.9200",
 *     "09. change": "0.2900",
 *     "10. change percent": "0.1134%"
 *     ...
 *   }
 * }
 */
public class StockQuote {

    @JsonProperty("Global Quote")
    private GlobalQuote globalQuote;

    // Alpha Vantage may return this on rate-limit or invalid key
    @JsonProperty("Information")
    private String information;

    @JsonProperty("Note")
    private String note;

    public GlobalQuote getGlobalQuote() { return globalQuote; }
    public String getInformation()      { return information; }
    public String getNote()             { return note; }

    public boolean isRateLimited() {
        return (information != null && information.length() > 0)
            || (note != null && note.length() > 0);
    }

    public boolean isValid() {
        return globalQuote != null && globalQuote.getPrice() != null && !globalQuote.getPrice().isBlank();
    }

    // ── Inner: the actual quote fields ────────────────────────────────────────

    public static class GlobalQuote {

        @JsonProperty("01. symbol")
        private String symbol;

        @JsonProperty("02. open")
        private String open;

        @JsonProperty("03. high")
        private String high;

        @JsonProperty("04. low")
        private String low;

        @JsonProperty("05. price")
        private String price;

        @JsonProperty("06. volume")
        private String volume;

        @JsonProperty("07. latest trading day")
        private String latestTradingDay;

        @JsonProperty("08. previous close")
        private String previousClose;

        @JsonProperty("09. change")
        private String change;

        /** Contains trailing "%" that must be stripped before parsing. */
        @JsonProperty("10. change percent")
        private String changePercent;

        public String getSymbol()          { return symbol; }
        public String getOpen()            { return open; }
        public String getHigh()            { return high; }
        public String getLow()             { return low; }
        public String getPrice()           { return price; }
        public String getVolume()          { return volume; }
        public String getLatestTradingDay(){ return latestTradingDay; }
        public String getPreviousClose()   { return previousClose; }
        public String getChange()          { return change; }

        /** Returns the change percent with the "%" stripped (e.g. "0.1134" not "0.1134%"). */
        public String getChangePercentClean() {
            if (changePercent == null) return "0";
            return changePercent.replace("%", "").trim();
        }
    }
}
