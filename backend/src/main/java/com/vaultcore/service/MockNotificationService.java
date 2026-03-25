package com.vaultcore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MockNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(MockNotificationService.class);

    // For integration testing to intercept the OTP sent to user
    private final ConcurrentHashMap<UUID, String> otpStore = new ConcurrentHashMap<>();

    @Async
    @Override
    public void sendChallenge(UUID userId, String otp) {
        log.warn("[MOCK-2FA] userId={} otp={}", userId, otp);
        otpStore.put(userId, otp);
    }

    public String getOtpForTest(UUID userId) {
        return otpStore.get(userId);
    }
}
