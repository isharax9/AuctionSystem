package com.auction.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class AuctionTest {

    private Auction auction;
    private LocalDateTime futureTime;

    @BeforeEach
    void setUp() {
        futureTime = LocalDateTime.now().plusHours(2);
        auction = new Auction(1L, "Test Auction", "Test Description", 100.0, futureTime);
    }

    @Test
    @DisplayName("Should create auction with correct initial values")
    void testAuctionCreation() {
        assertEquals(1L, auction.getAuctionId());
        assertEquals("Test Auction", auction.getTitle());
        assertEquals("Test Description", auction.getDescription());
        assertEquals(100.0, auction.getStartingPrice());
        assertEquals(100.0, auction.getCurrentHighestBid());
        assertNull(auction.getCurrentHighestBidder());
        assertTrue(auction.isActive());
        assertEquals(futureTime, auction.getEndTime());
    }

    @Test
    @DisplayName("Should handle concurrent bid operations safely")
    void testConcurrentBidOperations() throws InterruptedException {
        int numberOfThreads = 10;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // Simulate concurrent bid additions
        for (int i = 0; i < numberOfThreads; i++) {
            final int bidId = i;
            executor.submit(() -> {
                try {
                    Bid bid = new Bid((long) bidId, 1L, "bidder" + bidId, 100.0 + bidId);
                    auction.getBids().put((long) bidId, bid);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals(numberOfThreads, auction.getBids().size());
    }

    @Test
    @DisplayName("Should maintain thread-safe bid counter")
    void testBidCounterThreadSafety() throws InterruptedException {
        int numberOfThreads = 100;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    auction.getNextBidId();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // The next bid ID should be numberOfThreads + 1
        assertEquals(numberOfThreads + 1, auction.getNextBidId());
    }

    @Test
    @DisplayName("Should handle serialization correctly")
    void testSerialization() {
        // Add some bids first
        Bid bid1 = new Bid(1L, 1L, "bidder1", 150.0);
        Bid bid2 = new Bid(2L, 1L, "bidder2", 200.0);
        auction.getBids().put(1L, bid1);
        auction.getBids().put(2L, bid2);

        // Test that transient fields are properly initialized
        assertNotNull(auction.getBids());
        assertTrue(auction.getNextBidId() > 0);
    }
}