package com.vaultcore.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * PRD mock: stores OTPs in-memory for integration tests.
 * Note: logs OTP at WARN in dev/test profile as per PRD.
 */
@Service
@Profile({"dev", "test"})
@Slf4j
public class MockNotificationService implements NotificationService {

    private static final ConcurrentHashMap<String, String> OTP_BY_USER = new ConcurrentHashMap<>();

    @Override
    @Async
    public void sendChallenge(String userId, String otp, String channel) {
        OTP_BY_USER.put(userId, otp);
        log.warn("[MOCK-2FA] userId={} otp={}", userId, otp);
    }

    public static String getOtpForUser(String userId) {
        return OTP_BY_USER.get(userId);
    }
}

