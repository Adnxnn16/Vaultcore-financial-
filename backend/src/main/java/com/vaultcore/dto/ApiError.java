package com.vaultcore.dto;

import java.time.LocalDateTime;

public class ApiError {
    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;

    public ApiError() {}

    public ApiError(int status, String error, String message, String path, LocalDateTime timestamp) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp;
    }

    public static class ApiErrorBuilder {
        private int status;
        private String error;
        private String message;
        private String path;
        private LocalDateTime timestamp;

        public ApiErrorBuilder status(int status) { this.status = status; return this; }
        public ApiErrorBuilder error(String error) { this.error = error; return this; }
        public ApiErrorBuilder message(String message) { this.message = message; return this; }
        public ApiErrorBuilder path(String path) { this.path = path; return this; }
        public ApiErrorBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        public ApiError build() {
            return new ApiError(status, error, message, path, timestamp);
        }
    }

    public static ApiErrorBuilder builder() {
        return new ApiErrorBuilder();
    }

    // Getters and Setters
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
