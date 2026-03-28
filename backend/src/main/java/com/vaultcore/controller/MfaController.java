package com.vaultcore.controller;

import com.vaultcore.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/auth")
public class MfaController {

    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;

    public MfaController(StringRedisTemplate redisTemplate, PasswordEncoder passwordEncoder) {
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    public record MfaVerifyRequest(@NotBlank String otp) {}

    @PostMapping("/mfa/verify")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyMfa(@Valid @RequestBody MfaVerifyRequest req) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = jwt.getSubject();
        String otpKey = "otp:" + userId;
        String storedHashedOtp = redisTemplate.opsForValue().get(otpKey);

        if (storedHashedOtp == null || !passwordEncoder.matches(req.otp(), storedHashedOtp)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .status(HttpStatus.FORBIDDEN.value())
                            .error("INVALID_OTP")
                            .message("The provided OTP is invalid or has expired.")
                            .timestamp(LocalDateTime.now())
                            .data(null)
                            .build());
        }

        // Mark MFA as verified
        redisTemplate.delete(otpKey);
        redisTemplate.opsForValue().set("mfa_verified:" + userId, "true", 5, TimeUnit.MINUTES);

        return ResponseEntity.ok(ApiResponse.ok("MFA Verified. You can now proceed with your transfer.", null));
    }
}
