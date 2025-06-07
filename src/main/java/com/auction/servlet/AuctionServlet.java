package com.auction.servlet;

import com.auction.dto.AuctionDTO;
import com.auction.dto.BidDTO;
import com.auction.ejb.*;
import com.auction.entity.Auction;
import com.auction.entity.Bid;
import com.auction.entity.User;
import com.auction.session.UserSessionManagerRemote;
import com.auction.session.ActiveSessionInfo;
import jakarta.ejb.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

/**
 * Enhanced Online Auction System Servlet
 * Merged V1 and V2 functionalities for comprehensive auction management
 *
 * Copyright (c) 2025 Ishara Lakshitha (@isharax9). All rights reserved.
 *
 * Features:
 * - Complete session management with security
 * - Admin panel with role-based access
 * - User profile and password management
 * - Enhanced auction creation with hour/minute precision
 * - Auction history (completed auctions) display
 * - Real-time WebSocket integration
 * - Comprehensive error handling and validation
 * - Enhanced UI with responsive design
 */
@WebServlet(name = "AuctionServlet", urlPatterns = {"/auction/*"})
public class AuctionServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(AuctionServlet.class.getName());
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @EJB
    private AuctionServiceRemote auctionService;

    @EJB
    private BidServiceRemote bidService;

    @EJB
    private UserServiceRemote userService;

    @EJB
    private AuctionManagerSingleton auctionManager;

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
                showMainAuctionPage(request, out);
            } else if (pathInfo.startsWith("/auction/")) {
                String auctionIdStr = pathInfo.substring("/auction/".length());
                try {
                    Long auctionId = Long.parseLong(auctionIdStr);
                    showAuctionDetails(request, out, auctionId);
                } catch (NumberFormatException e) {
                    showError(out, "Invalid auction ID", "Please provide a valid auction ID.");
                }
            } else if (pathInfo.startsWith("/view/")) {
                String auctionIdStr = pathInfo.substring(6);
                showAuctionDetails(request, out, Long.parseLong(auctionIdStr));
            } else if (pathInfo.equals("/users")) {
                showUserList(out);
            } else if (pathInfo.equals("/status")) {
                showSystemStatus(out);
            } else if (pathInfo.equals("/sessions")) {
                showSessionStatus(request, out);
            } else if (pathInfo.equals("/profile")) {
                showUserProfile(request, out);
            } else if (pathInfo.equals("/change-password")) {
                showChangePasswordForm(request, out);
            } else if (pathInfo.equals("/logout")) {
                handleUserLogout(request, response);
                return;
            } else {
                showError(out, "Page not found", "The requested page does not exist.");
            }
        } catch (Exception e) {
            logger.severe("Error processing request: " + e.getMessage());
            showError(out, "Internal server error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.equals("/bid")) {
                handleBidSubmission(request, response);
            } else if (pathInfo != null && pathInfo.equals("/login")) {
                handleUserLogin(request, response);
            } else if (pathInfo != null && pathInfo.equals("/logout")) {
                handleUserLogout(request, response);
            } else if (pathInfo != null && pathInfo.equals("/register")) {
                handleUserRegistration(request, response);
            } else if (pathInfo != null && pathInfo.equals("/create")) {
                handleAuctionCreation(request, response);
            } else if (pathInfo != null && pathInfo.equals("/change-password")) {
                handlePasswordChange(request, response, out);
            } else {
                showError(out, "Invalid POST request", "The requested action is not valid.");
            }
        } catch (Exception e) {
            logger.severe("Error processing POST request: " + e.getMessage());
            response.sendRedirect("/AuctionSystem/auction/?error=system_error");
        }
    }

    /**
     * Enhanced main auction page with integrated V1 and V2 features
     */
    private void showMainAuctionPage(HttpServletRequest request, PrintWriter out) {
        String currentUser = getCurrentUser(request);
        boolean isLoggedIn = currentUser != null;
        boolean isAdmin = isLoggedIn && userService.isUserAdmin(currentUser);

        // Get active and completed auctions
        List<AuctionDTO> activeAuctions = auctionService.getAllActiveAuctions();
        List<AuctionDTO> completedAuctions = auctionService.getAllCompletedAuctions();

        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Online Auction System</title>");
        out.println("<link href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css' rel='stylesheet'>");

        // Add enhanced CSS for new components
        addEnhancedCSS(out);

        out.println("</head>");
        out.println("<body>");

        // Add copyright comment
        addCopyrightComment(out);

        out.println("<div class='container'>");

        // Header with enhanced navigation
        out.println("<div class='nav-bar'>");
        out.println("<span style='font-size:28px;'><strong>🏺 Nawwa Online Auction</strong></span>");
        out.println("<p>An online auction system for selling items with bidding capabilities.</p>");
        out.println("</div>");

        // Show messages if any
        showMessages(request, out);

        // Show user session info if logged in (V1 feature)
        if (currentUser != null) {
            showUserInfoBar(request, out, currentUser);
        }

        // Enhanced Navigation (V1 + V2 combined)
        showEnhancedNavigation(out, currentUser, isLoggedIn, isAdmin);

        // System Status Summary (V1 feature)
        showSystemStatusSummary(out, activeAuctions.size(), completedAuctions.size());

        out.println("<div class='main-content'>");

        if (!isLoggedIn) {
            out.println("<div class='left-panel'>");
            showLoginForm(out);
            out.println("</div>");
            out.println("<div class='right-panel'>");
            showRegistrationForm(out);
            out.println("</div>");
        } else {
            out.println("<div class='left-panel'>");
            showUserInfo(out, currentUser, isAdmin);
            showEnhancedAuctionCreationForm(out); // V2 enhanced form
            out.println("</div>");
            out.println("<div class='right-panel'>");
            showSystemStatistics(out, activeAuctions.size(), completedAuctions.size());
            out.println("</div>");
        }

        out.println("</div>"); // End main-content

        // Enhanced: Active auctions section (V2 with V1 features)
        showActiveAuctionsSection(out, activeAuctions, isLoggedIn);

        // NEW: Completed auctions history section (V2 feature)
        showCompletedAuctionsSection(out, completedAuctions, isLoggedIn);

        out.println("</div>"); // End container

        // Add footer
        addFooter(out);

        out.println("</body></html>");
    }

    /**
     * Enhanced navigation combining V1 comprehensive features with V2 styling
     */
    private void showEnhancedNavigation(PrintWriter out, String currentUser, boolean isLoggedIn, boolean isAdmin) {
        // The .nav-bar class is a flex container with "justify-content: space-between".
        // We create two inner divs to group links to the left and right for proper alignment.
        out.println("<nav class='nav-bar'>");

        // --- Left-side navigation links ---
        out.println("  <div class='nav-actions'>");
        // Replaced emoji with a Font Awesome icon for consistent styling.
        out.println("    <a href='/AuctionSystem/auction/' class='nav-link'><i class='fa-solid fa-house'></i> Home</a>");
        out.println("    <a href='/AuctionSystem/auction/users' class='nav-link'><i class='fa-solid fa-users'></i> Users</a>");
        out.println("    <a href='/AuctionSystem/auction/status' class='nav-link'><i class='fa-solid fa-chart-line'></i> System Status</a>");
        out.println("    <a href='/AuctionSystem/auction/sessions' class='nav-link'><i class='fa-solid fa-lock'></i> Sessions</a>");


        out.println("    <a href='/AuctionSystem/real-time-notifications.html' class='nav-link' target='_blank'><i class='fa-solid fa-bell'></i> Live Updates</a>");


        // --- Right-side user-specific links ---
        if (isLoggedIn) {

            out.println("    <a href='/AuctionSystem/auction/profile' class='nav-link'><i class='fa-solid fa-user-circle'></i> Profile</a>");
            if (isAdmin) {
                // The styled .admin-link is now grouped on the right.
                out.println("    <a href='/AuctionSystem/auction/admin/sessions/' class='nav-link admin-link'><i class='fa-solid fa-screwdriver-wrench'></i> Admin Panel</a>");
            }
            out.println("  </div>");
        }

        out.println("</nav>");
    }

    /**
     * V1 System Status Summary with V2 styling
     */
    private void showSystemStatusSummary(PrintWriter out, int activeCount, int completedCount) {
        int activeUsers = userService.getActiveUserCount();
        int activeSessions = sessionManager.getActiveSessionCount();
        double totalBidVolume = auctionManager.getTotalBidVolume();

        out.println("<div style='background-color: #f8f9fa; border-left: 4px solid #007bff; padding: 15px; margin: 15px 0; border-radius: 4px; font-size: 14px; color: #495057;'>");
        out.println("<div style='display: flex; align-items: center; margin-bottom: 5px;'>");
        out.println("<span style='margin-right: 8px; font-size: 16px;'>📈</span>");
        out.println("<strong style='color: #333;'>System Status:</strong>");
        out.println("<span style='margin-left: 8px;'>" + activeCount + " active auctions • " + completedCount + " completed • " + activeUsers + " users • " + activeSessions + " sessions • $" + String.format("%.2f", totalBidVolume) + " total volume</span>");
        out.println("</div>");
        out.println("<div style='font-size: 12px; color: #6c757d; margin-top: 5px;'>");
        out.println("Last updated: " + LocalDateTime.now().format(formatter) + " UTC");
        out.println("</div>");
        out.println("</div>");
    }

    /**
     * V1 User Info Bar with enhanced styling
     */
    private void showUserInfoBar(HttpServletRequest request, PrintWriter out, String username) {
        HttpSession session = request.getSession(false);
        String sessionToken = session != null ? (String) session.getAttribute("sessionToken") : null;

        // FIX: Replaced 'user-info' with 'message message-success' for the styled green bar.
        out.println("<div class='message message-success'>");

        // FIX: Replaced emoji with a Font Awesome icon for consistency.
        out.println("<strong><i class='fa-solid fa-user'></i> Welcome, " + escapeHtml(username) + "!</strong> ");

        // FIX: Replaced 'admin-badge' with the standard 'badge' class.
        if (userService.isUserAdmin(username)) {
            out.println("<span class='badge' style='background-color: #e74c3c;'>ADMIN</span> ");
        }

        if (sessionToken != null) {
            ActiveSessionInfo sessionInfo = sessionManager.getSessionInfo(sessionToken);
            if (sessionInfo != null) {
                out.println("| Session: " + sessionInfo.getSessionDurationMinutes() + " min ");
                out.println("| Inactive: " + sessionInfo.getInactiveDurationMinutes() + " min ago ");
            }
        }

        // FIX: Corrected the broken HTML href and CSS classes. Replaced emoji with icon.
        out.println("| <a href='/AuctionSystem/auction/change-password' class='btn btn-small btn-secondary'>");
        out.println("<i class='fa-solid fa-key'></i> Change Password");
        out.println("</a> |");

        // FIX: Used a valid button style. An inline style is used for the red color.
        out.println("<a href='/AuctionSystem/auction/logout' class='btn btn-small' style='background-color: #c0392b; color: white;'>");
        out.println("<i class='fas fa-sign-out-alt'></i> Logout");
        out.println("</a>");

        // FIX: Removed the stray, unopened </form> tag.
        out.println("</div>");
    }

    /**
     * V2 Enhanced auction creation form with hours/minutes precision
     */
    private void showEnhancedAuctionCreationForm(PrintWriter out) {
        out.println("<div class='form-container'>");
        out.println("<h3><i class='fas fa-plus-circle'></i> Create New Auction</h3>");
        out.println("<form method='post' action='/AuctionSystem/auction/create' class='auction-form'>");

        // Title field
        out.println("<div class='form-group'>");
        out.println("<label for='title'><i class='fas fa-tag'></i> Auction Title:</label>");
        out.println("<input type='text' id='title' name='title' required maxlength='100' placeholder='Enter auction title'>");
        out.println("</div>");

        // Description field
        out.println("<div class='form-group'>");
        out.println("<label for='description'><i class='fas fa-align-left'></i> Description:</label>");
        out.println("<textarea id='description' name='description' required maxlength='500' placeholder='Describe your item...'></textarea>");
        out.println("</div>");

        // Starting price field
        out.println("<div class='form-group'>");
        out.println("<label for='startingPrice'><i class='fas fa-dollar-sign'></i> Starting Price ($):</label>");
        out.println("<input type='number' id='startingPrice' name='startingPrice' required min='0.01' step='0.01' placeholder='0.00'>");
        out.println("</div>");

        // Enhanced: Duration selector with hours and minutes
        out.println("<div class='form-group'>");
        out.println("<label><i class='fas fa-clock'></i> Auction Duration:</label>");
        out.println("<div class='duration-selector'>");

        // Hours selector
        out.println("<div class='time-input-group'>");
        out.println("<label for='durationHours'>Hours:</label>");
        out.println("<select id='durationHours' name='durationHours' required>");
        for (int i = 0; i <= 168; i++) { // 0 to 168 hours (7 days)
            String selected = (i == 1) ? " selected" : "";
            out.println("<option value='" + i + "'" + selected + ">" + i + "</option>");
        }
        out.println("</select>");
        out.println("</div>");

        // Minutes selector
        out.println("<div class='time-input-group'>");
        out.println("<label for='durationMinutes'>Minutes:</label>");
        out.println("<select id='durationMinutes' name='durationMinutes' required>");
        for (int min = 0; min <= 60; min++) { // 0 to 60 min (1 hour)
            String selected = (min == 1) ? " selected" : "";
            out.println("<option value='" + min + "'" + selected + ">" + min + "</option>");
        }
        out.println("</select>");
        out.println("</div>");

        out.println("</div>"); // End duration-selector

        // Duration help text
        out.println("<div class='duration-help'>");
        out.println("<small><i class='fas fa-info-circle'></i> ");
        out.println("Minimum: 1 minute | Maximum: 7 days (168 hours)");
        out.println("</small>");
        out.println("</div>");

        out.println("</div>"); // End form-group

        // Submit button
        out.println("<div class='form-group'>");
        out.println("<button type='submit' class='btn btn-success btn-create'>");
        out.println("<i class='fas fa-gavel'></i> Create Auction");
        out.println("</button>");
        out.println("</div>");

        out.println("</form>");
        out.println("</div>");
    }

    /**
     * V2 Enhanced active auctions section
     */
    private void showActiveAuctionsSection(PrintWriter out, List<AuctionDTO> activeAuctions, boolean isLoggedIn) {
        out.println("<div class='auction-section'>");
        out.println("<div class='section-header'>");
        out.println("<h2><i class='fas fa-fire'></i> Active Auctions</h2>");
        out.println("<span class='badge'>" + activeAuctions.size() + " active</span>");
        out.println("</div>");

        if (activeAuctions.isEmpty()) {
            out.println("<div class='empty-state'>");
            out.println("<i class='fas fa-gavel'></i>");
            out.println("<p>No active auctions at the moment.</p>");
            if (isLoggedIn) {
                out.println("<p>Be the first to create one!</p>");
            }
            out.println("</div>");
        } else {
            out.println("<div class='table-container'>");
            out.println("<table class='auction-table'>");
            out.println("<thead>");
            out.println("<tr>");
            out.println("<th><i class='fas fa-hashtag'></i> ID</th>");
            out.println("<th><i class='fas fa-tag'></i> Title</th>");
            out.println("<th><i class='fas fa-align-left'></i> Description</th>");
            out.println("<th><i class='fas fa-dollar-sign'></i> Current Bid</th>");
            out.println("<th><i class='fas fa-user'></i> Leading Bidder</th>");
            out.println("<th><i class='fas fa-clock'></i> Ends At</th>");
            out.println("<th><i class='fas fa-chart-bar'></i> Bids</th>");
            out.println("<th><i class='fas fa-flag'></i> Status</th>");
            out.println("<th><i class='fas fa-cogs'></i> Actions</th>");
            out.println("</tr>");
            out.println("</thead>");
            out.println("<tbody>");

            for (AuctionDTO auction : activeAuctions) {
                boolean isExpired = auction.getEndTime().isBefore(LocalDateTime.now());
                out.println("<tr class='auction-row'" + (isExpired ? " style='background-color: #fff3cd;'" : "") + ">");
                out.println("<td><span class='auction-id'>#" + auction.getAuctionId() + "</span></td>");
                out.println("<td><strong>" + escapeHtml(auction.getTitle()) + "</strong></td>");
                out.println("<td>" + (auction.getDescription().length() > 50 ?
                        escapeHtml(auction.getDescription().substring(0, 50)) + "..." :
                        escapeHtml(auction.getDescription())) + "</td>");
                out.println("<td class='price'>$" + String.format("%.2f", auction.getCurrentHighestBid()) + "</td>");
                out.println("<td class='bidder'>" +
                        (auction.getCurrentHighestBidder() != null ?
                                "🏆 " + escapeHtml(auction.getCurrentHighestBidder()) :
                                "<em>No bids yet</em>") + "</td>");
                out.println("<td class='end-time'>" + auction.getEndTime().format(formatter) + "</td>");
                out.println("<td class='bid-count'>" + auction.getBidCount() + "</td>");
                out.println("<td class='status'>" + (auction.isActive() && !isExpired ? "🟢 Active" : "🔴 Ended") + "</td>");
                out.println("<td class='actions'>");
                out.println("<a href='/AuctionSystem/auction/view/" + auction.getAuctionId() +
                        "' class='btn btn-small'>");
                out.println("<i class='fas fa-eye'></i> View");
                out.println("</a>");
                out.println("</td>");
                out.println("</tr>");
            }

            out.println("</tbody>");
            out.println("</table>");
            out.println("</div>"); // End table-container
        }

        out.println("</div>"); // End auction-section
    }

    /**
     * V2 NEW: Completed auctions history section
     */
    private void showCompletedAuctionsSection(PrintWriter out, List<AuctionDTO> completedAuctions, boolean isLoggedIn) {
        out.println("<div class='auction-section history-section'>");
        out.println("<div class='section-header'>");
        out.println("<h2><i class='fas fa-history'></i> Auction History (Completed)</h2>");
        out.println("<span class='badge badge-secondary'>" + completedAuctions.size() + " completed</span>");
        out.println("</div>");

        if (completedAuctions.isEmpty()) {
            out.println("<div class='empty-state'>");
            out.println("<i class='fas fa-archive'></i>");
            out.println("<p>No completed auctions yet.</p>");
            out.println("<p>Completed auctions will appear here with winner information.</p>");
            out.println("</div>");
        } else {
            out.println("<div class='table-container'>");
            out.println("<table class='auction-table history-table'>");
            out.println("<thead>");
            out.println("<tr>");
            out.println("<th><i class='fas fa-hashtag'></i> ID</th>");
            out.println("<th><i class='fas fa-tag'></i> Title</th>");
            out.println("<th><i class='fas fa-trophy'></i> Final Price</th>");
            out.println("<th><i class='fas fa-crown'></i> Winner</th>");
            out.println("<th><i class='fas fa-calendar-check'></i> Completed</th>");
            out.println("<th><i class='fas fa-chart-bar'></i> Total Bids</th>");
            out.println("<th><i class='fas fa-flag'></i> Status</th>");
            out.println("<th><i class='fas fa-cogs'></i> Actions</th>");
            out.println("</tr>");
            out.println("</thead>");
            out.println("<tbody>");

            for (AuctionDTO auction : completedAuctions) {
                out.println("<tr class='auction-row history-row'>");
                out.println("<td><span class='auction-id'>#" + auction.getAuctionId() + "</span></td>");
                out.println("<td><strong>" + escapeHtml(auction.getTitle()) + "</strong></td>");

                // Final price with winner highlighting
                if (auction.hasWinner()) {
                    out.println("<td class='price winner-price'>$" +
                            String.format("%.2f", auction.getWinningBid()) + "</td>");
                } else {
                    out.println("<td class='price no-winner'>$" +
                            String.format("%.2f", auction.getStartingPrice()) + "</td>");
                }

                // Winner information
                if (auction.hasWinner()) {
                    out.println("<td class='winner'>");
                    out.println("<span class='winner-badge'>");
                    out.println("<i class='fas fa-crown'></i> " + escapeHtml(auction.getWinnerUsername()));
                    out.println("</span>");
                    out.println("</td>");
                } else {
                    out.println("<td class='no-winner'><em>No winner</em></td>");
                }

                // Completion time
                LocalDateTime completedTime = auction.getCompletedTime() != null ?
                        auction.getCompletedTime() : auction.getEndTime();
                out.println("<td class='end-time'>" + completedTime.format(formatter) + "</td>");

                // Total bids
                out.println("<td class='bid-count'>" + auction.getBidCount() + "</td>");

                // Status
                out.println("<td class='status'>" + auction.getDisplayStatus() + "</td>");

                // Actions
                out.println("<td class='actions'>");
                out.println("<a href='/AuctionSystem/auction/view/" + auction.getAuctionId() +
                        "' class='btn btn-small btn-secondary'>");
                out.println("<i class='fas fa-eye'></i> View Details");
                out.println("</a>");
                out.println("</td>");
                out.println("</tr>");
            }

            out.println("</tbody>");
            out.println("</table>");
            out.println("</div>"); // End table-container
        }

        out.println("</div>"); // End auction-section
    }

    /**
     * Enhanced auction details view combining V1 and V2 features
     */
    private void showAuctionDetails(HttpServletRequest request, PrintWriter out, Long auctionId) {
        String currentUser = getCurrentUser(request);
        boolean isLoggedIn = currentUser != null;

        AuctionDTO auction = auctionService.getAuction(auctionId);
        if (auction == null) {
            showError(out, "Auction Not Found", "The requested auction does not exist.");
            return;
        }

        List<Bid> bids = bidService.getBidsForAuction(auctionId);

        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Auction Details - " + escapeHtml(auction.getTitle()) + "</title>");
        out.println("<link href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css' rel='stylesheet'>");

        // Add enhanced CSS
        addEnhancedCSS(out);

        out.println("</head>");
        out.println("<body>");

        // Add copyright comment
        addCopyrightComment(out);

        out.println("<div class='container'>");

        out.println("<h1>🏺 " + escapeHtml(auction.getTitle()) + "</h1>");
        out.println("<a href='/AuctionSystem/auction/' class='btn btn-secondary'>← Back to Auctions</a><hr>");

        // Show user info if logged in (V1 feature)
        if (currentUser != null) {
            showUserInfoBar(request, out, currentUser);
        }

        // Show messages
        showMessages(request, out);

        out.println("<div class='auction-details-container'>");

        // Enhanced auction info section (V2 with V1 security)
        showEnhancedAuctionInfo(out, auction, isLoggedIn);

        // Bidding section (V1 + V2 combined)
        boolean isExpired = auction.getEndTime().isBefore(LocalDateTime.now());
        if (auction.isActive() && !isExpired && isLoggedIn) {
            showBiddingSection(out, auction);
        } else if (isExpired || !auction.isActive()) {
            showAuctionCompletionInfo(out, auction);
        } else if (!isLoggedIn) {
            out.println("<div class='bid-form'>");
            out.println("<h3>💰 Login Required to Bid</h3>");
            out.println("<p>Please <a href='/AuctionSystem/auction/' class='btn'>login</a> to place a bid on this auction.</p>");
            out.println("</div>");
        }

        // Bid history section (V1 comprehensive + V2 styling)
        showBidHistorySection(out, bids, auction.isActive() && !isExpired);

        out.println("</div>"); // End auction-details-container
        out.println("</div>"); // End container

        // Add footer
        addFooter(out);

        // Add WebSocket integration for active auctions (V2 feature)
        if (auction.isActive() && !isExpired) {
            addWebSocketScript(out, auctionId);
        }

        out.println("</body></html>");
    }

    /**
     * V1 + V2: Enhanced auction creation handler with hours/minutes precision
     */
    private void handleAuctionCreation(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String currentUser = getCurrentUser(request);
        if (currentUser == null) {
            response.sendRedirect("/AuctionSystem/auction/?error=not_logged_in");
            return;
        }

        try {
            String title = request.getParameter("title");
            String description = request.getParameter("description");
            String startingPriceStr = request.getParameter("startingPrice");
            String durationHoursStr = request.getParameter("durationHours");
            String durationMinutesStr = request.getParameter("durationMinutes");

            // V1 validation with V2 duration handling
            if (title == null || title.trim().isEmpty() ||
                    description == null || description.trim().isEmpty() ||
                    startingPriceStr == null || startingPriceStr.trim().isEmpty() ||
                    durationHoursStr == null || durationMinutesStr == null) {
                response.sendRedirect("/AuctionSystem/auction/?error=missing_fields");
                return;
            }

            double startingPrice = Double.parseDouble(startingPriceStr);
            int durationHours = Integer.parseInt(durationHoursStr);
            int durationMinutes = Integer.parseInt(durationMinutesStr);

            // V1 validation logic
            if (startingPrice <= 0) {
                response.sendRedirect("/AuctionSystem/auction/?error=invalid_price");
                return;
            }

            // V2 enhanced duration validation
            if (!auctionService.validateAuctionDuration(durationHours, durationMinutes)) {
                response.sendRedirect("/AuctionSystem/auction/?error=invalid_duration");
                return;
            }

            // V1 session activity update
            updateSessionActivity(request);

            // V2 enhanced auction creation
            AuctionDTO newAuction = auctionService.createAuction(
                    title.trim(),
                    description.trim(),
                    startingPrice,
                    durationHours,
                    durationMinutes
            );

            if (newAuction != null) {
                logger.info("Auction created successfully by " + currentUser +
                        ": " + title + " (Duration: " + durationHours + "h " + durationMinutes + "m)");
                response.sendRedirect("/AuctionSystem/auction/?success=auction_created&id=" +
                        newAuction.getAuctionId());
            } else {
                response.sendRedirect("/AuctionSystem/auction/?error=creation_failed");
            }

        } catch (NumberFormatException e) {
            logger.warning("Invalid number format in auction creation: " + e.getMessage());
            response.sendRedirect("/AuctionSystem/auction/?error=invalid_numbers");
        } catch (Exception e) {
            logger.severe("Error creating auction: " + e.getMessage());
            response.sendRedirect("/AuctionSystem/auction/?error=system_error");
        }
    }

    /**
     * V1 + V2: Enhanced bid submission handler
     */
    private void handleBidSubmission(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String currentUser = getCurrentUser(request);
        if (currentUser == null) {
            response.sendRedirect("/AuctionSystem/auction/?error=not_logged_in");
            return;
        }

        try {
            String auctionIdStr = request.getParameter("auctionId");
            String bidAmountStr = request.getParameter("bidAmount");

            if (auctionIdStr == null || bidAmountStr == null) {
                response.sendRedirect("/AuctionSystem/auction/?error=missing_fields");
                return;
            }

            Long auctionId = Long.parseLong(auctionIdStr);
            double bidAmount = Double.parseDouble(bidAmountStr);

            // V1 session validation
            updateSessionActivity(request);

            // V2 auction validation
            if (!auctionService.isAuctionActive(auctionId)) {
                response.sendRedirect("/AuctionSystem/auction/view/" + auctionId + "?error=auction_ended");
                return;
            }

            // V1 bid placement logic
            boolean success = bidService.placeBid(auctionId, currentUser, bidAmount);

            if (success) {
                logger.info("Bid placed successfully: User=" + currentUser +
                        ", Auction=" + auctionId + ", Amount=$" + bidAmount);
                response.sendRedirect("/AuctionSystem/auction/view/" + auctionId + "?success=bid_placed");
            } else {
                logger.warning("Bid placement failed: User=" + currentUser +
                        ", Auction=" + auctionId + ", Amount=$" + bidAmount);
                response.sendRedirect("/AuctionSystem/auction/view/" + auctionId + "?error=invalid_bid");
            }

        } catch (NumberFormatException e) {
            logger.warning("Invalid number format in bid placement: " + e.getMessage());
            response.sendRedirect("/AuctionSystem/auction/?error=invalid_numbers");
        } catch (Exception e) {
            logger.severe("Error placing bid: " + e.getMessage());
            response.sendRedirect("/AuctionSystem/auction/?error=system_error");
        }
    }

    /**
     * V1 Comprehensive user login handler
     */
    private void handleUserLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            response.sendRedirect("/AuctionSystem/auction/?error=missing_credentials");
            return;
        }

        if (userService.authenticateUser(username, password)) {
            HttpSession session = request.getSession();

            // Get client information for session security
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            // Create secure session token
            String sessionToken = sessionManager.createUserSession(
                    username,
                    session.getId(),
                    ipAddress,
                    userAgent != null ? userAgent : "Unknown"
            );

            // Store session token in HTTP session
            session.setAttribute("sessionToken", sessionToken);
            session.setAttribute("username", username);

            logger.info("User logged in successfully: " + username + " from IP: " + ipAddress);
            response.sendRedirect("/AuctionSystem/auction/?success=login_success");
        } else {
            logger.warning("Failed login attempt for user: " + username);
            response.sendRedirect("/AuctionSystem/auction/?error=login_failed");
        }
    }

    /**
     * V1 Comprehensive user logout handler
     */
    private void handleUserLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);

        if (session != null) {
            String sessionToken = (String) session.getAttribute("sessionToken");
            String username = (String) session.getAttribute("username");

            // Invalidate session in session manager
            if (sessionToken != null) {
                sessionManager.logout(sessionToken);
            }

            // Invalidate HTTP session
            session.invalidate();

            logger.info("User logged out: " + username);
        }

        response.sendRedirect("/AuctionSystem/auction/?message=logged_out");
    }

    /**
     * V1 Comprehensive user registration handler
     */
    private void handleUserRegistration(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            String username = request.getParameter("regUsername");
            String email = request.getParameter("regEmail");
            String password = request.getParameter("regPassword");
            String confirmPassword = request.getParameter("regConfirmPassword");

            // V1 comprehensive validation
            if (username == null || username.trim().isEmpty() ||
                    email == null || email.trim().isEmpty() ||
                    password == null || password.trim().isEmpty() ||
                    confirmPassword == null || confirmPassword.trim().isEmpty()) {
                response.sendRedirect("/AuctionSystem/auction/?error=missing_registration_fields");
                return;
            }

            if (!password.equals(confirmPassword)) {
                response.sendRedirect("/AuctionSystem/auction/?error=password_mismatch");
                return;
            }

            if (password.length() < 4) {
                response.sendRedirect("/AuctionSystem/auction/?error=password_too_short");
                return;
            }

            if (!isValidEmail(email.trim())) {
                response.sendRedirect("/AuctionSystem/auction/?error=invalid_email");
                return;
            }

            User newUser = userService.registerUser(username.trim(), email.trim(), password);

            if (newUser != null) {
                logger.info("User registered successfully: " + username.trim());
                response.sendRedirect("/AuctionSystem/auction/?success=registration_success");
            } else {
                logger.warning("Registration failed for user: " + username);
                response.sendRedirect("/AuctionSystem/auction/?error=user_already_exists");
            }

        } catch (Exception e) {
            logger.severe("Error during registration: " + e.getMessage());
            response.sendRedirect("/AuctionSystem/auction/?error=registration_error");
        }
    }

    /**
     * V1 Password change functionality
     */
    private void handlePasswordChange(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        String currentUser = getCurrentUser(request);
        if (currentUser == null) {
            showError(out, "Authentication Required", "Please login to change your password.");
            return;
        }

        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        // Validate inputs
        if (currentPassword == null || newPassword == null || confirmPassword == null ||
                currentPassword.trim().isEmpty() || newPassword.trim().isEmpty() || confirmPassword.trim().isEmpty()) {
            response.sendRedirect("/AuctionSystem/auction/change-password?error=missing_fields");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            response.sendRedirect("/AuctionSystem/auction/change-password?error=passwords_dont_match");
            return;
        }

        if (newPassword.length() < 4) {
            response.sendRedirect("/AuctionSystem/auction/change-password?error=password_too_short");
            return;
        }

        // Update session activity
        updateSessionActivity(request);

        boolean success = userService.changePassword(currentUser, currentPassword, newPassword);

        if (success) {
            response.sendRedirect("/AuctionSystem/auction/?message=password_changed");
        } else {
            response.sendRedirect("/AuctionSystem/auction/change-password?error=wrong_password");
        }
    }

    // [Additional V1 methods continue here...]

    private void showChangePasswordForm(HttpServletRequest request, PrintWriter out) {
        String currentUser = getCurrentUser(request);
        if (currentUser == null) {
            showError(out, "Authentication Required", "Please login to change your password.");
            return;
        }

        out.println("<html><head><title>Change Password</title>");
        out.println("<meta charset='UTF-8'>");
        out.println("</head><body style=\"margin:0;background:#f5f6fa;font-family:'Segoe UI',Arial,sans-serif;\">");

        out.println("<div style=\"max-width:400px;margin:50px auto;padding:32px 28px 26px 28px;background:#fff;border-radius:12px;box-shadow:0 2px 12px #0001;\">");
        out.println("<h1 style=\"font-size:1.7em;margin-bottom:10px;\">🔑 Change Password</h1>");
        out.println("<a href='/AuctionSystem/auction/' style=\"display:inline-block;margin-bottom:18px;padding:7px 16px;background:#f3f3fb;color:#333;text-decoration:none;border-radius:6px;font-weight:500;transition:background 0.2s;\">← Back to Main Page</a>");
        out.println("<hr style=\"margin:18px 0 26px 0;border:none;border-top:1px solid #e0e0e0;\">");

        String error = request.getParameter("error");
        if (error != null) {
            out.println("<div style=\"background:#ffe6e6;color:#c00;border:1px solid #ffb4b4;border-radius:6px;padding:12px 14px;margin-bottom:18px;font-size:1em;\">");
            switch (error) {
                case "wrong_password":
                    out.println("❌ Current password is incorrect.");
                    break;
                case "password_too_short":
                    out.println("❌ New password must be at least 4 characters long.");
                    break;
                case "passwords_dont_match":
                    out.println("❌ New passwords do not match.");
                    break;
                case "change_failed":
                    out.println("❌ Password change failed. Please try again.");
                    break;
                case "missing_fields":
                    out.println("❌ All fields are required.");
                    break;
            }
            out.println("</div>");
        }

        out.println("<form method='post' action='/AuctionSystem/auction/change-password'>");

        out.println("<div style=\"margin-bottom:16px;\">");
        out.println("<label for='current_password' style=\"display:block;font-weight:500;margin-bottom:5px;\">Current Password:</label>");
        out.println("<input type='password' id='current_password' name='currentPassword' required style=\"width:100%;padding:9px 10px;border:1.5px solid #ccd7e8;border-radius:6px;font-size:1em;\">");
        out.println("</div>");

        out.println("<div style=\"margin-bottom:16px;\">");
        out.println("<label for='new_password' style=\"display:block;font-weight:500;margin-bottom:5px;\">New Password:</label>");
        out.println("<input type='password' id='new_password' name='newPassword' required minlength='4' style=\"width:100%;padding:9px 10px;border:1.5px solid #ccd7e8;border-radius:6px;font-size:1em;\">");
        out.println("</div>");

        out.println("<div style=\"margin-bottom:18px;\">");
        out.println("<label for='confirm_password' style=\"display:block;font-weight:500;margin-bottom:5px;\">Confirm New Password:</label>");
        out.println("<input type='password' id='confirm_password' name='confirmPassword' required minlength='4' style=\"width:100%;padding:9px 10px;border:1.5px solid #ccd7e8;border-radius:6px;font-size:1em;\">");
        out.println("</div>");

        out.println("<div>");
        out.println("<input type='submit' value='Change Password' style=\"width:100%;background:#1eb980;color:#fff;font-size:1.07em;font-weight:600;padding:12px 0;border:none;border-radius:7px;cursor:pointer;box-shadow:0 1px 4px #0002;transition:background 0.2s;\">");
        out.println("</div>");

        out.println("</form>");
        out.println("</div>");
        out.println("</body></html>");
    }
    //    Session status
    private void showSessionStatus(HttpServletRequest request, PrintWriter out) {
        // --- CSS Style Definitions (for inline styles on cards) ---
        String bodyStyle = "style='margin:0; background:#f5f6fa; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif;'";
        String mainCardStyle = "style='max-width: 600px; margin: 40px auto; padding: 32px; background: #ffffff; border-radius: 12px; box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);'";
        String h1Style = "style='font-size: 1.8em; margin-top: 0; margin-bottom: 8px; display: flex; align-items: center; gap: 12px;'";
        String infoCardHeaderStyle = "style='margin-top: 0; font-size: 1.2em; display: flex; align-items: center; gap: 10px; border-bottom: 1px solid #e9ecef; padding-bottom: 12px; margin-bottom: 16px;'";
        String infoCardBaseStyle = "border-radius: 10px; padding: 24px; margin-bottom: 22px; box-shadow: 0 1px 4px rgba(0,0,0,0.04);";
        String statsCardStyle = "style='" + infoCardBaseStyle + " background: #f8faff; border: 1px solid #dbe6fb;'";
        String userCardStyle = "style='" + infoCardBaseStyle + " background: #f7fff9; border: 1px solid #d4f3dd;'";
        String guestCardStyle = "style='" + infoCardBaseStyle + " background: #fffaf2; border: 1px solid #ffe2ba;'";
        String rowStyle = "style='display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid #e9ecef;'";
        String lastRowStyle = "style='display: flex; justify-content: space-between; align-items: center; padding: 10px 0;'";
        String labelStyle = "style='font-weight: 600; color: #333;'";
        String valueStyle = "style='color: #555;'";


        // --- HTML Output ---
        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("  <title>Session Status</title>");
        out.println("  <meta charset='UTF-8'>");

        // --- ADD THIS STYLE BLOCK ---
        // This defines the styles for your 'btn-success' class and the 'hr' tag.
        out.println("  <style>");
        out.println("    .btn { display: inline-block; font-weight: 500; text-align: center; cursor: pointer; user-select: none; background-color: transparent; border: 1px solid transparent; padding: 8px 16px; font-size: 1rem; border-radius: 8px; text-decoration: none; transition: all .15s ease-in-out; }");
        out.println("    .btn-success { color: #fff; background-color: #28a745; border-color: #28a745; }");
        out.println("    .btn-success:hover { color: #fff; background-color: #218838; border-color: #1e7e34; }");
        out.println("    hr { margin: 24px 0; border: 0; border-top: 1px solid #e9ecef; }");
        out.println("  </style>");
        // --- END STYLE BLOCK ---

        out.println("</head>");
        out.println("<body " + bodyStyle + ">");

        out.println("<div " + mainCardStyle + ">");
        out.println("<h1 " + h1Style + "><span>🔐</span> Session Status</h1>");
        // This class will now be styled correctly by the <style> block above
        out.println("<a href='/AuctionSystem/auction/' class='btn btn-success'>← Back to Auctions</a>");
        out.println("<hr>");

        // Session Statistics Card
        int activeSessionCount = sessionManager.getActiveSessionCount();
        out.println("<div " + statsCardStyle + ">");
        out.println("<h3 " + infoCardHeaderStyle + "><span>📊</span>Session Statistics</h3>");
        out.println("<div " + rowStyle + "><span " + labelStyle + ">Total Active Sessions:</span><span " + valueStyle + ">" + activeSessionCount + "</span></div>");
        out.println("<div " + lastRowStyle + "><span " + labelStyle + ">Server Time:</span><span " + valueStyle + ">" + LocalDateTime.now().format(formatter) + " UTC</span></div>");
        out.println("</div>");

        // Current User Session Info Card (Conditional)
        String currentUser = getCurrentUser(request);
        if (currentUser != null) {
            HttpSession session = request.getSession(false);
            String sessionToken = (session != null) ? (String) session.getAttribute("sessionToken") : null;

            if (sessionToken != null) {
                ActiveSessionInfo sessionInfo = sessionManager.getSessionInfo(sessionToken);
                if (sessionInfo != null) {
                    out.println("<div " + userCardStyle + ">");
                    out.println("<h3 " + infoCardHeaderStyle + "><span>👤</span>Your Session Info</h3>");
                    out.println("<div " + rowStyle + "><span " + labelStyle + ">Username:</span><span " + valueStyle + ">" + sessionInfo.getUsername() + "</span></div>");
                    out.println("<div " + rowStyle + "><span " + labelStyle + ">Login Time:</span><span " + sessionInfo.getLoginTime().format(formatter) + " UTC</span></div>");
                    out.println("<div " + rowStyle + "><span " + labelStyle + ">Session Duration:</span><span " + valueStyle + ">" + sessionInfo.getSessionDurationMinutes() + " minutes</span></div>");
                    out.println("<div " + rowStyle + "><span " + labelStyle + ">Last Activity:</span><span " + valueStyle + ">" + sessionInfo.getInactiveDurationMinutes() + " minutes ago</span></div>");
                    out.println("<div " + lastRowStyle + "><span " + labelStyle + ">IP Address:</span><span " + valueStyle + ">" + sessionInfo.getIpAddress() + "</span></div>");
                    out.println("</div>");
                }
            }
        } else {
            out.println("<div " + guestCardStyle + ">");
            out.println("<h3 " + infoCardHeaderStyle + "><span>👤</span>Your Session Info</h3>");
            out.println("<p>You are not currently logged in.</p>");
            out.println("</div>");
        }

        out.println("</div>");
        addFooter(out);
        out.println("</body></html>");
    }
    /**
     * V1 Show user profile
     */
    private void showUserProfile(HttpServletRequest request, PrintWriter out) {
        String currentUser = getCurrentUser(request);
        if (currentUser == null) {
            showError(out, "Authentication Required", "Please login to view your profile.");
            return;
        }

        out.println("<html><head><title>User Profile</title>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("</head><body style='font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, \"Helvetica Neue\", Arial, sans-serif; margin: 0; padding: 0; background-color: #f8f9fa; color: #212529; line-height: 1.6;'>");

        // Container
        out.println("<div style='max-width: 1200px; margin: 0 auto; padding: 20px;'>");

        // Header
        out.println("<h1 style='color: #343a40; margin-bottom: 20px; font-size: 2rem; font-weight: 600; display: flex; align-items: center; gap: 10px;'>");
        out.println("<span>👤</span> User Profile</h1>");

        // Back button
        out.println("<a href='/AuctionSystem/auction/' style='display: inline-block; padding: 8px 16px; background-color: #6c757d; color: 4F565DFF; text-decoration: none; border-radius: 4px; font-size: 14px; margin-bottom: 20px; transition: background-color 0.3s ease;' onmouseover='this.style.backgroundColor=\"#5a6268\";' onmouseout='this.style.backgroundColor=\"#6c757d\";'>← Back to Auctions</a>");

        out.println("<hr style='border: none; border-top: 1px solid #dee2e6; margin: 20px 0;'>");

        User user = userService.getUserByUsername(currentUser);
        if (user != null) {
            // Profile Information Card
            out.println("<div style='background: white; border-radius: 8px; padding: 24px; margin-bottom: 24px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); border-left: 4px solid #007bff;'>");
            out.println("<h3 style='color: #495057; margin-top: 0; margin-bottom: 20px; font-size: 1.25rem; font-weight: 600; display: flex; align-items: center; gap: 8px;'>");
            out.println("<span>ℹ️</span> Profile Information</h3>");

            // Username
            out.println("<p style='margin: 12px 0; font-size: 15px;'>");
            out.println("<strong style='color: #495057; display: inline-block; width: 140px;'>Username:</strong> ");
            out.println("<span style='color: #212529;'>" + user.getUsername() + "</span>");
            if (user.isAdmin()) {
                out.println(" <span style='background: linear-gradient(135deg, #ffd700, #ffed4a); color: #8b4513; padding: 3px 8px; border-radius: 12px; font-size: 11px; font-weight: 600; text-transform: uppercase; margin-left: 8px;'>🔑 ADMIN</span>");
            }
            out.println("</p>");

            // Email
            out.println("<p style='margin: 12px 0; font-size: 15px;'>");
            out.println("<strong style='color: #495057; display: inline-block; width: 140px;'>Email:</strong> ");
            out.println("<span style='color: #212529;'>" + user.getEmail() + "</span>");
            out.println("</p>");

            // Last Activity
            out.println("<p style='margin: 12px 0; font-size: 15px;'>");
            out.println("<strong style='color: #495057; display: inline-block; width: 140px;'>Last Activity:</strong> ");
            out.println("<span style='color: #6c757d;'>" + user.getLastActivity().format(formatter) + " UTC</span>");
            out.println("</p>");

            // Account Status
            out.println("<p style='margin: 12px 0; font-size: 15px;'>");
            out.println("<strong style='color: #495057; display: inline-block; width: 140px;'>Account Status:</strong> ");
            if (user.isActive()) {
                out.println("<span style='color: #28a745; font-weight: 600;'>🟢 Active</span>");
            } else {
                out.println("<span style='color: #dc3545; font-weight: 600;'>🔴 Inactive</span>");
            }
            out.println("</p>");

            // Change Password Button
            out.println("<p style='margin-top: 20px; padding-top: 16px; border-top: 1px solid #e9ecef;'>");
            out.println("<a href='/AuctionSystem/auction/change-password' style='display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 4px; font-size: 14px; font-weight: 500; transition: background-color 0.3s ease;' onmouseover='this.style.backgroundColor=\"#0056b3\";' onmouseout='this.style.backgroundColor=\"#007bff\";'>🔑 Change Password</a>");
            out.println("</p>");
            out.println("</div>");

            // Active Sessions Card
            List<ActiveSessionInfo> userSessions = sessionManager.getActiveSessionsForUser(currentUser);
            out.println("<div style='background: white; border-radius: 8px; padding: 24px; margin-bottom: 24px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); border-left: 4px solid #28a745;'>");
            out.println("<h3 style='color: #495057; margin-top: 0; margin-bottom: 20px; font-size: 1.25rem; font-weight: 600; display: flex; align-items: center; gap: 8px;'>");
            out.println("<span>🔐</span> Your Active Sessions (" + userSessions.size() + ")</h3>");

            if (userSessions.isEmpty()) {
                out.println("<p style='color: #6c757d; font-style: italic; margin: 0;'>No other active sessions found.</p>");
            } else {
                // Sessions Table
                out.println("<div style='overflow-x: auto;'>");
                out.println("<table style='width: 100%; border-collapse: collapse; margin-top: 10px;'>");
                out.println("<thead>");
                out.println("<tr style='background-color: #f8f9fa;'>");
                out.println("<th style='padding: 12px; text-align: left; border-bottom: 2px solid #dee2e6; font-weight: 600; color: #495057;'>Login Time</th>");
                out.println("<th style='padding: 12px; text-align: left; border-bottom: 2px solid #dee2e6; font-weight: 600; color: #495057;'>Duration</th>");
                out.println("<th style='padding: 12px; text-align: left; border-bottom: 2px solid #dee2e6; font-weight: 600; color: #495057;'>IP Address</th>");
                out.println("<th style='padding: 12px; text-align: left; border-bottom: 2px solid #dee2e6; font-weight: 600; color: #495057;'>Last Activity</th>");
                out.println("</tr>");
                out.println("</thead>");
                out.println("<tbody>");

                for (ActiveSessionInfo sessionInfo : userSessions) {
                    out.println("<tr style='transition: background-color 0.2s ease;' onmouseover='this.style.backgroundColor=\"#f8f9fa\";' onmouseout='this.style.backgroundColor=\"transparent\";'>");
                    out.println("<td style='padding: 12px; border-bottom: 1px solid #e9ecef; color: #212529;'>" + sessionInfo.getLoginTime().format(formatter) + "</td>");
                    out.println("<td style='padding: 12px; border-bottom: 1px solid #e9ecef; color: #212529;'>");
                    out.println("<span style='background-color: #e7f3ff; color: #0056b3; padding: 2px 6px; border-radius: 3px; font-size: 12px; font-weight: 500;'>" + sessionInfo.getSessionDurationMinutes() + " min</span>");
                    out.println("</td>");
                    out.println("<td style='padding: 12px; border-bottom: 1px solid #e9ecef; color: #212529; font-family: monospace;'>" + sessionInfo.getIpAddress() + "</td>");
                    out.println("<td style='padding: 12px; border-bottom: 1px solid #e9ecef; color: #6c757d;'>" + sessionInfo.getInactiveDurationMinutes() + " min ago</td>");
                    out.println("</tr>");
                }

                out.println("</tbody>");
                out.println("</table>");
                out.println("</div>");
            }
            out.println("</div>");
        }

        out.println("</div>"); // End container

        // Add responsive CSS
        out.println("<style>");
        out.println("@media (max-width: 768px) {");
        out.println("  body { padding: 10px !important; }");
        out.println("  h1 { font-size: 1.5rem !important; }");
        out.println("  .profile-card { padding: 16px !important; }");
        out.println("  table { font-size: 14px !important; }");
        out.println("  th, td { padding: 8px !important; }");
        out.println("}");
        out.println("</style>");

        addFooter(out);

        out.println("</body></html>");
    }

    /**
     * V2 Show user list with INLINE CSS
     */
    private void showUserList(PrintWriter out) {
        // --- Define all CSS styles as strings for cleaner code ---

        String containerStyle = "style='max-width: 1200px; margin: 20px auto; padding: 20px; background-color: #fdfdfd; border-radius: 10px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08); font-family: sans-serif;'";
        String h1Style = "style='color: #333; margin-bottom: 20px; border-bottom: 2px solid #eee; padding-bottom: 10px;'";
        String btnStyle = "style='display: inline-block; background-color: #007bff; color: white; padding: 10px 15px; text-decoration: none; border-radius: 5px; font-weight: 600;'";
        String tableStyle = "style='width: 100%; border-collapse: collapse; margin-top: 20px; font-size: 0.95em;'";
        String thStyle = "style='background-color: #f8f9fa; color: #333; font-weight: 600; text-align: left; padding: 15px; border-bottom: 2px solid #dee2e6;'";
        String tdStyle = "style='padding: 15px; border-bottom: 1px solid #e9ecef; color: #555;'";
        String adminBadgeStyle = "style='background-color: #ffc107; color: #333; padding: 4px 8px; border-radius: 5px; font-size: 0.8em; font-weight: 700; white-space: nowrap;'";

        // Style for table rows (default)
        String trStyle = "style='background-color: #ffffff;'";
        // Style for alternating table rows (for zebra-striping)
        String trEvenStyle = "style='background-color: #f8f9fa;'";


        // --- Start HTML Output ---

        out.println("<html><head><title>Active Users</title>");
        out.println("<meta charset='UTF-8'>");

        // A small style block is required for hover effects, which cannot be inlined.
        out.println("<style>");
        out.println("  tr.user-row:hover { background-color: #e9ecef !important; cursor: pointer; }");
        out.println("  a.btn-back:hover { background-color: #0056b3 !important; }");
        out.println("</style>");

        out.println("</head><body>");

        out.println("<div " + containerStyle + ">");
        out.println("<h1 " + h1Style + ">👥 Active Users</h1>");
        out.println("<a href='/AuctionSystem/auction/' class='btn-back' " + btnStyle + ">← Back to Auctions</a><hr style='border: none; border-top: 1px solid #eee; margin-top: 20px;'>");

        List<User> users = userService.getAllActiveUsers();

        if (users.isEmpty()) {
            out.println("<p>No active users found.</p>");
        } else {
            out.println("<table " + tableStyle + ">");

            // Table Header
            out.println("<tr>");
            out.println("<th " + thStyle + ">Username</th>");
            out.println("<th " + thStyle + ">Email</th>");
            out.println("<th " + thStyle + ">Role</th>");
            out.println("<th " + thStyle + ">Last Activity</th>");
            out.println("<th " + thStyle + ">Sessions</th>");
            out.println("</tr>");

            // Table Body
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                int userSessionCount = sessionManager.getActiveSessionsForUser(user.getUsername()).size();

                // Apply zebra-striping based on row index
                String currentRowStyle = (i % 2 == 0) ? trEvenStyle : trStyle;

                out.println("<tr class='user-row' " + currentRowStyle + ">");
                out.println("<td " + tdStyle + "><strong>" + user.getUsername() + "</strong></td>");
                out.println("<td " + tdStyle + ">" + user.getEmail() + "</td>");
                out.println("<td " + tdStyle + ">" + (user.isAdmin() ? "<span " + adminBadgeStyle + ">🔑 ADMIN</span>" : "👤 USER") + "</td>");
                out.println("<td " + tdStyle + ">" + user.getLastActivity().format(formatter) + "</td>");
                out.println("<td " + tdStyle + ">" + userSessionCount + " active</td>");
                out.println("</tr>");
            }

            out.println("</table>");
        }

        out.println("</div>");
        addFooter(out);
        out.println("</body></html>");
    }

    /**
     * V1 + V2 Enhanced system status
     */
    private void showSystemStatus(PrintWriter out) {
        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>System Status - Online Auction System</title>");
        out.println("<link href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css' rel='stylesheet'>");

        addEnhancedCSS(out);
        out.println("</head>");
        out.println("<body>");

        addCopyrightComment(out);

        out.println("<div class='container form-container'>");

        // Main Title and Back Button
        out.println("<div class='section-header' style='border-bottom:none;margin-bottom:0;'>");
        out.println("<h2 style='font-size:2.2rem;display:flex;align-items:center;gap:10px;'><span style=\"font-size:2rem;\">📊</span> System Status</h2>");
        out.println("</div>");
        out.println("<a href='/AuctionSystem/auction/' class='btn btn-secondary' style='margin:16px 0 12px 0;display:inline-block;'>← Back to Auctions</a>");
        out.println("<hr style='margin:0 0 24px 0;'>");

        // System Statistics
        int activeAuctions = auctionService.getActiveAuctionCount();
        int activeUsers = userService.getActiveUserCount();
        int activeSessions = sessionManager.getActiveSessionCount();
        double totalBidVolume = auctionManager.getTotalBidVolume();

        // System Statistics Card
        out.println("<div class='auction-section' style='background:#f8f9fa;'>");
        out.println("<h3 style='font-size:1.2rem;margin-bottom:16px;display:flex;align-items:center;font-weight:bold;'><span style=\"font-size:1.2rem;\">📉</span>&nbsp;System Statistics</h3>");
        out.println("<p><strong>Active Auctions:</strong> " + activeAuctions + "</p>");
        out.println("<p><strong>Registered Users:</strong> " + activeUsers + "</p>");
        out.println("<p><strong>Active Sessions:</strong> " + activeSessions + "</p>");
        out.println("<p><strong>Total Bid Volume:</strong> $" + String.format("%.2f", totalBidVolume) + "</p>");
        out.println("<p><strong>Server Time:</strong> " + LocalDateTime.now().format(formatter) + " UTC</p>");
        out.println("</div>");

        // EJB Components Status Card
        out.println("<div class='auction-section' style='background:#f8f9fa;'>");
        out.println("<h3 style='font-size:1.2rem;margin-bottom:16px;display:flex;align-items:center;font-weight:bold;'><span style=\"font-size:1.2rem;\">🛠️</span>&nbsp;EJB Components Status</h3>");
        out.println("<p>✅ AuctionService (Stateless EJB) - Active</p>");
        out.println("<p>✅ BidService (Stateless EJB) - Active</p>");
        out.println("<p>✅ UserService (Stateful EJB) - Active</p>");
        out.println("<p>✅ UserSessionManager (Singleton EJB) - Active</p>");
        out.println("<p>✅ AuctionManager (Singleton EJB) - Active</p>");
        out.println("<p>✅ BidNotificationMDB (Message-Driven Bean) - Active</p>");
        out.println("</div>");

        // Services & Security Status Card
        out.println("<div class='auction-section' style='background:#f8f9fa;'>");
        out.println("<h3 style='font-size:1.2rem;margin-bottom:16px;display:flex;align-items:center;font-weight:bold;'><span style=\"font-size:1.2rem;\">🛡️</span>&nbsp;Services & Security Status</h3>");
        out.println("<p>✅ JMS Messaging - Active</p>");
        out.println("<p>✅ Session Management - Active</p>");
        out.println("<p>✅ Password Authentication - Active</p>");
        out.println("<p>✅ Admin Panel Security - Active</p>");
        out.println("<p>✅ Session Security Validation - Active</p>");
        out.println("<p>✅ WebSocket Real-time Updates - Active</p>");
        out.println("</div>");

        // Security Features Card
        out.println("<div class='auction-section' style='background:#f8f9fa;'>");
        out.println("<h3 style='font-size:1.2rem;margin-bottom:16px;display:flex;align-items:center;font-weight:bold;'><span style=\"font-size:1.2rem;\">🔐</span>&nbsp;Security Features</h3>");
        out.println("<p>✅ SHA-256 Password Hashing</p>");
        out.println("<p>✅ Session Token Security</p>");
        out.println("<p>✅ IP Address Validation</p>");
        out.println("<p>✅ Role-Based Access Control</p>");
        out.println("<p>✅ Automatic Session Cleanup</p>");
        out.println("</div>");

        out.println("</div>"); // container, form-container

        addFooter(out);
        out.println("</body></html>");
    }

    // [V2 Enhanced UI Components...]

    /**
     * V2 Enhanced login form - CSS classes and structure match enhanced theme
     */
    private void showLoginForm(PrintWriter out) {
        out.println("<div class='form-container'>");
        out.println("<h3><i class='fas fa-sign-in-alt'></i> User Login</h3>");
        out.println("<form method='post' action='/AuctionSystem/auction/login'>");

        out.println("<div class='form-group'>");
        out.println("<label for='username'><i class='fas fa-user'></i> Username or Email:</label>");
        out.println("<input type='text' id='username' name='username' required placeholder='Enter username or email'>");
        out.println("</div>");

        out.println("<div class='form-group'>");
        out.println("<label for='password'><i class='fas fa-lock'></i> Password:</label>");
        out.println("<input type='password' id='password' name='password' required placeholder='Enter password'>");
        out.println("</div>");

        out.println("<div class='form-group'>");
        out.println("<input type='submit' value='Login' class='btn btn-success btn-create'>");
        out.println("</div>");

        out.println("</form>");

        out.println("<div class='message-info' style='margin-top: 15px;'>");
        out.println("<p><small><strong>📚 Sample Users:</strong></small></p>");
        out.println("<p><small>• john_doe, jane_smith, bob_wilson, alice_brown <br>(Password: <code>1234</code>)</small></p>");
        out.println("<p><small><strong>🔑 Admin Access:</strong> admin@auction.com <br>(Password: <code>11010001</code>)</small></p>");
        out.println("</div>");

        out.println("</div>");
    }

    /**
     * V2 Enhanced registration form - CSS classes and structure match enhanced theme
     */
    private void showRegistrationForm(PrintWriter out) {
        out.println("<div class='form-container'>");
        out.println("<h3><i class='fas fa-user-plus'></i> Create New Account</h3>");
        out.println("<form method='post' action='/AuctionSystem/auction/register'>");

        out.println("<div class='form-group'>");
        out.println("<label for='reg_username'><i class='fas fa-user'></i> Username:</label>");
        out.println("<input type='text' id='reg_username' name='regUsername' required placeholder='Choose a unique username'>");
        out.println("</div>");

        out.println("<div class='form-group'>");
        out.println("<label for='reg_email'><i class='fas fa-envelope'></i> Email:</label>");
        out.println("<input type='email' id='reg_email' name='regEmail' required placeholder='Enter your email address'>");
        out.println("</div>");

        out.println("<div class='form-group'>");
        out.println("<label for='reg_password'><i class='fas fa-lock'></i> Password:</label>");
        out.println("<input type='password' id='reg_password' name='regPassword' required minlength='4' placeholder='Minimum 4 characters'>");
        out.println("</div>");

        out.println("<div class='form-group'>");
        out.println("<label for='reg_confirm_password'><i class='fas fa-lock'></i> Confirm Password:</label>");
        out.println("<input type='password' id='reg_confirm_password' name='regConfirmPassword' required minlength='4' placeholder='Confirm your password'>");
        out.println("</div>");

        out.println("<div class='form-group'>");
        out.println("<input type='submit' value='Register' class='btn btn-secondary btn-create'>");
        out.println("</div>");

        out.println("</form>");
        out.println("</div>");
    }

    /**
     * V2 Enhanced user info with statistics using enhanced CSS classes.
     */
    private void showUserInfo(PrintWriter out, String username, boolean isAdmin) {
        out.println("<div class='form-container user-info' style='margin-bottom: 24px;'>");

        // User Welcome Section
        out.println("<div style='display: flex; align-items: center; gap: 20px; flex-wrap: wrap; margin-bottom: 12px;'>");
        out.println("<h3 style='display:flex;align-items:center;gap:10px;margin:0;'>");
        out.println("<i class='fas fa-user-circle'></i> Welcome, " + escapeHtml(username) + "!");
        out.println("</h3>");
        if (isAdmin) {
            out.println("<span class='admin-badge'><i class='fas fa-crown'></i> Administrator</span>");
        }
        out.println("</div>");

        // User Actions (buttons)
        out.println("<div style='margin-bottom: 18px;'>");
        out.println("<div style='display:flex;gap:10px;flex-wrap:wrap;'>");

        out.println("<a href='/AuctionSystem/auction/?view=my_wins' class='btn btn-small btn-success' style='text-decoration:none;'>");
        out.println("<i class='fas fa-trophy'></i> My Wins");
        out.println("</a>");

        out.println("<a href='/AuctionSystem/real-time-notifications.html' class='btn btn-small btn-secondary' style='text-decoration:none;'>");
        out.println("<i class='fas fa-bell'></i> Live Updates");
        out.println("</a>");

        if (isAdmin) {
            out.println("<a href='/AuctionSystem/auction/admin/sessions' class='btn btn-small' style='background:linear-gradient(135deg,#e74c3c 0%,#c0392b 100%);color:white;'>");
            out.println("<i class='fas fa-cogs'></i> Admin Panel");
            out.println("</a>");
        }
        out.println("</div>");
        out.println("</div>");

        // User statistics
        showUserStatistics(out, username);

        out.println("</div>");
    }

    /**
     * V2 Show user statistics
     */
    private void showUserStatistics(PrintWriter out, String username) {
        try {
            // Get user's won auctions
            List<AuctionDTO> wonAuctions = auctionService.getUserWonAuctions(username);

            // Calculate total value of won auctions
            double totalWinnings = wonAuctions.stream()
                    .mapToDouble(AuctionDTO::getWinningBid)
                    .sum();

            out.println("<div class='user-stats'>");
            out.println("<h4><i class='fas fa-chart-bar'></i> Your Statistics</h4>");
            out.println("<div class='user-stats-grid'>");

            out.println("<div class='user-stat'>");
            out.println("<div class='stat-value'>" + wonAuctions.size() + "</div>");
            out.println("<div class='stat-label'>Auctions Won</div>");
            out.println("</div>");

            out.println("<div class='user-stat'>");
            out.println("<div class='stat-value'>$" + String.format("%.2f", totalWinnings) + "</div>");
            out.println("<div class='stat-label'>Total Spent</div>");
            out.println("</div>");

            out.println("</div>");
            out.println("</div>");

        } catch (Exception e) {
            logger.warning("Error getting user statistics for " + username + ": " + e.getMessage());
        }
    }

    /**
     * V2 Enhanced system statistics with completed auctions
     * (Modern UI, system statistics in grid cards; matches your enhanced CSS)
     */
    private void showSystemStatistics(PrintWriter out, int activeCount, int completedCount) {
        out.println("<div class='stats-container'>");
        out.println("<h3 style='margin-bottom:18px;display:flex;align-items:center;gap:10px;'><i class='fas fa-chart-line'></i> System Statistics</h3>");

        out.println("<div class='stats-grid'>");

        // Active auctions stat
        out.println("<div class='stat-card active-stat'>");
        out.println("<div class='stat-icon'><i class='fas fa-fire'></i></div>");
        out.println("<div class='stat-info'>");
        out.println("<div class='stat-value'>" + activeCount + "</div>");
        out.println("<div class='stat-label'>Active Auctions</div>");
        out.println("</div>");
        out.println("</div>");

        // Completed auctions stat
        out.println("<div class='stat-card completed-stat'>");
        out.println("<div class='stat-icon'><i class='fas fa-check-circle'></i></div>");
        out.println("<div class='stat-info'>");
        out.println("<div class='stat-value'>" + completedCount + "</div>");
        out.println("<div class='stat-label'>Completed</div>");
        out.println("</div>");
        out.println("</div>");

        // Total auctions stat
        out.println("<div class='stat-card total-stat'>");
        out.println("<div class='stat-icon'><i class='fas fa-gavel'></i></div>");
        out.println("<div class='stat-info'>");
        out.println("<div class='stat-value'>" + (activeCount + completedCount) + "</div>");
        out.println("<div class='stat-label'>Total Auctions</div>");
        out.println("</div>");
        out.println("</div>");

        // Real-time notifications link
        out.println("<div class='stat-card notifications-stat'>");
        out.println("<div class='stat-icon'><i class='fas fa-bell'></i></div>");
        out.println("<div class='stat-info'>");
        out.println("<a href='/AuctionSystem/real-time-notifications.html' class='stat-link'>");
        out.println("<div class='stat-value'>LIVE</div>");
        out.println("<div class='stat-label'>Notifications</div>");
        out.println("</a>");
        out.println("</div>");
        out.println("</div>");

        out.println("</div>"); // End stats-grid
        out.println("</div>"); // End stats-container
    }

    /**
     * V2 NEW: Enhanced auction info with status indicators
     */
    private void showEnhancedAuctionInfo(PrintWriter out, AuctionDTO auction, boolean isLoggedIn) {
        out.println("<div class='auction-info-section'>");

        // Auction header with status
        out.println("<div class='auction-header'>");
        out.println("<div class='auction-title-section'>");
        out.println("<h2>" + escapeHtml(auction.getTitle()) + "</h2>");
        out.println("<span class='auction-id'>#" + auction.getAuctionId() + "</span>");
        out.println("</div>");

        // Status indicator
        out.println("<div class='auction-status'>");
        if (auction.isActive()) {
            out.println("<span class='status-badge active'>");
            out.println("<i class='fas fa-circle'></i> ACTIVE");
            out.println("</span>");
        } else {
            out.println("<span class='status-badge completed'>");
            out.println(auction.getDisplayStatus());
            out.println("</span>");
        }
        out.println("</div>");

        out.println("</div>"); // End auction-header

        // Auction details grid
        out.println("<div class='auction-details-grid'>");

        // Left column - Basic info
        out.println("<div class='detail-column'>");

        out.println("<div class='detail-card'>");
        out.println("<h3><i class='fas fa-info-circle'></i> Auction Information</h3>");
        out.println("<div class='detail-item'>");
        out.println("<strong>Description:</strong>");
        out.println("<p>" + escapeHtml(auction.getDescription()) + "</p>");
        out.println("</div>");

        out.println("<div class='detail-item'>");
        out.println("<strong>Starting Price:</strong>");
        out.println("<span class='price'>$" + String.format("%.2f", auction.getStartingPrice()) + "</span>");
        out.println("</div>");

        out.println("<div class='detail-item'>");
        out.println("<strong>Started:</strong>");
        out.println("<span class='time'>" + auction.getStartTime().format(formatter) + "</span>");
        out.println("</div>");

        if (auction.isActive()) {
            out.println("<div class='detail-item'>");
            out.println("<strong>Ends:</strong>");
            out.println("<span class='time end-time'>" + auction.getEndTime().format(formatter) + "</span>");
            out.println("</div>");
        } else {
            out.println("<div class='detail-item'>");
            out.println("<strong>Ended:</strong>");
            LocalDateTime endTime = auction.getCompletedTime() != null ?
                    auction.getCompletedTime() : auction.getEndTime();
            out.println("<span class='time'>" + endTime.format(formatter) + "</span>");
            out.println("</div>");
        }

        out.println("</div>"); // End detail-card
        out.println("</div>"); // End detail-column

        // Right column - Bidding info
        out.println("<div class='detail-column'>");

        out.println("<div class='detail-card bidding-card'>");
        if (auction.isActive()) {
            out.println("<h3><i class='fas fa-gavel'></i> Current Bidding</h3>");
        } else {
            out.println("<h3><i class='fas fa-trophy'></i> Final Results</h3>");
        }

        // Current/Final bid
        out.println("<div class='bid-display-large'>");
        out.println("<div class='bid-amount'>");
        if (auction.isActive()) {
            out.println("$" + String.format("%.2f", auction.getCurrentHighestBid()));
        } else {
            out.println("$" + String.format("%.2f", auction.getWinningBid()));
        }
        out.println("</div>");
        out.println("<div class='bid-label'>");
        out.println(auction.isActive() ? "Current Highest Bid" : "Final Price");
        out.println("</div>");
        out.println("</div>");

        // Bidder info
        if (auction.isActive()) {
            if (auction.getCurrentHighestBidder() != null) {
                out.println("<div class='bidder-info'>");
                out.println("<strong>Leading Bidder:</strong>");
                out.println("<span class='bidder-name'>" + escapeHtml(auction.getCurrentHighestBidder()) + "</span>");
                out.println("</div>");
            } else {
                out.println("<div class='no-bids'>");
                out.println("<em>No bids placed yet</em>");
                out.println("</div>");
            }
        } else {
            // Winner information
            if (auction.hasWinner()) {
                out.println("<div class='winner-info'>");
                out.println("<div class='winner-badge-large'>");
                out.println("<i class='fas fa-crown'></i>");
                out.println("<span class='winner-title'>WINNER</span>");
                out.println("<span class='winner-name'>" + escapeHtml(auction.getWinnerUsername()) + "</span>");
                out.println("</div>");
                out.println("</div>");
            } else {
                out.println("<div class='no-winner'>");
                out.println("<i class='fas fa-times-circle'></i>");
                out.println("<span>No Winner - No Bids Received</span>");
                out.println("</div>");
            }
        }

        // Bid statistics
        out.println("<div class='bid-stats'>");
        out.println("<div class='stat-item'>");
        out.println("<span class='stat-value'>" + auction.getBidCount() + "</span>");
        out.println("<span class='stat-label'>Total Bids</span>");
        out.println("</div>");
        out.println("</div>");

        out.println("</div>"); // End detail-card
        out.println("</div>"); // End detail-column

        out.println("</div>"); // End auction-details-grid
        out.println("</div>"); // End auction-info-section
    }

    /**
     * V2 NEW: Auction completion info for ended auctions
     */
    private void showAuctionCompletionInfo(PrintWriter out, AuctionDTO auction) {
        out.println("<div class='completion-info-section'>");
        out.println("<div class='completion-card'>");

        if (auction.hasWinner()) {
            out.println("<div class='completion-success'>");
            out.println("<h3><i class='fas fa-trophy'></i> Auction Completed Successfully</h3>");
            out.println("<div class='completion-details'>");
            out.println("<p><strong>Winner:</strong> " + escapeHtml(auction.getWinnerUsername()) + "</p>");
            out.println("<p><strong>Final Price:</strong> $" + String.format("%.2f", auction.getWinningBid()) + "</p>");
            out.println("<p><strong>Total Bids:</strong> " + auction.getBidCount() + "</p>");

            if (auction.getEndReason() != null) {
                out.println("<p><strong>End Reason:</strong> " + auction.getEndReason() + "</p>");
            }
            out.println("</div>");
            out.println("</div>");
        } else {
            out.println("<div class='completion-no-winner'>");
            out.println("<h3><i class='fas fa-times-circle'></i> Auction Ended Without Winner</h3>");
            out.println("<div class='completion-details'>");
            out.println("<p>This auction ended without receiving any bids.</p>");
            out.println("<p><strong>Starting Price:</strong> $" + String.format("%.2f", auction.getStartingPrice()) + "</p>");

            if (auction.getEndReason() != null) {
                out.println("<p><strong>End Reason:</strong> " + auction.getEndReason() + "</p>");
            }
            out.println("</div>");
            out.println("</div>");
        }

        out.println("</div>"); // End completion-card
        out.println("</div>"); // End completion-info-section
    }

    /**
     * V2 Enhanced bidding section
     */
    private void showBiddingSection(PrintWriter out, AuctionDTO auction) {
        out.println("<div class='bidding-section'>");
        out.println("<div class='bidding-card'>");
        out.println("<h3><i class='fas fa-gavel'></i> Place Your Bid</h3>");

        out.println("<form method='post' action='/AuctionSystem/auction/bid' class='bid-form'>");
        out.println("<input type='hidden' name='auctionId' value='" + auction.getAuctionId() + "'>");

        out.println("<div class='bid-input-section'>");
        out.println("<div class='current-bid-info'>");
        out.println("<span class='current-bid-label'>Current Bid:</span>");
        out.println("<span class='current-bid-amount'>$" + String.format("%.2f", auction.getCurrentHighestBid()) + "</span>");
        out.println("</div>");

        out.println("<div class='bid-input-group'>");
        out.println("<label for='bidAmount'><i class='fas fa-dollar-sign'></i> Your Bid:</label>");
        out.println("<input type='number' id='bidAmount' name='bidAmount' ");
        out.println("min='" + (auction.getCurrentHighestBid() + 5.01) + "' ");
        out.println("step='0.01' required placeholder='Enter bid amount'>");
        out.println("<small>Minimum bid: $" + String.format("%.2f", auction.getCurrentHighestBid() + 5.01) + "</small>");
        out.println("</div>");

        out.println("<button type='submit' class='btn btn-bid'>");
        out.println("<i class='fas fa-gavel'></i> Place Bid");
        out.println("</button>");
        out.println("</div>");

        out.println("</form>");
        out.println("</div>"); // End bidding-card
        out.println("</div>"); // End bidding-section
    }

    /**
     * V1 + V2 Enhanced bid history section
     */
    private void showBidHistorySection(PrintWriter out, List<Bid> bids, boolean isActive) {
        out.println("<div class='bid-history-section'>");
        out.println("<h3><i class='fas fa-history'></i> Bid History</h3>");

        if (bids.isEmpty()) {
            out.println("<div class='empty-state'>");
            out.println("<i class='fas fa-gavel'></i>");
            out.println("<p>No bids have been placed yet.</p>");
            if (isActive) {
                out.println("<p>Be the first to place a bid!</p>");
            }
            out.println("</div>");
        } else {
            out.println("<div class='bid-history-table'>");
            out.println("<table>");
            out.println("<thead>");
            out.println("<tr>");
            out.println("<th>Bid #</th>");
            out.println("<th>Bidder</th>");
            out.println("<th>Amount</th>");
            out.println("<th>Time</th>");
            out.println("<th>Status</th>");
            out.println("</tr>");
            out.println("</thead>");
            out.println("<tbody>");

            for (Bid bid : bids) {
                out.println("<tr" + (bid.isWinning() ? " class='winning-bid'" : "") + ">");
                out.println("<td><strong>#" + bid.getBidId() + "</strong></td>");
                out.println("<td>" + escapeHtml(bid.getBidderUsername()) + "</td>");
                out.println("<td class='bid-amount'>$" + String.format("%.2f", bid.getBidAmount()) + "</td>");
                out.println("<td class='bid-time'>" + bid.getBidTime().format(formatter) + "</td>");
                out.println("<td class='bid-status'>");
                if (bid.isWinning() && isActive) {
                    out.println("<span class='status-badge winning'>");
                    out.println("<i class='fas fa-crown'></i> Leading");
                    out.println("</span>");
                } else if (bid.isWinning() && !isActive) {
                    out.println("<span class='status-badge winner'>");
                    out.println("<i class='fas fa-trophy'></i> Winner");
                    out.println("</span>");
                } else {
                    out.println("<span class='status-badge outbid'>⚪ Outbid</span>");
                }
                out.println("</td>");
                out.println("</tr>");
            }

            out.println("</tbody>");
            out.println("</table>");
            out.println("</div>");
        }

        out.println("</div>"); // End bid-history-section
    }

    /**
     * V1 + V2 Enhanced message display
     */
    private void showMessages(HttpServletRequest request, PrintWriter out) {
        String error = request.getParameter("error");
        String success = request.getParameter("success");
        String message = request.getParameter("message");
        String info = request.getParameter("info");

        if (error != null) {
            out.println("<div class='message message-error'>");
            out.println();
            switch (error) {
                case "login_failed":
                    out.println("❌ Login failed! Invalid username/email or password.");
                    break;
                case "session_expired":
                    out.println("⏰ Your session has expired. Please login again.");
                    break;
                case "access_denied":
                    out.println("🚫 Access denied. Admin privileges required.");
                    break;
                case "admin_login_required":
                    out.println("🔐 Please login with admin credentials to access the admin panel.");
                    break;
                case "missing_credentials":
                    out.println("📝 Please enter both username/email and password.");
                    break;
                case "not_logged_in":
                    out.println("🔑 Please log in to create auctions or place bids.");
                    break;
                case "invalid_bid":
                    out.println("💸 Invalid bid amount. Please enter a valid bid higher than the current bid.");
                    break;
                case "auction_not_found":
                    out.println("🔎 The requested auction could not be found.");
                    break;
                case "auction_ended":
                    out.println("⏳ This auction has already ended. You cannot place more bids.");
                    break;
                case "invalid_duration":
                    out.println("🕒 Invalid auction duration. Please select between 1 minute and 7 days.");
                    break;
                case "missing_fields":
                    out.println("⚠️ Please fill in all required fields.");
                    break;
                case "invalid_price":
                    out.println("💲 Please enter a valid starting price greater than $0.");
                    break;
                case "invalid_numbers":
                    out.println("🔢 Please enter valid numeric values for price and duration.");
                    break;
                case "creation_failed":
                    out.println("❗ Failed to create auction. Please try again.");
                    break;
                case "system_error":
                    out.println("💥 A system error occurred. Please try again later.");
                    break;
                case "user_already_exists":
                    out.println("👤 Username or email already exists. Please choose different credentials.");
                    break;
                case "password_mismatch":
                    out.println("🔁 Passwords do not match. Please try again.");
                    break;
                case "password_too_short":
                    out.println("📏 Password must be at least 4 characters long.");
                    break;
                case "invalid_email":
                    out.println("📧 Please enter a valid email address.");
                    break;
                default:
                    out.println("❌ " + escapeHtml(error));
            }
            out.println("</div>");
        }

        if (success != null) {
            out.println("<div class='message message-success'>");
            out.println("<i class='fas fa-check-circle'></i> ");
            switch (success) {
                case "auction_created":
                    String auctionId = request.getParameter("id");
                    out.println("Auction created successfully!");
                    if (auctionId != null) {
                        out.println(" <a href='/AuctionSystem/auction/view/" + auctionId + "'>View your auction</a>");
                    }
                    break;
                case "bid_placed":
                    out.println("Your bid has been placed successfully!");
                    break;
                case "login_success":
                    out.println("Welcome! You have been logged in successfully.");
                    break;
                case "registration_success":
                    out.println("Registration successful! You can now log in.");
                    break;
                default:
                    out.println("Operation completed successfully: " + escapeHtml(success));
            }
            out.println("</div>");
        }

        if (message != null) {
            out.println("<div class='message message-success'>");
            out.println("<i class='fas fa-check-circle'></i> ");
            switch (message) {
                case "logged_out":
                    out.println("You have been logged out successfully.");
                    break;
                case "password_changed":
                    out.println("Password changed successfully!");
                    break;
                default:
                    out.println(escapeHtml(message));
            }
            out.println("</div>");
        }

        if (info != null) {
            out.println("<div class='message message-info'>");
            out.println("<i class='fas fa-info-circle'></i> ");
            out.println(escapeHtml(info));
            out.println("</div>");
        }
    }

    /**
     * V2 Enhanced CSS injection
     */
    private void addEnhancedCSS(PrintWriter out) {
        out.println("<style>");
        out.println("/* Enhanced Auction System Styles */");
        out.println("/* Copyright (c) 2025 Ishara Lakshitha (@isharax9). All rights reserved. */");

        // Include the complete CSS from the external file
        out.println("@import url('/AuctionSystem/css/enhanced-auction-styles.css');");

        out.println("</style>");
    }

    /**
     * V2 Add WebSocket integration script
     */
    private void addWebSocketScript(PrintWriter out, Long auctionId) {
        out.println("<script>");
        out.println("// Enhanced WebSocket Integration for Real-time Updates");
        out.println("// Copyright (c) 2025 Ishara Lakshitha (@isharax9). All rights reserved.");
        out.println("");
        out.println("let ws = null;");
        out.println("let reconnectAttempts = 0;");
        out.println("const maxReconnectAttempts = 5;");
        out.println("const auctionId = " + auctionId + ";");
        out.println("");
        out.println("function connectWebSocket() {");
        out.println("    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';");
        out.println("    const wsUrl = protocol + '//' + window.location.host + '/AuctionSystem/auction-updates/' + auctionId;");
        out.println("    ");
        out.println("    try {");
        out.println("        ws = new WebSocket(wsUrl);");
        out.println("        ");
        out.println("        ws.onopen = function(event) {");
        out.println("            console.log('WebSocket connected for auction:', auctionId);");
        out.println("            reconnectAttempts = 0;");
        out.println("            updateConnectionStatus(true);");
        out.println("        };");
        out.println("        ");
        out.println("        ws.onmessage = function(event) {");
        out.println("            try {");
        out.println("                const data = JSON.parse(event.data);");
        out.println("                handleWebSocketMessage(data);");
        out.println("            } catch (e) {");
        out.println("                console.error('Error parsing WebSocket message:', e);");
        out.println("            }");
        out.println("        };");
        out.println("        ");
        out.println("        ws.onclose = function(event) {");
        out.println("            console.log('WebSocket disconnected');");
        out.println("            updateConnectionStatus(false);");
        out.println("            ");
        out.println("            if (reconnectAttempts < maxReconnectAttempts) {");
        out.println("                setTimeout(() => {");
        out.println("                    reconnectAttempts++;");
        out.println("                    console.log('Reconnecting... Attempt:', reconnectAttempts);");
        out.println("                    connectWebSocket();");
        out.println("                }, 3000 * reconnectAttempts);");
        out.println("            }");
        out.println("        };");
        out.println("        ");
        out.println("        ws.onerror = function(error) {");
        out.println("            console.error('WebSocket error:', error);");
        out.println("            updateConnectionStatus(false);");
        out.println("        };");
        out.println("        ");
        out.println("    } catch (e) {");
        out.println("        console.error('Failed to create WebSocket connection:', e);");
        out.println("        updateConnectionStatus(false);");
        out.println("    }");
        out.println("}");
        out.println("");
        out.println("function handleWebSocketMessage(data) {");
        out.println("    if (data.type === 'bidUpdate') {");
        out.println("        const bidUpdate = data.data;");
        out.println("        updateBidDisplay(bidUpdate);");
        out.println("        showBidNotification(bidUpdate);");
        out.println("        setTimeout(() => window.location.reload(), 2000);");
        out.println("    }");
        out.println("}");
        out.println("");
        out.println("function updateBidDisplay(bidUpdate) {");
        out.println("    const currentBidElement = document.querySelector('.current-bid-amount');");
        out.println("    const bidAmountElement = document.querySelector('.bid-amount');");
        out.println("    const bidderElement = document.querySelector('.bidder-name');");
        out.println("    ");
        out.println("    if (currentBidElement) {");
        out.println("        currentBidElement.textContent = '$' + bidUpdate.bidAmount.toFixed(2);");
        out.println("        currentBidElement.classList.add('bid-update-animation');");
        out.println("        setTimeout(() => currentBidElement.classList.remove('bid-update-animation'), 1000);");
        out.println("    }");
        out.println("    ");
        out.println("    if (bidAmountElement) {");
        out.println("        bidAmountElement.textContent = '$' + bidUpdate.bidAmount.toFixed(2);");
        out.println("    }");
        out.println("    ");
        out.println("    if (bidderElement) {");
        out.println("        bidderElement.textContent = bidUpdate.bidderUsername;");
        out.println("    }");
        out.println("    ");
        out.println("    const bidInput = document.getElementById('bidAmount');");
        out.println("    if (bidInput) {");
        out.println("        const newMin = bidUpdate.bidAmount + 5.01;");
        out.println("        bidInput.min = newMin.toFixed(2);");
        out.println("    }");
        out.println("}");
        out.println("");
        out.println("function showBidNotification(bidUpdate) {");
        out.println("    const notification = document.createElement('div');");
        out.println("    notification.className = 'live-indicator';");
        out.println("    notification.innerHTML = `");
        out.println("        <i class='fas fa-gavel'></i> ");
        out.println("        New bid: $${bidUpdate.bidAmount.toFixed(2)} by ${bidUpdate.bidderUsername}");
        out.println("    `;");
        out.println("    ");
        out.println("    document.body.appendChild(notification);");
        out.println("    ");
        out.println("    setTimeout(() => {");
        out.println("        if (notification.parentNode) {");
        out.println("            notification.parentNode.removeChild(notification);");
        out.println("        }");
        out.println("    }, 5000);");
        out.println("}");
        out.println("");
        out.println("function updateConnectionStatus(connected) {");
        out.println("    let indicator = document.querySelector('.live-indicator.connection-status');");
        out.println("    ");
        out.println("    if (!indicator) {");
        out.println("        indicator = document.createElement('div');");
        out.println("        indicator.className = 'live-indicator connection-status';");
        out.println("        indicator.style.top = '80px';");
        out.println("        document.body.appendChild(indicator);");
        out.println("    }");
        out.println("    ");
        out.println("    if (connected) {");
        out.println("        indicator.className = 'live-indicator connection-status';");
        out.println("        indicator.innerHTML = '<i class=\"fas fa-wifi\"></i> Live Updates Active';");
        out.println("    } else {");
        out.println("        indicator.className = 'live-indicator connection-status disconnected';");
        out.println("        indicator.innerHTML = '<i class=\"fas fa-wifi\"></i> Reconnecting...';");
        out.println("    }");
        out.println("}");
        out.println("");
        out.println("document.addEventListener('DOMContentLoaded', function() {");
        out.println("    connectWebSocket();");
        out.println("    ");
        out.println("    setInterval(() => {");
        out.println("        if (ws && ws.readyState === WebSocket.OPEN) {");
        out.println("            ws.send(JSON.stringify({type: 'heartbeat', timestamp: Date.now()}));");
        out.println("        }");
        out.println("    }, 30000);");
        out.println("});");
        out.println("");
        out.println("window.addEventListener('beforeunload', function() {");
        out.println("    if (ws) {");
        out.println("        ws.close();");
        out.println("    }");
        out.println("});");
        out.println("</script>");
    }

    /**
     * V2 Add copyright comment
     */
    private void addCopyrightComment(PrintWriter out) {
        out.println("<!--");
        out.println("    Enhanced Online Auction System - Merged V1 + V2");
        out.println("    Copyright (c) 2025 Ishara Lakshitha (@isharax9). All rights reserved.");
        out.println("    ");
        out.println("    This comprehensive auction system combines:");
        out.println("    - V1: Complete session management, admin features, user management");
        out.println("    - V2: Enhanced UI, auction history, duration control, WebSocket integration");
        out.println("    ");
        out.println("    Author: Ishara Lakshitha (@isharax9)");
        out.println("    Project: AuctionSystem - Complete Feature Integration");
        out.println("    Created: June 2025");
        out.println("    Features: EJB, JMS, WebSocket, Real-time Bidding, Complete User Management");
        out.println("-->");
    }

    /**
     * V2 Add footer
     */
    private void addFooter(PrintWriter out) {
        out.println("</div>"); // Close container
        out.println("<footer style=\"text-align: center; padding: 20px; margin-top: 40px; border-top: 1px solid #ddd; background-color: #f8f9fa;\">");
        out.println("<div style=\"color: #666; font-size: 14px;\">");
        out.println("<p>&copy; 2025 <strong>Ishara Lakshitha</strong>. All rights reserved.</p>");
        out.println("<p style=\"margin: 5px 0;\">");
        out.println("<i class=\"fas fa-code\"></i>");
        out.println("Developed by <a href=\"https://github.com/isharax9\" target=\"_blank\" style=\"color: #007bff; text-decoration: none;\">@isharax9</a>");
        out.println("</p>");
        out.println("<p style=\"margin: 0; font-size: 12px; color: #888;\">");
        out.println("Online Auction System | BCD 1 Research Assignment");
        out.println("</p>");
        out.println("</div>");
        out.println("</footer>");
        out.println("</body></html>");
    }

    /**
     * V2 Show error page
     */
    private void showError(PrintWriter out, String title, String message) {
        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>" + escapeHtml(title) + " - Online Auction System</title>");
        out.println("<link href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css' rel='stylesheet'>");
        addEnhancedCSS(out);
        out.println("</head>");
        out.println("<body>");

        addCopyrightComment(out);

        out.println("<div class='container'>");
        out.println("<div class='nav-bar'>");
        out.println("<h1><i class='fas fa-exclamation-triangle'></i> " + escapeHtml(title) + "</h1>");
        out.println("</div>");

        out.println("<div class='error-content'>");
        out.println("<div class='empty-state'>");
        out.println("<i class='fas fa-exclamation-circle'></i>");
        out.println("<h2>" + escapeHtml(message) + "</h2>");
        out.println("<div style='margin-top: 30px;'>");
        out.println("<a href='/AuctionSystem/auction/' class='btn btn-success'>");
        out.println("<i class='fas fa-home'></i> Go to Dashboard");
        out.println("</a>");
        out.println("<a href='javascript:history.back()' class='btn btn-secondary' style='margin-left: 15px;'>");
        out.println("<i class='fas fa-arrow-left'></i> Go Back");
        out.println("</a>");
        out.println("</div>");
        out.println("</div>");
        out.println("</div>");

        out.println("</div>");
        addFooter(out);
        out.println("</body></html>");
    }

    // Helper methods from V1
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

    private void updateSessionActivity(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String sessionToken = (String) session.getAttribute("sessionToken");
            if (sessionToken != null) {
                sessionManager.updateSessionActivity(sessionToken);
            }
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}