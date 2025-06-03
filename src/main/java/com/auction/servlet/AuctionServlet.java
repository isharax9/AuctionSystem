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
            } else if (pathInfo.equals("/change-password")) {
                showChangePasswordForm(request, out);
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
            } else if (pathInfo != null && pathInfo.equals("/change-password")) {
                handlePasswordChange(request, response, out);
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
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<style>");
        out.println("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }");
        out.println(".container { max-width: 1200px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        out.println("table { border-collapse: collapse; width: 100%; margin: 20px 0; }");
        out.println("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
        out.println("th { background-color: #007bff; color: white; }");
        out.println(".form-container { margin: 20px 0; padding: 20px; border: 1px solid #ddd; border-radius: 8px; background-color: #f8f9fa; }");
        out.println(".status { background-color: #e7f3ff; padding: 15px; margin: 15px 0; border-radius: 5px; border-left: 4px solid #007bff; }");
        out.println(".user-info { background-color: #d4edda; padding: 15px; margin: 15px 0; border-radius: 5px; border-left: 4px solid #28a745; }");
        out.println(".logout-btn { background-color: #dc3545; color: white; padding: 8px 15px; text-decoration: none; border-radius: 4px; border: none; cursor: pointer; }");
        out.println(".nav-link { margin-right: 20px; color: #007bff; text-decoration: none; font-weight: 500; }");
        out.println(".nav-link:hover { text-decoration: underline; }");
        out.println(".form-group { margin: 15px 0; }");
        out.println(".form-group label { display: block; font-weight: bold; margin-bottom: 5px; color: #333; }");
        out.println(".form-group input, .form-group textarea { padding: 10px; width: 100%; box-sizing: border-box; border: 1px solid #ddd; border-radius: 4px; }");
        out.println(".btn { background-color: #007bff; color: white; padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; text-decoration: none; display: inline-block; }");
        out.println(".btn:hover { background-color: #0056b3; }");
        out.println(".btn-success { background-color: #28a745; }");
        out.println(".btn-success:hover { background-color: #218838; }");
        out.println(".error-msg { color: #721c24; background-color: #f8d7da; padding: 15px; margin: 15px 0; border-radius: 5px; border-left: 4px solid #dc3545; }");
        out.println(".success-msg { color: #155724; background-color: #d4edda; padding: 15px; margin: 15px 0; border-radius: 5px; border-left: 4px solid #28a745; }");
        out.println(".admin-badge { background-color: #dc3545; color: white; padding: 3px 8px; border-radius: 3px; font-size: 12px; margin-left: 10px; }");
        out.println(".admin-link { color: #dc3545 !important; font-weight: bold; }");
        out.println(".header { text-align: center; margin-bottom: 30px; }");
        out.println(".header h1 { color: #333; margin-bottom: 10px; }");
        out.println(".nav-bar { background-color: #343a40; padding: 15px; margin: -20px -20px 20px -20px; border-radius: 10px 10px 0 0; }");
        out.println(".nav-bar a { color: white; text-decoration: none; margin-right: 20px; padding: 8px 12px; border-radius: 4px; }");
        out.println(".nav-bar a:hover { background-color: #495057; }");
        out.println("</style></head><body>");

        out.println("<div class='container'>");

        // Header
        out.println("<div class='header'>");
        out.println("<h1>🏺 Online Auction System</h1>");
        out.println("<p>Welcome to the premier online auction platform</p>");
        out.println("</div>");

        // Show messages if any
        String error = request.getParameter("error");
        String message = request.getParameter("message");

        if (error != null) {
            out.println("<div class='error-msg'>");
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
                default:
                    out.println("❌ " + error);
            }
            out.println("</div>");
        }

        if (message != null) {
            out.println("<div class='success-msg'>");
            switch (message) {
                case "logged_out":
                    out.println("✅ You have been logged out successfully.");
                    break;
                case "password_changed":
                    out.println("✅ Password changed successfully!");
                    break;
                default:
                    out.println("✅ " + message);
            }
            out.println("</div>");
        }

        // Show user session info if logged in
        String currentUser = getCurrentUser(request);
        if (currentUser != null) {
            showUserInfoBar(request, out, currentUser);
        }

        // Navigation
        out.println("<div class='nav-bar'>");
        out.println("<a href='/AuctionSystem/auction/' class='nav-link'>🏠 Home</a>");
        out.println("<a href='/AuctionSystem/auction/users' class='nav-link'>👥 Users</a>");
        out.println("<a href='/AuctionSystem/auction/status' class='nav-link'>📊 System Status</a>");
        out.println("<a href='/AuctionSystem/auction/sessions' class='nav-link'>🔐 Sessions</a>");
        if (currentUser != null) {
            out.println("<a href='/AuctionSystem/auction/profile' class='nav-link'>👤 Profile</a>");
            if (userService.isUserAdmin(currentUser)) {
                out.println("<a href='/AuctionSystem/auction/admin/sessions/' class='nav-link admin-link'>🔧 Admin Panel</a>");
            }
        }
        out.println("</div>");

        // System Status Summary
        int activeAuctions = auctionService.getActiveAuctionCount();
        int activeUsers = userService.getActiveUserCount();
        int activeSessions = sessionManager.getActiveSessionCount();

        out.println("<div class='status'>");
        out.println("<strong>📈 System Status:</strong> ");
        out.println(activeAuctions + " active auctions • " + activeUsers + " registered users • " +
                activeSessions + " active sessions");
        out.println("<br><small>Last updated: " + LocalDateTime.now().format(formatter) + " UTC</small>");
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

        out.println("</div>"); // Close container
        out.println("</body></html>");
    }

    private void showUserInfoBar(HttpServletRequest request, PrintWriter out, String username) {
        HttpSession session = request.getSession(false);
        String sessionToken = session != null ? (String) session.getAttribute("sessionToken") : null;

        out.println("<div class='user-info'>");
        out.println("<strong>👤 Welcome, " + username + "!</strong> ");

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

    private void showLoginForm(PrintWriter out) {
        out.println("<div class='form-container'>");
        out.println("<h3>🔐 User Login</h3>");
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
        out.println("<input type='submit' value='Login' class='btn'>");
        out.println("</div>");
        out.println("</form>");
        out.println("<div style='background-color: #e9ecef; padding: 15px; margin-top: 15px; border-radius: 5px;'>");
        out.println("<p><small><strong>📚 Sample Users:</strong></small></p>");
        out.println("<p><small>• john_doe, jane_smith, bob_wilson, alice_brown (Password: <code>1234</code>)</small></p>");
        out.println("<p><small><strong>🔑 Admin Access:</strong> admin@auction.com (Password: <code>11010001</code>)</small></p>");
        out.println("</div>");
        out.println("</div>");
    }

    private void showRegistrationForm(PrintWriter out) {
        out.println("<div class='form-container'>");
        out.println("<h3>📝 Create New Account</h3>");
        out.println("<form method='post' action='/AuctionSystem/auction/register'>");
        out.println("<div class='form-group'>");
        out.println("<label for='reg_username'>Username:</label>");
        out.println("<input type='text' id='reg_username' name='username' required placeholder='Choose a unique username'>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<label for='reg_email'>Email:</label>");
        out.println("<input type='email' id='reg_email' name='email' required placeholder='Enter your email address'>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<label for='reg_password'>Password:</label>");
        out.println("<input type='password' id='reg_password' name='password' required minlength='4' placeholder='Minimum 4 characters'>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<input type='submit' value='Register' class='btn btn-success'>");
        out.println("</div>");
        out.println("</form>");
        out.println("</div>");
    }

    private void showAuctionCreationForm(PrintWriter out) {
        out.println("<div class='form-container'>");
        out.println("<h3>🎯 Create New Auction</h3>");
        out.println("<form method='post' action='/AuctionSystem/auction/create'>");
        out.println("<div class='form-group'>");
        out.println("<label for='title'>Auction Title:</label>");
        out.println("<input type='text' id='title' name='title' required placeholder='Enter auction title'>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<label for='description'>Description:</label>");
        out.println("<textarea id='description' name='description' required placeholder='Describe the item being auctioned' rows='3'></textarea>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<label for='startingPrice'>Starting Price ($):</label>");
        out.println("<input type='number' id='startingPrice' name='startingPrice' step='0.01' min='0.01' required placeholder='0.00'>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<label for='duration'>Duration (hours):</label>");
        out.println("<input type='number' id='duration' name='duration' min='1' max='168' value='24' required>");
        out.println("<small>Maximum 168 hours (7 days)</small>");
        out.println("</div>");
        out.println("<div class='form-group'>");
        out.println("<input type='submit' value='Create Auction' class='btn btn-success'>");
        out.println("</div>");
        out.println("</form>");
        out.println("</div>");
    }

    private void showActiveAuctionsList(PrintWriter out, boolean isLoggedIn) {
        out.println("<h2>🔥 Active Auctions</h2>");

        List<AuctionDTO> auctions = auctionService.getAllActiveAuctions();

        if (auctions.isEmpty()) {
            out.println("<div style='text-align: center; padding: 40px; background-color: #f8f9fa; border-radius: 8px;'>");
            out.println("<h3>📭 No Active Auctions</h3>");
            out.println("<p>There are currently no active auctions. " + (isLoggedIn ? "Why not create one?" : "Please login to create an auction.") + "</p>");
            out.println("</div>");
        } else {
            out.println("<table>");
            out.println("<tr>");
            out.println("<th>ID</th>");
            out.println("<th>Title</th>");
            out.println("<th>Description</th>");
            out.println("<th>Current Bid</th>");
            out.println("<th>Highest Bidder</th>");
            out.println("<th>End Time</th>");
            out.println("<th>Status</th>");
            out.println("<th>Actions</th>");
            out.println("</tr>");

            for (AuctionDTO auction : auctions) {
                boolean isExpired = auction.getEndTime().isBefore(LocalDateTime.now());
                out.println("<tr" + (isExpired ? " style='background-color: #fff3cd;'" : "") + ">");
                out.println("<td><strong>#" + auction.getAuctionId() + "</strong></td>");
                out.println("<td><strong>" + auction.getTitle() + "</strong></td>");
                out.println("<td>" + (auction.getDescription().length() > 50 ?
                        auction.getDescription().substring(0, 50) + "..." : auction.getDescription()) + "</td>");
                out.println("<td><strong>$" + String.format("%.2f", auction.getCurrentHighestBid()) + "</strong></td>");
                out.println("<td>" + (auction.getCurrentHighestBidder() != null ?
                        "🏆 " + auction.getCurrentHighestBidder() : "📭 No bids yet") + "</td>");
                out.println("<td>" + auction.getEndTime().format(formatter) + "</td>");
                out.println("<td>" + (auction.isActive() && !isExpired ? "🟢 Active" : "🔴 Ended") + "</td>");
                out.println("<td><a href='/AuctionSystem/auction/auction/" + auction.getAuctionId() +
                        "' class='btn' style='padding: 5px 10px; font-size: 14px;'>👁️ View</a></td>");
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
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<style>");
        out.println("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }");
        out.println(".container { max-width: 1000px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        out.println("table { border-collapse: collapse; width: 100%; margin: 20px 0; }");
        out.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        out.println("th { background-color: #007bff; color: white; }");
        out.println(".bid-form { margin: 20px 0; padding: 20px; border: 1px solid #28a745; border-radius: 8px; background-color: #f8fff9; }");
        out.println(".auction-info { background-color: #f8f9fa; padding: 20px; margin: 15px 0; border-radius: 8px; border-left: 4px solid #007bff; }");
        out.println(".user-info { background-color: #d4edda; padding: 15px; margin: 15px 0; border-radius: 5px; border-left: 4px solid #28a745; }");
        out.println(".logout-btn { background-color: #dc3545; color: white; padding: 8px 15px; text-decoration: none; border-radius: 4px; border: none; cursor: pointer; }");
        out.println(".btn { background-color: #007bff; color: white; padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer; text-decoration: none; display: inline-block; }");
        out.println(".btn:hover { background-color: #0056b3; }");
        out.println(".btn-success { background-color: #28a745; }");
        out.println(".btn-success:hover { background-color: #218838; }");
        out.println(".admin-badge { background-color: #dc3545; color: white; padding: 3px 8px; border-radius: 3px; font-size: 12px; margin-left: 10px; }");
        out.println(".status-active { color: #28a745; font-weight: bold; }");
        out.println(".status-ended { color: #dc3545; font-weight: bold; }");
        out.println(".winning-bid { background-color: #d4edda; }");
        out.println("</style></head><body>");

        out.println("<div class='container'>");
        out.println("<h1>🏺 " + auction.getTitle() + "</h1>");
        out.println("<a href='/AuctionSystem/auction/' class='btn'>← Back to Auctions</a><hr>");

        // Show user info if logged in
        if (currentUser != null) {
            showUserInfoBar(request, out, currentUser);
        }

        // Auction Information
        boolean isExpired = auction.getEndTime().isBefore(LocalDateTime.now());
        out.println("<div class='auction-info'>");
        out.println("<h3>📋 Auction Details</h3>");
        out.println("<p><strong>Description:</strong> " + auction.getDescription() + "</p>");
        out.println("<p><strong>Starting Price:</strong> $" + String.format("%.2f", auction.getStartingPrice()) + "</p>");
        out.println("<p><strong>Current Highest Bid:</strong> <span style='font-size: 1.2em; color: #28a745;'><strong>$" +
                String.format("%.2f", auction.getCurrentHighestBid()) + "</strong></span></p>");
        out.println("<p><strong>Highest Bidder:</strong> " +
                (auction.getCurrentHighestBidder() != null ? "🏆 " + auction.getCurrentHighestBidder() : "📭 No bids yet") + "</p>");
        out.println("<p><strong>End Time:</strong> " + auction.getEndTime().format(formatter) + " UTC</p>");
        out.println("<p><strong>Status:</strong> <span class='" + (auction.isActive() && !isExpired ? "status-active" : "status-ended") + "'>" +
                (auction.isActive() && !isExpired ? "🟢 Active" : "🔴 Ended") + "</span></p>");
        out.println("</div>");

        // Bidding Form (only if auction is active and user is logged in)
        if (auction.isActive() && !isExpired) {
            if (currentUser != null) {
                double minimumBid = auction.getCurrentHighestBid() + 5.0;
                out.println("<div class='bid-form'>");
                out.println("<h3>💰 Place Your Bid</h3>");
                out.println("<p><strong>Minimum bid:</strong> $" + String.format("%.2f", minimumBid) + "</p>");
                out.println("<form method='post' action='/AuctionSystem/auction/bid'>");
                out.println("<input type='hidden' name='auctionId' value='" + auctionId + "'>");
                out.println("<input type='hidden' name='username' value='" + currentUser + "'>");
                out.println("<div style='display: flex; align-items: center; gap: 10px;'>");
                out.println("<label for='bidAmount'><strong>Bid Amount: $</strong></label>");
                out.println("<input type='number' id='bidAmount' name='bidAmount' step='0.01' min='" +
                        (minimumBid + 0.01) + "' required style='padding: 10px; border: 1px solid #ddd; border-radius: 4px;'>");
                out.println("<input type='submit' value='Place Bid' class='btn btn-success'>");
                out.println("</div>");
                out.println("</form>");
                out.println("</div>");
            } else {
                out.println("<div class='bid-form'>");
                out.println("<h3>💰 Login Required to Bid</h3>");
                out.println("<p>Please <a href='/AuctionSystem/auction/' class='btn'>login</a> to place a bid on this auction.</p>");
                out.println("</div>");
            }
        } else if (isExpired) {
            out.println("<div style='background-color: #f8d7da; padding: 20px; margin: 20px 0; border-radius: 8px; border-left: 4px solid #dc3545;'>");
            out.println("<h3>⏰ Auction Ended</h3>");
            out.println("<p>This auction has ended. No more bids can be placed.</p>");
            if (auction.getCurrentHighestBidder() != null) {
                out.println("<p><strong>Winner:</strong> 🏆 " + auction.getCurrentHighestBidder() +
                        " with a bid of $" + String.format("%.2f", auction.getCurrentHighestBid()) + "</p>");
            }
            out.println("</div>");
        }

        // Bid History
        showBidHistory(out, auctionId);

        out.println("</div>"); // Close container
        out.println("</body></html>");
    }

    private void showBidHistory(PrintWriter out, Long auctionId) {
        out.println("<h3>📈 Bid History</h3>");
        List<Bid> bids = bidService.getBidsForAuction(auctionId);

        if (bids.isEmpty()) {
            out.println("<div style='text-align: center; padding: 20px; background-color: #f8f9fa; border-radius: 8px;'>");
            out.println("<p>📭 No bids have been placed on this auction yet.</p>");
            out.println("</div>");
        } else {
            out.println("<table>");
            out.println("<tr><th>Bid #</th><th>Bidder</th><th>Amount</th><th>Time</th><th>Status</th></tr>");

            for (Bid bid : bids) {
                out.println("<tr" + (bid.isWinning() ? " class='winning-bid'" : "") + ">");
                out.println("<td><strong>#" + bid.getBidId() + "</strong></td>");
                out.println("<td>" + bid.getBidderUsername() + "</td>");
                out.println("<td><strong>$" + String.format("%.2f", bid.getBidAmount()) + "</strong></td>");
                out.println("<td>" + bid.getBidTime().format(formatter) + "</td>");
                out.println("<td>" + (bid.isWinning() ? "🏆 <strong>Winning</strong>" : "⚪ Outbid") + "</td>");
                out.println("</tr>");
            }

            out.println("</table>");
        }
    }

    private void showChangePasswordForm(HttpServletRequest request, PrintWriter out) {
        String currentUser = getCurrentUser(request);
        if (currentUser == null) {
            showError(out, "Please login to change your password.");
            return;
        }

        out.println("<html><head><title>Change Password</title>");
        out.println("<meta charset='UTF-8'>");
        out.println("<style>");
        out.println("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }");
        out.println(".container { max-width: 500px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        out.println(".form-group { margin: 20px 0; }");
        out.println(".form-group label { display: block; font-weight: bold; margin-bottom: 8px; color: #333; }");
        out.println(".form-group input { padding: 12px; width: 100%; box-sizing: border-box; border: 1px solid #ddd; border-radius: 4px; }");
        out.println(".btn { background-color: #007bff; color: white; padding: 12px 24px; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; width: 100%; }");
        out.println(".btn:hover { background-color: #0056b3; }");
        out.println(".error-msg { color: #721c24; background-color: #f8d7da; padding: 15px; margin: 15px 0; border-radius: 5px; border-left: 4px solid #dc3545; }");
        out.println("</style></head><body>");

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

    private void handlePasswordChange(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        String currentUser = getCurrentUser(request);
        if (currentUser == null) {
            showError(out, "Please login to change your password.");
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

    private void showSessionStatus(HttpServletRequest request, PrintWriter out) {
        out.println("<html><head><title>Session Status</title>");
        out.println("<meta charset='UTF-8'>");
        out.println("<style>");
        out.println("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }");
        out.println(".container { max-width: 800px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        out.println(".status-card { background-color: #f8f9fa; padding: 20px; margin: 15px 0; border-radius: 8px; border-left: 4px solid #007bff; }");
        out.println(".btn { background-color: #007bff; color: white; padding: 8px 16px; text-decoration: none; border-radius: 4px; }");
        out.println("</style></head><body>");

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

    private void showUserProfile(HttpServletRequest request, PrintWriter out) {
        String currentUser = getCurrentUser(request);
        if (currentUser == null) {
            showError(out, "Please login to view your profile.");
            return;
        }

        out.println("<html><head><title>User Profile</title>");
        out.println("<meta charset='UTF-8'>");
        out.println("<style>");
        out.println("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }");
        out.println(".container { max-width: 800px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        out.println("table { border-collapse: collapse; width: 100%; }");
        out.println("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
        out.println("th { background-color: #007bff; color: white; }");
        out.println(".profile-card { background-color: #f8f9fa; padding: 20px; margin: 15px 0; border-radius: 8px; border-left: 4px solid #007bff; }");
        out.println(".btn { background-color: #007bff; color: white; padding: 8px 16px; text-decoration: none; border-radius: 4px; }");
        out.println(".admin-badge { background-color: #dc3545; color: white; padding: 3px 8px; border-radius: 3px; font-size: 12px; }");
        out.println("</style></head><body>");

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
                out.println("<meta http-equiv='refresh' content='3;url=/AuctionSystem/auction/auction/" + auctionId + "'>");
                out.println("<style>body { font-family: Arial, sans-serif; text-align: center; padding: 50px; background-color: #f5f5f5; }</style>");
                out.println("</head><body>");
                out.println("<div style='background-color: white; padding: 40px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); display: inline-block;'>");
                out.println("<h2>✅ Bid Placed Successfully!</h2>");
                out.println("<p>Your bid of <strong>$" + String.format("%.2f", bidAmount) + "</strong> has been placed on auction #" + auctionId + ".</p>");
                out.println("<p>Redirecting to auction details...</p>");
                out.println("<a href='/AuctionSystem/auction/auction/" + auctionId + "' style='background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px;'>View Auction Now</a>");
                out.println("</div>");
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
            response.sendRedirect("/AuctionSystem/auction/");
        } else {
            logger.warning("Failed login attempt for user: " + username);
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
        String password = request.getParameter("password");

        if (username == null || email == null || password == null ||
                username.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
            showError(out, "All fields are required for registration.");
            return;
        }

        if (password.length() < 4) {
            showError(out, "Password must be at least 4 characters long.");
            return;
        }

        User user = userService.registerUser(username, email, password);

        if (user != null) {
            out.println("<html><head>");
            out.println("<meta http-equiv='refresh' content='3;url=/AuctionSystem/auction/'>");
            out.println("<style>body { font-family: Arial, sans-serif; text-align: center; padding: 50px; background-color: #f5f5f5; }</style>");
            out.println("</head><body>");
            out.println("<div style='background-color: white; padding: 40px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); display: inline-block;'>");
            out.println("<h2>✅ Registration Successful!</h2>");
            out.println("<p>Welcome, <strong>" + username + "</strong>! Your account has been created successfully.</p>");
            out.println("<p>You can now login with your credentials to participate in auctions.</p>");
            out.println("<p>Redirecting to main page...</p>");
            out.println("<a href='/AuctionSystem/auction/' style='background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px;'>Go to Main Page</a>");
            out.println("</div>");
            out.println("</body></html>");
        } else {
            showError(out, "Registration failed. Username might already exist or password requirements not met.");
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

        if (title == null || description == null || startingPriceStr == null || durationStr == null ||
                title.trim().isEmpty() || description.trim().isEmpty()) {
            showError(out, "All fields are required to create an auction.");
            return;
        }

        try {
            double startingPrice = Double.parseDouble(startingPriceStr);
            int durationHours = Integer.parseInt(durationStr);

            if (startingPrice <= 0) {
                showError(out, "Starting price must be greater than 0.");
                return;
            }

            if (durationHours < 1 || durationHours > 168) {
                showError(out, "Duration must be between 1 and 168 hours (7 days).");
                return;
            }

            LocalDateTime endTime = LocalDateTime.now().plusHours(durationHours);

            // Update session activity
            updateSessionActivity(request);

            AuctionDTO auction = auctionService.createAuction(title, description, startingPrice, endTime);

            if (auction != null) {
                out.println("<html><head>");
                out.println("<meta http-equiv='refresh' content='3;url=/AuctionSystem/auction/auction/" +
                        auction.getAuctionId() + "'>");
                out.println("<style>body { font-family: Arial, sans-serif; text-align: center; padding: 50px; background-color: #f5f5f5; }</style>");
                out.println("</head><body>");
                out.println("<div style='background-color: white; padding: 40px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); display: inline-block;'>");
                out.println("<h2>✅ Auction Created Successfully!</h2>");
                out.println("<p>Auction '<strong>" + title + "</strong>' has been created with ID: <strong>#" + auction.getAuctionId() + "</strong></p>");
                out.println("<p>Starting price: $" + String.format("%.2f", startingPrice) + "</p>");
                out.println("<p>Duration: " + durationHours + " hours</p>");
                out.println("<p>Redirecting to auction details...</p>");
                out.println("<a href='/AuctionSystem/auction/auction/" + auction.getAuctionId() +
                        "' style='background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px;'>View Auction</a>");
                out.println("</div>");
                out.println("</body></html>");
            } else {
                showError(out, "Failed to create auction. Please try again.");
            }

        } catch (NumberFormatException e) {
            showError(out, "Invalid starting price or duration. Please enter valid numbers.");
        }
    }

    private void showUserList(PrintWriter out) {
        out.println("<html><head><title>Active Users</title>");
        out.println("<meta charset='UTF-8'>");
        out.println("<style>");
        out.println("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }");
        out.println(".container { max-width: 1000px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        out.println("table { border-collapse: collapse; width: 100%; }");
        out.println("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
        out.println("th { background-color: #007bff; color: white; }");
        out.println(".btn { background-color: #007bff; color: white; padding: 8px 16px; text-decoration: none; border-radius: 4px; }");
        out.println(".admin-badge { background-color: #dc3545; color: white; padding: 3px 8px; border-radius: 3px; font-size: 12px; }");
        out.println("</style></head><body>");

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

    private void showSystemStatus(PrintWriter out) {
        out.println("<html><head><title>System Status</title>");
        out.println("<meta charset='UTF-8'>");
        out.println("<style>");
        out.println("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }");
        out.println(".container { max-width: 1000px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        out.println(".status-card { background-color: #f8f9fa; padding: 20px; margin: 15px 0; border-radius: 8px; border-left: 4px solid #007bff; }");
        out.println(".btn { background-color: #007bff; color: white; padding: 8px 16px; text-decoration: none; border-radius: 4px; }");
        out.println("</style></head><body>");

        out.println("<div class='container'>");
        out.println("<h1>📊 System Status</h1>");
        out.println("<a href='/AuctionSystem/auction/' class='btn'>← Back to Auctions</a><hr>");

        // System Statistics
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
        out.println("</body></html>");
    }

    private void showError(PrintWriter out, String errorMessage) {
        out.println("<html><head><title>Error</title>");
        out.println("<meta charset='UTF-8'>");
        out.println("<style>");
        out.println("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; text-align: center; }");
        out.println(".error-container { max-width: 600px; margin: 50px auto; background-color: white; padding: 40px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); border-left: 4px solid #dc3545; }");
        out.println(".btn { background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px; display: inline-block; margin-top: 20px; }");
        out.println("</style></head><body>");
        out.println("<div class='error-container'>");
        out.println("<h1>❌ Error</h1>");
        out.println("<p style='font-size: 18px; color: #721c24;'>" + errorMessage + "</p>");
        out.println("<a href='/AuctionSystem/auction/' class='btn'>← Back to Home</a>");
        out.println("</div>");
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