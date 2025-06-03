package com.auction.filter;

import com.auction.session.UserSessionManagerRemote;
import com.auction.ejb.UserServiceRemote;
import jakarta.ejb.EJB;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

@WebFilter(urlPatterns = {"/auction/admin/*"})
public class AdminFilter implements Filter {

    private static final Logger logger = Logger.getLogger(AdminFilter.class.getName());

    @EJB
    private UserSessionManagerRemote sessionManager;

    @EJB
    private UserServiceRemote userService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession httpSession = httpRequest.getSession(false);
        String sessionToken = null;
        String username = null;

        if (httpSession != null) {
            sessionToken = (String) httpSession.getAttribute("sessionToken");
            username = (String) httpSession.getAttribute("username");
        }

        // Check if user is logged in and session is valid
        if (sessionToken != null && sessionManager.isSessionValid(sessionToken) && username != null) {
            // Check if user is admin
            if (userService.isUserAdmin(username)) {
                // Update session activity
                sessionManager.updateSessionActivity(sessionToken);
                chain.doFilter(request, response);
                return;
            } else {
                logger.warning("Non-admin user attempted to access admin panel: " + username);
                httpResponse.sendRedirect("/AuctionSystem/auction/?error=access_denied");
                return;
            }
        }

        // Not logged in or not admin - redirect to login
        logger.warning("Unauthorized access attempt to admin panel");
        httpResponse.sendRedirect("/AuctionSystem/auction/?error=admin_login_required");
    }
}