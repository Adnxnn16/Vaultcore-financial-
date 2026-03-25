package com.vaultcore.dto;

import java.util.UUID;

public class UserResponse {
    private String id; // Internal UUID
    private String username;
    private String email;
    private String fullName;
    private String role;

    public UserResponse() {}

    public UserResponse(String id, String username, String email, String fullName, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    public static class UserResponseBuilder {
        private String id;
        private String username;
        private String email;
        private String fullName;
        private String role;

        public UserResponseBuilder id(String id) { this.id = id; return this; }
        public UserResponseBuilder username(String username) { this.username = username; return this; }
        public UserResponseBuilder email(String email) { this.email = email; return this; }
        public UserResponseBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public UserResponseBuilder role(String role) { this.role = role; return this; }

        public UserResponse build() {
            return new UserResponse(id, username, email, fullName, role);
        }
    }

    public static UserResponseBuilder builder() {
        return new UserResponseBuilder();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
