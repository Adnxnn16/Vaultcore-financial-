package com.vaultcore.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service("customMockNotificationService")
@Profile({"dev", "test"})
@Slf4j
public class MockNotificationService implements NotificationService {

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
