package com.vaultcore.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class PortfolioResponse {
    private UUID userId;
    private List<StockPositionResponse> positions;
    private BigDecimal totalMarketValue;
    private BigDecimal totalCost;
    private BigDecimal totalGainLoss;
    private BigDecimal totalGainLossPercent;

    public PortfolioResponse() {}

    public PortfolioResponse(UUID userId, List<StockPositionResponse> positions, BigDecimal totalMarketValue, BigDecimal totalCost, BigDecimal totalGainLoss, BigDecimal totalGainLossPercent) {
        this.userId = userId;
        this.positions = positions;
        this.totalMarketValue = totalMarketValue;
        this.totalCost = totalCost;
        this.totalGainLoss = totalGainLoss;
        this.totalGainLossPercent = totalGainLossPercent;
    }

    public static class PortfolioResponseBuilder {
        private UUID userId;
        private List<StockPositionResponse> positions;
        private BigDecimal totalMarketValue;
        private BigDecimal totalCost;
        private BigDecimal totalGainLoss;
        private BigDecimal totalGainLossPercent;

        public PortfolioResponseBuilder userId(UUID userId) { this.userId = userId; return this; }
        public PortfolioResponseBuilder positions(List<StockPositionResponse> positions) { this.positions = positions; return this; }
        public PortfolioResponseBuilder totalMarketValue(BigDecimal totalMarketValue) { this.totalMarketValue = totalMarketValue; return this; }
        public PortfolioResponseBuilder totalCost(BigDecimal totalCost) { this.totalCost = totalCost; return this; }
        public PortfolioResponseBuilder totalGainLoss(BigDecimal totalGainLoss) { this.totalGainLoss = totalGainLoss; return this; }
        public PortfolioResponseBuilder totalGainLossPercent(BigDecimal totalGainLossPercent) { this.totalGainLossPercent = totalGainLossPercent; return this; }

        public PortfolioResponse build() {
            return new PortfolioResponse(userId, positions, totalMarketValue, totalCost, totalGainLoss, totalGainLossPercent);
        }
    }

    public static PortfolioResponseBuilder builder() {
        return new PortfolioResponseBuilder();
    }

    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public List<StockPositionResponse> getPositions() { return positions; }
    public void setPositions(List<StockPositionResponse> positions) { this.positions = positions; }

    public BigDecimal getTotalMarketValue() { return totalMarketValue; }
    public void setTotalMarketValue(BigDecimal totalMarketValue) { this.totalMarketValue = totalMarketValue; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public BigDecimal getTotalGainLoss() { return totalGainLoss; }
    public void setTotalGainLoss(BigDecimal totalGainLoss) { this.totalGainLoss = totalGainLoss; }

    public BigDecimal getTotalGainLossPercent() { return totalGainLossPercent; }
    public void setTotalGainLossPercent(BigDecimal totalGainLossPercent) { this.totalGainLossPercent = totalGainLossPercent; }
}
