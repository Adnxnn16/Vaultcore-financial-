package com.vaultcore.dto;

import java.math.BigDecimal;

public class StockPositionResponse {
    private String symbol;
    private String companyName;
    private Integer quantity;
    private BigDecimal averageCost;
    private BigDecimal currentPrice;
    private BigDecimal marketValue;
    private BigDecimal unrealizedGain;
    private BigDecimal unrealizedGainPercent;

    public StockPositionResponse() {}

    public StockPositionResponse(String symbol, String companyName, Integer quantity, BigDecimal averageCost, BigDecimal currentPrice, BigDecimal marketValue, BigDecimal unrealizedGain, BigDecimal unrealizedGainPercent) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.quantity = quantity;
        this.averageCost = averageCost;
        this.currentPrice = currentPrice;
        this.marketValue = marketValue;
        this.unrealizedGain = unrealizedGain;
        this.unrealizedGainPercent = unrealizedGainPercent;
    }

    public static class StockPositionResponseBuilder {
        private String symbol;
        private String companyName;
        private Integer quantity;
        private BigDecimal averageCost;
        private BigDecimal currentPrice;
        private BigDecimal marketValue;
        private BigDecimal unrealizedGain;
        private BigDecimal unrealizedGainPercent;

        public StockPositionResponseBuilder symbol(String symbol) { this.symbol = symbol; return this; }
        public StockPositionResponseBuilder companyName(String companyName) { this.companyName = companyName; return this; }
        public StockPositionResponseBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public StockPositionResponseBuilder averageCost(BigDecimal averageCost) { this.averageCost = averageCost; return this; }
        public StockPositionResponseBuilder currentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; return this; }
        public StockPositionResponseBuilder marketValue(BigDecimal marketValue) { this.marketValue = marketValue; return this; }
        public StockPositionResponseBuilder unrealizedGain(BigDecimal unrealizedGain) { this.unrealizedGain = unrealizedGain; return this; }
        public StockPositionResponseBuilder unrealizedGainPercent(BigDecimal unrealizedGainPercent) { this.unrealizedGainPercent = unrealizedGainPercent; return this; }

        public StockPositionResponse build() {
            return new StockPositionResponse(symbol, companyName, quantity, averageCost, currentPrice, marketValue, unrealizedGain, unrealizedGainPercent);
        }
    }

    public static StockPositionResponseBuilder builder() {
        return new StockPositionResponseBuilder();
    }

    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getAverageCost() { return averageCost; }
    public void setAverageCost(BigDecimal averageCost) { this.averageCost = averageCost; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public BigDecimal getMarketValue() { return marketValue; }
    public void setMarketValue(BigDecimal marketValue) { this.marketValue = marketValue; }

    public BigDecimal getUnrealizedGain() { return unrealizedGain; }
    public void setUnrealizedGain(BigDecimal unrealizedGain) { this.unrealizedGain = unrealizedGain; }

    public BigDecimal getUnrealizedGainPercent() { return unrealizedGainPercent; }
    public void setUnrealizedGainPercent(BigDecimal unrealizedGainPercent) { this.unrealizedGainPercent = unrealizedGainPercent; }
}
