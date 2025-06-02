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

@WebServlet(name = "AuctionServlet", urlPatterns = {"/auction/*"})
public class AuctionServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(AuctionServlet.class.getName());
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
                showAuctionList(request, out);
            } else if (pathInfo.startsWith("/auction/")) {
                String auctionIdStr = pathInfo.substring("/auction/".length());
                try {
                    Long auctionId = Long.parseLong(auctionIdStr);
                    showAuctionDetails(request, out, auctionId);
                } catch (NumberFormatException e) {
                    showError(out, "Invalid auction ID");
                }
            } else if (pathInfo.equals("/users")) {
                showUserList(out);
            } else if (pathInfo.equals("/status")) {
                showSystemStatus(out);
            } else if (pathInfo.equals("/sessions")) {
                showSessionStatus(request, out);
            } else if (pathInfo.equals("/profile")) {
                showUserProfile(request, out);
            } else {
                showError(out, "Page not found");
            }
        } catch (Exception e) {
            logger.severe("Error processing request: " + e.getMessage());
            showError(out, "Internal server error: " + e.getMessage());
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
                handleBidSubmission(request, out);
            } else if (pathInfo != null && pathInfo.equals("/login")) {
                handleUserLogin(request, response);
            } else if (pathInfo != null && pathInfo.equals("/logout")) {
                handleUserLogout(request, response);
            } else if (pathInfo != null && pathInfo.equals("/register")) {
                handleUserRegistration(request, out);
            } else if (pathInfo != null && pathInfo.equals("/create")) {
                handleAuctionCreation(request, out);
            } else {
                showError(out, "Invalid POST request");
            }
        } catch (Exception e) {
            logger.severe("Error processing POST request: " + e.getMessage());
            showError(out, "Internal server error: " + e.getMessage());
        }
    }

    private void showAuctionList(HttpServletRequest request, PrintWriter out) {
        out.println("<html><head><title>Online Auction System</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; }");
        out.println("table { border-collapse: collapse; width: 100%; }");
        out.println("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
        out.println("th { background-color: #f2f2f2; }");
        out.println(".bid-form { margin: 20px 0; padding: 15px; border: 1px solid #ccc; }");
        out.println(".status { background-color: #e7f3ff; padding: 10px; margin: 10px 0; }");
        out.println(".user-info { background-color: #d4edda; padding: 10px; margin: 10px 0; border-radius: 5px; }");
        out.println(".logout-btn { background-color: #dc3545; color: white; padding: 5px 10px; text-decoration: none; border-radius: 3px; }");
        out.println(".nav-link { margin-right: 15px; }");
        out.println("</style></head><body>");

        out.println("<h1>🏺 Online Auction System</h1>");

        // Show user session info if logged in
        String currentUser = getCurrentUser(request);
        if (currentUser != null) {
            showUserInfoBar(request, out, currentUser);
        }

        // Navigation
        out.println("<nav>");
        out.println("<a href='/AuctionSystem/auction/' class='nav-link'>Home</a>");
        out.println("<a href='/AuctionSystem/auction/users' class='nav-link'>Users</a>");
        out.println("<a href='/AuctionSystem/auction/status' class='nav-link'>System Status</a>");
        out.println("<a href='/AuctionSystem/auction/sessions' class='nav-link'>Sessions</a>");
        if (currentUser != null) {
            out.println("<a href='/AuctionSystem/auction/profile' class='nav-link'>Profile</a>");
        }
        out.println("</nav><hr>");

        // System Status Summary
        int activeAuctions = auctionService.getActiveAuctionCount();
        int activeUsers = userService.getActiveUserCount();
        int activeSessions = sessionManager.getActiveSessionCount();

        out.println("<div class='status'>");
        out.println("<strong>System Status:</strong> ");
        out.println(activeAuctions + " active auctions, " + activeUsers + " active users, " +
                activeSessions + " active sessions");
        out.println("</div>");

        // Login/Registration Forms (only show if not logged in)
        if (currentUser == null) {
            showLoginForm(out);
            showRegistrationForm(out);
        }

        // Auction Creation Form (only show if logged in)
        if (currentUser != null) {
            showAuctionCreationForm(out);
        }

        // Active Auctions List
        showActiveAuctionsList(out, currentUser != null);

        out.println("<hr><p><em>Last updated: " + LocalDateTime.now().format(formatter) + "</em></p>");
        out.println("</body></html>");
    }

    private void showUserInfoBar(HttpServletRequest request, PrintWriter out, String username) {
        HttpSession session = request.getSession(false);
        String sessionToken = session != null ? (String) session.getAttribute("sessionToken") : null;

        out.println("<div class='user-info'>");
        out.println("<strong>👤 Welcome, " + username + "!</strong> ");

        if (sessionToken != null) {
            ActiveSessionInfo sessionInfo = sessionManager.getSessionInfo(sessionToken);
            if (sessionInfo != null) {
                out.println("| Session time: " + sessionInfo.getSessionDurationMinutes() + " minutes ");
                out.println("| Last activity: " + sessionInfo.getInactiveDurationMinutes() + " min ago ");
            }
        }

        out.println("| <form method='post' action='/AuctionSystem/auction/logout' style='display: inline;'>");
        out.println("<button type='submit' class='logout-btn'>Logout</button>");
        out.println("</form>");
        out.println("</div>");
    }

    private void showLoginForm(PrintWriter out) {
        out.println("<div class='bid-form'>");
        out.println("<h3>🔐 User Login</h3>");
        out.println("<form method='post' action='/AuctionSystem/auction/login'>");
        out.println("Username: <input type='text' name='username' required> ");
        out.println("<input type='submit' value='Login'>");
        out.println("</form></div>");
    }

    private void showRegistrationForm(PrintWriter out) {
        out.println("<div class='bid-form'>");
        out.println("<h3>📝 Quick User Registration</h3>");
        out.println("<form method='post' action='/AuctionSystem/auction/register'>");
        out.println("Username: <input type='text' name='username' required> ");
        out.println("Email: <input type='email' name='email' required> ");
        out.println("<input type='submit' value='Register'>");
        out.println("</form></div>");
    }

    private void showAuctionCreationForm(PrintWriter out) {
        out.println("<div class='bid-form'>");
        out.println("<h3>🎯 Create New Auction</h3>");
        out.println("<form method='post' action='/AuctionSystem/auction/create'>");
        out.println("Title: <input type='text' name='title' required><br><br>");
        out.println("Description: <textarea name='description' required></textarea><br><br>");
        out.println("Starting Price: $<input type='number' name='startingPrice' step='0.01' required><br><br>");
        out.println("Duration (hours): <input type='number' name='duration' min='1' max='168' value='24' required><br><br>");
        out.println("<input type='submit' value='Create Auction'>");
        out.println("</form></div>");
    }

    private void showActiveAuctionsList(PrintWriter out, boolean isLoggedIn) {
        out.println("<h2>🔥 Active Auctions</h2>");

        List<AuctionDTO> auctions = auctionService.getAllActiveAuctions();

        if (auctions.isEmpty()) {
            out.println("<p>No active auctions at the moment.</p>");
        } else {
            out.println("<table>");
            out.println("<tr><th>ID</th><th>Title</th><th>Description</th><th>Current Bid</th>");
            out.println("<th>Highest Bidder</th><th>End Time</th><th>Actions</th></tr>");

            for (AuctionDTO auction : auctions) {
                out.println("<tr>");
                out.println("<td>" + auction.getAuctionId() + "</td>");
                out.println("<td><strong>" + auction.getTitle() + "</strong></td>");
                out.println("<td>" + auction.getDescription() + "</td>");
                out.println("<td>$" + String.format("%.2f", auction.getCurrentHighestBid()) + "</td>");
                out.println("<td>" + (auction.getCurrentHighestBidder() != null ?
                        auction.getCurrentHighestBidder() : "No bids yet") + "</td>");
                out.println("<td>" + auction.getEndTime().format(formatter) + "</td>");
                out.println("<td><a href='/AuctionSystem/auction/auction/" + auction.getAuctionId() +
                        "'>View Details</a></td>");
                out.println("</tr>");
            }

            out.println("</table>");
        }
    }

    private void showAuctionDetails(HttpServletRequest request, PrintWriter out, Long auctionId) {
        AuctionDTO auction = auctionService.getAuction(auctionId);

        if (auction == null) {
            showError(out, "Auction not found");
            return;
        }

        String currentUser = getCurrentUser(request);

        out.println("<html><head><title>Auction: " + auction.getTitle() + "</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; }");
        out.println("table { border-collapse: collapse; width: 100%; }");
        out.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        out.println("th { background-color: #f2f2f2; }");
        out.println(".bid-form { margin: 20px 0; padding: 15px; border: 1px solid #ccc; }");
        out.println(".auction-info { background-color: #f9f9f9; padding: 15px; margin: 10px 0; }");
        out.println(".user-info { background-color: #d4edda; padding: 10px; margin: 10px 0; border-radius: 5px; }");
        out.println(".logout-btn { background-color: #dc3545; color: white; padding: 5px 10px; text-decoration: none; border-radius: 3px; }");
        out.println("</style></head><body>");

        out.println("<h1>🏺 " + auction.getTitle() + "</h1>");
        out.println("<a href='/AuctionSystem/auction/'>← Back to Auctions</a><hr>");

        // Show user info if logged in
        if (currentUser != null) {
            showUserInfoBar(request, out, currentUser);
        }

        // Auction Information
        out.println("<div class='auction-info'>");
        out.println("<h3>Auction Details</h3>");
        out.println("<p><strong>Description:</strong> " + auction.getDescription() + "</p>");
        out.println("<p><strong>Starting Price:</strong> $" +
                String.format("%.2f", auction.getStartingPrice()) + "</p>");
        out.println("<p><strong>Current Highest Bid:</strong> $" +
                String.format("%.2f", auction.getCurrentHighestBid()) + "</p>");
        out.println("<p><strong>Highest Bidder:</strong> " +
                (auction.getCurrentHighestBidder() != null ? auction.getCurrentHighestBidder() : "None") + "</p>");
        out.println("<p><strong>End Time:</strong> " + auction.getEndTime().format(formatter) + "</p>");
        out.println("<p><strong>Status:</strong> " + (auction.isActive() ? "🟢 Active" : "🔴 Closed") + "</p>");
        out.println("</div>");

        // Bidding Form (only if auction is active and user is logged in)
        if (auction.isActive() && auction.getEndTime().isAfter(LocalDateTime.now())) {
            if (currentUser != null) {
                out.println("<div class='bid-form'>");
                out.println("<h3>💰 Place Your Bid</h3>");
                out.println("<p>Minimum bid: $" +
                        String.format("%.2f", auction.getCurrentHighestBid() + 5.0) + "</p>");
                out.println("<form method='post' action='/AuctionSystem/auction/bid'>");
                out.println("<input type='hidden' name='auctionId' value='" + auctionId + "'>");
                out.println("<input type='hidden' name='username' value='" + currentUser + "'>");
                out.println("Bid Amount: $<input type='number' name='bidAmount' step='0.01' min='" +
                        (auction.getCurrentHighestBid() + 5.01) + "' required> ");
                out.println("<input type='submit' value='Place Bid'>");
                out.println("</form>");
                out.println("</div>");
            } else {
                out.println("<div class='bid-form'>");
                out.println("<h3>💰 Login Required to Bid</h3>");
                out.println("<p>Please <a href='/AuctionSystem/auction/'>login</a> to place a bid on this auction.</p>");
                out.println("</div>");
            }
        }

        // Bid History
        showBidHistory(out, auctionId);

        out.println("<hr><p><em>Last updated: " + LocalDateTime.now().format(formatter) + "</em></p>");
        out.println("</body></html>");
    }

    private void showBidHistory(PrintWriter out, Long auctionId) {
        out.println("<h3>📈 Bid History</h3>");
        List<Bid> bids = bidService.getBidsForAuction(auctionId);

        if (bids.isEmpty()) {
            out.println("<p>No bids placed yet.</p>");
        } else {
            out.println("<table>");
            out.println("<tr><th>Bid ID</th><th>Bidder</th><th>Amount</th><th>Time</th><th>Status</th></tr>");

            for (Bid bid : bids) {
                out.println("<tr" + (bid.isWinning() ? " style='background-color: #d4edda;'" : "") + ">");
                out.println("<td>" + bid.getBidId() + "</td>");
                out.println("<td>" + bid.getBidderUsername() + "</td>");
                out.println("<td>$" + String.format("%.2f", bid.getBidAmount()) + "</td>");
                out.println("<td>" + bid.getBidTime().format(formatter) + "</td>");
                out.println("<td>" + (bid.isWinning() ? "🏆 Winning" : "Outbid") + "</td>");
                out.println("</tr>");
            }

            out.println("</table>");
        }
    }

    private void showSessionStatus(HttpServletRequest request, PrintWriter out) {
        out.println("<html><head><title>Session Status</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; }");
        out.println("table { border-collapse: collapse; width: 100%; }");
        out.println("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
        out.println("th { background-color: #f2f2f2; }");
        out.println(".status-card { background-color: #f8f9fa; padding: 20px; margin: 10px 0; border-radius: 5px; }");
        out.println("</style></head><body>");

        out.println("<h1>🔐 Session Status</h1>");
        out.println("<a href='/AuctionSystem/auction/'>← Back to Auctions</a><hr>");

        // Session Statistics
        int activeSessionCount = sessionManager.getActiveSessionCount();
        out.println("<div class='status-card'>");
        out.println("<h3>📊 Session Statistics</h3>");
        out.println("<p><strong>Active Sessions:</strong> " + activeSessionCount + "</p>");
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
                    out.println("<p><strong>Login Time:</strong> " + sessionInfo.getLoginTime().format(formatter) + "</p>");
                    out.println("<p><strong>Session Duration:</strong> " + sessionInfo.getSessionDurationMinutes() + " minutes</p>");
                    out.println("<p><strong>Last Activity:</strong> " + sessionInfo.getInactiveDurationMinutes() + " minutes ago</p>");
                    out.println("<p><strong>IP Address:</strong> " + sessionInfo.getIpAddress() + "</p>");
                    out.println("</div>");
                }
            }
        }

        out.println("</body></html>");
    }

    private void showUserProfile(HttpServletRequest request, PrintWriter out) {
        String currentUser = getCurrentUser(request);
        if (currentUser == null) {
            showError(out, "Please login to view your profile.");
            return;
        }

        out.println("<html><head><title>User Profile</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; }");
        out.println("table { border-collapse: collapse; width: 100%; }");
        out.println("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
        out.println("th { background-color: #f2f2f2; }");
        out.println(".profile-card { background-color: #f8f9fa; padding: 20px; margin: 10px 0; border-radius: 5px; }");
        out.println("</style></head><body>");

        out.println("<h1>👤 User Profile</h1>");
        out.println("<a href='/AuctionSystem/auction/'>← Back to Auctions</a><hr>");

        User user = userService.getUserByUsername(currentUser);
        if (user != null) {
            out.println("<div class='profile-card'>");
            out.println("<h3>Profile Information</h3>");
            out.println("<p><strong>Username:</strong> " + user.getUsername() + "</p>");
            out.println("<p><strong>Email:</strong> " + user.getEmail() + "</p>");
            out.println("<p><strong>Last Activity:</strong> " + user.getLastActivity().format(formatter) + "</p>");
            out.println("<p><strong>Status:</strong> " + (user.isActive() ? "🟢 Active" : "🔴 Inactive") + "</p>");
            out.println("</div>");

            // Show user's active sessions
            List<ActiveSessionInfo> userSessions = sessionManager.getActiveSessionsForUser(currentUser);
            out.println("<div class='profile-card'>");
            out.println("<h3>Your Active Sessions (" + userSessions.size() + ")</h3>");

            if (userSessions.isEmpty()) {
                out.println("<p>No active sessions found.</p>");
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

        out.println("</body></html>");
    }

    private void handleBidSubmission(HttpServletRequest request, PrintWriter out) {
        String auctionIdStr = request.getParameter("auctionId");
        String username = request.getParameter("username");
        String bidAmountStr = request.getParameter("bidAmount");

        // Validate session first
        String currentUser = getCurrentUser(request);
        if (currentUser == null || !currentUser.equals(username)) {
            showError(out, "Session expired or invalid. Please login again.");
            return;
        }

        try {
            Long auctionId = Long.parseLong(auctionIdStr);
            double bidAmount = Double.parseDouble(bidAmountStr);

            // Update session activity
            updateSessionActivity(request);

            boolean success = bidService.placeBid(auctionId, username, bidAmount);

            if (success) {
                // Redirect to auction details page
                out.println("<html><head>");
                out.println("<meta http-equiv='refresh' content='2;url=/AuctionSystem/auction/auction/" + auctionId + "'>");
                out.println("</head><body>");
                out.println("<h2>✅ Bid Placed Successfully!</h2>");
                out.println("<p>Your bid of $" + String.format("%.2f", bidAmount) + " has been placed.</p>");
                out.println("<p>Redirecting to auction details...</p>");
                out.println("<a href='/AuctionSystem/auction/auction/" + auctionId + "'>Click here if not redirected</a>");
                out.println("</body></html>");
            } else {
                showError(out, "Bid placement failed. Please check if your bid meets the minimum requirements.");
            }

        } catch (NumberFormatException e) {
            showError(out, "Invalid bid amount or auction ID");
        }
    }

    private void handleUserLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String username = request.getParameter("username");

        if (userService.authenticateUser(username)) {
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

            logger.info("User logged in successfully: " + username);
            response.sendRedirect("/AuctionSystem/auction/");
        } else {
            response.sendRedirect("/AuctionSystem/auction/?error=login_failed");
        }
    }

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

    private void handleUserRegistration(HttpServletRequest request, PrintWriter out) {
        String username = request.getParameter("username");
        String email = request.getParameter("email");

        User user = userService.registerUser(username, email);

        if (user != null) {
            out.println("<html><head>");
            out.println("<meta http-equiv='refresh' content='2;url=/AuctionSystem/auction/'>");
            out.println("</head><body>");
            out.println("<h2>✅ Registration Successful!</h2>");
            out.println("<p>Welcome, " + username + "! You can now login to participate in auctions.</p>");
            out.println("<p>Redirecting to main page...</p>");
            out.println("<a href='/AuctionSystem/auction/'>Click here if not redirected</a>");
            out.println("</body></html>");
        } else {
            showError(out, "Registration failed. Username might already exist.");
        }
    }

    private void handleAuctionCreation(HttpServletRequest request, PrintWriter out) {
        // Validate session first
        String currentUser = getCurrentUser(request);
        if (currentUser == null) {
            showError(out, "Please login to create auctions.");
            return;
        }

        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String startingPriceStr = request.getParameter("startingPrice");
        String durationStr = request.getParameter("duration");

        try {
            double startingPrice = Double.parseDouble(startingPriceStr);
            int durationHours = Integer.parseInt(durationStr);

            LocalDateTime endTime = LocalDateTime.now().plusHours(durationHours);

            // Update session activity
            updateSessionActivity(request);

            AuctionDTO auction = auctionService.createAuction(title, description, startingPrice, endTime);

            if (auction != null) {
                out.println("<html><head>");
                out.println("<meta http-equiv='refresh' content='2;url=/AuctionSystem/auction/auction/" +
                        auction.getAuctionId() + "'>");
                out.println("</head><body>");
                out.println("<h2>✅ Auction Created Successfully!</h2>");
                out.println("<p>Auction '" + title + "' has been created with ID: " + auction.getAuctionId() + "</p>");
                out.println("<p>Redirecting to auction details...</p>");
                out.println("<a href='/AuctionSystem/auction/auction/" + auction.getAuctionId() +
                        "'>Click here if not redirected</a>");
                out.println("</body></html>");
            } else {
                showError(out, "Failed to create auction");
            }

        } catch (NumberFormatException e) {
            showError(out, "Invalid starting price or duration");
        }
    }

    private void showUserList(PrintWriter out) {
        out.println("<html><head><title>Active Users</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; }");
        out.println("table { border-collapse: collapse; width: 100%; }");
        out.println("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
        out.println("th { background-color: #f2f2f2; }");
        out.println("</style></head><body>");

        out.println("<h1>👥 Active Users</h1>");
        out.println("<a href='/AuctionSystem/auction/'>← Back to Auctions</a><hr>");

        List<User> users = userService.getAllActiveUsers();

        if (users.isEmpty()) {
            out.println("<p>No active users found.</p>");
        } else {
            out.println("<table>");
            out.println("<tr><th>Username</th><th>Email</th><th>Last Activity</th><th>Sessions</th></tr>");

            for (User user : users) {
                int userSessionCount = sessionManager.getActiveSessionsForUser(user.getUsername()).size();
                out.println("<tr>");
                out.println("<td>" + user.getUsername() + "</td>");
                out.println("<td>" + user.getEmail() + "</td>");
                out.println("<td>" + user.getLastActivity().format(formatter) + "</td>");
                out.println("<td>" + userSessionCount + " active</td>");
                out.println("</tr>");
            }

            out.println("</table>");
        }

        out.println("</body></html>");
    }

    private void showSystemStatus(PrintWriter out) {
        out.println("<html><head><title>System Status</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; }");
        out.println(".status-card { background-color: #f8f9fa; padding: 20px; margin: 10px 0; border-radius: 5px; }");
        out.println("</style></head><body>");

        out.println("<h1>📊 System Status</h1>");
        out.println("<a href='/AuctionSystem/auction/'>← Back to Auctions</a><hr>");

        // System Statistics
        int activeAuctions = auctionService.getActiveAuctionCount();
        int activeUsers = userService.getActiveUserCount();
        int activeSessions = sessionManager.getActiveSessionCount();
        double totalBidVolume = auctionManager.getTotalBidVolume();

        out.println("<div class='status-card'>");
        out.println("<h3>📈 System Statistics</h3>");
        out.println("<p><strong>Active Auctions:</strong> " + activeAuctions + "</p>");
        out.println("<p><strong>Active Users:</strong> " + activeUsers + "</p>");
        out.println("<p><strong>Active Sessions:</strong> " + activeSessions + "</p>");
        out.println("<p><strong>Total Bid Volume:</strong> $" + String.format("%.2f", totalBidVolume) + "</p>");
        out.println("<p><strong>Server Time:</strong> " + LocalDateTime.now().format(formatter) + "</p>");
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
        out.println("<h3>📡 JMS & Session Status</h3>");
        out.println("<p>✅ Bid Updates Topic - Active</p>");
        out.println("<p>✅ Message Processing - Active</p>");
        out.println("<p>✅ Session Management - Active</p>");
        out.println("<p>✅ Session Security Validation - Active</p>");
        out.println("</div>");

        out.println("</body></html>");
    }

    private void showError(PrintWriter out, String errorMessage) {
        out.println("<html><head><title>Error</title>");
        out.println("<style>body { font-family: Arial, sans-serif; margin: 20px; }</style>");
        out.println("</head><body>");
        out.println("<h1>❌ Error</h1>");
        out.println("<p>" + errorMessage + "</p>");
        out.println("<a href='/AuctionSystem/auction/'>← Back to Auctions</a>");
        out.println("</body></html>");
    }

    // Helper methods for session management
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
}