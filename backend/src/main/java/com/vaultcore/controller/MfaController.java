package com.vaultcore.controller;

import com.vaultcore.aspect.NoAudit;
import com.vaultcore.dto.ApiResponse;
import com.vaultcore.dto.MfaVerifyRequest;
import com.vaultcore.service.MfaVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/mfa")
public class MfaController {

    private final MfaVerificationService mfaVerificationService;

    public MfaController(MfaVerificationService mfaVerificationService) {
        this.mfaVerificationService = mfaVerificationService;
    }

    /**
     * PRD §7 — POST /auth/mfa/verify. JWT subject must match {@code userId} in body.
     */
    @NoAudit
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verify(
            @Valid @RequestBody MfaVerifyRequest request,
            Authentication authentication
    ) {
        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .error("UNAUTHORIZED")
                            .message("JWT required")
                            .timestamp(LocalDateTime.now())
                            .data(null)
                            .build());
        }
        if (!jwt.getSubject().equals(request.getUserId().toString())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .status(HttpStatus.FORBIDDEN.value())
                            .error("FORBIDDEN")
                            .message("userId does not match authenticated subject")
                            .timestamp(LocalDateTime.now())
                            .data(null)
                            .build());
        }

        mfaVerificationService.verifyOtp(request.getUserId(), request.getOtp());

        return ResponseEntity.ok(ApiResponse.ok("MFA verified; you may retry the transfer",
                Map.of("userId", request.getUserId().toString())));
    }
}
