package com.vaultcore.service;

import com.vaultcore.exception.OtpVerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.vaultcore.service.LocalOtpStore;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MfaVerificationServiceTest {

    @Mock
    private LocalOtpStore localOtpStore;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MfaVerificationService mfaVerificationService;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mfaVerificationService, "maxAttempts", 3);
        ReflectionTestUtils.setField(mfaVerificationService, "otpTtlSeconds", 300L);
    }

    @Test
    void verifyOtp_success_clearsKeysAndSetsVerified() {
        when(localOtpStore.get("otp:" + userId)).thenReturn("hash");
        when(passwordEncoder.matches("123456", "hash")).thenReturn(true);

        mfaVerificationService.verifyOtp(userId, "123456");

        verify(localOtpStore).delete("otp:" + userId);
        verify(localOtpStore).deleteAttempts("otp_attempts:" + userId);
        verify(localOtpStore).set(eq("mfa_verified:" + userId), eq("true"));
    }

    @Test
    void verifyOtp_wrongIncrementsAttempts() {
        when(localOtpStore.get("otp:" + userId)).thenReturn("hash");
        when(passwordEncoder.matches("000000", "hash")).thenReturn(false);
        when(localOtpStore.increment("otp_attempts:" + userId)).thenReturn(1L);

        OtpVerificationException ex = assertThrows(OtpVerificationException.class,
                () -> mfaVerificationService.verifyOtp(userId, "000000"));
        assertTrue(ex.getMessage().contains("Invalid"));
        assertEquals(2, ex.getRemainingAttempts());
        verify(localOtpStore).increment("otp_attempts:" + userId);
    }

    @Test
    void verifyOtp_maxAttempts_invalidatesOtp() {
        when(localOtpStore.get("otp:" + userId)).thenReturn("hash");
        when(passwordEncoder.matches("000000", "hash")).thenReturn(false);
        when(localOtpStore.increment("otp_attempts:" + userId)).thenReturn(3L);

        OtpVerificationException ex = assertThrows(OtpVerificationException.class,
                () -> mfaVerificationService.verifyOtp(userId, "000000"));
        assertTrue(ex.getMessage().contains("invalidated"));
        assertEquals(0, ex.getRemainingAttempts());
        verify(localOtpStore).delete("otp:" + userId);
        verify(localOtpStore).deleteAttempts("otp_attempts:" + userId);
    }

    @Test
    void verifyOtp_missingPending_throws() {
        when(localOtpStore.get("otp:" + userId)).thenReturn(null);

        OtpVerificationException ex = assertThrows(OtpVerificationException.class,
                () -> mfaVerificationService.verifyOtp(userId, "123456"));
        assertTrue(ex.getMessage().contains("No pending"));
    }
}
