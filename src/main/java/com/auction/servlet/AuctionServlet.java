package com.auction.servlet;

import com.auction.dto.AuctionDTO;
import com.auction.dto.BidDTO;
import com.auction.ejb.*;
import com.auction.entity.Auction;
import com.auction.entity.Bid;
import com.auction.entity.User;
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                showAuctionList(out);
            } else if (pathInfo.startsWith("/auction/")) {
                String auctionIdStr = pathInfo.substring("/auction/".length());
                try {
                    Long auctionId = Long.parseLong(auctionIdStr);
                    showAuctionDetails(out, auctionId);
                } catch (NumberFormatException e) {
                    showError(out, "Invalid auction ID");
                }
            } else if (pathInfo.equals("/users")) {
                showUserList(out);
            } else if (pathInfo.equals("/status")) {
                showSystemStatus(out);
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

    private void showAuctionList(PrintWriter out) {
        out.println("<html><head><title>Online Auction System</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; }");
        out.println("table { border-collapse: collapse; width: 100%; }");
        out.println("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
        out.println("th { background-color: #f2f2f2; }");
        out.println(".bid-form { margin: 20px 0; padding: 15px; border: 1px solid #ccc; }");
        out.println(".status { background-color: #e7f3ff; padding: 10px; margin: 10px 0; }");
        out.println("</style></head><body>");

        out.println("<h1>🏺 Online Auction System</h1>");

        // Navigation
        out.println("<nav>");
        out.println("<a href='/AuctionSystem/auction/'>Home</a> | ");
        out.println("<a href='/AuctionSystem/auction/users'>Users</a> | ");
        out.println("<a href='/AuctionSystem/auction/status'>System Status</a>");
        out.println("</nav><hr>");

        // System Status Summary
        int activeAuctions = auctionService.getActiveAuctionCount();
        int activeUsers = userService.getActiveUserCount();
        out.println("<div class='status'>");
        out.println("<strong>System Status:</strong> ");
        out.println(activeAuctions + " active auctions, " + activeUsers + " active users");
        out.println("</div>");

        // User Registration Form
        out.println("<div class='bid-form'>");
        out.println("<h3>Quick User Registration</h3>");
        out.println("<form method='post' action='/AuctionSystem/auction/register'>");
        out.println("Username: <input type='text' name='username' required> ");
        out.println("Email: <input type='email' name='email' required> ");
        out.println("<input type='submit' value='Register'>");
        out.println("</form></div>");

        // Auction Creation Form
        out.println("<div class='bid-form'>");
        out.println("<h3>Create New Auction</h3>");
        out.println("<form method='post' action='/AuctionSystem/auction/create'>");
        out.println("Title: <input type='text' name='title' required><br><br>");
        out.println("Description: <textarea name='description' required></textarea><br><br>");
        out.println("Starting Price: $<input type='number' name='startingPrice' step='0.01' required><br><br>");
        out.println("Duration (hours): <input type='number' name='duration' min='1' max='168' value='24' required><br><br>");
        out.println("<input type='submit' value='Create Auction'>");
        out.println("</form></div>");

        // Active Auctions List
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

        out.println("<hr><p><em>Last updated: " + LocalDateTime.now().format(formatter) + "</em></p>");
        out.println("</body></html>");
    }

    private void showAuctionDetails(PrintWriter out, Long auctionId) {
        AuctionDTO auction = auctionService.getAuction(auctionId);

        if (auction == null) {
            showError(out, "Auction not found");
            return;
        }

        out.println("<html><head><title>Auction: " + auction.getTitle() + "</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; }");
        out.println("table { border-collapse: collapse; width: 100%; }");
        out.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        out.println("th { background-color: #f2f2f2; }");
        out.println(".bid-form { margin: 20px 0; padding: 15px; border: 1px solid #ccc; }");
        out.println(".auction-info { background-color: #f9f9f9; padding: 15px; margin: 10px 0; }");
        out.println("</style></head><body>");

        out.println("<h1>🏺 " + auction.getTitle() + "</h1>");
        out.println("<a href='/AuctionSystem/auction/'>← Back to Auctions</a><hr>");

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

        // Bidding Form (only if auction is active)
        if (auction.isActive() && auction.getEndTime().isAfter(LocalDateTime.now())) {
            out.println("<div class='bid-form'>");
            out.println("<h3>💰 Place Your Bid</h3>");
            out.println("<p>Minimum bid: $" +
                    String.format("%.2f", auction.getCurrentHighestBid() + 5.0) + "</p>");
            out.println("<form method='post' action='/AuctionSystem/auction/bid'>");
            out.println("<input type='hidden' name='auctionId' value='" + auctionId + "'>");
            out.println("Username: <input type='text' name='username' required> ");
            out.println("Bid Amount: $<input type='number' name='bidAmount' step='0.01' min='" +
                    (auction.getCurrentHighestBid() + 5.01) + "' required> ");
            out.println("<input type='submit' value='Place Bid'>");
            out.println("</form>");
            out.println("</div>");
        }

        // Bid History
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

        out.println("<hr><p><em>Last updated: " + LocalDateTime.now().format(formatter) + "</em></p>");
        out.println("</body></html>");
    }

    private void handleBidSubmission(HttpServletRequest request, PrintWriter out) {
        String auctionIdStr = request.getParameter("auctionId");
        String username = request.getParameter("username");
        String bidAmountStr = request.getParameter("bidAmount");

        try {
            Long auctionId = Long.parseLong(auctionIdStr);
            double bidAmount = Double.parseDouble(bidAmountStr);

            // Authenticate user first
            if (!userService.authenticateUser(username)) {
                showError(out, "User authentication failed. Please register first.");
                return;
            }

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
            session.setAttribute("username", username);
            response.sendRedirect("/AuctionSystem/auction/");
        } else {
            response.sendRedirect("/AuctionSystem/auction/?error=login_failed");
        }
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
            out.println("<p>Welcome, " + username + "! You can now participate in auctions.</p>");
            out.println("<p>Redirecting to main page...</p>");
            out.println("<a href='/AuctionSystem/auction/'>Click here if not redirected</a>");
            out.println("</body></html>");
        } else {
            showError(out, "Registration failed. Username might already exist.");
        }
    }

    private void handleAuctionCreation(HttpServletRequest request, PrintWriter out) {
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String startingPriceStr = request.getParameter("startingPrice");
        String durationStr = request.getParameter("duration");

        try {
            double startingPrice = Double.parseDouble(startingPriceStr);
            int durationHours = Integer.parseInt(durationStr);

            LocalDateTime endTime = LocalDateTime.now().plusHours(durationHours);

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
            out.println("<tr><th>Username</th><th>Email</th><th>Last Activity</th></tr>");

            for (User user : users) {
                out.println("<tr>");
                out.println("<td>" + user.getUsername() + "</td>");
                out.println("<td>" + user.getEmail() + "</td>");
                out.println("<td>" + user.getLastActivity().format(formatter) + "</td>");
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
        double totalBidVolume = auctionManager.getTotalBidVolume();

        out.println("<div class='status-card'>");
        out.println("<h3>📈 System Statistics</h3>");
        out.println("<p><strong>Active Auctions:</strong> " + activeAuctions + "</p>");
        out.println("<p><strong>Active Users:</strong> " + activeUsers + "</p>");
        out.println("<p><strong>Total Bid Volume:</strong> $" + String.format("%.2f", totalBidVolume) + "</p>");
        out.println("<p><strong>Server Time:</strong> " + LocalDateTime.now().format(formatter) + "</p>");
        out.println("</div>");

        out.println("<div class='status-card'>");
        out.println("<h3>🔧 EJB Components Status</h3>");
        out.println("<p>✅ AuctionService (Stateless EJB) - Active</p>");
        out.println("<p>✅ BidService (Stateless EJB) - Active</p>");
        out.println("<p>✅ UserService (Stateful EJB) - Active</p>");
        out.println("<p>✅ AuctionManager (Singleton EJB) - Active</p>");
        out.println("<p>✅ BidNotificationMDB (Message-Driven Bean) - Active</p>");
        out.println("</div>");

        out.println("<div class='status-card'>");
        out.println("<h3>📡 JMS Status</h3>");
        out.println("<p>✅ Bid Updates Topic - Active</p>");
        out.println("<p>✅ Message Processing - Active</p>");
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
}