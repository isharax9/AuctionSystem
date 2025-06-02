package com.auction.session;

import jakarta.ejb.Remote;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Remote
public interface UserSessionManagerRemote {
    // Session creation and validation
    String createUserSession(String username, String sessionId, String ipAddress, String userAgent);
    boolean isSessionValid(String sessionToken);
    boolean isUserLoggedIn(String username);

    // Session management
    void logout(String sessionToken);
    void logoutUser(String username);
    void logoutAllSessions(String username);

    // Session information
    ActiveSessionInfo getSessionInfo(String sessionToken);
    List<ActiveSessionInfo> getActiveSessionsForUser(String username);
    List<ActiveSessionInfo> getAllActiveSessions();

    // Session cleanup
    void cleanupExpiredSessions();
    void invalidateSession(String sessionToken);

    // Security features
    boolean validateSessionSecurity(String sessionToken, String ipAddress, String userAgent);
    void updateSessionActivity(String sessionToken);

    // Session statistics
    int getActiveSessionCount();
    Map<String, Integer> getUserSessionCounts();
}