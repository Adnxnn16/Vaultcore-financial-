package com.vaultcore.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "reference_number", nullable = false, unique = true, length = 50)
    private String referenceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id")
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id")
    private Account destinationAccount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Column(nullable = false, length = 20)
    private String status; // PENDING, COMPLETED, FAILED, REVERSED, MFA_REQUIRED

    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType; // TRANSFER, STOCK_BUY, STOCK_SELL

    @Column(length = 500)
    private String description;

    @Column(name = "mfa_token")
    private String mfaToken;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public Transaction() {}

    public Transaction(UUID id, String referenceNumber, Account sourceAccount, Account destinationAccount, BigDecimal amount, String currency, String status, String transactionType, String description, String mfaToken, LocalDateTime createdAt, LocalDateTime completedAt) {
        this.id = id;
        this.referenceNumber = referenceNumber;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
        this.currency = (currency != null) ? currency : "USD";
        this.status = status;
        this.transactionType = transactionType;
        this.description = description;
        this.mfaToken = mfaToken;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public static class TransactionBuilder {
        private UUID id;
        private String referenceNumber;
        private Account sourceAccount;
        private Account destinationAccount;
        private BigDecimal amount;
        private String currency = "USD";
        private String status;
        private String transactionType;
        private String description;
        private String mfaToken;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;

        public TransactionBuilder id(UUID id) { this.id = id; return this; }
        public TransactionBuilder referenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; return this; }
        public TransactionBuilder sourceAccount(Account sourceAccount) { this.sourceAccount = sourceAccount; return this; }
        public TransactionBuilder destinationAccount(Account destinationAccount) { this.destinationAccount = destinationAccount; return this; }
        public TransactionBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public TransactionBuilder currency(String currency) { this.currency = currency; return this; }
        public TransactionBuilder status(String status) { this.status = status; return this; }
        public TransactionBuilder transactionType(String transactionType) { this.transactionType = transactionType; return this; }
        public TransactionBuilder description(String description) { this.description = description; return this; }
        public TransactionBuilder mfaToken(String mfaToken) { this.mfaToken = mfaToken; return this; }
        public TransactionBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public TransactionBuilder completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }

        public Transaction build() {
            return new Transaction(id, referenceNumber, sourceAccount, destinationAccount, amount, currency, status, transactionType, description, mfaToken, createdAt, completedAt);
        }
    }

    public static TransactionBuilder builder() {
        return new TransactionBuilder();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public Account getSourceAccount() { return sourceAccount; }
    public void setSourceAccount(Account sourceAccount) { this.sourceAccount = sourceAccount; }

    public Account getDestinationAccount() { return destinationAccount; }
    public void setDestinationAccount(Account destinationAccount) { this.destinationAccount = destinationAccount; }

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

    public String getMfaToken() { return mfaToken; }
    public void setMfaToken(String mfaToken) { this.mfaToken = mfaToken; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
