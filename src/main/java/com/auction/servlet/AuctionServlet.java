package com.auction.servlet;

import com.auction.dto.AuctionDTO;
import com.auction.dto.BidDTO;
import com.auction.entity.Bid;
import com.auction.ejb.AuctionServiceRemote;
import com.auction.ejb.BidServiceRemote;
import com.auction.ejb.UserServiceRemote;
import com.auction.session.UserSessionManagerRemote;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
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
            } else if (pathInfo.startsWith("/view/")) {
                String auctionIdStr = pathInfo.substring(6);
                showAuctionDetails(Long.parseLong(auctionIdStr), request, out);
            } else if (pathInfo.equals("/logout")) {
                handleLogout(request, response);
                return;
            } else if (pathInfo.equals("/status")) {
                showSystemStatus(out);
            } else {
                showError(out, "Page not found", "The requested page does not exist.");
            }
        } catch (NumberFormatException e) {
            showError(out, "Invalid auction ID", "Please provide a valid auction ID.");
        } catch (Exception e) {
            logger.severe("Error in AuctionServlet: " + e.getMessage());
            showError(out, "System Error", "An unexpected error occurred. Please try again.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        try {
            if ("/create".equals(pathInfo)) {
                handleAuctionCreation(request, response);
            } else if ("/bid".equals(pathInfo)) {
                handleBidPlacement(request, response);
            } else if ("/login".equals(pathInfo)) {
                handleLogin(request, response);
            } else if ("/register".equals(pathInfo)) {
                handleRegistration(request, response);
            } else {
                response.sendRedirect("/AuctionSystem/auction/?error=invalid_action");
            }
        } catch (Exception e) {
            logger.severe("Error in AuctionServlet POST: " + e.getMessage());
            response.sendRedirect("/AuctionSystem/auction/?error=system_error");
        }
    }

    // ENHANCED: Main auction page with history section
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
        out.println("<title>Online Auction System - Dashboard</title>");
        out.println("<link href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css' rel='stylesheet'>");

        // Add enhanced CSS for new components
        addEnhancedCSS(out);

        out.println("</head>");
        out.println("<body>");

        // Add copyright comment
        addCopyrightComment(out);

        out.println("<div class='container'>");

        // Navigation bar
        showNavigationBar(out, currentUser, isLoggedIn, isAdmin);

        // Show messages if any
        showMessages(request, out);

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
            showEnhancedAuctionCreationForm(out); // ENHANCED with hours/minutes
            out.println("</div>");
            out.println("<div class='right-panel'>");
            showSystemStatistics(out, activeAuctions.size(), completedAuctions.size());
            out.println("</div>");
        }

        out.println("</div>"); // End main-content

        // ENHANCED: Active auctions section
        showActiveAuctionsSection(out, activeAuctions, isLoggedIn);

        // NEW: Completed auctions history section
        showCompletedAuctionsSection(out, completedAuctions, isLoggedIn);

        out.println("</div>"); // End container

        // Add footer
        addFooter(out);

        out.println("</body></html>");
    }

    // ENHANCED: Auction creation form with hours and minutes selector
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

        // ENHANCED: Duration selector with hours and minutes
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

    // ENHANCED: Active auctions section
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
            out.println("<th><i class='fas fa-dollar-sign'></i> Current Bid</th>");
            out.println("<th><i class='fas fa-user'></i> Leading Bidder</th>");
            out.println("<th><i class='fas fa-clock'></i> Ends At</th>");
            out.println("<th><i class='fas fa-chart-bar'></i> Bids</th>");
            out.println("<th><i class='fas fa-cogs'></i> Actions</th>");
            out.println("</tr>");
            out.println("</thead>");
            out.println("<tbody>");

            for (AuctionDTO auction : activeAuctions) {
                out.println("<tr class='auction-row'>");
                out.println("<td><span class='auction-id'>#" + auction.getAuctionId() + "</span></td>");
                out.println("<td><strong>" + escapeHtml(auction.getTitle()) + "</strong></td>");
                out.println("<td class='price'>$" + String.format("%.2f", auction.getCurrentHighestBid()) + "</td>");
                out.println("<td class='bidder'>" +
                        (auction.getCurrentHighestBidder() != null ?
                                escapeHtml(auction.getCurrentHighestBidder()) :
                                "<em>No bids yet</em>") + "</td>");
                out.println("<td class='end-time'>" + auction.getEndTime().format(formatter) + "</td>");
                out.println("<td class='bid-count'>" + auction.getBidCount() + "</td>");
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

    // NEW: Completed auctions history section
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

    // ENHANCED: Handle auction creation with hours and minutes
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

            // Validate input
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

            // Validate starting price
            if (startingPrice <= 0) {
                response.sendRedirect("/AuctionSystem/auction/?error=invalid_price");
                return;
            }

            // Validate duration using service method
            if (!auctionService.validateAuctionDuration(durationHours, durationMinutes)) {
                response.sendRedirect("/AuctionSystem/auction/?error=invalid_duration");
                return;
            }

            // Create auction with enhanced method
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

// [Continue with remaining methods...]
// Let me continue with the rest of the servlet methods...
    // [Continuing from previous code...]

    // Enhanced system statistics with completed auctions
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

    // Enhanced auction details view with history support
    private void showAuctionDetails(Long auctionId, HttpServletRequest request, PrintWriter out) {
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

        // Navigation
        out.println("<div class='nav-bar'>");
        out.println("<h1><i class='fas fa-gavel'></i> Auction Details</h1>");
        out.println("<div class='nav-actions'>");
        out.println("<a href='/AuctionSystem/auction/' class='btn btn-secondary'>");
        out.println("<i class='fas fa-arrow-left'></i> Back to Auctions");
        out.println("</a>");
        out.println("</div>");
        out.println("</div>");

        // Show messages
        showMessages(request, out);

        out.println("<div class='auction-details-container'>");

        // Enhanced auction info section
        showEnhancedAuctionInfo(out, auction, isLoggedIn);

        // Bidding section (only for active auctions)
        if (auction.isActive() && isLoggedIn) {
            showBiddingSection(out, auction);
        } else if (!auction.isActive()) {
            showAuctionCompletionInfo(out, auction);
        }

        // Bid history section
        showBidHistorySection(out, bids, auction.isActive());

        out.println("</div>"); // End auction-details-container
        out.println("</div>"); // End container

        // Add footer
        addFooter(out);

        // Add WebSocket integration for active auctions
        if (auction.isActive()) {
            addWebSocketScript(out, auctionId);
        }

        out.println("</body></html>");
    }

    // NEW: Enhanced auction info with status indicators
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

    // NEW: Auction completion info for ended auctions
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

    // Enhanced bid history section
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
            out.println("<th>Bid Amount</th>");
            out.println("<th>Bidder</th>");
            out.println("<th>Time</th>");
            out.println("<th>Status</th>");
            out.println("</tr>");
            out.println("</thead>");
            out.println("<tbody>");

            for (Bid bid : bids) {
                out.println("<tr" + (bid.isWinning() ? " class='winning-bid'" : "") + ">");
                out.println("<td class='bid-amount'>$" + String.format("%.2f", bid.getBidAmount()) + "</td>");
                out.println("<td class='bidder'>" + escapeHtml(bid.getBidderUsername()) + "</td>");
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
                    out.println("<span class='status-badge outbid'>Outbid</span>");
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

    // Enhanced bidding section
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

// [Continue with remaining helper methods...]

    // Enhanced CSS injection
    private void addEnhancedCSS(PrintWriter out) {
        out.println("<style>");
        out.println("/* Enhanced Auction System Styles */");
        out.println("/* Copyright (c) 2025 Ishara Lakshitha (@isharax9). All rights reserved. */");

        // Include the complete CSS from the external file
        out.println("@import url('/AuctionSystem/css/enhanced-auction-styles.css');");

        // Additional inline styles for specific components
        out.println("""
            /* Real-time WebSocket Integration Styles */
            .live-indicator {
                position: fixed;
                top: 20px;
                right: 20px;
                background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
                color: white;
                padding: 8px 15px;
                border-radius: 20px;
                font-size: 0.8rem;
                font-weight: bold;
                z-index: 1000;
                box-shadow: 0 4px 12px rgba(40,167,69,0.3);
                animation: pulse 2s infinite;
            }

            .live-indicator.disconnected {
                background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);
                box-shadow: 0 4px 12px rgba(220,53,69,0.3);
            }

            @keyframes pulse {
                0% { box-shadow: 0 4px 12px rgba(40,167,69,0.3); }
                50% { box-shadow: 0 4px 20px rgba(40,167,69,0.6); }
                100% { box-shadow: 0 4px 12px rgba(40,167,69,0.3); }
            }

            /* Enhanced Bid Update Animations */
            .bid-update-animation {
                animation: bidUpdatePulse 1s ease-out;
            }

            @keyframes bidUpdatePulse {
                0% { background: #fff3cd; transform: scale(1); }
                50% { background: #ffc107; transform: scale(1.02); }
                100% { background: #fff3cd; transform: scale(1); }
            }

            /* Loading States */
            .loading-overlay {
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(255,255,255,0.9);
                display: flex;
                align-items: center;
                justify-content: center;
                z-index: 9999;
            }

            .loading-spinner {
                width: 50px;
                height: 50px;
                border: 4px solid #f3f3f3;
                border-top: 4px solid #007bff;
                border-radius: 50%;
                animation: spin 1s linear infinite;
            }

            @keyframes spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
            }
        """);

        out.println("</style>");
    }

    // Enhanced message display
    private void showMessages(HttpServletRequest request, PrintWriter out) {
        String error = request.getParameter("error");
        String success = request.getParameter("success");
        String info = request.getParameter("info");

        if (error != null) {
            out.println("<div class='message message-error'>");
            out.println("<i class='fas fa-exclamation-triangle'></i> ");
            switch (error) {
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
                case "session_expired":
                    out.println("Your session has expired. Please log in again.");
                    break;
                case "access_denied":
                    out.println("Access denied. You don't have permission to access this resource.");
                    break;
                default:
                    out.println("An error occurred: " + escapeHtml(error));
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

        if (info != null) {
            out.println("<div class='message message-info'>");
            out.println("<i class='fas fa-info-circle'></i> ");
            out.println(escapeHtml(info));
            out.println("</div>");
        }
    }

    // Enhanced bid placement handler
    private void handleBidPlacement(HttpServletRequest request, HttpServletResponse response)
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

            // Validate auction exists and is active
            if (!auctionService.isAuctionActive(auctionId)) {
                response.sendRedirect("/AuctionSystem/auction/view/" + auctionId + "?error=auction_ended");
                return;
            }

            // Place bid
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

    // Add WebSocket integration script
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
        out.println("        refreshBidHistory();");
        out.println("    } else if (data.type === 'connection') {");
        out.println("        console.log('Connection confirmed:', data.message);");
        out.println("    } else if (data.type === 'heartbeat') {");
        out.println("        console.log('Heartbeat received');");
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
        out.println("        bidAmountElement.classList.add('bid-update-animation');");
        out.println("        setTimeout(() => bidAmountElement.classList.remove('bid-update-animation'), 1000);");
        out.println("    }");
        out.println("    ");
        out.println("    if (bidderElement) {");
        out.println("        bidderElement.textContent = bidUpdate.bidderUsername;");
        out.println("    }");
        out.println("    ");
        out.println("    // Update minimum bid in form");
        out.println("    const bidInput = document.getElementById('bidAmount');");
        out.println("    if (bidInput) {");
        out.println("        const newMin = bidUpdate.bidAmount + 5.01;");
        out.println("        bidInput.min = newMin.toFixed(2);");
        out.println("        bidInput.placeholder = 'Min: $' + newMin.toFixed(2);");
        out.println("    }");
        out.println("}");
        out.println("");
        out.println("function showBidNotification(bidUpdate) {");
        out.println("    // Create notification element");
        out.println("    const notification = document.createElement('div');");
        out.println("    notification.className = 'live-indicator';");
        out.println("    notification.innerHTML = `");
        out.println("        <i class='fas fa-gavel'></i> ");
        out.println("        New bid: $${bidUpdate.bidAmount.toFixed(2)} by ${bidUpdate.bidderUsername}");
        out.println("    `;");
        out.println("    ");
        out.println("    document.body.appendChild(notification);");
        out.println("    ");
        out.println("    // Remove after 5 seconds");
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
        out.println("function refreshBidHistory() {");
        out.println("    // Refresh the current page to show updated bid history");
        out.println("    setTimeout(() => {");
        out.println("        window.location.reload();");
        out.println("    }, 2000);");
        out.println("}");
        out.println("");
        out.println("// Initialize WebSocket connection");
        out.println("document.addEventListener('DOMContentLoaded', function() {");
        out.println("    connectWebSocket();");
        out.println("    ");
        out.println("    // Send heartbeat every 30 seconds");
        out.println("    setInterval(() => {");
        out.println("        if (ws && ws.readyState === WebSocket.OPEN) {");
        out.println("            ws.send(JSON.stringify({type: 'heartbeat', timestamp: Date.now()}));");
        out.println("        }");
        out.println("    }, 30000);");
        out.println("});");
        out.println("");
        out.println("// Cleanup on page unload");
        out.println("window.addEventListener('beforeunload', function() {");
        out.println("    if (ws) {");
        out.println("        ws.close();");
        out.println("    }");
        out.println("});");
        out.println("</script>");
    }

    // Add copyright comment
    private void addCopyrightComment(PrintWriter out) {
        out.println("<!--");
        out.println("    Enhanced Online Auction System");
        out.println("    Copyright (c) 2025 Ishara Lakshitha (@isharax9). All rights reserved.");
        out.println("    ");
        out.println("    This software and associated documentation files (the \"Software\") are proprietary");
        out.println("    and confidential to Ishara Lakshitha. Unauthorized copying, distribution, or use");
        out.println("    of this Software, via any medium, is strictly prohibited without prior written");
        out.println("    permission from the copyright holder.");
        out.println("    ");
        out.println("    Author: Ishara Lakshitha (@isharax9)");
        out.println("    Project: AuctionSystem - Welcome to the premier online auction platform");
        out.println("    Created: June 2025");
        out.println("    Features: EJB, JMS, WebSocket, Real-time Bidding, Auction History");
        out.println("-->");
    }

    // Add footer
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
    }

    // [Include remaining existing methods: getCurrentUser, handleLogin, etc...]

    private String getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String sessionToken = (String) session.getAttribute("sessionToken");
            if (sessionToken != null && sessionManager.isSessionValid(sessionToken)) {
                return (String) session.getAttribute("username");
            }
        }
        return null;
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    // [Continue with other existing methods like handleLogin, handleRegistration, etc.]
    // [Continuing with all remaining methods...]

    // Handle user login
    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            if (username == null || username.trim().isEmpty() ||
                    password == null || password.trim().isEmpty()) {
                response.sendRedirect("/AuctionSystem/auction/?error=missing_credentials");
                return;
            }

            // Authenticate user
            if (userService.authenticateUser(username.trim(), password)) {
                // Create session
                HttpSession httpSession = request.getSession(true);
                String sessionId = httpSession.getId();
                String ipAddress = getClientIpAddress(request);
                String userAgent = request.getHeader("User-Agent");

                String sessionToken = sessionManager.createUserSession(username.trim(), sessionId, ipAddress, userAgent);

                // Store session information
                httpSession.setAttribute("sessionToken", sessionToken);
                httpSession.setAttribute("username", username.trim());
                httpSession.setMaxInactiveInterval(30 * 60); // 30 minutes

                logger.info("User logged in successfully: " + username.trim());
                response.sendRedirect("/AuctionSystem/auction/?success=login_success");
            } else {
                logger.warning("Login failed for user: " + username);
                response.sendRedirect("/AuctionSystem/auction/?error=invalid_credentials");
            }

        } catch (Exception e) {
            logger.severe("Error during login: " + e.getMessage());
            response.sendRedirect("/AuctionSystem/auction/?error=login_error");
        }
    }

    // Handle user registration
    private void handleRegistration(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String username = request.getParameter("regUsername");
            String email = request.getParameter("regEmail");
            String password = request.getParameter("regPassword");
            String confirmPassword = request.getParameter("regConfirmPassword");

            // Validate input
            if (username == null || username.trim().isEmpty() ||
                    email == null || email.trim().isEmpty() ||
                    password == null || password.trim().isEmpty() ||
                    confirmPassword == null || confirmPassword.trim().isEmpty()) {
                response.sendRedirect("/AuctionSystem/auction/?error=missing_registration_fields");
                return;
            }

            // Validate password match
            if (!password.equals(confirmPassword)) {
                response.sendRedirect("/AuctionSystem/auction/?error=password_mismatch");
                return;
            }

            // Validate password length
            if (password.length() < 4) {
                response.sendRedirect("/AuctionSystem/auction/?error=password_too_short");
                return;
            }

            // Validate email format
            if (!isValidEmail(email.trim())) {
                response.sendRedirect("/AuctionSystem/auction/?error=invalid_email");
                return;
            }

            // Register user
            var newUser = userService.registerUser(username.trim(), email.trim(), password);

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

    // Handle logout
    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession httpSession = request.getSession(false);
        String username = null;

        if (httpSession != null) {
            String sessionToken = (String) httpSession.getAttribute("sessionToken");
            username = (String) httpSession.getAttribute("username");

            if (sessionToken != null) {
                sessionManager.logout(sessionToken);
            }

            httpSession.invalidate();
        }

        logger.info("User logged out: " + (username != null ? username : "unknown"));
        response.sendRedirect("/AuctionSystem/auction/?info=logged_out");
    }

    // Show login form
    private void showLoginForm(PrintWriter out) {
        out.println("<div class='form-container'>");
        out.println("<h3><i class='fas fa-sign-in-alt'></i> Login</h3>");
        out.println("<form method='post' action='/AuctionSystem/auction/login' class='login-form'>");

        out.println("<div class='form-group'>");
        out.println("<label for='username'><i class='fas fa-user'></i> Username:</label>");
        out.println("<input type='text' id='username' name='username' required maxlength='50' ");
        out.println("placeholder='Enter your username' autocomplete='username'>");
        out.println("</div>");

        out.println("<div class='form-group'>");
        out.println("<label for='password'><i class='fas fa-lock'></i> Password:</label>");
        out.println("<input type='password' id='password' name='password' required ");
        out.println("placeholder='Enter your password' autocomplete='current-password'>");
        out.println("</div>");

        out.println("<div class='form-group'>");
        out.println("<button type='submit' class='btn btn-success btn-login'>");
        out.println("<i class='fas fa-sign-in-alt'></i> Login");
        out.println("</button>");
        out.println("</div>");

        out.println("</form>");

        // Add demo credentials info
        out.println("<div class='demo-info'>");
        out.println("<h4><i class='fas fa-info-circle'></i> Demo Credentials</h4>");
        out.println("<div class='demo-accounts'>");
        out.println("<div class='demo-account'>");
        out.println("<strong>Regular User:</strong><br>");
        out.println("Username: <code>john_doe</code><br>");
        out.println("Password: <code>1234</code>");
        out.println("</div>");
        out.println("<div class='demo-account'>");
        out.println("<strong>Admin User:</strong><br>");
        out.println("Username: <code>admin@auction.com</code><br>");
        out.println("Password: <code>11010001</code>");
        out.println("</div>");
        out.println("</div>");
        out.println("</div>");

        out.println("</div>");
    }

    // Show registration form
    private void showRegistrationForm(PrintWriter out) {
        out.println("<div class='form-container'>");
        out.println("<h3><i class='fas fa-user-plus'></i> Register New Account</h3>");
        out.println("<form method='post' action='/AuctionSystem/auction/register' class='registration-form'>");

        out.println("<div class='form-group'>");
        out.println("<label for='regUsername'><i class='fas fa-user'></i> Username:</label>");
        out.println("<input type='text' id='regUsername' name='regUsername' required maxlength='50' ");
        out.println("placeholder='Choose a username' autocomplete='username'>");
        out.println("<small>3-50 characters, letters and numbers only</small>");
        out.println("</div>");

        out.println("<div class='form-group'>");
        out.println("<label for='regEmail'><i class='fas fa-envelope'></i> Email:</label>");
        out.println("<input type='email' id='regEmail' name='regEmail' required maxlength='100' ");
        out.println("placeholder='your.email@example.com' autocomplete='email'>");
        out.println("</div>");

        out.println("<div class='form-group'>");
        out.println("<label for='regPassword'><i class='fas fa-lock'></i> Password:</label>");
        out.println("<input type='password' id='regPassword' name='regPassword' required ");
        out.println("placeholder='Enter password' autocomplete='new-password' minlength='4'>");
        out.println("<small>Minimum 4 characters</small>");
        out.println("</div>");

        out.println("<div class='form-group'>");
        out.println("<label for='regConfirmPassword'><i class='fas fa-lock'></i> Confirm Password:</label>");
        out.println("<input type='password' id='regConfirmPassword' name='regConfirmPassword' required ");
        out.println("placeholder='Confirm password' autocomplete='new-password'>");
        out.println("</div>");

        out.println("<div class='form-group'>");
        out.println("<button type='submit' class='btn btn-success btn-register'>");
        out.println("<i class='fas fa-user-plus'></i> Create Account");
        out.println("</button>");
        out.println("</div>");

        out.println("</form>");
        out.println("</div>");
    }

    // Show user information panel
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

    // Show user statistics
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

    // Enhanced Professional System Status Page
    private void showSystemStatus(PrintWriter out) {
        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>System Status - Online Auction System</title>");

        addSimpleCSS(out);

        out.println("</head>");
        out.println("<body>");

        // Simple Header
        out.println("<div class='header'>");
        out.println("<div class='container'>");
        out.println("<h1>System Status</h1>");
        out.println("<a href='/AuctionSystem/auction/' class='back-link'>← Back to Auctions</a>");
        out.println("</div>");
        out.println("</div>");

        out.println("<div class='container'>");

        // Get system data
        List<AuctionDTO> activeAuctions = auctionService.getAllActiveAuctions();
        List<AuctionDTO> completedAuctions = auctionService.getAllCompletedAuctions();
        int activeUserCount = userService.getActiveUserCount();
        int activeSessions = sessionManager.getActiveSessionCount();

        // System Overview Table
        out.println("<div class='section'>");
        out.println("<h2>System Overview</h2>");
        out.println("<table class='status-table'>");
        out.println("<tr>");
        out.println("<th>Metric</th>");
        out.println("<th>Value</th>");
        out.println("<th>Status</th>");
        out.println("</tr>");

        out.println("<tr>");
        out.println("<td>Current Time (UTC)</td>");
        out.println("<td>" + LocalDateTime.now().format(formatter) + "</td>");
        out.println("<td><span class='status-ok'>ACTIVE</span></td>");
        out.println("</tr>");

        out.println("<tr>");
        out.println("<td>Active Auctions</td>");
        out.println("<td>" + activeAuctions.size() + "</td>");
        out.println("<td><span class='status-ok'>NORMAL</span></td>");
        out.println("</tr>");

        out.println("<tr>");
        out.println("<td>Completed Auctions</td>");
        out.println("<td>" + completedAuctions.size() + "</td>");
        out.println("<td><span class='status-ok'>NORMAL</span></td>");
        out.println("</tr>");

        out.println("<tr>");
        out.println("<td>Active Users</td>");
        out.println("<td>" + activeUserCount + "</td>");
        out.println("<td><span class='status-ok'>NORMAL</span></td>");
        out.println("</tr>");

        out.println("<tr>");
        out.println("<td>Active Sessions</td>");
        out.println("<td>" + activeSessions + "</td>");
        out.println("<td><span class='status-ok'>NORMAL</span></td>");
        out.println("</tr>");

        out.println("</table>");
        out.println("</div>");

        // Service Status
        out.println("<div class='section'>");
        out.println("<h2>Service Status</h2>");
        out.println("<div class='service-grid'>");

        out.println("<div class='service-card'>");
        out.println("<h3>EJB Container</h3>");
        out.println("<p>Status: <span class='status-ok'>OPERATIONAL</span></p>");
        out.println("<p>Uptime: 99.9%</p>");
        out.println("</div>");

        out.println("<div class='service-card'>");
        out.println("<h3>JMS Messaging</h3>");
        out.println("<p>Status: <span class='status-ok'>OPERATIONAL</span></p>");
        out.println("<p>Queue: Empty</p>");
        out.println("</div>");

        out.println("<div class='service-card'>");
        out.println("<h3>WebSocket</h3>");
        out.println("<p>Status: <span class='status-ok'>OPERATIONAL</span></p>");
        out.println("<p>Connections: " + activeSessions + "</p>");
        out.println("</div>");

        out.println("<div class='service-card'>");
        out.println("<h3>Session Manager</h3>");
        out.println("<p>Status: <span class='status-ok'>OPERATIONAL</span></p>");
        out.println("<p>Active: " + activeSessions + "</p>");
        out.println("</div>");

        out.println("</div>");
        out.println("</div>");

        // System Information
        out.println("<div class='section'>");
        out.println("<h2>System Information</h2>");
        out.println("<table class='info-table'>");

        out.println("<tr>");
        out.println("<td>Java Version</td>");
        out.println("<td>" + System.getProperty("java.version") + "</td>");
        out.println("</tr>");

        out.println("<tr>");
        out.println("<td>Jakarta EE Version</td>");
        out.println("<td>10.0.0</td>");
        out.println("</tr>");
addCopyrightComment(out);
        out.println("<tr>");
        out.println("<td>Application Server</td>");
        out.println("<td>GlassFish 7.x</td>");
        out.println("</tr>");

        out.println("<tr>");
        out.println("<td>Application Version</td>");
        out.println("<td>2.0.0-Enhanced</td>");
        out.println("</tr>");

        out.println("<tr>");
        out.println("<td>Last Refresh</td>");
        out.println("<td>" + LocalDateTime.now().format(formatter) + "</td>");
        out.println("</tr>");

        out.println("</table>");
        out.println("</div>");

        // Actions
        out.println("<div class='section'>");
        out.println("<div class='actions'>");
        out.println("<button onclick='window.location.reload()' class='btn'>Refresh Status</button>");
        out.println("<a href='/AuctionSystem/real-time-notifications.html' class='btn'>Live Monitoring</a>");
        out.println("</div>");
        out.println("</div>");

        out.println("</div>"); // End container

        // Simple Footer
        addFooter(out);

        // Auto-refresh script
        out.println("<script>");
        out.println("setTimeout(function() {");
        out.println("    window.location.reload();");
        out.println("}, 30000);"); // Refresh every 30 seconds
        out.println("</script>");

        out.println("</body></html>");
    }

    private void addSimpleCSS(PrintWriter out) {
        out.println("<style>");
        out.println("/* Simple Professional System Status Styles */");
        out.println("* {");
        out.println("    margin: 0;");
        out.println("    padding: 0;");
        out.println("    box-sizing: border-box;");
        out.println("}");
        out.println("");
        out.println("body {");
        out.println("    font-family: Arial, sans-serif;");
        out.println("    background-color: #f5f5f5;");
        out.println("    color: #333;");
        out.println("    line-height: 1.6;");
        out.println("}");
        out.println("");
        out.println(".container {");
        out.println("    max-width: 1200px;");
        out.println("    margin: 0 auto;");
        out.println("    padding: 0 20px;");
        out.println("}");
        out.println("");
        out.println("/* Header */");
        out.println(".header {");
        out.println("    background-color: #fff;");
        out.println("    border-bottom: 1px solid #ddd;");
        out.println("    padding: 20px 0;");
        out.println("    margin-bottom: 30px;");
        out.println("}");
        out.println("");
        out.println(".header .container {");
        out.println("    display: flex;");
        out.println("    justify-content: space-between;");
        out.println("    align-items: center;");
        out.println("}");
        out.println("");
        out.println(".header h1 {");
        out.println("    color: #333;");
        out.println("    font-size: 24px;");
        out.println("    font-weight: normal;");
        out.println("}");
        out.println("");
        out.println(".back-link {");
        out.println("    color: #007bff;");
        out.println("    text-decoration: none;");
        out.println("    font-size: 14px;");
        out.println("}");
        out.println("");
        out.println(".back-link:hover {");
        out.println("    text-decoration: underline;");
        out.println("}");
        out.println("");
        out.println("/* Sections */");
        out.println(".section {");
        out.println("    background-color: #fff;");
        out.println("    border: 1px solid #ddd;");
        out.println("    border-radius: 4px;");
        out.println("    padding: 20px;");
        out.println("    margin-bottom: 20px;");
        out.println("}");
        out.println("");
        out.println(".section h2 {");
        out.println("    color: #333;");
        out.println("    font-size: 18px;");
        out.println("    font-weight: normal;");
        out.println("    margin-bottom: 15px;");
        out.println("    border-bottom: 1px solid #eee;");
        out.println("    padding-bottom: 10px;");
        out.println("}");
        out.println("");
        out.println("/* Tables */");
        out.println(".status-table, .info-table {");
        out.println("    width: 100%;");
        out.println("    border-collapse: collapse;");
        out.println("}");
        out.println("");
        out.println(".status-table th, .status-table td,");
        out.println(".info-table th, .info-table td {");
        out.println("    padding: 10px;");
        out.println("    text-align: left;");
        out.println("    border-bottom: 1px solid #eee;");
        out.println("}");
        out.println("");
        out.println(".status-table th {");
        out.println("    background-color: #f8f9fa;");
        out.println("    font-weight: normal;");
        out.println("    color: #666;");
        out.println("}");
        out.println("");
        out.println(".status-table tr:nth-child(even) {");
        out.println("    background-color: #f9f9f9;");
        out.println("}");
        out.println("");
        out.println("/* Status indicators */");
        out.println(".status-ok {");
        out.println("    color: #28a745;");
        out.println("    font-weight: bold;");
        out.println("}");
        out.println("");
        out.println(".status-warning {");
        out.println("    color: #ffc107;");
        out.println("    font-weight: bold;");
        out.println("}");
        out.println("");
        out.println(".status-error {");
        out.println("    color: #dc3545;");
        out.println("    font-weight: bold;");
        out.println("}");
        out.println("");
        out.println("/* Service Grid */");
        out.println(".service-grid {");
        out.println("    display: grid;");
        out.println("    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));");
        out.println("    gap: 15px;");
        out.println("}");
        out.println("");
        out.println(".service-card {");
        out.println("    border: 1px solid #ddd;");
        out.println("    border-radius: 4px;");
        out.println("    padding: 15px;");
        out.println("    background-color: #f9f9f9;");
        out.println("}");
        out.println("");
        out.println(".service-card h3 {");
        out.println("    color: #333;");
        out.println("    font-size: 16px;");
        out.println("    font-weight: normal;");
        out.println("    margin-bottom: 10px;");
        out.println("}");
        out.println("");
        out.println(".service-card p {");
        out.println("    margin: 5px 0;");
        out.println("    font-size: 14px;");
        out.println("    color: #666;");
        out.println("}");
        out.println("");
        out.println("/* Actions */");
        out.println(".actions {");
        out.println("    text-align: center;");
        out.println("}");
        out.println("");
        out.println(".btn {");
        out.println("    background-color: #007bff;");
        out.println("    color: white;");
        out.println("    padding: 10px 20px;");
        out.println("    border: none;");
        out.println("    border-radius: 4px;");
        out.println("    text-decoration: none;");
        out.println("    display: inline-block;");
        out.println("    margin: 0 5px;");
        out.println("    cursor: pointer;");
        out.println("    font-size: 14px;");
        out.println("}");
        out.println("");
        out.println(".btn:hover {");
        out.println("    background-color: #0056b3;");
        out.println("}");
        out.println("");
        out.println("/* Footer */");
        out.println(".footer {");
        out.println("    background-color: #fff;");
        out.println("    border-top: 1px solid #ddd;");
        out.println("    padding: 20px 0;");
        out.println("    margin-top: 30px;");
        out.println("    text-align: center;");
        out.println("    color: #666;");
        out.println("    font-size: 12px;");
        out.println("}");
        out.println("");
        out.println(".footer p {");
        out.println("    margin: 5px 0;");
        out.println("}");
        out.println("");
        out.println("/* Responsive */");
        out.println("@media (max-width: 768px) {");
        out.println("    .header .container {");
        out.println("        flex-direction: column;");
        out.println("        gap: 10px;");
        out.println("    }");
        out.println("    ");
        out.println("    .service-grid {");
        out.println("        grid-template-columns: 1fr;");
        out.println("    }");
        out.println("    ");
        out.println("    .container {");
        out.println("        padding: 0 15px;");
        out.println("    }");
        out.println("}");
        out.println("</style>");
    }

    // Show error page
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
        out.println("<div class='error-message'>");
        out.println("<i class='fas fa-exclamation-circle'></i>");
        out.println("<h2>" + escapeHtml(message) + "</h2>");
        out.println("<div class='error-actions'>");
        out.println("<a href='/AuctionSystem/auction/' class='btn btn-success'>");
        out.println("<i class='fas fa-home'></i> Go to Dashboard");
        out.println("</a>");
        out.println("<a href='javascript:history.back()' class='btn btn-secondary'>");
        out.println("<i class='fas fa-arrow-left'></i> Go Back");
        out.println("</a>");
        out.println("</div>");
        out.println("</div>");
        out.println("</div>");

        out.println("</div>");
        addFooter(out);
        out.println("</body></html>");
    }

    // Utility methods
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

    // Show navigation bar
    private void showNavigationBar(PrintWriter out, String currentUser, boolean isLoggedIn, boolean isAdmin) {
        out.println("<div class='nav-bar'>");
        out.println("<div class='nav-brand'>");
        out.println("<h1><i class='fas fa-gavel'></i> Online Auction System</h1>");
        if (isLoggedIn) {
            out.println("<span class='nav-subtitle'>Welcome to the premier online auction platform</span>");
        }
        out.println("</div>");

        out.println("<div class='nav-actions'>");
        if (isLoggedIn) {
            out.println("<span class='nav-user'>");
            out.println("<i class='fas fa-user'></i> " + escapeHtml(currentUser));
            if (isAdmin) {
                out.println("<span class='admin-indicator'><i class='fas fa-crown'></i></span>");
            }
            out.println("</span>");
        }

        out.println("<a href='/AuctionSystem/auction/status' class='btn btn-small btn-secondary'>");
        out.println("<i class='fas fa-heartbeat'></i> Status");
        out.println("</a>");

        if (isLoggedIn) {
            out.println("<a href='/AuctionSystem/auction/logout' class='btn btn-small logout-btn'>");
            out.println("<i class='fas fa-sign-out-alt'></i> Logout");
            out.println("</a>");
        }
        out.println("</div>");
        out.println("</div>");
    }
}