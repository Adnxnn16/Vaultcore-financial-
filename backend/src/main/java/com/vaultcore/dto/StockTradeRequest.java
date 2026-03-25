package com.vaultcore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class StockTradeRequest {
    @NotBlank
    private String symbol;
    
    @NotBlank
    private String type; // BUY or SELL
    
    @NotNull
    @Positive
    private Integer quantity;
    
    @NotBlank
    private String sourceAccountNumber; // Depending on type, the funding or destination bank account

    // For MFA-protected high-value trades
    private String otpCode;

    public StockTradeRequest() {}

    public StockTradeRequest(String symbol, String type, Integer quantity, String sourceAccountNumber, String otpCode) {
        this.symbol = symbol;
        this.type = type;
        this.quantity = quantity;
        this.sourceAccountNumber = sourceAccountNumber;
        this.otpCode = otpCode;
    }

    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getSourceAccountNumber() { return sourceAccountNumber; }
    public void setSourceAccountNumber(String sourceAccountNumber) { this.sourceAccountNumber = sourceAccountNumber; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
}
