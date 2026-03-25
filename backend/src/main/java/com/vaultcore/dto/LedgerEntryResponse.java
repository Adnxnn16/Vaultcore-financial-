package com.vaultcore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LedgerEntryResponse {

    private String id;
    private String accountId;
    private String transactionId;
    private String entryType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private LocalDateTime createdAt;

    public LedgerEntryResponse() {}

    public LedgerEntryResponse(String id, String accountId, String transactionId, String entryType,
                               BigDecimal amount, BigDecimal balanceAfter, LocalDateTime createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.transactionId = transactionId;
        this.entryType = entryType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getEntryType() { return entryType; }
    public void setEntryType(String entryType) { this.entryType = entryType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
