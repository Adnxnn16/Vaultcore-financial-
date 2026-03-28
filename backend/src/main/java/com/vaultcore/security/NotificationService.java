package com.vaultcore.security;

/**
 * Abstraction for sending OTP challenges.
 * PRD: MockNotificationService for dev/test and async @Async notification.
 */
public interface NotificationService {
    void sendChallenge(String userId, String otp, String channel);
}

