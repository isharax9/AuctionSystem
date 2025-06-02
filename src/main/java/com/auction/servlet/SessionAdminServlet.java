package com.auction.servlet;

import com.auction.session.ActiveSessionInfo;
import com.auction.session.UserSessionManagerRemote;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@WebServlet(name = "SessionAdminServlet", urlPatterns = {"/auction/admin/sessions/*"})
public class SessionAdminServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(SessionAdminServlet.class.getName());
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @EJB
    private UserSessionManagerRemote sessionManager;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                showSessionDashboard(out);
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

    private void showSessionDashboard(PrintWriter out) {
        out.println("<html><head><title>Session Management Dashboard</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; }");
        out.println("table { border-collapse: collapse; width: 100%; margin: 20px 0; }");
        out.println("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
        out.println("th { background-color: #f2f2f2; }");
        out.println(".stats-card { background-color: #f8f9fa; padding: 20px; margin: 10px 0; border-radius: 5px; display: inline-block; min-width: 200px; }");
        out.println(".btn { background-color: #007bff; color: white; padding: 8px 16px; text-decoration: none; border-radius: 4px; border: none; cursor: pointer; }");
        out.println(".btn-danger { background-color: #dc3545; }");
        out.println(".btn:hover { opacity: 0.8; }");
        out.println("</style></head><body>");

        out.println("<h1>🔐 Session Management Dashboard</h1>");
        out.println("<a href='/AuctionSystem/auction/'>← Back to Auctions</a><hr>");

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
        out.println("<h2>" + activeSessionCount + "</h2>");
        out.println("</div>");

        out.println("<div style='margin: 20px 0;'>");
        out.println("<a href='/AuctionSystem/auction/admin/sessions/cleanup' class='btn'>🧹 Cleanup Expired Sessions</a>");
        out.println("</div>");
    }

    private void showActiveSessionsTable(PrintWriter out) {
        List<ActiveSessionInfo> sessions = sessionManager.getAllActiveSessions();

        out.println("<h2>👥 Active Sessions</h2>");

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
        out.println("<th>Actions</th>");
        out.println("</tr>");

        for (ActiveSessionInfo session : sessions) {
            out.println("<tr>");
            out.println("<td>" + session.getUsername() + "</td>");
            out.println("<td>" + session.getLoginTime().format(formatter) + "</td>");
            out.println("<td>" + session.getLastActivity().format(formatter) + "</td>");
            out.println("<td>" + session.getSessionDurationMinutes() + "</td>");
            out.println("<td>" + session.getIpAddress() + "</td>");
            out.println("<td>");
            out.println("<form method='post' style='display: inline;'>");
            out.println("<input type='hidden' name='action' value='logoutSession'>");
            out.println("<input type='hidden' name='sessionToken' value='" + session.getSessionToken() + "'>");
            out.println("<button type='submit' class='btn btn-danger'>Logout</button>");
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
        out.println("<th>Actions</th>");
        out.println("</tr>");

        for (Map.Entry<String, Integer> entry : userCounts.entrySet()) {
            if (entry.getValue() > 0) {
                out.println("<tr>");
                out.println("<td>" + entry.getKey() + "</td>");
                out.println("<td>" + entry.getValue() + "</td>");
                out.println("<td>");
                out.println("<form method='post' style='display: inline;'>");
                out.println("<input type='hidden' name='action' value='logoutUser'>");
                out.println("<input type='hidden' name='username' value='" + entry.getKey() + "'>");
                out.println("<button type='submit' class='btn btn-danger'>Logout All</button>");
                out.println("</form>");
                out.println("</td>");
                out.println("</tr>");
            }
        }

        out.println("</table>");
    }

    private void showError(PrintWriter out, String errorMessage) {
        out.println("<html><head><title>Error</title>");
        out.println("<style>body { font-family: Arial, sans-serif; margin: 20px; }</style>");
        out.println("</head><body>");
        out.println("<h1>❌ Error</h1>");
        out.println("<p>" + errorMessage + "</p>");
        out.println("<a href='/AuctionSystem/auction/admin/sessions/'>← Back to Session Dashboard</a>");
        out.println("</body></html>");
    }
}