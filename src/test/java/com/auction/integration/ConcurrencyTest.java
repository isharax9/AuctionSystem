package com.auction.integration;

import com.auction.dto.AuctionDTO;
import com.auction.ejb.AuctionServiceBean;
import com.auction.ejb.BidServiceBean;
import com.auction.ejb.UserServiceBean;
import com.auction.entity.Bid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Concurrency Testing - Testing thread safety under load
 * WHY: Your auction system will have multiple users bidding simultaneously
 *      Thread safety bugs can cause: lost bids, incorrect winners, data corruption
 *
 * CRITICAL: These tests simulate real-world concurrent usage
 */
class ConcurrencyTest {

    private AuctionServiceBean auctionService;
    private BidServiceBean bidService;
    private UserServiceBean userService;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() throws Exception {
        auctionService = new AuctionServiceBean();
        bidService = new BidServiceBean();
        userService = new UserServiceBean();
        executorService = Executors.newFixedThreadPool(20);

        // Clear and initialize
        clearStaticData();
        auctionService.init();
        userService.init();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (executorService != null) {
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        }
        clearStaticData();
    }

    private void clearStaticData() throws Exception {
        // Clear auctions map
        Field auctionsField = AuctionServiceBean.class.getDeclaredField("auctions");
        auctionsField.setAccessible(true);
        ConcurrentHashMap<?, ?> auctions = (ConcurrentHashMap<?, ?>) auctionsField.get(null);
        auctions.clear();

        // Clear users map
        Field usersField = UserServiceBean.class.getDeclaredField("users");
        usersField.setAccessible(true);
        ConcurrentHashMap<?, ?> users = (ConcurrentHashMap<?, ?>) usersField.get(null);
        users.clear();

        // Reset the counter value instead of replacing the field
        Field counterField = AuctionServiceBean.class.getDeclaredField("auctionIdCounter");
        counterField.setAccessible(true);
        AtomicLong counter = (AtomicLong) counterField.get(null);
        counter.set(1); // Reset the value to 1
    }

    @Test
    @DisplayName("Should handle concurrent bid placement correctly")
    void testConcurrentBidPlacement() throws InterruptedException {
        // WHAT WE'RE TESTING: Race conditions in bid placement
        // SCENARIO: Multiple users trying to bid at exactly the same time

        // Setup
        LocalDateTime endTime = LocalDateTime.now().plusHours(24);
        AuctionDTO auction = auctionService.createAuction("Hot Item", "Everyone wants this", 100.0, endTime);

        int numberOfBidders = 50;
        CountDownLatch startLatch = new CountDownLatch(1); // Synchronize start
        CountDownLatch doneLatch = new CountDownLatch(numberOfBidders);
        AtomicInteger successfulBids = new AtomicInteger(0);

        // Create bidders with properly spaced bid amounts
        for (int i = 0; i < numberOfBidders; i++) {
            final int bidderNum = i;

            executorService.submit(() -> {
                try {
                    String username = "bidder" + bidderNum;
                    userService.registerUser(username, username + "@test.com", "password123");

                    // Wait for all threads to be ready
                    startLatch.await();

                    // Each bidder places a different amount to avoid bid validation conflicts
                    // Starting from 110 (above starting price + min increment) and spacing by 10
                    double bidAmount = 110.0 + (bidderNum * 10);
                    boolean success = bidService.placeBid(auction.getAuctionId(), username, bidAmount);
                    if (success) {
                        successfulBids.incrementAndGet();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Start all bidders simultaneously
        startLatch.countDown();

        // Wait for all to complete
        assertTrue(doneLatch.await(30, TimeUnit.SECONDS));

        // Verify results - focus on what we can guarantee
        AuctionDTO finalAuction = auctionService.getAuction(auction.getAuctionId());

        // Verify basic constraints
        assertNotNull(finalAuction);
        assertTrue(finalAuction.getCurrentHighestBid() >= 110.0); // At least the minimum bid
        assertTrue(finalAuction.getCurrentHighestBid() <= 110.0 + ((numberOfBidders - 1) * 10)); // At most the maximum bid
        assertNotNull(finalAuction.getCurrentHighestBidder());
        assertTrue(finalAuction.getCurrentHighestBidder().startsWith("bidder"));

        // All bids should be successful since they have different amounts with proper spacing
        for (int i : new int[]{successfulBids.get(), bidService.getBidCount(auction.getAuctionId())}) {
            assertEquals(numberOfBidders, i);
        }

        // Verify all bids are stored correctly
        List<Bid> allBids = bidService.getBidsForAuction(auction.getAuctionId());
        assertEquals(numberOfBidders, allBids.size());

        // The highest bid should be the actual highest amount
        double expectedHighestBid = 110.0 + ((numberOfBidders - 1) * 10);
        assertEquals(expectedHighestBid, finalAuction.getCurrentHighestBid(), 0.01);
    }

    @Test
    @DisplayName("Should handle concurrent auction creation")
    void testConcurrentAuctionCreation() throws InterruptedException {
        // WHAT WE'RE TESTING: Thread safety of auction ID generation and storage

        int numberOfAuctions = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfAuctions);
        ConcurrentHashMap<Long, String> createdAuctions = new ConcurrentHashMap<>();

        LocalDateTime endTime = LocalDateTime.now().plusHours(24);

        for (int i = 0; i < numberOfAuctions; i++) {
            final int auctionNum = i;

            executorService.submit(() -> {
                try {
                    startLatch.await();

                    String title = "Auction " + auctionNum;
                    AuctionDTO auction = auctionService.createAuction(
                            title,
                            "Description " + auctionNum,
                            100.0 + auctionNum,
                            endTime
                    );

                    if (auction != null) {
                        createdAuctions.put(auction.getAuctionId(), title);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(30, TimeUnit.SECONDS));

        // Verify all auctions were created with unique IDs
        assertEquals(numberOfAuctions, createdAuctions.size());
        assertEquals(numberOfAuctions, auctionService.getActiveAuctionCount());

        // Verify all auction IDs are unique (no overwrites)
        List<AuctionDTO> allAuctions = auctionService.getAllActiveAuctions();
        assertEquals(numberOfAuctions, allAuctions.stream().mapToLong(AuctionDTO::getAuctionId).distinct().count());
    }

    @Test
    @DisplayName("Should handle concurrent user registration")
    void testConcurrentUserRegistration() throws InterruptedException {
        // WHAT WE'RE TESTING: Thread safety of user creation and duplicate prevention

        int numberOfUsers = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfUsers);
        AtomicInteger successfulRegistrations = new AtomicInteger(0);

        for (int i = 0; i < numberOfUsers; i++) {
            final int userNum = i;

            executorService.submit(() -> {
                try {
                    startLatch.await();

                    String username = "user" + userNum;
                    String email = "user" + userNum + "@test.com";

                    if (userService.registerUser(username, email, "password123") != null) {
                        successfulRegistrations.incrementAndGet();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(30, TimeUnit.SECONDS));

        // All registrations should succeed (unique usernames/emails)
        assertEquals(numberOfUsers, successfulRegistrations.get());
    }

    @Test
    @DisplayName("Should prevent duplicate registrations under concurrency")
    void testConcurrentDuplicateRegistration() throws InterruptedException {
        // WHAT WE'RE TESTING: Duplicate prevention under race conditions

        int numberOfAttempts = 50;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfAttempts);
        AtomicInteger successfulRegistrations = new AtomicInteger(0);

        // All threads try to register the same username
        String duplicateUsername = "duplicateuser";
        String duplicateEmail = "duplicate@test.com";

        for (int i = 0; i < numberOfAttempts; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();

                    if (userService.registerUser(duplicateUsername, duplicateEmail, "password123") != null) {
                        successfulRegistrations.incrementAndGet();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(30, TimeUnit.SECONDS));

        // Only ONE registration should succeed
        assertEquals(1, successfulRegistrations.get());
    }

    @Test
    @DisplayName("Should maintain data consistency under high concurrent load")
    void testHighConcurrencyDataConsistency() throws InterruptedException {
        // WHAT WE'RE TESTING: Overall system stability under a heavy load

        // Create base auction
        LocalDateTime endTime = LocalDateTime.now().plusHours(24);
        AuctionDTO auction = auctionService.createAuction("Stress Test Auction", "High load test", 100.0, endTime);

        int numberOfOperations = 200;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfOperations);

        AtomicInteger bidAttempts = new AtomicInteger(0);
        AtomicInteger successfulBids = new AtomicInteger(0);

        for (int i = 0; i < numberOfOperations; i++) {
            final int operationNum = i;

            executorService.submit(() -> {
                try {
                    startLatch.await();

                    String username = "stressuser" + operationNum;

                    // Register user
                    userService.registerUser(username, username + "@test.com", "password123");

                    // Try to place bid with proper spacing to ensure validation passes
                    bidAttempts.incrementAndGet();
                    double bidAmount = 110.0 + (operationNum * 5); // Space bids by $5
                    if (bidService.placeBid(auction.getAuctionId(), username, bidAmount)) {
                        successfulBids.incrementAndGet();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(60, TimeUnit.SECONDS));

        // Verify system consistency
        assertEquals(numberOfOperations, bidAttempts.get());
        assertEquals(numberOfOperations, successfulBids.get()); // All should succeed with different amounts

        AuctionDTO finalAuction = auctionService.getAuction(auction.getAuctionId());
        assertNotNull(finalAuction);

        // Verify the highest bid is within expected range
        double expectedHighestBid = 110.0 + ((numberOfOperations - 1) * 5);
        assertEquals(expectedHighestBid, finalAuction.getCurrentHighestBid(), 0.01);

        // Verify winner is one of our stress test users
        assertTrue(finalAuction.getCurrentHighestBidder().startsWith("stressuser"));

        // Bid count should match
        assertEquals(numberOfOperations, bidService.getBidCount(auction.getAuctionId()));

        // All bids should be properly stored
        List<Bid> allBids = bidService.getBidsForAuction(auction.getAuctionId());
        assertEquals(numberOfOperations, allBids.size());
    }

    @Test
    @DisplayName("Should handle concurrent same-amount bid attempts correctly")
    void testConcurrentSameBidAmount() throws InterruptedException {
        // WHAT WE'RE TESTING: Handling of identical bid amounts under concurrency
        // SCENARIO: Multiple users trying to bid the exact same amount

        LocalDateTime endTime = LocalDateTime.now().plusHours(24);
        AuctionDTO auction = auctionService.createAuction("Same Bid Test", "Testing identical bids", 100.0, endTime);

        int numberOfBidders = 20;
        double sameBidAmount = 150.0;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfBidders);
        AtomicInteger successfulBids = new AtomicInteger(0);

        for (int i = 0; i < numberOfBidders; i++) {
            final int bidderNum = i;

            executorService.submit(() -> {
                try {
                    String username = "samebidder" + bidderNum;
                    userService.registerUser(username, username + "@test.com", "password123");

                    startLatch.await();

                    // All try to bid the same amount
                    boolean success = bidService.placeBid(auction.getAuctionId(), username, sameBidAmount);
                    if (success) {
                        successfulBids.incrementAndGet();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(30, TimeUnit.SECONDS));

        // Only one bid should succeed (first one to get through)
        assertEquals(1, successfulBids.get());
        assertEquals(1, bidService.getBidCount(auction.getAuctionId()));

        AuctionDTO finalAuction = auctionService.getAuction(auction.getAuctionId());
        assertEquals(sameBidAmount, finalAuction.getCurrentHighestBid(), 0.01);
        assertTrue(finalAuction.getCurrentHighestBidder().startsWith("samebidder"));
    }
}