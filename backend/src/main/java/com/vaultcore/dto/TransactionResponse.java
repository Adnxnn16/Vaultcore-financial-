package com.vaultcore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {
    private String id;
    private String referenceNumber;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String transactionType;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public TransactionResponse() {}

    public TransactionResponse(String id, String referenceNumber, String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount, String currency, String status, String transactionType, String description, LocalDateTime createdAt, LocalDateTime completedAt) {
        this.id = id;
        this.referenceNumber = referenceNumber;
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.transactionType = transactionType;
        this.description = description;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public static class TransactionResponseBuilder {
        private String id;
        private String referenceNumber;
        private String sourceAccountNumber;
        private String destinationAccountNumber;
        private BigDecimal amount;
        private String currency;
        private String status;
        private String transactionType;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;

        public TransactionResponseBuilder id(String id) { this.id = id; return this; }
        public TransactionResponseBuilder referenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; return this; }
        public TransactionResponseBuilder sourceAccountNumber(String sourceAccountNumber) { this.sourceAccountNumber = sourceAccountNumber; return this; }
        public TransactionResponseBuilder destinationAccountNumber(String destinationAccountNumber) { this.destinationAccountNumber = destinationAccountNumber; return this; }
        public TransactionResponseBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public TransactionResponseBuilder currency(String currency) { this.currency = currency; return this; }
        public TransactionResponseBuilder status(String status) { this.status = status; return this; }
        public TransactionResponseBuilder transactionType(String transactionType) { this.transactionType = transactionType; return this; }
        public TransactionResponseBuilder description(String description) { this.description = description; return this; }
        public TransactionResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public TransactionResponseBuilder completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }

        public TransactionResponse build() {
            return new TransactionResponse(id, referenceNumber, sourceAccountNumber, destinationAccountNumber, amount, currency, status, transactionType, description, createdAt, completedAt);
        }
    }

    public static TransactionResponseBuilder builder() {
        return new TransactionResponseBuilder();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public String getSourceAccountNumber() { return sourceAccountNumber; }
    public void setSourceAccountNumber(String sourceAccountNumber) { this.sourceAccountNumber = sourceAccountNumber; }

    public String getDestinationAccountNumber() { return destinationAccountNumber; }
    public void setDestinationAccountNumber(String destinationAccountNumber) { this.destinationAccountNumber = destinationAccountNumber; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
