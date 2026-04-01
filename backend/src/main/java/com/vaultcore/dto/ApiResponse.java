package com.vaultcore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard API envelope required by PRD v2.0:
 * {status, error, message, timestamp, data}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
    private T data;

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .status(200)
                .error(null)
                .message(message)
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }
}

