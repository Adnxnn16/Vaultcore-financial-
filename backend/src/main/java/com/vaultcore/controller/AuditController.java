package com.vaultcore.controller;

import com.vaultcore.entity.AuditLog;
import com.vaultcore.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit/logs")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * PRD §4.7 — filter: userId, method (service method name), from, to + pagination.
     */
    @GetMapping
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        if (userId == null && (method == null || method.isBlank()) && from == null && to == null) {
            return ResponseEntity.ok(auditService.getAuditLogs(pageable));
        }
        return ResponseEntity.ok(auditService.filterAuditLogs(userId, method, from, to, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLog> getAuditLog(@PathVariable UUID id) {
        return auditService.getAuditLogById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
