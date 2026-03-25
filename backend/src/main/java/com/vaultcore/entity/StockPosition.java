package com.vaultcore.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_positions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "symbol"})
})
public class StockPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(nullable = false)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "average_cost", nullable = false, precision = 19, scale = 4)
    private BigDecimal averageCost = BigDecimal.ZERO;

    @Column(name = "current_price", precision = 19, scale = 4)
    private BigDecimal currentPrice = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public StockPosition() {}

    public StockPosition(UUID id, User user, String symbol, String companyName, BigDecimal quantity, BigDecimal averageCost, BigDecimal currentPrice, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.symbol = symbol;
        this.companyName = companyName;
        this.quantity = (quantity != null) ? quantity : BigDecimal.ZERO;
        this.averageCost = (averageCost != null) ? averageCost : BigDecimal.ZERO;
        this.currentPrice = (currentPrice != null) ? currentPrice : BigDecimal.ZERO;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static class StockPositionBuilder {
        private UUID id;
        private User user;
        private String symbol;
        private String companyName;
        private BigDecimal quantity = BigDecimal.ZERO;
        private BigDecimal averageCost = BigDecimal.ZERO;
        private BigDecimal currentPrice = BigDecimal.ZERO;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public StockPositionBuilder id(UUID id) { this.id = id; return this; }
        public StockPositionBuilder user(User user) { this.user = user; return this; }
        public StockPositionBuilder symbol(String symbol) { this.symbol = symbol; return this; }
        public StockPositionBuilder companyName(String companyName) { this.companyName = companyName; return this; }
        public StockPositionBuilder quantity(BigDecimal quantity) { this.quantity = quantity; return this; }
        public StockPositionBuilder averageCost(BigDecimal averageCost) { this.averageCost = averageCost; return this; }
        public StockPositionBuilder currentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; return this; }
        public StockPositionBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public StockPositionBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public StockPosition build() {
            return new StockPosition(id, user, symbol, companyName, quantity, averageCost, currentPrice, createdAt, updatedAt);
        }
    }

    public static StockPositionBuilder builder() {
        return new StockPositionBuilder();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getAverageCost() { return averageCost; }
    public void setAverageCost(BigDecimal averageCost) { this.averageCost = averageCost; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
