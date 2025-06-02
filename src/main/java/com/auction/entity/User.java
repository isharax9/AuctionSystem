package com.auction.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String email;
    private LocalDateTime lastActivity;
    private boolean active;

    public User() {}

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.lastActivity = LocalDateTime.now();
        this.active = true;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}