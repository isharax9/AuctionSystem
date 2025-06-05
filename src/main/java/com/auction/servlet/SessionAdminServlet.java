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

        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Admin Session Management Dashboard</title>");
        out.println("<link href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css' rel='stylesheet'>");

        // CSS matching the exact auction system theme
        out.println("<style>");

        // Base styles from auction system
        out.println("* { margin: 0; padding: 0; box-sizing: border-box; }");
        out.println("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f5f5f5; min-height: 100vh; padding: 20px; }");

        // Container matching auction system
        out.println(".container { max-width: 1200px; margin: 0 auto; background-color: white; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); overflow: hidden; }");

        // Navigation bar exactly like auction system
        out.println(".nav-bar { background-color: #343a40; padding: 15px 30px; color: white; text-align: center; }");
        out.println(".nav-bar h1 { font-size: 2rem; margin-bottom: 5px; display: flex; align-items: center; justify-content: center; gap: 15px; }");
        out.println(".nav-bar p { opacity: 0.9; font-size: 1rem; margin-bottom: 15px; }");

        // Admin info bar
        out.println(".admin-info { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px; }");
        out.println(".user-info { color: rgba(255,255,255,0.9); font-weight: 500; }");
        out.println(".nav-links { display: flex; gap: 10px; flex-wrap: wrap; }");

        // Main content grid like auction system
        out.println(".main-content { padding: 30px; display: grid; grid-template-columns: 1fr 1fr; gap: 30px; }");
        out.println(".left-panel, .right-panel { display: flex; flex-direction: column; gap: 20px; }");
        out.println(".full-width { grid-column: 1 / -1; }");

        // Buttons exactly like auction system
        out.println(".btn { background-color: #007bff; color: white; padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; text-decoration: none; display: inline-flex; align-items: center; gap: 8px; justify-content: center; transition: all 0.3s ease; }");
        out.println(".btn:hover { background-color: #0056b3; transform: translateY(-1px); }");
        out.println(".btn:disabled { background-color: #6c757d; cursor: not-allowed; transform: none; }");
        out.println(".btn-success { background-color: #28a745; }");
        out.println(".btn-success:hover { background-color: #218838; }");
        out.println(".btn-danger { background-color: #dc3545; }");
        out.println(".btn-danger:hover { background-color: #c82333; }");
        out.println(".btn-secondary { background-color: #6c757d; }");
        out.println(".btn-secondary:hover { background-color: #5a6268; }");
        out.println(".btn-small { padding: 8px 16px; font-size: 12px; }");

        // Statistics cards like auction system bid stats
        out.println(".stats-container { background-color: #f8f9fa; padding: 20px; border-radius: 8px; border-left: 4px solid #007bff; }");
        out.println(".stats-container h3 { color: #333; margin-bottom: 20px; font-size: 1.3rem; display: flex; align-items: center; gap: 10px; }");
        out.println(".stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; margin-bottom: 20px; }");
        out.println(".stat-card { background-color: white; padding: 15px; border-radius: 8px; text-align: center; border: 1px solid #ddd; }");
        out.println(".stat-label { font-size: 0.9rem; color: #6c757d; margin-bottom: 5px; font-weight: bold; }");
        out.println(".stat-value { font-size: 1.4rem; font-weight: bold; color: #333; }");

        // Table containers like auction system notifications
        out.println(".table-section { background-color: #f8f9fa; border-radius: 8px; border-left: 4px solid #007bff; overflow: hidden; display: flex; flex-direction: column; }");
        out.println(".table-header { background-color: #f8f9fa; padding: 20px; display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #ddd; }");
        out.println(".table-header h3 { color: #333; margin: 0; font-size: 1.3rem; display: flex; align-items: center; gap: 10px; }");
        out.println(".table-content { background-color: #f8f9fa; padding: 20px; max-height: 60vh; min-height: 400px; overflow-y: auto; }");

        // Tables
        out.println("table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        out.println("th, td { padding: 15px; text-align: left; border-bottom: 1px solid #ddd; }");
        out.println("th { background-color: #343a40; color: white; font-weight: 600; }");
        out.println("tr:hover { background-color: #f8f9fa; }");
        out.println("tbody tr:last-child td { border-bottom: none; }");

        // Session ID styling like bid items
        out.println(".session-id { font-family: monospace; background: #e7f3ff; color: #007bff; padding: 4px 8px; border-radius: 4px; font-size: 0.9rem; border: 1px solid #007bff; }");
        out.println(".admin-badge { background: #dc3545; color: white; padding: 4px 8px; border-radius: 4px; font-size: 0.8rem; font-weight: bold; display: inline-flex; align-items: center; gap: 4px; }");
        out.println(".user-badge { background: #007bff; color: white; padding: 4px 8px; border-radius: 4px; font-size: 0.8rem; font-weight: bold; display: inline-flex; align-items: center; gap: 4px; }");

        // Messages like auction system notifications
        out.println(".message { background: white; border-radius: 5px; padding: 15px; margin-bottom: 15px; border-left: 4px solid; animation: slideIn 0.3s ease-out; border: 1px solid #ddd; display: flex; align-items: center; gap: 10px; }");
        out.println(".message-success { border-left-color: #28a745; background-color: #d4edda; color: #155724; }");
        out.println(".message-error { border-left-color: #dc3545; background-color: #f8d7da; color: #721c24; }");
        out.println("@keyframes slideIn { from { opacity: 0; transform: translateX(-20px); } to { opacity: 1; transform: translateX(0); } }");

        // Empty state like auction system
        out.println(".empty-state { text-align: center; padding: 40px 20px; color: #6c757d; }");
        out.println(".empty-state i { font-size: 3rem; margin-bottom: 15px; opacity: 0.5; }");

        // Badge like auction system
        out.println(".badge { background-color: #007bff; color: white; padding: 3px 8px; border-radius: 3px; font-size: 12px; }");

        // Scrollbar styling like auction system
        out.println(".table-content::-webkit-scrollbar { width: 8px; }");
        out.println(".table-content::-webkit-scrollbar-track { background: #f1f1f1; border-radius: 4px; }");
        out.println(".table-content::-webkit-scrollbar-thumb { background: #007bff; border-radius: 4px; }");
        out.println(".table-content::-webkit-scrollbar-thumb:hover { background: #0056b3; }");

        // Responsive design like auction system
        out.println("@media (max-width: 768px) {");
        out.println(".main-content { grid-template-columns: 1fr; }");
        out.println(".nav-bar h1 { font-size: 1.5rem; }");
        out.println(".admin-info { flex-direction: column; }");
        out.println(".stats-grid { grid-template-columns: 1fr; }");
        out.println(".table-content { height: 50vh; min-height: 300px; }");
        out.println("}");

        out.println("</style>");
        out.println("</head>");
        out.println("<body>");

        // Add copyright comment exactly like auction system
        out.println("<!--");
        out.println("    Admin Session Management Dashboard");
        out.println("    Copyright (c) 2025 Ishara Lakshitha. All rights reserved.");
        out.println("    ");
        out.println("    This software and associated documentation files (the \"Software\") are proprietary");
        out.println("    and confidential to Ishara Lakshitha. Unauthorized copying, distribution, or use");
        out.println("    of this Software, via any medium, is strictly prohibited without prior written");
        out.println("    permission from the copyright holder.");
        out.println("    ");
        out.println("    Author: Ishara Lakshitha (@isharax9)");
        out.println("    Project: AuctionSystem - BCD 1 Research Assignment");
        out.println("    Created: June 2025");
        out.println("-->");

        out.println("<div class='container'>");

        // Navigation bar exactly like auction system
        out.println("<div class='nav-bar'>");
        out.println("<h1><i class='fas fa-shield-alt'></i>Admin Session Management Dashboard</h1>");
        out.println("<p>Real-time session monitoring and user management interface</p>");
        out.println("<div class='admin-info'>");
        out.println("<div class='user-info'>");
        out.println("<i class='fas fa-user-shield'></i> Welcome, Administrator (" + currentUser + ") | Access Level: <strong>ADMIN</strong>");
        out.println("</div>");
        out.println("<div class='nav-links'>");
        out.println("<a href='/AuctionSystem/auction/admin/sessions/' class='btn'><i class='fas fa-tachometer-alt'></i>Dashboard</a>");
        out.println("<a href='/AuctionSystem/auction/' class='btn btn-secondary'><i class='fas fa-arrow-left'></i>Back to Auction</a>");
        out.println("<a href='/AuctionSystem/auction/logout' class='btn btn-danger'><i class='fas fa-sign-out-alt'></i>Admin Logout</a>");
        out.println("</div>");
        out.println("</div>");
        out.println("</div>");

        // Show messages if any
        String message = request.getParameter("message");
        String error = request.getParameter("error");

        out.println("<div class='main-content'>");
        out.println("<div class='full-width'>");

        if (message != null) {
            out.println("<div class='message message-success'>");
            out.println("<i class='fas fa-check-circle'></i>");
            switch (message) {
                case "cleanup_done":
                    out.println("Expired sessions cleaned up successfully!");
                    break;
                case "user_logged_out":
                    out.println("User logged out successfully!");
                    break;
                case "session_logged_out":
                    out.println("Session terminated successfully!");
                    break;
                default:
                    out.println(message);
            }
            out.println("</div>");
        }

        if (error != null) {
            out.println("<div class='message message-error'>");
            out.println("<i class='fas fa-exclamation-triangle'></i>");
            out.println("Error: " + error);
            out.println("</div>");
        }

        out.println("</div>"); // End full-width for messages

        // Statistics section
        out.println("<div class='left-panel'>");
        showSessionStatistics(out);
        out.println("</div>");

        out.println("<div class='right-panel'>");
        // Show user session counts
        showUserSessionCounts(out);
        out.println("</div>");

        // Active sessions table (full width)
        out.println("<div class='full-width'>");
        showActiveSessionsTable(out);
        out.println("</div>");

        out.println("</div>"); // End main-content
        out.println("</div>"); // End container

        // Add the footer exactly as requested
        out.println("<!-- Footer Copyright -->");
        out.println("<footer style='text-align: center; padding: 20px; margin-top: 40px; border-top: 1px solid #ddd; background-color: #f8f9fa;'>");
        out.println("<div style='color: #666; font-size: 14px;'>");
        out.println("<p>&copy; 2025 <strong>Ishara Lakshitha</strong>. All rights reserved.</p>");
        out.println("<p style='margin: 5px 0;'>");
        out.println("<i class='fas fa-code'></i>");
        out.println("Developed by <a href='https://github.com/isharax9' target='_blank' style='color: #007bff; text-decoration: none;'>@isharax9</a>");
        out.println("</p>");
        out.println("<p style='margin: 0; font-size: 12px; color: #888;'>");
        out.println("AuctionSystem Admin Dashboard | BCD 1 Research Assignment");
        out.println("</p>");
        out.println("</div>");
        out.println("</footer>");

        out.println("</body></html>");
    }

    private void showSessionStatistics(PrintWriter out) {
        int activeSessionCount = sessionManager.getActiveSessionCount();

        out.println("<div class='stats-container'>");
        out.println("<h3><i class='fas fa-chart-line'></i>Session Statistics</h3>");
        out.println("<div class='stats-grid'>");
        out.println("<div class='stat-card'>");
        out.println("<div class='stat-label'>Active Sessions</div>");
        out.println("<div class='stat-value'>" + activeSessionCount + "</div>");
        out.println("</div>");
        out.println("</div>");
        out.println("<div>");
        out.println("<a href='/AuctionSystem/auction/admin/sessions/cleanup' class='btn btn-success'>");
        out.println("<i class='fas fa-broom'></i> Cleanup Expired Sessions");
        out.println("</a>");
        out.println("</div>");
        out.println("</div>");
    }

    private void showActiveSessionsTable(PrintWriter out) {
        List<ActiveSessionInfo> sessions = sessionManager.getAllActiveSessions();

        out.println("<div class='table-section'>");
        out.println("<div class='table-header'>");
        out.println("<h3><i class='fas fa-users'></i>Active Sessions</h3>");
        out.println("<span class='badge'>" + sessions.size() + " sessions</span>");
        out.println("</div>");

        out.println("<div class='table-content'>");
        if (sessions.isEmpty()) {
            out.println("<div class='empty-state'>");
            out.println("<i class='fas fa-users-slash'></i>");
            out.println("<p>No active sessions found.</p>");
            out.println("</div>");
        } else {
            out.println("<table>");
            out.println("<tr>");
            out.println("<th><i class='fas fa-user'></i> Username</th>");
            out.println("<th><i class='fas fa-clock'></i> Login Time</th>");
            out.println("<th><i class='fas fa-heartbeat'></i> Last Activity</th>");
            out.println("<th><i class='fas fa-stopwatch'></i> Duration (min)</th>");
            out.println("<th><i class='fas fa-map-marker-alt'></i> IP Address</th>");
            out.println("<th><i class='fas fa-key'></i> Session ID</th>");
            out.println("<th><i class='fas fa-cogs'></i> Actions</th>");
            out.println("</tr>");

            for (ActiveSessionInfo session : sessions) {
                out.println("<tr>");
                out.println("<td><strong>" + session.getUsername() + "</strong></td>");
                out.println("<td>" + session.getLoginTime().format(formatter) + "</td>");
                out.println("<td>" + session.getLastActivity().format(formatter) + "</td>");
                out.println("<td>" + session.getSessionDurationMinutes() + "</td>");
                out.println("<td>" + session.getIpAddress() + "</td>");
                out.println("<td><span class='session-id'>" + session.getSessionToken().substring(0, 8) + "...</span></td>");
                out.println("<td>");
                out.println("<form method='post' style='display: inline;'>");
                out.println("<input type='hidden' name='action' value='logoutSession'>");
                out.println("<input type='hidden' name='sessionToken' value='" + session.getSessionToken() + "'>");
                out.println("<button type='submit' class='btn btn-danger btn-small'>");
                out.println("<i class='fas fa-sign-out-alt'></i> Force Logout");
                out.println("</button>");
                out.println("</form>");
                out.println("</td>");
                out.println("</tr>");
            }
            out.println("</table>");
        }
        out.println("</div>");
        out.println("</div>");
    }

    private void showUserSessionCounts(PrintWriter out) {
        Map<String, Integer> userCounts = sessionManager.getUserSessionCounts();

        out.println("<div class='table-section'>");
        out.println("<div class='table-header'>");
        out.println("<h3><i class='fas fa-user-friends'></i>User Sessions</h3>");
        out.println("<span class='badge'>" + userCounts.size() + " users</span>");
        out.println("</div>");

        out.println("<div class='table-content'>");
        if (userCounts.isEmpty()) {
            out.println("<div class='empty-state'>");
            out.println("<i class='fas fa-user-slash'></i>");
            out.println("<p>No users with active sessions.</p>");
            out.println("</div>");
        } else {
            out.println("<table>");
            out.println("<tr>");
            out.println("<th><i class='fas fa-user'></i> Username</th>");
            out.println("<th><i class='fas fa-layer-group'></i> Sessions</th>");
            out.println("<th><i class='fas fa-shield-alt'></i> Status</th>");
            out.println("<th><i class='fas fa-cogs'></i> Actions</th>");
            out.println("</tr>");

            for (Map.Entry<String, Integer> entry : userCounts.entrySet()) {
                if (entry.getValue() > 0) {
                    boolean isAdmin = userService.isUserAdmin(entry.getKey());
                    out.println("<tr>");
                    out.println("<td><strong>" + entry.getKey() + "</strong></td>");
                    out.println("<td>" + entry.getValue() + "</td>");
                    out.println("<td>");
                    if (isAdmin) {
                        out.println("<span class='admin-badge'><i class='fas fa-key'></i> ADMIN</span>");
                    } else {
                        out.println("<span class='user-badge'><i class='fas fa-user'></i> USER</span>");
                    }
                    out.println("</td>");
                    out.println("<td>");
                    out.println("<form method='post' style='display: inline;'>");
                    out.println("<input type='hidden' name='action' value='logoutUser'>");
                    out.println("<input type='hidden' name='username' value='" + entry.getKey() + "'>");
                    out.println("<button type='submit' class='btn btn-danger btn-small'>");
                    out.println("<i class='fas fa-sign-out-alt'></i> Logout All");
                    out.println("</button>");
                    out.println("</form>");
                    out.println("</td>");
                    out.println("</tr>");
                }
            }
            out.println("</table>");
        }
        out.println("</div>");
        out.println("</div>");
    }

    private void showError(PrintWriter out, String errorMessage) {
        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Admin Error</title>");
        out.println("<link href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css' rel='stylesheet'>");
        out.println("<style>");
        out.println("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f5f5f5; min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 20px; }");
        out.println(".error-container { background: white; padding: 40px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); text-align: center; max-width: 500px; width: 100%; }");
        out.println(".error-icon { font-size: 4rem; color: #dc3545; margin-bottom: 20px; }");
        out.println("h1 { color: #343a40; margin-bottom: 15px; }");
        out.println("p { color: #6c757d; margin-bottom: 25px; }");
        out.println(".btn { background-color: #007bff; color: white; padding: 10px 20px; border-radius: 4px; text-decoration: none; display: inline-flex; align-items: center; gap: 8px; transition: all 0.3s ease; }");
        out.println(".btn:hover { background-color: #0056b3; }");
        out.println("</style>");
        out.println("</head><body>");
        out.println("<div class='error-container'>");
        out.println("<i class='fas fa-exclamation-triangle error-icon'></i>");
        out.println("<h1>Admin Panel Error</h1>");
        out.println("<p>" + errorMessage + "</p>");
        out.println("<a href='/AuctionSystem/auction/admin/sessions/' class='btn'>");
        out.println("<i class='fas fa-arrow-left'></i> Back to Admin Dashboard");
        out.println("</a>");
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