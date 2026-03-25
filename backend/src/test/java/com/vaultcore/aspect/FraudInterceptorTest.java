package com.vaultcore.aspect;

import com.vaultcore.dto.TransferRequest;
import com.vaultcore.exception.PendingMfaException;
import com.vaultcore.service.NotificationService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.vaultcore.service.LocalOtpStore;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FraudInterceptorTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private LocalOtpStore localOtpStore;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private FraudInterceptor fraudInterceptor;

    private final UUID userId = UUID.randomUUID();
    private TransferRequest request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fraudInterceptor, "fraudThreshold", new BigDecimal("1000.00"));
        ReflectionTestUtils.setField(fraudInterceptor, "otpTtlSeconds", 300L);

        request = TransferRequest.builder()
                .sourceAccountNumber("SRC123")
                .destinationAccountNumber("DEST456")
                .amount(new BigDecimal("500.00"))
                .build();
    }

    @Test
    void testUnderThreshold_ProceedsNormally() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{request, userId});
        when(joinPoint.proceed()).thenReturn("SUCCESS");

        Object result = fraudInterceptor.interceptTransfer(joinPoint);

        assertEquals("SUCCESS", result);
        verify(joinPoint, times(1)).proceed();
        verifyNoInteractions(localOtpStore);
        verifyNoInteractions(notificationService);
    }

    @Test
    void testOverThreshold_NoMfaVerified_ThrowsException() throws Throwable {
        // Arrange
        request.setAmount(new BigDecimal("1500.00"));
        when(joinPoint.getArgs()).thenReturn(new Object[]{request, userId});
        
        when(localOtpStore.get("mfa_verified:" + userId)).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-otp");

        // Act & Assert
        PendingMfaException ex = assertThrows(PendingMfaException.class, () -> {
            fraudInterceptor.interceptTransfer(joinPoint);
        });

        assertEquals("PENDING_MFA", ex.getTransferReference());

        // Verify OTP was stored
        verify(localOtpStore, times(1)).set(eq("otp:" + userId), eq("hashed-otp"));
        verify(notificationService, times(1)).sendChallenge(eq(userId), anyString());
        verify(joinPoint, never()).proceed(); // Transfer must halt
    }

    @Test
    void testOverThreshold_MfaAlreadyVerified_ProceedsNormally() throws Throwable {
        // Arrange
        request.setAmount(new BigDecimal("1500.00"));
        when(joinPoint.getArgs()).thenReturn(new Object[]{request, userId});
        
        when(localOtpStore.get("mfa_verified:" + userId)).thenReturn("true");

        when(joinPoint.proceed()).thenReturn("SUCCESS");

        // Act
        Object result = fraudInterceptor.interceptTransfer(joinPoint);

        // Assert
        assertEquals("SUCCESS", result);
        verify(localOtpStore, times(1)).delete("mfa_verified:" + userId);
        verify(joinPoint, times(1)).proceed();
        verifyNoInteractions(notificationService); // OTP not dispatched again
    }
}
