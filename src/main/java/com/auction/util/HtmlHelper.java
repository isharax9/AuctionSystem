package com.auction.util;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HtmlHelper {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static void writeHtmlHead(PrintWriter out, String title, String... additionalCss) {
        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>" + title + "</title>");

        // Main CSS
        out.println("<link rel='stylesheet' type='text/css' href='/AuctionSystem/css/main.css'>");

        // Additional CSS files
        for (String css : additionalCss) {
            out.println("<link rel='stylesheet' type='text/css' href='/AuctionSystem/css/" + css + "'>");
        }

        out.println("</head>");
        out.println("<body>");
    }

    public static void writeHtmlFooter(PrintWriter out) {
        out.println("<footer>");
        out.println("<div>");
        out.println("<p>&copy; 2025 <strong>Ishara Lakshitha</strong>. All rights reserved.</p>");
        out.println("<p>");
        out.println("<i class=\"fas fa-code\"></i>");
        out.println("Developed by <a href=\"https://github.com/isharax9\" target=\"_blank\">@isharax9</a>");
        out.println("</p>");
        out.println("<p style=\"margin: 0; font-size: 12px; color: #888;\">");
        out.println("Auction System Dashboard | BCD 1 Research Assignment");
        out.println("</p>");
        out.println("</div>");
        out.println("</footer>");
        out.println("</body>");
        out.println("</html>");
    }

    public static void writeErrorPage(PrintWriter out, String errorMessage) {
        writeHtmlHead(out, "Error");
        out.println("<div class='error-container'>");
        out.println("<h1>❌ Error</h1>");
        out.println("<p style='font-size: 18px; color: #721c24;'>" + errorMessage + "</p>");
        out.println("<a href='/AuctionSystem/auction/' class='btn'>← Back to Home</a>");
        out.println("</div>");
        writeHtmlFooter(out);
    }

    public static void writeSuccessPage(PrintWriter out, String title, String message, String redirectUrl, int delaySeconds) {
        writeHtmlHead(out, title);
        out.println("<meta http-equiv='refresh' content='" + delaySeconds + ";url=" + redirectUrl + "'>");
        out.println("<div style='background-color: white; padding: 40px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); display: inline-block; text-align: center; margin: 50px auto; max-width: 600px;'>");
        out.println("<h2>✅ " + title + "</h2>");
        out.println("<p>" + message + "</p>");
        out.println("<p>Redirecting in " + delaySeconds + " seconds...</p>");
        out.println("<a href='" + redirectUrl + "' class='btn'>Go Now</a>");
        out.println("</div>");
        writeHtmlFooter(out);
    }

    public static void writeNavigation(PrintWriter out, String currentUser, boolean isAdmin) {
        out.println("<div class='nav-bar'>");
        out.println("<a href='/AuctionSystem/auction/'>🏠 Home</a>");
        out.println("<a href='/AuctionSystem/auction/users'>👥 Users</a>");
        out.println("<a href='/AuctionSystem/auction/status'>📊 System Status</a>");
        out.println("<a href='/AuctionSystem/auction/sessions'>🔐 Sessions</a>");
        out.println("<a href='/AuctionSystem/real-time-notifications.html' target='_blank'>🔔 Notifications</a>");
        if (currentUser != null) {
            out.println("<a href='/AuctionSystem/auction/profile'>👤 Profile</a>");
            if (isAdmin) {
                out.println("<a href='/AuctionSystem/auction/admin/sessions/' class='admin-link'>🔧 Admin Panel</a>");
            }
        }
        out.println("</div>");
    }
}