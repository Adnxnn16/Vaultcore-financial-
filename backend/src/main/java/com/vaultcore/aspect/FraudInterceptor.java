package com.vaultcore.aspect;

import com.vaultcore.dto.TransferRequest;
import com.vaultcore.exception.PendingMfaException;
import com.vaultcore.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class FraudInterceptor {

    private final NotificationService notificationService;
    private final StringRedisTemplate redisTemplate;
    // We'll use a password encoder to store hashed OTPs as requested, but BCrypt might be too slow for high-throughput
    // We'll stick to BCrypt if a bean is available, or just store plaintext in Redis if it's protected.
    // The spec literally says: "BCrypt hash OTP, store in Redis key otp:{userId}"
    private final PasswordEncoder passwordEncoder;

    @Value("${app.fraud.threshold:1000}")
    private BigDecimal fraudThreshold;

    @Value("${app.fraud.otp-ttl-seconds:300}")
    private long otpTtlSeconds;

    @Around("execution(* com.vaultcore.service.TransferService.transfer(..))")
    public Object interceptTransfer(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        
        TransferRequest request = null;
        UUID userId = null;
        
        for (Object arg : args) {
            if (arg instanceof TransferRequest) {
                request = (TransferRequest) arg;
            } else if (arg instanceof UUID) {
                userId = (UUID) arg;
            }
        }
        
        if (request != null && userId != null) {
            BigDecimal amount = request.getAmount();

            // Check if amount exceeds the threshold
            if (amount != null && amount.compareTo(fraudThreshold) > 0) {
                
                String mfaVerifiedKey = "mfa_verified:" + userId;
                String isVerified = redisTemplate.opsForValue().get(mfaVerifiedKey);
                
                if ("true".equalsIgnoreCase(isVerified)) {
                    log.info("[FRAUD-INTERCEPTOR] MFA already verified for userId: {}. Proceeding with large transfer.", userId);
                    // Consume the verified token so it's one-time use
                    redisTemplate.delete(mfaVerifiedKey);
                    return joinPoint.proceed();
                } else {
                    log.warn("[FRAUD-INTERCEPTOR] Large transfer ({} > {}) detected. Triggering MFA for userId: {}", 
                            amount, fraudThreshold, userId);
                            
                    // Generate 6-digit OTP
                    String otp = generateOtp();
                    
                    // BCrypt hash OTP and store
                    String hashedOtp = passwordEncoder.encode(otp);
                    String otpKey = "otp:" + userId;
                    redisTemplate.opsForValue().set(otpKey, hashedOtp, otpTtlSeconds, TimeUnit.SECONDS);
                    
                    // Fire Notification Service async
                    notificationService.sendChallenge(userId, otp);
                    
                    // Abort normal flow and inform client to verify 
                    throw new PendingMfaException("Transfer amount exceeds threshold. MFA challenge dispatched.", "PENDING_MFA");
                }
            }
        }
        
        // Proceed normally if under threshold or no Request/UUID matched
        return joinPoint.proceed();
    }
    
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(900000) + 100000;
        return String.valueOf(num);
    }
}
