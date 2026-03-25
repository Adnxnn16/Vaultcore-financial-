package com.vaultcore.service;

import com.vaultcore.exception.OtpVerificationException;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MfaVerificationService {

    private final LocalOtpStore localOtpStore;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.fraud.otp-max-attempts:3}")
    private int maxAttempts;

    @Value("${app.fraud.otp-ttl-seconds:300}")
    private long otpTtlSeconds;

    public MfaVerificationService(LocalOtpStore localOtpStore, PasswordEncoder passwordEncoder) {
        this.localOtpStore = localOtpStore;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Validates OTP from Redis, enforces attempt limit, then sets one-time mfa_verified flag for {@link com.vaultcore.aspect.FraudInterceptor}.
     */
    public void verifyOtp(UUID userId, String plainOtp) {
        String otpKey = "otp:" + userId;
        String attemptsKey = "otp_attempts:" + userId;
        String hash = localOtpStore.get(otpKey);

        if (hash == null) {
            throw new OtpVerificationException("No pending OTP for this user", 0);
        }

        if (!passwordEncoder.matches(plainOtp, hash)) {
            Long fails = localOtpStore.increment(attemptsKey);
            int remaining = maxAttempts - (fails != null ? fails.intValue() : 0);
            if (fails != null && fails >= maxAttempts) {
                localOtpStore.delete(otpKey);
                localOtpStore.deleteAttempts(attemptsKey);
                throw new OtpVerificationException("OTP invalidated after too many failed attempts", 0);
            }
            throw new OtpVerificationException("Invalid OTP", Math.max(remaining, 0));
        }

        localOtpStore.delete(otpKey);
        localOtpStore.deleteAttempts(attemptsKey);
        localOtpStore.set("mfa_verified:" + userId, "true");
    }
}
