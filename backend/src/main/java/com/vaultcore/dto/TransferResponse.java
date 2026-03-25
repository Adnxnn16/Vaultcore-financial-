package com.vaultcore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferResponse {
    private String referenceNumber;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String description;
    private LocalDateTime completedAt;
    private boolean mfaRequired;
    private String message;

    public TransferResponse() {}

    public TransferResponse(String referenceNumber, String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount, String currency, String status, String description, LocalDateTime completedAt, boolean mfaRequired, String message) {
        this.referenceNumber = referenceNumber;
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.description = description;
        this.completedAt = completedAt;
        this.mfaRequired = mfaRequired;
        this.message = message;
    }

    public static class TransferResponseBuilder {
        private String referenceNumber;
        private String sourceAccountNumber;
        private String destinationAccountNumber;
        private BigDecimal amount;
        private String currency;
        private String status;
        private String description;
        private LocalDateTime completedAt;
        private boolean mfaRequired;
        private String message;

        public TransferResponseBuilder referenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; return this; }
        public TransferResponseBuilder sourceAccountNumber(String sourceAccountNumber) { this.sourceAccountNumber = sourceAccountNumber; return this; }
        public TransferResponseBuilder destinationAccountNumber(String destinationAccountNumber) { this.destinationAccountNumber = destinationAccountNumber; return this; }
        public TransferResponseBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public TransferResponseBuilder currency(String currency) { this.currency = currency; return this; }
        public TransferResponseBuilder status(String status) { this.status = status; return this; }
        public TransferResponseBuilder description(String description) { this.description = description; return this; }
        public TransferResponseBuilder completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }
        public TransferResponseBuilder mfaRequired(boolean mfaRequired) { this.mfaRequired = mfaRequired; return this; }
        public TransferResponseBuilder message(String message) { this.message = message; return this; }

        public TransferResponse build() {
            return new TransferResponse(referenceNumber, sourceAccountNumber, destinationAccountNumber, amount, currency, status, description, completedAt, mfaRequired, message);
        }
    }

    public static TransferResponseBuilder builder() {
        return new TransferResponseBuilder();
    }

    // Getters and Setters
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

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public boolean isMfaRequired() { return mfaRequired; }
    public void setMfaRequired(boolean mfaRequired) { this.mfaRequired = mfaRequired; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
