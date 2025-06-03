package com.auction.servlet;

import com.auction.session.ActiveSessionInfo;
import com.auction.session.UserSessionManagerRemote;
import com.auction.ejb.UserServiceRemote;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@WebServlet(name = "SessionAdminServlet", urlPatterns = {"/auction/admin/sessions/*"})
public class SessionAdminServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(SessionAdminServlet.class.getName());
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @EJB
    private UserSessionManagerRemote sessionManager;

    @EJB
    private UserServiceRemote userService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                showSessionDashboard(request, out);
            } else if (pathInfo.equals("/cleanup")) {
                sessionManager.cleanupExpiredSessions();
                response.sendRedirect("/AuctionSystem/auction/admin/sessions/?message=cleanup_done");
            } else {
                showError(out, "Invalid request");
            }
        } catch (Exception e) {
            logger.severe("Error in SessionAdminServlet: " + e.getMessage());
            showError(out, "Internal server error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        String username = request.getParameter("username");
        String sessionToken = request.getParameter("sessionToken");

        try {
            if ("logoutUser".equals(action) && username != null) {
                sessionManager.logoutUser(username);
                response.sendRedirect("/AuctionSystem/auction/admin/sessions/?message=user_logged_out");
            } else if ("logoutSession".equals(action) && sessionToken != null) {
                sessionManager.logout(sessionToken);
                response.sendRedirect("/AuctionSystem/auction/admin/sessions/?message=session_logged_out");
            } else {
                response.sendRedirect("/AuctionSystem/auction/admin/sessions/?error=invalid_action");
            }
        } catch (Exception e) {
            logger.severe("Error in SessionAdminServlet POST: " + e.getMessage());
            response.sendRedirect("/AuctionSystem/auction/admin/sessions/?error=operation_failed");
        }
    }

    private void showSessionDashboard(HttpServletRequest request, PrintWriter out) {
        String currentUser = getCurrentUser(request);

        out.println("<html><head><title>Admin Session Management Dashboard</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f8f9fa; }");
        out.println("table { border-collapse: collapse; width: 100%; margin: 20px 0; background-color: white; }");
        out.println("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
        out.println("th { background-color: #dc3545; color: white; }");
        out.println(".stats-card { background-color: white; padding: 20px; margin: 10px 0; border-radius: 5px; display: inline-block; min-width: 200px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }");
        out.println(".btn { background-color: #007bff; color: white; padding: 8px 16px; text-decoration: none; border-radius: 4px; border: none; cursor: pointer; margin: 5px; }");
        out.println(".btn-danger { background-color: #dc3545; }");
        out.println(".btn-success { background-color: #28a745; }");
        out.println(".btn:hover { opacity: 0.8; }");
        out.println(".admin-header { background-color: #dc3545; color: white; padding: 20px; margin: -20px -20px 20px -20px; border-radius: 5px; }");
        out.println(".admin-nav { background-color: #343a40; padding: 10px; margin: -20px -20px 20px -20px; }");
        out.println(".admin-nav a { color: white; text-decoration: none; margin-right: 20px; padding: 5px 10px; border-radius: 3px; }");
        out.println(".admin-nav a:hover { background-color: #495057; }");
        out.println("</style></head><body>");

        // Admin Header
        out.println("<div class='admin-header'>");
        out.println("<h1>🔐 Admin Session Management Dashboard</h1>");
        out.println("<p>Welcome, Administrator (" + currentUser + ") | Access Level: ADMIN</p>");
        out.println("</div>");

        // Admin Navigation
        out.println("<div class='admin-nav'>");
        out.println("<a href='/AuctionSystem/auction/admin/sessions/'>Session Dashboard</a>");
        out.println("<a href='/AuctionSystem/auction/'>← Back to Main Site</a>");
        out.println("<a href='/AuctionSystem/auction/logout' class='btn btn-danger' style='float: right;'>Admin Logout</a>");
        out.println("</div>");

        // Show messages if any
        String message = request.getParameter("message");
        String error = request.getParameter("error");

        if (message != null) {
            out.println("<div style='background-color: #d4edda; color: #155724; padding: 10px; margin: 10px 0; border-radius: 5px;'>");
            switch (message) {
                case "cleanup_done":
                    out.println("✅ Expired sessions cleaned up successfully!");
                    break;
                case "user_logged_out":
                    out.println("✅ User logged out successfully!");
                    break;
                case "session_logged_out":
                    out.println("✅ Session terminated successfully!");
                    break;
                default:
                    out.println("✅ " + message);
            }
            out.println("</div>");
        }

        if (error != null) {
            out.println("<div style='background-color: #f8d7da; color: #721c24; padding: 10px; margin: 10px 0; border-radius: 5px;'>");
            out.println("❌ " + error);
            out.println("</div>");
        }

        // Show statistics
        showSessionStatistics(out);

        // Show active sessions
        showActiveSessionsTable(out);

        // Show user session counts
        showUserSessionCounts(out);

        out.println("</body></html>");
    }

    private void showSessionStatistics(PrintWriter out) {
        int activeSessionCount = sessionManager.getActiveSessionCount();

        out.println("<h2>📊 Session Statistics</h2>");
        out.println("<div class='stats-card'>");
        out.println("<h3>Active Sessions</h3>");
        out.println("<h2 style='color: #007bff;'>" + activeSessionCount + "</h2>");
        out.println("</div>");

        out.println("<div style='margin: 20px 0;'>");
        out.println("<a href='/AuctionSystem/auction/admin/sessions/cleanup' class='btn btn-success'>🧹 Cleanup Expired Sessions</a>");
        out.println("</div>");
    }

    private void showActiveSessionsTable(PrintWriter out) {
        List<ActiveSessionInfo> sessions = sessionManager.getAllActiveSessions();

        out.println("<h2>👥 Active Sessions (" + sessions.size() + ")</h2>");

        if (sessions.isEmpty()) {
            out.println("<p>No active sessions found.</p>");
            return;
        }

        out.println("<table>");
        out.println("<tr>");
        out.println("<th>Username</th>");
        out.println("<th>Login Time</th>");
        out.println("<th>Last Activity</th>");
        out.println("<th>Duration (min)</th>");
        out.println("<th>IP Address</th>");
        out.println("<th>Session ID</th>");
        out.println("<th>Actions</th>");
        out.println("</tr>");

        for (ActiveSessionInfo session : sessions) {
            out.println("<tr>");
            out.println("<td><strong>" + session.getUsername() + "</strong></td>");
            out.println("<td>" + session.getLoginTime().format(formatter) + "</td>");
            out.println("<td>" + session.getLastActivity().format(formatter) + "</td>");
            out.println("<td>" + session.getSessionDurationMinutes() + "</td>");
            out.println("<td>" + session.getIpAddress() + "</td>");
            out.println("<td>" + session.getSessionToken().substring(0, 8) + "...</td>");
            out.println("<td>");
            out.println("<form method='post' style='display: inline;'>");
            out.println("<input type='hidden' name='action' value='logoutSession'>");
            out.println("<input type='hidden' name='sessionToken' value='" + session.getSessionToken() + "'>");
            out.println("<button type='submit' class='btn btn-danger'>Force Logout</button>");
            out.println("</form>");
            out.println("</td>");
            out.println("</tr>");
        }

        out.println("</table>");
    }

    private void showUserSessionCounts(PrintWriter out) {
        Map<String, Integer> userCounts = sessionManager.getUserSessionCounts();

        out.println("<h2>👤 Users with Active Sessions</h2>");

        if (userCounts.isEmpty()) {
            out.println("<p>No users with active sessions.</p>");
            return;
        }

        out.println("<table>");
        out.println("<tr>");
        out.println("<th>Username</th>");
        out.println("<th>Session Count</th>");
        out.println("<th>Admin Status</th>");
        out.println("<th>Actions</th>");
        out.println("</tr>");

        for (Map.Entry<String, Integer> entry : userCounts.entrySet()) {
            if (entry.getValue() > 0) {
                boolean isAdmin = userService.isUserAdmin(entry.getKey());
                out.println("<tr>");
                out.println("<td><strong>" + entry.getKey() + "</strong></td>");
                out.println("<td>" + entry.getValue() + "</td>");
                out.println("<td>" + (isAdmin ? "🔑 ADMIN" : "👤 USER") + "</td>");
                out.println("<td>");
                out.println("<form method='post' style='display: inline;'>");
                out.println("<input type='hidden' name='action' value='logoutUser'>");
                out.println("<input type='hidden' name='username' value='" + entry.getKey() + "'>");
                out.println("<button type='submit' class='btn btn-danger'>Logout All Sessions</button>");
                out.println("</form>");
                out.println("</td>");
                out.println("</tr>");
            }
        }

        out.println("</table>");
    }

    private void showError(PrintWriter out, String errorMessage) {
        out.println("<html><head><title>Admin Error</title>");
        out.println("<style>body { font-family: Arial, sans-serif; margin: 20px; background-color: #f8f9fa; }</style>");
        out.println("</head><body>");
        out.println("<div style='background-color: #dc3545; color: white; padding: 20px; border-radius: 5px;'>");
        out.println("<h1>❌ Admin Panel Error</h1>");
        out.println("<p>" + errorMessage + "</p>");
        out.println("<a href='/AuctionSystem/auction/admin/sessions/' style='color: white;'>← Back to Admin Dashboard</a>");
        out.println("</div>");
        out.println("</body></html>");
    }

    private String getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        String sessionToken = (String) session.getAttribute("sessionToken");
        String username = (String) session.getAttribute("username");

        if (sessionToken != null && sessionManager.isSessionValid(sessionToken)) {
            return username;
        }

        return null;
    }
}