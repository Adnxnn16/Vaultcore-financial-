package com.vaultcore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class MfaVerifyRequest {

    @NotNull
    private UUID userId;

    @NotBlank
    @Size(min = 6, max = 6)
    @Pattern(regexp = "\\d{6}", message = "OTP must be 6 digits")
    private String otp;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
