package com.vaultcore.service;

import com.vaultcore.entity.AuditLog;
import com.vaultcore.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Page<AuditLog> getAuditLogsByUserId(UUID userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Page<AuditLog> searchAuditLogs(String action, Pageable pageable) {
        return auditLogRepository.findByActionContainingIgnoreCaseOrderByCreatedAtDesc(action, pageable);
    }

    public void logAction(String action, String methodName, String parameters, String result, String status, String errorMessage) {
        UUID userId = getCurrentUserId();
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action(action)
                .methodName(methodName)
                .parameters(truncate(parameters, 2000))
                .result(truncate(result, 2000))
                .status(status)
                .errorMessage(errorMessage)
                .build();
        auditLogRepository.save(auditLog);
    }

    private UUID getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
                String sub = jwt.getSubject();
                return UUID.fromString(sub);
            }
        } catch (Exception e) {
            log.debug("Could not extract user ID from security context: {}", e.getMessage());
        }
        return null;
    }

    private String truncate(String input, int maxLength) {
        if (input == null) return null;
        return input.length() > maxLength ? input.substring(0, maxLength) + "..." : input;
    }
}
