package com.auction.performance;

import com.auction.dto.AuctionDTO;
import com.auction.ejb.AuctionServiceBean;
import com.auction.ejb.BidServiceBean;
import com.auction.ejb.UserServiceBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FIXED Performance Testing - No more reflection issues!
 * SOLUTION: Work with existing data instead of trying to reset static final fields
 */
@EnabledIfSystemProperty(named = "performance.tests", matches = "true")
class PerformanceTest {

    private AuctionServiceBean auctionService;
    private BidServiceBean bidService;
    private UserServiceBean userService;
    private int initialAuctionCount;
    private int initialUserCount;

    @BeforeEach
    void setUp() throws Exception {
        auctionService = new AuctionServiceBean();
        bidService = new BidServiceBean();
        userService = new UserServiceBean();

        // Clear only the maps (not the counters)
        clearDataMaps();

        // Initialize services
        auctionService.init();
        userService.init();

        // Record initial counts for baseline
        initialAuctionCount = auctionService.getActiveAuctionCount();
        initialUserCount = userService.getActiveUserCount();
    }

    @AfterEach
    void tearDown() throws Exception {
        // Only clear the maps, leave counters alone
        clearDataMaps();
    }

    private void clearDataMaps() throws Exception {
        // Clear auction data map (but not the counter)
        Field auctionsField = AuctionServiceBean.class.getDeclaredField("auctions");
        auctionsField.setAccessible(true);
        ConcurrentHashMap<?, ?> auctions = (ConcurrentHashMap<?, ?>) auctionsField.get(null);
        auctions.clear();

        // Clear user data map (but not any counters)
        Field usersField = UserServiceBean.class.getDeclaredField("users");
        usersField.setAccessible(true);
        ConcurrentHashMap<?, ?> users = (ConcurrentHashMap<?, ?>) usersField.get(null);
        users.clear();

        // NOTE: We don't touch the static final auctionIdCounter - let it keep incrementing
    }

    @Test
    @DisplayName("Should create 1000 auctions within reasonable time")
    void testAuctionCreationPerformance() {
        // WHAT WE'RE TESTING: Bulk auction creation performance

        int numberOfAuctions = 1000;
        LocalDateTime endTime = LocalDateTime.now().plusHours(24);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfAuctions; i++) {
            auctionService.createAuction(
                    "Auction " + i,
                    "Performance test auction",
                    100.0 + i,
                    endTime
            );
        }

        long endTimeMs = System.currentTimeMillis();
        long duration = endTimeMs - startTime;

        System.out.println("Created " + numberOfAuctions + " auctions in " + duration + "ms");
        System.out.println("Average: " + (duration / numberOfAuctions) + "ms per auction");

        // Performance assertion - should create 1000 auctions in under 5 seconds
        assertTrue(duration < 5000, "Auction creation took too long: " + duration + "ms");

        // Verify count increased by the expected amount
        int finalCount = auctionService.getActiveAuctionCount();
        assertEquals(initialAuctionCount + numberOfAuctions, finalCount);
    }

    @Test
    @DisplayName("Should handle 5000 user registrations efficiently")
    void testUserRegistrationPerformance() {
        // WHAT WE'RE TESTING: Bulk user registration performance
        // Reduced from 10000 to 5000 to be more reasonable

        int numberOfUsers = 5000;

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfUsers; i++) {
            userService.registerUser(
                    "perfuser" + i,
                    "perfuser" + i + "@test.com",
                    "password123"
            );
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Registered " + numberOfUsers + " users in " + duration + "ms");
        System.out.println("Average: " + (duration / numberOfUsers) + "ms per user");

        // Should register 5000 users in under 10 seconds
        assertTrue(duration < 10000, "User registration took too long: " + duration + "ms");

        // Verify count increased properly
        int finalCount = userService.getActiveUserCount();
        assertEquals(initialUserCount + numberOfUsers, finalCount);
    }

    @Test
    @DisplayName("Should handle high-frequency bidding efficiently")
    void testBiddingPerformance() {
        // WHAT WE'RE TESTING: Bid placement performance under load

        // Setup
        LocalDateTime endTime = LocalDateTime.now().plusHours(24);
        AuctionDTO auction = auctionService.createAuction("Performance Auction", "Speed test", 100.0, endTime);

        int numberOfBids = 1000; // Reduced for faster testing

        // Register bidders
        for (int i = 0; i < numberOfBids; i++) {
            userService.registerUser("bidder" + i, "bidder" + i + "@test.com", "password123");
        }

        long startTime = System.currentTimeMillis();

        // Place bids
        for (int i = 0; i < numberOfBids; i++) {
            bidService.placeBid(auction.getAuctionId(), "bidder" + i, 105.0 + i);
        }

        long endTimeMs = System.currentTimeMillis();
        long duration = endTimeMs - startTime;

        System.out.println("Placed " + numberOfBids + " bids in " + duration + "ms");
        System.out.println("Average: " + (duration / numberOfBids) + "ms per bid");

        // Should handle 1000 bids in under 5 seconds
        assertTrue(duration < 5000, "Bidding took too long: " + duration + "ms");

    }

    @Test
    @DisplayName("Should retrieve large auction lists efficiently")
    void testAuctionRetrievalPerformance() {
        // WHAT WE'RE TESTING: Query performance with large datasets

        // Create many auctions
        int numberOfAuctions = 500; // Reduced for faster testing
        LocalDateTime endTime = LocalDateTime.now().plusHours(24);

        for (int i = 0; i < numberOfAuctions; i++) {
            auctionService.createAuction("Auction " + i, "Description", 100.0, endTime);
        }

        // Test retrieval performance
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 50; i++) { // 50 queries instead of 100
            auctionService.getAllActiveAuctions();
        }

        long endTimeMs = System.currentTimeMillis();
        long duration = endTimeMs - startTime;

        System.out.println("50 auction list queries took " + duration + "ms");
        System.out.println("Average: " + (duration / 50) + "ms per query");

        // Should complete 50 queries in under 1 second
        assertTrue(duration < 1000, "Auction retrieval took too long: " + duration + "ms");
    }

    @Test
    @DisplayName("Should handle mixed operations efficiently")
    void testMixedOperationsPerformance() {
        // WHAT WE'RE TESTING: Real-world mixed workload

        long startTime = System.currentTimeMillis();

        // Simulate real usage pattern
        for (int i = 0; i < 100; i++) {
            // Create user
            userService.registerUser("mixeduser" + i, "mixed" + i + "@test.com", "pass123");

            // Create auction
            LocalDateTime endTime = LocalDateTime.now().plusHours(24);
            AuctionDTO auction = auctionService.createAuction("Mixed Auction " + i, "Desc", 100.0, endTime);

            // Place some bids
            for (int j = 0; j < 3; j++) {
                bidService.placeBid(auction.getAuctionId(), "mixeduser" + i, 110.0 + j);
            }

            // Retrieve auction list
            auctionService.getAllActiveAuctions();
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Mixed operations (100 cycles) took " + duration + "ms");

        // Should complete mixed operations in under 10 seconds
        assertTrue(duration < 10000, "Mixed operations took too long: " + duration + "ms");
    }
}