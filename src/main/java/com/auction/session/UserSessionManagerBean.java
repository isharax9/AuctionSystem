package com.auction.session;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.*;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
@Startup
@Remote(UserSessionManagerRemote.class)
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Lock(LockType.READ) // Default lock type
public class UserSessionManagerBean implements UserSessionManagerRemote {

    private static final Logger logger = Logger.getLogger(UserSessionManagerBean.class.getName());

    // Thread-safe storage for active sessions
    private static final ConcurrentHashMap<String, ActiveSessionInfo> activeSessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Set<String>> userSessions = new ConcurrentHashMap<>();

    private SecureRandom secureRandom;

    @PostConstruct
    public void init() {
        logger.info("Initializing User Session Manager");
        secureRandom = new SecureRandom();
        logger.info("User Session Manager initialized successfully");
    }

    @PreDestroy
    public void cleanup() {
        logger.info("Shutting down User Session Manager");
        activeSessions.clear();
        userSessions.clear();
        logger.info("User Session Manager shutdown complete");
    }

    @Override
    @Lock(LockType.WRITE)
    public String createUserSession(String username, String sessionId, String ipAddress, String userAgent) {
        logger.info("Creating session for user: " + username);

        // Generate secure session token
        String sessionToken = generateSecureToken();

        // Create session info
        ActiveSessionInfo sessionInfo = new ActiveSessionInfo(sessionToken, username, sessionId, ipAddress, userAgent);

        // Store session
        activeSessions.put(sessionToken, sessionInfo);

        // Track user sessions
        userSessions.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet()).add(sessionToken);

        logger.info("Session created successfully for user: " + username + " with token: " + sessionToken.substring(0, 8) + "...");
        return sessionToken;
    }

    @Override
    public boolean isSessionValid(String sessionToken) {
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return false;
        }

        ActiveSessionInfo session = activeSessions.get(sessionToken);
        if (session == null || !session.isActive()) {
            return false;
        }

        // Check if session is expired
        if (session.isExpired()) {
            logger.info("Session expired for user: " + session.getUsername());
            invalidateSession(sessionToken);
            return false;
        }

        return true;
    }

    @Override
    public boolean isUserLoggedIn(String username) {
        Set<String> sessions = userSessions.get(username);
        if (sessions == null || sessions.isEmpty()) {
            return false;
        }

        // Check if user has any valid sessions
        return sessions.stream().anyMatch(this::isSessionValid);
    }

    @Override
    @Lock(LockType.WRITE)
    public void logout(String sessionToken) {
        logger.info("Logging out session: " + (sessionToken != null ? sessionToken.substring(0, 8) + "..." : "null"));

        ActiveSessionInfo session = activeSessions.get(sessionToken);
        if (session != null) {
            String username = session.getUsername();

            // Remove from active sessions
            activeSessions.remove(sessionToken);

            // Remove from user sessions
            Set<String> sessions = userSessions.get(username);
            if (sessions != null) {
                sessions.remove(sessionToken);
                if (sessions.isEmpty()) {
                    userSessions.remove(username);
                }
            }

            logger.info("User logged out successfully: " + username);
        }
    }

    @Override
    @Lock(LockType.WRITE)
    public void logoutUser(String username) {
        logger.info("Logging out all sessions for user: " + username);

        Set<String> sessions = userSessions.get(username);
        if (sessions != null) {
            // Create a copy to avoid concurrent modification
            Set<String> sessionsCopy = new HashSet<>(sessions);
            sessionsCopy.forEach(this::logout);
        }
    }

    @Override
    @Lock(LockType.WRITE)
    public void logoutAllSessions(String username) {
        logoutUser(username);
    }

    @Override
    public ActiveSessionInfo getSessionInfo(String sessionToken) {
        return activeSessions.get(sessionToken);
    }

    @Override
    public List<ActiveSessionInfo> getActiveSessionsForUser(String username) {
        Set<String> sessions = userSessions.get(username);
        if (sessions == null) {
            return Collections.emptyList();
        }

        return sessions.stream()
                .map(activeSessions::get)
                .filter(Objects::nonNull)
                .filter(session -> session.isActive() && !session.isExpired())
                .collect(Collectors.toList());
    }

    @Override
    public List<ActiveSessionInfo> getAllActiveSessions() {
        return activeSessions.values().stream()
                .filter(session -> session.isActive() && !session.isExpired())
                .collect(Collectors.toList());
    }

    @Override
    @Lock(LockType.WRITE)
    public void cleanupExpiredSessions() {
        logger.info("Starting expired session cleanup");

        List<String> expiredTokens = activeSessions.entrySet().stream()
                .filter(entry -> entry.getValue().isExpired())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        expiredTokens.forEach(this::logout);

        logger.info("Cleaned up " + expiredTokens.size() + " expired sessions");
    }

    @Override
    @Lock(LockType.WRITE)
    public void invalidateSession(String sessionToken) {
        logout(sessionToken);
    }

    @Override
    public boolean validateSessionSecurity(String sessionToken, String ipAddress, String userAgent) {
        ActiveSessionInfo session = activeSessions.get(sessionToken);
        if (session == null) {
            return false;
        }

        // Basic security validation - IP and User Agent should match
        boolean ipMatch = session.getIpAddress().equals(ipAddress);
        boolean userAgentMatch = session.getUserAgent().equals(userAgent);

        if (!ipMatch || !userAgentMatch) {
            logger.warning("Security validation failed for session: " + sessionToken.substring(0, 8) + "...");
            logger.warning("IP match: " + ipMatch + ", User-Agent match: " + userAgentMatch);
            return false;
        }

        return true;
    }

    @Override
    @Lock(LockType.WRITE)
    public void updateSessionActivity(String sessionToken) {
        ActiveSessionInfo session = activeSessions.get(sessionToken);
        if (session != null) {
            session.setLastActivity(LocalDateTime.now());
        }
    }

    @Override
    public int getActiveSessionCount() {
        return (int) activeSessions.values().stream()
                .filter(session -> session.isActive() && !session.isExpired())
                .count();
    }

    @Override
    public Map<String, Integer> getUserSessionCounts() {
        return userSessions.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (int) entry.getValue().stream()
                                .map(activeSessions::get)
                                .filter(Objects::nonNull)
                                .filter(session -> session.isActive() && !session.isExpired())
                                .count()
                ));
    }

    // Scheduled cleanup every 15 minutes
    @Schedule(hour = "*", minute = "*/15", persistent = false)
    public void scheduledCleanup() {
        cleanupExpiredSessions();
    }

    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getEncoder().encodeToString(tokenBytes);
    }
}