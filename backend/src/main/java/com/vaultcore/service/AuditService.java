package com.vaultcore.service;

import com.vaultcore.entity.AuditLog;
import com.vaultcore.repository.UserRepository;
import com.vaultcore.repository.AuditLogRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Page<AuditLog> getAuditLogsByUserId(UUID userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Page<AuditLog> searchAuditLogs(String action, Pageable pageable) {
        return auditLogRepository.findByActionContainingIgnoreCaseOrderByCreatedAtDesc(action, pageable);
    }

    public Page<AuditLog> filterAuditLogs(UUID userId, String method, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (method != null && !method.isBlank()) {
                String like = "%" + method.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.like(cb.lower(root.get("methodName")), like));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return auditLogRepository.findAll(spec, pageable);
    }

    public Optional<AuditLog> getAuditLogById(UUID id) {
        return auditLogRepository.findById(id);
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
                return userRepository.findByKeycloakId(sub).map(u -> u.getId()).orElse(null);
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
