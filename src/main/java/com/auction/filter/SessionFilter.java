package com.auction.filter;

import com.auction.session.UserSessionManagerRemote;
import jakarta.ejb.EJB;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

@WebFilter(urlPatterns = {"/auction/*"})
public class SessionFilter implements Filter {

    private static final Logger logger = Logger.getLogger(SessionFilter.class.getName());

    @EJB
    private UserSessionManagerRemote sessionManager;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();

        // Skip filter for login, register, and static resources
        if (shouldSkipFilter(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession httpSession = httpRequest.getSession(false);
        String sessionToken = null;

        if (httpSession != null) {
            sessionToken = (String) httpSession.getAttribute("sessionToken");
        }

        // Validate session
        if (sessionToken != null && sessionManager.isSessionValid(sessionToken)) {
            // Update session activity
            sessionManager.updateSessionActivity(sessionToken);

            // Validate session security
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            if (!sessionManager.validateSessionSecurity(sessionToken, ipAddress, userAgent)) {
                logger.warning("Security validation failed for session, invalidating");
                sessionManager.invalidateSession(sessionToken);
                redirectToLogin(httpResponse);
                return;
            }

            chain.doFilter(request, response);
        } else {
            // Session invalid or expired
            if (httpSession != null) {
                httpSession.invalidate();
            }
            redirectToLogin(httpResponse);
        }
    }

    private boolean shouldSkipFilter(String requestURI) {
        return requestURI.endsWith("/login") ||
                requestURI.endsWith("/register") ||
                requestURI.contains("/css/") ||
                requestURI.contains("/js/") ||
                requestURI.contains("/images/") ||
                requestURI.endsWith("/status") ||
                requestURI.equals("/AuctionSystem/auction/");  // Allow main page
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void redirectToLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/AuctionSystem/auction/?error=session_expired");
    }
}