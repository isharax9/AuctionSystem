package com.auction.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String email;
    private String passwordHash;  // Store hashed password for security
    private LocalDateTime lastActivity;
    private boolean active;

    public User() {}

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.lastActivity = LocalDateTime.now();
        this.active = true;
        // Default password for sample users
        this.passwordHash = hashPassword("1234");
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.lastActivity = LocalDateTime.now();
        this.active = true;
        this.passwordHash = hashPassword(password);
    }

    // Password hashing method (simple SHA-256 for demo)
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    // Verify password
    public boolean verifyPassword(String password) {
        return passwordHash.equals(hashPassword(password));
    }

    // Update password
    public void setPassword(String password) {
        this.passwordHash = hashPassword(password);
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}