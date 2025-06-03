
package com.auction.integration;

import com.auction.dto.AuctionDTO;
import com.auction.ejb.AuctionServiceBean;
import com.auction.ejb.BidServiceBean;
import com.auction.ejb.UserServiceBean;
import com.auction.entity.Bid;
import com.auction.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * UPDATED Integration Testing - Fixed reflection issue with final fields
 */
class AuctionFlowIntegrationTest {

    private AuctionServiceBean auctionService;
    private BidServiceBean bidService;
    private UserServiceBean userService;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize all services
        auctionService = new AuctionServiceBean();
        bidService = new BidServiceBean();
        userService = new UserServiceBean();

        // Clear static data
        clearStaticData();

        // Initialize services
        auctionService.init();
        userService.init();
    }

    @AfterEach
    void tearDown() throws Exception {
        clearStaticData();
    }

    private void clearStaticData() throws Exception {
        // Clear auction data
        Field auctionsField = AuctionServiceBean.class.getDeclaredField("auctions");
        auctionsField.setAccessible(true);
        ConcurrentHashMap<?, ?> auctions = (ConcurrentHashMap<?, ?>) auctionsField.get(null);
        auctions.clear();

        // Clear user data
        Field usersField = UserServiceBean.class.getDeclaredField("users");
        usersField.setAccessible(true);
        ConcurrentHashMap<?, ?> users = (ConcurrentHashMap<?, ?>) usersField.get(null);
        users.clear();

        // Reset counters - Fixed approach for final fields
        try {
            Field counterField = AuctionServiceBean.class.getDeclaredField("auctionIdCounter");
            counterField.setAccessible(true);

            // Check if field is final
            if (Modifier.isFinal(counterField.getModifiers())) {
                // For final fields, we need to reset the AtomicLong's internal value
                AtomicLong counter = (AtomicLong) counterField.get(null);
                if (counter != null) {
                    counter.set(1L); // Reset to initial value
                }
            } else {
                // For non-final fields, we can replace the entire object
                counterField.set(null, new AtomicLong(1L));
            }
        } catch (IllegalAccessException e) {
            // If we can't reset the counter, that's okay for testing
            // Tests should still work as each test gets unique IDs
            System.out.println("Warning: Could not reset auction ID counter. Tests will use sequential IDs.");
        }
    }

    @Test
    @DisplayName("Complete auction flow - create auction, register users, place bids")
    void testCompleteAuctionFlow() {
        // WHAT WE'RE TESTING: End-to-end auction process

        // Step 1: Register users (FIXED: Using your actual registerUser method)
        User bidder1 = userService.registerUser("bidder1", "bidder1@test.com", "password123");
        User bidder2 = userService.registerUser("bidder2", "bidder2@test.com", "password123");
        User seller = userService.registerUser("seller", "seller@test.com", "password123");

        assertNotNull(bidder1);
        assertNotNull(bidder2);
        assertNotNull(seller);

        // Verify users were created correctly
        assertEquals("bidder1", bidder1.getUsername());
        assertTrue(bidder1.verifyPassword("password123"));

        // Step 2: Create auction
        LocalDateTime endTime = LocalDateTime.now().plusHours(24);
        AuctionDTO auction = auctionService.createAuction(
                "Vintage Watch",
                "Beautiful vintage Rolex",
                500.0,
                endTime
        );

        assertNotNull(auction);
        assertEquals(500.0, auction.getCurrentHighestBid());
        assertNull(auction.getCurrentHighestBidder());

        // Step 3: Authenticate users (simulating login)
        assertTrue(userService.authenticateUser("bidder1", "password123"));
        assertTrue(userService.authenticateUser("bidder2", "password123"));

        // Step 4: Place bids in sequence
        boolean bid1Success = bidService.placeBid(auction.getAuctionId(), "bidder1", 550.0);
        assertTrue(bid1Success);

        boolean bid2Success = bidService.placeBid(auction.getAuctionId(), "bidder2", 600.0);
        assertTrue(bid2Success);

        boolean bid3Success = bidService.placeBid(auction.getAuctionId(), "bidder1", 650.0);
        assertTrue(bid3Success);

        // Step 5: Verify auction state
        AuctionDTO updatedAuction = auctionService.getAuction(auction.getAuctionId());
        assertEquals(650.0, updatedAuction.getCurrentHighestBid());
        assertEquals("bidder1", updatedAuction.getCurrentHighestBidder());

        // Step 6: Verify bid history
        List<Bid> bids = bidService.getBidsForAuction(auction.getAuctionId());
        assertEquals(3, bids.size());

        // Step 7: Verify user properties are maintained
        User retrievedBidder1 = userService.getUserByUsername("bidder1");
        assertNotNull(retrievedBidder1);
        assertTrue(retrievedBidder1.isActive());
        assertFalse(retrievedBidder1.isAdmin());
    }

    @Test
    @DisplayName("Should test user authentication edge cases")
    void testUserAuthenticationEdgeCases() {
        // WHAT WE'RE TESTING: Your authentication logic with various scenarios

        // Register a user
        User testUser = userService.registerUser("testuser", "test@example.com", "mypassword");
        assertNotNull(testUser);

        // Test correct authentication
        assertTrue(userService.authenticateUser("testuser", "mypassword"));

        // Test authentication with email (if your service supports it)
        // Note: This depends on your UserServiceBean implementation
        // assertTrue(userService.authenticateUser("test@example.com", "mypassword"));

        // Test wrong password
        assertFalse(userService.authenticateUser("testuser", "wrongpassword"));

        // Test non-existent user
        assertFalse(userService.authenticateUser("nonexistent", "mypassword"));

        // Test deactivated user
        userService.deactivateUser("testuser");
        assertFalse(userService.authenticateUser("testuser", "mypassword"));
    }

    @Test
    @DisplayName("Should handle password changes correctly")
    void testPasswordChangeFlow() {
        // WHAT WE'RE TESTING: Password change functionality

        // Register user
        User user = userService.registerUser("changeuser", "change@test.com", "oldpassword");
        assertNotNull(user);

        // Verify initial password works
        assertTrue(userService.authenticateUser("changeuser", "oldpassword"));

        // Change password
        boolean changeSuccess = userService.changePassword("changeuser", "oldpassword", "newpassword");
        assertTrue(changeSuccess);

        // Old password should no longer work
        assertFalse(userService.authenticateUser("changeuser", "oldpassword"));

        // New password should work
        assertTrue(userService.authenticateUser("changeuser", "newpassword"));

        // Test wrong old password
        boolean failedChange = userService.changePassword("changeuser", "wrongold", "anothernew");
        assertFalse(failedChange);

        // Current password should still be the last successful change
        assertTrue(userService.authenticateUser("changeuser", "newpassword"));
    }

    @Test
    @DisplayName("Should handle admin users correctly")
    void testAdminUserFlow() {
        // WHAT WE'RE TESTING: Admin functionality

        // Your UserServiceBean.init() creates admin user
        // Check if admin exists and can authenticate
        assertTrue(userService.authenticateUser("admin@auction.com", "11010001"));
        assertTrue(userService.isUserAdmin("admin@auction.com"));

        // Create a regular user
        User regularUser = userService.registerUser("regular", "regular@test.com", "password");
        assertNotNull(regularUser);
        assertFalse(regularUser.isAdmin());
        assertFalse(userService.isUserAdmin("regular"));

        // Test that admin privileges are properly checked
        assertTrue(userService.isUserAdmin("admin@auction.com"));
        assertFalse(userService.isUserAdmin("regular"));
        assertFalse(userService.isUserAdmin("nonexistent"));
    }
}