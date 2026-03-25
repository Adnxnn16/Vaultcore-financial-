package com.vaultcore.dto;

import java.time.LocalDateTime;

/**
 * Standard API envelope required by PRD v2.0:
 * {status, error, message, timestamp, data}
 */
public class ApiResponse<T> {
    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
    private T data;

    public ApiResponse() {}

    public ApiResponse(int status, String error, String message, LocalDateTime timestamp, T data) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
        this.data = data;
    }

    public static class ApiResponseBuilder<T> {
        private int status;
        private String error;
        private String message;
        private LocalDateTime timestamp;
        private T data;

        public ApiResponseBuilder<T> status(int status) { this.status = status; return this; }
        public ApiResponseBuilder<T> error(String error) { this.error = error; return this; }
        public ApiResponseBuilder<T> message(String message) { this.message = message; return this; }
        public ApiResponseBuilder<T> timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public ApiResponseBuilder<T> data(T data) { this.data = data; return this; }

        public ApiResponse<T> build() {
            return new ApiResponse<>(status, error, message, timestamp, data);
        }
    }

    public static <T> ApiResponseBuilder<T> builder() {
        return new ApiResponseBuilder<>();
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .status(200)
                .error(null)
                .message(message)
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }

    // Getters and Setters
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
