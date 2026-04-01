package com.vaultcore.exception;

public class MfaRequiredException extends RuntimeException {
    private final String transactionReference;

    public MfaRequiredException(String message, String transactionReference) {
        super(message);
        this.transactionReference = transactionReference;
    }

    public String getTransactionReference() {
        return transactionReference;
    }
}
