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
        out.println("<title>Online Auction System - Enhanced Dashboard</title>");
        out.println("<link href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css' rel='stylesheet'>");

        // Add enhanced CSS for new components
        addEnhancedCSS(out);

        out.println("</head>");
        out.println("<body>");

        // Add copyright comment
        addCopyrightComment(out);

        out.println("<div class='container'>");

        // Header with enhanced navigation
        out.println("<div class='header'>");
        out.println("<h1>🏺 Online Auction System</h1>");
        out.println("<p>Enhanced with History & Duration Control - Premium Auction Platform</p>");
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
        out.println("<div class='nav-bar'>");
        out.println("<a href='/AuctionSystem/auction/' class='nav-link'>🏠 Home</a>");
        out.println("<a href='/AuctionSystem/auction/users' class='nav-link'>👥 Users</a>");
        out.println("<a href='/AuctionSystem/auction/status' class='nav-link'>📊 System Status</a>");
        out.println("<a href='/AuctionSystem/auction/sessions' class='nav-link'>🔐 Sessions</a>");
        out.println("<a href='/AuctionSystem/real-time-notifications.html' class='nav-link' target='_blank'>🔔 Notifications</a>");

        if (currentUser != null) {
            out.println("<a href='/AuctionSystem/auction/profile' class='nav-link'>👤 Profile</a>");
            if (isAdmin) {
                out.println("<a href='/AuctionSystem/auction/admin/sessions/' class='nav-link admin-link'>🔧 Admin Panel</a>");
            }
        }
        out.println("</div>");
    }

    /**
     * V1 System Status Summary with V2 styling
     */
    private void showSystemStatusSummary(PrintWriter out, int activeCount, int completedCount) {
        int activeUsers = userService.getActiveUserCount();
        int activeSessions = sessionManager.getActiveSessionCount();
        double totalBidVolume = auctionManager.getTotalBidVolume();

        out.println("<div class='status'>");
        out.println("<strong>📈 System Status:</strong> ");
        out.println(activeCount + " active auctions • " + completedCount + " completed • " +
                activeUsers + " users • " + activeSessions + " sessions • ");
        out.println("$" + String.format("%.2f", totalBidVolume) + " total volume");
        out.println("<br><small>Last updated: " + LocalDateTime.now().format(formatter) + " UTC</small>");
        out.println("</div>");
    }

    /**
     * V1 User Info Bar with enhanced styling
     */
    private void showUserInfoBar(HttpServletRequest request, PrintWriter out, String username) {
        HttpSession session = request.getSession(false);
        String sessionToken = session != null ? (String) session.getAttribute("sessionToken") : null;

        out.println("<div class='user-info'>");
        out.println("<strong>👤 Welcome, " + escapeHtml(username) + "!</strong> ");

        // Show admin badge if user is admin
        if (userService.isUserAdmin(username)) {
            out.println("<span class='admin-badge'>🔑 ADMIN</span> ");
        }

        if (sessionToken != null) {
            ActiveSessionInfo sessionInfo = sessionManager.getSessionInfo(sessionToken);
            if (sessionInfo != null) {
                out.println("| Session: " + sessionInfo.getSessionDurationMinutes() + " min ");
                out.println("| Inactive: " + sessionInfo.getInactiveDurationMinutes() + " min ago ");
            }
        }

        out.println("| <a href='/AuctionSystem/auction/change-password' style='margin-right: 10px; color: #007bff;'>🔑 Change Password</a>");

        out.println("| <form method='post' action='/AuctionSystem/auction/logout' style='display: inline;'>");
        out.println("<button type='submit' class='logout-btn'>🚪 Logout</button>");
        out.println("</form>");
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
        int[] minutes = {0, 15, 30, 45};
        for (int min : minutes) {
            out.println("<option value='" + min + "'>" + min + "</option>");
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
        out.println("<a href='/AuctionSystem/auction/' class='btn'>← Back to Auctions</a><hr>");

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

    /**
     * V1 Show change password form
     */
    private void showChangePasswordForm(HttpServletRequest request, PrintWriter out) {
        String currentUser = getCurrentUser(request);
        if (currentUser == null) {
            showError(out, "Authentication Required", "Please login to change your password.");
            return;
        }

        out.println("<html><head><title>Change Password</title>");
        out.println("<meta charset='UTF-8'>");
        addEnhancedCSS(out);
        out.println("</head><body>");

        out.println("<div class='container'>");
        out.println("<h1>🔑 Change Password</h1>");
        out.println("<a href='/AuctionSystem/auction/'>← Back to Main Page</a><hr>");

        String error = request.getParameter("error");
        if (error != null) {
            out.println("<div class='error-msg'>");
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
        out.println("<div class='form-group'>");
        out.println("<label for='current_password'>Current Password:</label>");
        out.println("<input type='password' id='current_password' name='currentPassword' required>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<label for='new_password'>New Password:</label>");
        out.println("<input type='password' id='new_password' name='newPassword' required minlength='4'>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<label for='confirm_password'>Confirm New Password:</label>");
        out.println("<input type='password' id='confirm_password' name='confirmPassword' required minlength='4'>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<input type='submit' value='Change Password' class='btn'>");
        out.println("</div>");
        out.println("</form>");

        out.println("</div>");
        out.println("</body></html>");
    }

    /**
     * V1 Show session status
     */
    private void showSessionStatus(HttpServletRequest request, PrintWriter out) {
        out.println("<html><head><title>Session Status</title>");
        out.println("<meta charset='UTF-8'>");
        addEnhancedCSS(out);
        out.println("</head><body>");

        out.println("<div class='container'>");
        out.println("<h1>🔐 Session Status</h1>");
        out.println("<a href='/AuctionSystem/auction/' class='btn'>← Back to Auctions</a><hr>");

        // Session Statistics
        int activeSessionCount = sessionManager.getActiveSessionCount();
        out.println("<div class='status-card'>");
        out.println("<h3>📊 Session Statistics</h3>");
        out.println("<p><strong>Total Active Sessions:</strong> " + activeSessionCount + "</p>");
        out.println("<p><strong>Server Time:</strong> " + LocalDateTime.now().format(formatter) + " UTC</p>");
        out.println("</div>");

        // Current user session info
        String currentUser = getCurrentUser(request);
        if (currentUser != null) {
            HttpSession session = request.getSession(false);
            String sessionToken = session != null ? (String) session.getAttribute("sessionToken") : null;

            if (sessionToken != null) {
                ActiveSessionInfo sessionInfo = sessionManager.getSessionInfo(sessionToken);
                if (sessionInfo != null) {
                    out.println("<div class='status-card'>");
                    out.println("<h3>👤 Your Session Info</h3>");
                    out.println("<p><strong>Username:</strong> " + sessionInfo.getUsername() + "</p>");
                    out.println("<p><strong>Login Time:</strong> " + sessionInfo.getLoginTime().format(formatter) + " UTC</p>");
                    out.println("<p><strong>Session Duration:</strong> " + sessionInfo.getSessionDurationMinutes() + " minutes</p>");
                    out.println("<p><strong>Last Activity:</strong> " + sessionInfo.getInactiveDurationMinutes() + " minutes ago</p>");
                    out.println("<p><strong>IP Address:</strong> " + sessionInfo.getIpAddress() + "</p>");
                    out.println("</div>");
                }
            }
        } else {
            out.println("<div class='status-card'>");
            out.println("<h3>👤 Session Info</h3>");
            out.println("<p>You are not currently logged in.</p>");
            out.println("</div>");
        }

        out.println("</div>");
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
        addEnhancedCSS(out);
        out.println("</head><body>");

        out.println("<div class='container'>");
        out.println("<h1>👤 User Profile</h1>");
        out.println("<a href='/AuctionSystem/auction/' class='btn'>← Back to Auctions</a><hr>");

        User user = userService.getUserByUsername(currentUser);
        if (user != null) {
            out.println("<div class='profile-card'>");
            out.println("<h3>Profile Information</h3>");
            out.println("<p><strong>Username:</strong> " + user.getUsername());
            if (user.isAdmin()) {
                out.println(" <span class='admin-badge'>🔑 ADMIN</span>");
            }
            out.println("</p>");
            out.println("<p><strong>Email:</strong> " + user.getEmail() + "</p>");
            out.println("<p><strong>Last Activity:</strong> " + user.getLastActivity().format(formatter) + " UTC</p>");
            out.println("<p><strong>Account Status:</strong> " + (user.isActive() ? "🟢 Active" : "🔴 Inactive") + "</p>");
            out.println("<p><a href='/AuctionSystem/auction/change-password' class='btn'>🔑 Change Password</a></p>");
            out.println("</div>");

            // Show user's active sessions
            List<ActiveSessionInfo> userSessions = sessionManager.getActiveSessionsForUser(currentUser);
            out.println("<div class='profile-card'>");
            out.println("<h3>Your Active Sessions (" + userSessions.size() + ")</h3>");

            if (userSessions.isEmpty()) {
                out.println("<p>No other active sessions found.</p>");
            } else {
                out.println("<table>");
                out.println("<tr><th>Login Time</th><th>Duration</th><th>IP Address</th><th>Last Activity</th></tr>");

                for (ActiveSessionInfo sessionInfo : userSessions) {
                    out.println("<tr>");
                    out.println("<td>" + sessionInfo.getLoginTime().format(formatter) + "</td>");
                    out.println("<td>" + sessionInfo.getSessionDurationMinutes() + " min</td>");
                    out.println("<td>" + sessionInfo.getIpAddress() + "</td>");
                    out.println("<td>" + sessionInfo.getInactiveDurationMinutes() + " min ago</td>");
                    out.println("</tr>");
                }

                out.println("</table>");
            }
            out.println("</div>");
        }

        out.println("</div>");
        out.println("</body></html>");
    }

    /**
     * V1 Show user list
     */
    private void showUserList(PrintWriter out) {
        out.println("<html><head><title>Active Users</title>");
        out.println("<meta charset='UTF-8'>");
        addEnhancedCSS(out);
        out.println("</head><body>");

        out.println("<div class='container'>");
        out.println("<h1>👥 Active Users</h1>");
        out.println("<a href='/AuctionSystem/auction/' class='btn'>← Back to Auctions</a><hr>");

        List<User> users = userService.getAllActiveUsers();

        if (users.isEmpty()) {
            out.println("<p>No active users found.</p>");
        } else {
            out.println("<table>");
            out.println("<tr><th>Username</th><th>Email</th><th>Role</th><th>Last Activity</th><th>Sessions</th></tr>");

            for (User user : users) {
                int userSessionCount = sessionManager.getActiveSessionsForUser(user.getUsername()).size();
                out.println("<tr>");
                out.println("<td><strong>" + user.getUsername() + "</strong></td>");
                out.println("<td>" + user.getEmail() + "</td>");
                out.println("<td>" + (user.isAdmin() ? "<span class='admin-badge'>🔑 ADMIN</span>" : "👤 USER") + "</td>");
                out.println("<td>" + user.getLastActivity().format(formatter) + "</td>");
                out.println("<td>" + userSessionCount + " active</td>");
                out.println("</tr>");
            }

            out.println("</table>");
        }

        out.println("</div>");
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

        out.println("<div class='container'>");
        out.println("<h1>📊 System Status</h1>");
        out.println("<a href='/AuctionSystem/auction/' class='btn'>← Back to Auctions</a><hr>");

        // System Statistics (V1 comprehensive data)
        int activeAuctions = auctionService.getActiveAuctionCount();
        int activeUsers = userService.getActiveUserCount();
        int activeSessions = sessionManager.getActiveSessionCount();
        double totalBidVolume = auctionManager.getTotalBidVolume();

        out.println("<div class='status-card'>");
        out.println("<h3>📈 System Statistics</h3>");
        out.println("<p><strong>Active Auctions:</strong> " + activeAuctions + "</p>");
        out.println("<p><strong>Registered Users:</strong> " + activeUsers + "</p>");
        out.println("<p><strong>Active Sessions:</strong> " + activeSessions + "</p>");
        out.println("<p><strong>Total Bid Volume:</strong> $" + String.format("%.2f", totalBidVolume) + "</p>");
        out.println("<p><strong>Server Time:</strong> " + LocalDateTime.now().format(formatter) + " UTC</p>");
        out.println("</div>");

        out.println("<div class='status-card'>");
        out.println("<h3>🔧 EJB Components Status</h3>");
        out.println("<p>✅ AuctionService (Stateless EJB) - Active</p>");
        out.println("<p>✅ BidService (Stateless EJB) - Active</p>");
        out.println("<p>✅ UserService (Stateful EJB) - Active</p>");
        out.println("<p>✅ UserSessionManager (Singleton EJB) - Active</p>");
        out.println("<p>✅ AuctionManager (Singleton EJB) - Active</p>");
        out.println("<p>✅ BidNotificationMDB (Message-Driven Bean) - Active</p>");
        out.println("</div>");

        out.println("<div class='status-card'>");
        out.println("<h3>📡 Services & Security Status</h3>");
        out.println("<p>✅ JMS Messaging - Active</p>");
        out.println("<p>✅ Session Management - Active</p>");
        out.println("<p>✅ Password Authentication - Active</p>");
        out.println("<p>✅ Admin Panel Security - Active</p>");
        out.println("<p>✅ Session Security Validation - Active</p>");
        out.println("<p>✅ WebSocket Real-time Updates - Active</p>");
        out.println("</div>");

        out.println("<div class='status-card'>");
        out.println("<h3>🔐 Security Features</h3>");
        out.println("<p>✅ SHA-256 Password Hashing</p>");
        out.println("<p>✅ Session Token Security</p>");
        out.println("<p>✅ IP Address Validation</p>");
        out.println("<p>✅ Role-Based Access Control</p>");
        out.println("<p>✅ Automatic Session Cleanup</p>");
        out.println("</div>");

        out.println("</div>");
        addFooter(out);
        out.println("</body></html>");
    }

    // [V2 Enhanced UI Components...]

    /**
     * V2 Enhanced login form
     */
    private void showLoginForm(PrintWriter out) {
        out.println("<div class='form-container'>");
        out.println("<h3><i class='fas fa-sign-in-alt'></i> User Login</h3>");
        out.println("<form method='post' action='/AuctionSystem/auction/login'>");
        out.println("<div class='form-group'>");
        out.println("<label for='username'>Username or Email:</label>");
        out.println("<input type='text' id='username' name='username' required placeholder='Enter username or email'>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<label for='password'>Password:</label>");
        out.println("<input type='password' id='password' name='password' required placeholder='Enter password'>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<input type='submit' value='Login' class='btn btn-success'>");
        out.println("</div>");
        out.println("</form>");
        out.println("<div style='background-color: #e9ecef; padding: 15px; margin-top: 15px; border-radius: 5px;'>");
        out.println("<p><small><strong>📚 Sample Users:</strong></small></p>");
        out.println("<p><small>• john_doe, jane_smith, bob_wilson, alice_brown (Password: <code>1234</code>)</small></p>");
        out.println("<p><small><strong>🔑 Admin Access:</strong> admin@auction.com (Password: <code>11010001</code>)</small></p>");
        out.println("</div>");
        out.println("</div>");
    }

    /**
     * V2 Enhanced registration form
     */
    private void showRegistrationForm(PrintWriter out) {
        out.println("<div class='form-container'>");
        out.println("<h3><i class='fas fa-user-plus'></i> Create New Account</h3>");
        out.println("<form method='post' action='/AuctionSystem/auction/register'>");
        out.println("<div class='form-group'>");
        out.println("<label for='reg_username'>Username:</label>");
        out.println("<input type='text' id='reg_username' name='regUsername' required placeholder='Choose a unique username'>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<label for='reg_email'>Email:</label>");
        out.println("<input type='email' id='reg_email' name='regEmail' required placeholder='Enter your email address'>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<label for='reg_password'>Password:</label>");
        out.println("<input type='password' id='reg_password' name='regPassword' required minlength='4' placeholder='Minimum 4 characters'>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<label for='reg_confirm_password'>Confirm Password:</label>");
        out.println("<input type='password' id='reg_confirm_password' name='regConfirmPassword' required minlength='4' placeholder='Confirm your password'>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<input type='submit' value='Register' class='btn btn-success'>");
        out.println("</div>");
        out.println("</form>");
        out.println("</div>");
    }

    /**
     * V2 Enhanced user info with statistics
     */
    private void showUserInfo(PrintWriter out, String username, boolean isAdmin) {
        out.println("<div class='user-info-container'>");
        out.println("<div class='user-welcome'>");
        out.println("<h3><i class='fas fa-user-circle'></i> Welcome, " + escapeHtml(username) + "!</h3>");

        if (isAdmin) {
            out.println("<div class='admin-badge'>");
            out.println("<i class='fas fa-crown'></i> Administrator");
            out.println("</div>");
        }

        out.println("</div>");

        // User actions
        out.println("<div class='user-actions'>");
        out.println("<div class='action-buttons'>");

        // View won auctions
        out.println("<a href='/AuctionSystem/auction/?view=my_wins' class='btn btn-small'>");
        out.println("<i class='fas fa-trophy'></i> My Wins");
        out.println("</a>");

        // Live notifications
        out.println("<a href='/AuctionSystem/real-time-notifications.html' class='btn btn-small'>");
        out.println("<i class='fas fa-bell'></i> Live Updates");
        out.println("</a>");

        // Admin panel (if admin)
        if (isAdmin) {
            out.println("<a href='/AuctionSystem/auction/admin/sessions' class='btn btn-small btn-admin'>");
            out.println("<i class='fas fa-cogs'></i> Admin Panel");
            out.println("</a>");
        }

        // Logout button
        out.println("<a href='/AuctionSystem/auction/logout' class='btn btn-small logout-btn'>");
        out.println("<i class='fas fa-sign-out-alt'></i> Logout");
        out.println("</a>");

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
     */
    private void showSystemStatistics(PrintWriter out, int activeCount, int completedCount) {
        out.println("<div class='stats-container'>");
        out.println("<h3><i class='fas fa-chart-line'></i> System Statistics</h3>");

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
            out.println("<i class='fas fa-exclamation-triangle'></i> ");
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
                    out.println("Please log in to create auctions or place bids.");
                    break;
                case "invalid_bid":
                    out.println("Invalid bid amount. Please enter a valid bid higher than the current bid.");
                    break;
                case "auction_not_found":
                    out.println("The requested auction could not be found.");
                    break;
                case "auction_ended":
                    out.println("This auction has already ended. You cannot place more bids.");
                    break;
                case "invalid_duration":
                    out.println("Invalid auction duration. Please select between 1 minute and 7 days.");
                    break;
                case "missing_fields":
                    out.println("Please fill in all required fields.");
                    break;
                case "invalid_price":
                    out.println("Please enter a valid starting price greater than $0.");
                    break;
                case "invalid_numbers":
                    out.println("Please enter valid numeric values for price and duration.");
                    break;
                case "creation_failed":
                    out.println("Failed to create auction. Please try again.");
                    break;
                case "system_error":
                    out.println("A system error occurred. Please try again later.");
                    break;
                case "user_already_exists":
                    out.println("Username or email already exists. Please choose different credentials.");
                    break;
                case "password_mismatch":
                    out.println("Passwords do not match. Please try again.");
                    break;
                case "password_too_short":
                    out.println("Password must be at least 4 characters long.");
                    break;
                case "invalid_email":
                    out.println("Please enter a valid email address.");
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
        out.println("Auction System Dashboard | BCD 1 Research Assignment");
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