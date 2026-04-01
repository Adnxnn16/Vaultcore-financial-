package com.vaultcore.exception;

/**
 * Thrown when a transfer requires MFA verification (OTP challenge).
 * The FraudInterceptor stores the pending transfer request + OTP in Redis
 * and halts the transfer execution by throwing this exception.
 */
public class PendingMfaException extends RuntimeException {

    private final String transferReference;

    public PendingMfaException(String message, String transferReference) {
        super(message);
        this.transferReference = transferReference;
    }

    public String getTransferReference() {
        return transferReference;
    }
}

