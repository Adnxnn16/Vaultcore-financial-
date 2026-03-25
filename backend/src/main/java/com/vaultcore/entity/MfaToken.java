package com.vaultcore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mfa_tokens")
public class MfaToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private String otp;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private LocalDateTime expiry;

    @Column(nullable = false)
    private boolean used = false;

    public MfaToken() {}

    public MfaToken(UUID id, String token, String otp, UUID userId, LocalDateTime expiry, boolean used) {
        this.id = id;
        this.token = token;
        this.otp = otp;
        this.userId = userId;
        this.expiry = expiry;
        this.used = used;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public LocalDateTime getExpiry() { return expiry; }
    public void setExpiry(LocalDateTime expiry) { this.expiry = expiry; }

    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
}
