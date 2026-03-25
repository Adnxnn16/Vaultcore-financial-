package com.vaultcore.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public class TransferRequest {

    /** Optional when {@link #fromAccountId} is set (PRD: fromAccountId). */
    @Size(max = 20)
    private String sourceAccountNumber;

    /** Optional when {@link #toAccountId} is set (PRD: toAccountId). */
    @Size(max = 20)
    private String destinationAccountNumber;

    private UUID fromAccountId;

    private UUID toAccountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 15, fraction = 4)
    private BigDecimal amount;

    @JsonAlias("note")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private String currency;

    // For MFA-protected high-value transfers
    private String otpCode;

    public TransferRequest() {}

    public TransferRequest(String sourceAccountNumber, String destinationAccountNumber, UUID fromAccountId, UUID toAccountId,
                           BigDecimal amount, String description, String currency, String otpCode) {
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.description = description;
        this.currency = currency;
        this.otpCode = otpCode;
    }

    public static class TransferRequestBuilder {
        private String sourceAccountNumber;
        private String destinationAccountNumber;
        private UUID fromAccountId;
        private UUID toAccountId;
        private BigDecimal amount;
        private String description;
        private String currency;
        private String otpCode;

        public TransferRequestBuilder sourceAccountNumber(String sourceAccountNumber) { this.sourceAccountNumber = sourceAccountNumber; return this; }
        public TransferRequestBuilder destinationAccountNumber(String destinationAccountNumber) { this.destinationAccountNumber = destinationAccountNumber; return this; }
        public TransferRequestBuilder fromAccountId(UUID fromAccountId) { this.fromAccountId = fromAccountId; return this; }
        public TransferRequestBuilder toAccountId(UUID toAccountId) { this.toAccountId = toAccountId; return this; }
        public TransferRequestBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public TransferRequestBuilder description(String description) { this.description = description; return this; }
        public TransferRequestBuilder currency(String currency) { this.currency = currency; return this; }
        public TransferRequestBuilder otpCode(String otpCode) { this.otpCode = otpCode; return this; }

        public TransferRequest build() {
            return new TransferRequest(sourceAccountNumber, destinationAccountNumber, fromAccountId, toAccountId, amount, description, currency, otpCode);
        }
    }

    public static TransferRequestBuilder builder() {
        return new TransferRequestBuilder();
    }

    // Getters and Setters
    public String getSourceAccountNumber() { return sourceAccountNumber; }
    public void setSourceAccountNumber(String sourceAccountNumber) { this.sourceAccountNumber = sourceAccountNumber; }

    public String getDestinationAccountNumber() { return destinationAccountNumber; }
    public void setDestinationAccountNumber(String destinationAccountNumber) { this.destinationAccountNumber = destinationAccountNumber; }

    public UUID getFromAccountId() { return fromAccountId; }
    public void setFromAccountId(UUID fromAccountId) { this.fromAccountId = fromAccountId; }

    public UUID getToAccountId() { return toAccountId; }
    public void setToAccountId(UUID toAccountId) { this.toAccountId = toAccountId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
}
