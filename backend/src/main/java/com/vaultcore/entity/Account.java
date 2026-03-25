package com.vaultcore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "account_type", nullable = false, length = 20)
    private String type; // SAVINGS, CHECKING

    @Column(length = 100)
    private String nickname;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Account() {}

    public Account(UUID id, String accountNumber, User user, BigDecimal balance, String type, String nickname, String currency, Boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.user = user;
        this.balance = (balance != null) ? balance : BigDecimal.ZERO;
        this.type = type;
        this.nickname = nickname;
        this.currency = (currency != null) ? currency : "USD";
        this.active = (active != null) ? active : true;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static class AccountBuilder {
        private UUID id;
        private String accountNumber;
        private User user;
        private BigDecimal balance = BigDecimal.ZERO;
        private String type;
        private String nickname;
        private String currency = "USD";
        private Boolean active = true;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public AccountBuilder id(UUID id) { this.id = id; return this; }
        public AccountBuilder accountNumber(String accountNumber) { this.accountNumber = accountNumber; return this; }
        public AccountBuilder user(User user) { this.user = user; return this; }
        public AccountBuilder balance(BigDecimal balance) { this.balance = balance; return this; }
        public AccountBuilder type(String type) { this.type = type; return this; }
        public AccountBuilder nickname(String nickname) { this.nickname = nickname; return this; }
        public AccountBuilder currency(String currency) { this.currency = currency; return this; }
        public AccountBuilder active(Boolean active) { this.active = active; return this; }
        public AccountBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public AccountBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Account build() {
            return new Account(id, accountNumber, user, balance, type, nickname, currency, active, createdAt, updatedAt);
        }
    }

    public static AccountBuilder builder() {
        return new AccountBuilder();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
