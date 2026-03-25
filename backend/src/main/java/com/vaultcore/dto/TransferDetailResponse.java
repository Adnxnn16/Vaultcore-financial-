package com.vaultcore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TransferDetailResponse {

    private String transactionId;
    private String referenceNumber;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private List<String> ledgerEntryIds;

    public TransferDetailResponse() {}

    public TransferDetailResponse(String transactionId, String referenceNumber, String sourceAccountNumber,
                                  String destinationAccountNumber, BigDecimal amount, String currency, String status,
                                  String description, LocalDateTime createdAt, LocalDateTime completedAt,
                                  List<String> ledgerEntryIds) {
        this.transactionId = transactionId;
        this.referenceNumber = referenceNumber;
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.description = description;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.ledgerEntryIds = ledgerEntryIds;
    }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public List<String> getLedgerEntryIds() { return ledgerEntryIds; }
    public void setLedgerEntryIds(List<String> ledgerEntryIds) { this.ledgerEntryIds = ledgerEntryIds; }
}
