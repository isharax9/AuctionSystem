package com.auction.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

/**
 * Testing the Bid entity - This tests your basic data objects
 * WHY: Ensures your Bid objects work correctly before testing business logic
 */
class BidTest {

    private Bid bid;
    private LocalDateTime testTime;

    @BeforeEach // This runs before EACH test method
    void setUp() {
        testTime = LocalDateTime.now();
        bid = new Bid(1L, 100L, "testuser", 250.50);
    }

    @Test
    @DisplayName("Should create bid with correct initial values")
    void testBidCreation() {
        // WHAT WE'RE TESTING: Constructor and getters work properly
        assertEquals(1L, bid.getBidId());
        assertEquals(100L, bid.getAuctionId());
        assertEquals("testuser", bid.getBidderUsername());
        assertEquals(250.50, bid.getBidAmount());
        assertFalse(bid.isWinning()); // New bids start as not winning
        assertNotNull(bid.getBidTime()); // Time should be set automatically
    }

    @Test
    @DisplayName("Should handle winning status correctly")
    void testWinningStatus() {
        // WHAT WE'RE TESTING: Bid winning status can be changed
        assertFalse(bid.isWinning());

        bid.setWinning(true);
        assertTrue(bid.isWinning());

        bid.setWinning(false);
        assertFalse(bid.isWinning());
    }

    @Test
    @DisplayName("Should handle setters correctly")
    void testSetters() {
        // WHAT WE'RE TESTING: All setters work properly
        bid.setBidId(999L);
        bid.setAuctionId(888L);
        bid.setBidderUsername("newuser");
        bid.setBidAmount(500.75);
        bid.setBidTime(testTime);

        assertEquals(999L, bid.getBidId());
        assertEquals(888L, bid.getAuctionId());
        assertEquals("newuser", bid.getBidderUsername());
        assertEquals(500.75, bid.getBidAmount());
        assertEquals(testTime, bid.getBidTime());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        // WHAT WE'RE TESTING: Your object handles edge cases
        bid.setBidderUsername(null);
        bid.setBidTime(null);

        assertNull(bid.getBidderUsername());
        assertNull(bid.getBidTime());
    }

    @Test
    @DisplayName("Should handle zero and negative amounts")
    void testEdgeCaseAmounts() {
        // WHAT WE'RE TESTING: Edge cases with bid amounts
        bid.setBidAmount(0.0);
        assertEquals(0.0, bid.getBidAmount());

        bid.setBidAmount(-100.0);
        assertEquals(-100.0, bid.getBidAmount());
        // NOTE: Your business logic should prevent negative bids,
        // but the entity should store whatever it's given
    }
}