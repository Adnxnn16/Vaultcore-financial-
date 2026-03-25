package com.vaultcore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountResponse {
    private String id;
    private String accountNumber;
    private String accountType;
    private BigDecimal balance;
    private String currency;
    private Boolean active;
    private LocalDateTime createdAt;
    private String ownerName;
    private String nickname;

    public AccountResponse() {}

    public AccountResponse(String id, String accountNumber, String accountType, BigDecimal balance, String currency, Boolean active, LocalDateTime createdAt, String ownerName, String nickname) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.currency = currency;
        this.active = active;
        this.createdAt = createdAt;
        this.ownerName = ownerName;
        this.nickname = nickname;
    }

    public static class AccountResponseBuilder {
        private String id;
        private String accountNumber;
        private String accountType;
        private BigDecimal balance;
        private String currency;
        private Boolean active;
        private LocalDateTime createdAt;
        private String ownerName;
        private String nickname;

        public AccountResponseBuilder id(String id) { this.id = id; return this; }
        public AccountResponseBuilder accountNumber(String accountNumber) { this.accountNumber = accountNumber; return this; }
        public AccountResponseBuilder accountType(String accountType) { this.accountType = accountType; return this; }
        public AccountResponseBuilder balance(BigDecimal balance) { this.balance = balance; return this; }
        public AccountResponseBuilder currency(String currency) { this.currency = currency; return this; }
        public AccountResponseBuilder active(Boolean active) { this.active = active; return this; }
        public AccountResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public AccountResponseBuilder ownerName(String ownerName) { this.ownerName = ownerName; return this; }
        public AccountResponseBuilder nickname(String nickname) { this.nickname = nickname; return this; }

        public AccountResponse build() {
            return new AccountResponse(id, accountNumber, accountType, balance, currency, active, createdAt, ownerName, nickname);
        }
    }

    public static AccountResponseBuilder builder() {
        return new AccountResponseBuilder();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}
