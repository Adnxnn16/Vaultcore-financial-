package com.vaultcore.exception;

/**
 * Invalid or exhausted MFA OTP verification (PRD §7).
 */
public class OtpVerificationException extends RuntimeException {

    private final int remainingAttempts;

    public OtpVerificationException(String message, int remainingAttempts) {
        super(message);
        this.remainingAttempts = remainingAttempts;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }
}
