package com.vaultcore.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(name = "entry_type", nullable = false, length = 10)
    private String entryType; // DEBIT, CREDIT

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public LedgerEntry() {}

    public LedgerEntry(UUID id, Account account, Transaction transaction, String entryType, BigDecimal amount, BigDecimal balanceAfter, LocalDateTime createdAt) {
        this.id = id;
        this.account = account;
        this.transaction = transaction;
        this.entryType = entryType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.createdAt = createdAt;
    }

    public static class LedgerEntryBuilder {
        private UUID id;
        private Account account;
        private Transaction transaction;
        private String entryType;
        private BigDecimal amount;
        private BigDecimal balanceAfter;
        private LocalDateTime createdAt;

        public LedgerEntryBuilder id(UUID id) { this.id = id; return this; }
        public LedgerEntryBuilder account(Account account) { this.account = account; return this; }
        public LedgerEntryBuilder transaction(Transaction transaction) { this.transaction = transaction; return this; }
        public LedgerEntryBuilder entryType(String entryType) { this.entryType = entryType; return this; }
        public LedgerEntryBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public LedgerEntryBuilder balanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; return this; }
        public LedgerEntryBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public LedgerEntry build() {
            return new LedgerEntry(id, account, transaction, entryType, amount, balanceAfter, createdAt);
        }
    }

    public static LedgerEntryBuilder builder() {
        return new LedgerEntryBuilder();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction transaction) { this.transaction = transaction; }

    public String getEntryType() { return entryType; }
    public void setEntryType(String entryType) { this.entryType = entryType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
