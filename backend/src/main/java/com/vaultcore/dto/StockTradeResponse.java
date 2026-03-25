package com.vaultcore.dto;

import java.math.BigDecimal;

public class StockTradeResponse {
    private String transactionId;
    private String referenceNumber;
    private String symbol;
    private String type;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private String status;
    private boolean mfaRequired;
    private String message;

    public StockTradeResponse() {}

    public StockTradeResponse(String transactionId, String referenceNumber, String symbol, String type, Integer quantity, BigDecimal price, BigDecimal totalAmount, String status, boolean mfaRequired, String message) {
        this.transactionId = transactionId;
        this.referenceNumber = referenceNumber;
        this.symbol = symbol;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.totalAmount = totalAmount;
        this.status = status;
        this.mfaRequired = mfaRequired;
        this.message = message;
    }

    public static class StockTradeResponseBuilder {
        private String transactionId;
        private String referenceNumber;
        private String symbol;
        private String type;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal totalAmount;
        private String status;
        private boolean mfaRequired;
        private String message;

        public StockTradeResponseBuilder transactionId(String transactionId) { this.transactionId = transactionId; return this; }
        public StockTradeResponseBuilder referenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; return this; }
        public StockTradeResponseBuilder symbol(String symbol) { this.symbol = symbol; return this; }
        public StockTradeResponseBuilder type(String type) { this.type = type; return this; }
        public StockTradeResponseBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public StockTradeResponseBuilder price(BigDecimal price) { this.price = price; return this; }
        public StockTradeResponseBuilder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public StockTradeResponseBuilder status(String status) { this.status = status; return this; }
        public StockTradeResponseBuilder mfaRequired(boolean mfaRequired) { this.mfaRequired = mfaRequired; return this; }
        public StockTradeResponseBuilder message(String message) { this.message = message; return this; }

        public StockTradeResponse build() {
            return new StockTradeResponse(transactionId, referenceNumber, symbol, type, quantity, price, totalAmount, status, mfaRequired, message);
        }
    }

    public static StockTradeResponseBuilder builder() {
        return new StockTradeResponseBuilder();
    }

    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isMfaRequired() { return mfaRequired; }
    public void setMfaRequired(boolean mfaRequired) { this.mfaRequired = mfaRequired; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
