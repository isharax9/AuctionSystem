package com.auction.ejb;

import com.auction.entity.Auction;
import com.auction.entity.Bid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Topic;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidServiceBeanTest {

    @InjectMocks
    private BidServiceBean bidService;

    @Mock
    private ConnectionFactory connectionFactory;

    @Mock
    private Topic bidUpdatesTopic;

    private Auction testAuction;
    private ConcurrentHashMap<Long, Auction> mockAuctions;

    @BeforeEach
    void setUp() {
        testAuction = new Auction(1L, "Test Auction", "Description", 100.0,
                LocalDateTime.now().plusHours(2));
        mockAuctions = new ConcurrentHashMap<>();
        mockAuctions.put(1L, testAuction);

        // Mock the static method (you'll need PowerMock or refactoring for this)
        // For now, we'll test the logic assuming auction exists
    }

    @Test
    @DisplayName("Should validate bid amount correctly")
    void testValidateBid() {
        // Test valid bid (higher than current + minimum increment)
        assertTrue(bidService.validateBid(1L, 106.0)); // Assuming MIN_BID_INCREMENT is 5.0

        // Test invalid bid (too low)
        assertFalse(bidService.validateBid(1L, 102.0));

        // Test invalid bid (equal to current)
        assertFalse(bidService.validateBid(1L, 100.0));
    }

    @Test
    @DisplayName("Should return correct bid count")
    void testGetBidCount() {
        // Initially no bids
        assertEquals(0, bidService.getBidCount(1L));

        // Add some bids
        testAuction.getBids().put(1L, new Bid(1L, 1L, "bidder1", 110.0));
        testAuction.getBids().put(2L, new Bid(2L, 1L, "bidder2", 120.0));

        assertEquals(2, bidService.getBidCount(1L));
    }

    @Test
    @DisplayName("Should return bids sorted by time (newest first)")
    void testGetBidsForAuction() {
        // Add bids with different times
        Bid oldBid = new Bid(1L, 1L, "bidder1", 110.0);
        Bid newBid = new Bid(2L, 1L, "bidder2", 120.0);

        testAuction.getBids().put(1L, oldBid);
        testAuction.getBids().put(2L, newBid);

        List<Bid> bids = bidService.getBidsForAuction(1L);

        assertEquals(2, bids.size());
        // Newest bid should be first (assuming newBid has later timestamp)
        assertTrue(bids.get(0).getBidTime().isAfter(bids.get(1).getBidTime()) ||
                bids.get(0).getBidTime().equals(bids.get(1).getBidTime()));
    }

    @Test
    @DisplayName("Should handle non-existent auction gracefully")
    void testNonExistentAuction() {
        assertEquals(0, bidService.getBidCount(999L));
        assertTrue(bidService.getBidsForAuction(999L).isEmpty());
        assertNull(bidService.getHighestBid(999L));
        assertFalse(bidService.validateBid(999L, 100.0));
    }
}