package com.auction.session;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ActiveSessionInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sessionToken;
    private String username;
    private String sessionId;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime loginTime;
    private LocalDateTime lastActivity;
    private boolean isActive;
    private int maxInactiveMinutes;

    public ActiveSessionInfo() {}

    public ActiveSessionInfo(String sessionToken, String username, String sessionId,
                             String ipAddress, String userAgent) {
        this.sessionToken = sessionToken;
        this.username = username;
        this.sessionId = sessionId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.loginTime = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
        this.isActive = true;
        this.maxInactiveMinutes = 30; // Default 30 minutes
    }

    // Check if session is expired
    public boolean isExpired() {
        return lastActivity.plusMinutes(maxInactiveMinutes).isBefore(LocalDateTime.now());
    }

    // Get session duration in minutes
    public long getSessionDurationMinutes() {
        return java.time.Duration.between(loginTime, LocalDateTime.now()).toMinutes();
    }

    // Get inactive duration in minutes
    public long getInactiveDurationMinutes() {
        return java.time.Duration.between(lastActivity, LocalDateTime.now()).toMinutes();
    }

    // Getters and Setters
    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }

    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getMaxInactiveMinutes() { return maxInactiveMinutes; }
    public void setMaxInactiveMinutes(int maxInactiveMinutes) { this.maxInactiveMinutes = maxInactiveMinutes; }
}