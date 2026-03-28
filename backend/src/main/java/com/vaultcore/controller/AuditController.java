package com.vaultcore.controller;

import com.vaultcore.entity.AuditLog;
import com.vaultcore.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit/logs")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(auditService.getAuditLogs(pageable));
    }

    @GetMapping(params = "userId")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByUser(
            @RequestParam UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(auditService.getAuditLogsByUserId(userId, pageable));
    }

    @GetMapping(params = "method")
    public ResponseEntity<Page<AuditLog>> searchAuditLogs(
            @RequestParam(name = "method") String action,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(auditService.searchAuditLogs(action, pageable));
    }
}
