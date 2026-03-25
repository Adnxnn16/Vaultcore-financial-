package com.vaultcore.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable append-only audit log entry.
 * PRD Week 4: Every controller/service method call is recorded here via AuditAspect.
 * DB triggers prevent UPDATE and DELETE on this table.
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String action; // LOGIN, TRANSFER, STOCK_TRADE, MFA_ENABLE, FRAUD_ALERT, SERVICE_EXEC

    @Column(name = "method_name", nullable = false, length = 200)
    private String methodName;

    @Column(columnDefinition = "TEXT")
    private String parameters;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(nullable = false, length = 20)
    private String status; // SUCCESS, FAILED

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AuditLog() {}

    public AuditLog(UUID id, UUID userId, String action, String methodName, String parameters,
                    String result, String ipAddress, String status, String errorMessage, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.methodName = methodName;
        this.parameters = parameters;
        this.result = result;
        this.ipAddress = ipAddress;
        this.status = status;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
    }

    // ── Builder ────────────────────────────────────────────────────────────────

    public static class AuditLogBuilder {
        private UUID id;
        private UUID userId;
        private String action;
        private String methodName;
        private String parameters;
        private String result;
        private String ipAddress;
        private String status;
        private String errorMessage;
        private LocalDateTime createdAt;

        public AuditLogBuilder id(UUID id)                     { this.id = id; return this; }
        public AuditLogBuilder userId(UUID userId)             { this.userId = userId; return this; }
        public AuditLogBuilder action(String action)           { this.action = action; return this; }
        public AuditLogBuilder methodName(String methodName)   { this.methodName = methodName; return this; }
        public AuditLogBuilder parameters(String parameters)   { this.parameters = parameters; return this; }
        public AuditLogBuilder result(String result)           { this.result = result; return this; }
        public AuditLogBuilder ipAddress(String ipAddress)     { this.ipAddress = ipAddress; return this; }
        public AuditLogBuilder status(String status)           { this.status = status; return this; }
        public AuditLogBuilder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public AuditLogBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public AuditLog build() {
            return new AuditLog(id, userId, action, methodName, parameters, result, ipAddress, status, errorMessage, createdAt);
        }
    }

    public static AuditLogBuilder builder() {
        return new AuditLogBuilder();
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public UUID getId()                        { return id; }
    public void setId(UUID id)                 { this.id = id; }

    public UUID getUserId()                    { return userId; }
    public void setUserId(UUID userId)         { this.userId = userId; }

    public String getAction()                  { return action; }
    public void setAction(String action)       { this.action = action; }

    public String getMethodName()              { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }

    public String getParameters()              { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }

    public String getResult()                  { return result; }
    public void setResult(String result)       { this.result = result; }

    public String getIpAddress()               { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getStatus()                  { return status; }
    public void setStatus(String status)       { this.status = status; }

    public String getErrorMessage()            { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCreatedAt()        { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
