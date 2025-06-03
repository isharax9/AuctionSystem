package com.auction.ejb;

import com.auction.dto.AuctionDTO;
import com.auction.entity.Auction;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testing AuctionServiceBean - This is your main business logic for auctions
 * WHY: This is where auction creation, retrieval, and management happens
 *
 * CHALLENGE: EJB testing is tricky because of static fields and @PostConstruct
 * SOLUTION: We'll use reflection to access private static fields
 */
class AuctionServiceBeanTest {

    private AuctionServiceBean auctionService;
    private ConcurrentHashMap<Long, Auction> originalAuctions;

    @BeforeEach
    void setUp() throws Exception {
        auctionService = new AuctionServiceBean();

        // ADVANCED TECHNIQUE: Access private static field using reflection
        // WHY: We need to clean up static data between tests
        Field auctionsField = AuctionServiceBean.class.getDeclaredField("auctions");
        auctionsField.setAccessible(true);
        originalAuctions = (ConcurrentHashMap<Long, Auction>) auctionsField.get(null);

        // Clear any existing auctions for clean test
        originalAuctions.clear();

        // Reset counter using the new method instead of reflection
        AuctionServiceBean.resetCounterForTesting();

        // Manually call @PostConstruct method
        auctionService.init();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        originalAuctions.clear();
    }

    @Test
    @DisplayName("Should create auction successfully")
    void testCreateAuction() {
        // WHAT WE'RE TESTING: Auction creation works correctly
        String title = "Test Auction";
        String description = "Test Description";
        double startingPrice = 100.0;
        LocalDateTime endTime = LocalDateTime.now().plusHours(24);

        AuctionDTO result = auctionService.createAuction(title, description, startingPrice, endTime);

        // Verify the returned DTO
        Assertions.assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(description, result.getDescription());
        assertEquals(startingPrice, result.getStartingPrice());
        assertEquals(startingPrice, result.getCurrentHighestBid()); // Should start with starting price
        Assertions.assertNull(result.getCurrentHighestBidder()); // No bidder initially
        Assertions.assertTrue(result.isActive());
        assertEquals(endTime, result.getEndTime());

        // Verify auction was stored

    }

    @Test
    @DisplayName("Should retrieve auction by ID")
    void testGetAuction() {
        // WHAT WE'RE TESTING: Auction retrieval works

        // First create an auction
        LocalDateTime endTime = LocalDateTime.now().plusHours(24);
        AuctionDTO created = auctionService.createAuction("Test", "Description", 100.0, endTime);

        // Then retrieve it
        AuctionDTO retrieved = auctionService.getAuction(created.getAuctionId());

        Assertions.assertNotNull(retrieved);
        assertEquals(created.getAuctionId(), retrieved.getAuctionId());
        assertEquals("Test", retrieved.getTitle());
    }

    @Test
    @DisplayName("Should return null for non-existent auction")
    void testGetNonExistentAuction() {
        // WHAT WE'RE TESTING: Graceful handling of missing data
        AuctionDTO result = auctionService.getAuction(999L);
        Assertions.assertNull(result);
    }

    @Test
    @DisplayName("Should return only active auctions")
    void testGetAllActiveAuctions() {
        // WHAT WE'RE TESTING: Filtering logic works correctly

        // Create active auction
        LocalDateTime futureTime = LocalDateTime.now().plusHours(24);
        auctionService.createAuction("Active Auction", "Description", 100.0, futureTime);

        // Create expired auction
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        auctionService.createAuction("Expired Auction", "Description", 100.0, pastTime);

        List<AuctionDTO> activeAuctions = auctionService.getAllActiveAuctions();

        // Only the active auction should be returned


    }

    @Test
    @DisplayName("Should close auction successfully")
    void testCloseAuction() {
        // WHAT WE'RE TESTING: Auction can be manually closed

        LocalDateTime endTime = LocalDateTime.now().plusHours(24);
        AuctionDTO created = auctionService.createAuction("Test", "Description", 100.0, endTime);

        boolean result = auctionService.closeAuction(created.getAuctionId());
        Assertions.assertTrue(result);

        // Verify auction is no longer active
        Assertions.assertFalse(auctionService.isAuctionActive(created.getAuctionId()));
    }

    @Test
    @DisplayName("Should return false when closing non-existent auction")
    void testCloseNonExistentAuction() {
        // WHAT WE'RE TESTING: Error handling for invalid operations
        boolean result = auctionService.closeAuction(999L);
        Assertions.assertFalse(result);
    }

    @Test
    @DisplayName("Should count active auctions correctly")
    void testGetActiveAuctionCount() {
        // WHAT WE'RE TESTING: Counting logic is accurate



        // Create some auctions
        LocalDateTime futureTime = LocalDateTime.now().plusHours(24);
        auctionService.createAuction("Auction 1", "Description", 100.0, futureTime);
        auctionService.createAuction("Auction 2", "Description", 200.0, futureTime);


        // Create expired auction (shouldn't count)
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        auctionService.createAuction("Expired", "Description", 300.0, pastTime);

    }

    @Test
    @DisplayName("Should handle concurrent auction creation")
    void testConcurrentAuctionCreation() throws InterruptedException {
        // WHAT WE'RE TESTING: Thread safety of auction creation

        int numberOfThreads = 10;
        Thread[] threads = new Thread[numberOfThreads];
        LocalDateTime endTime = LocalDateTime.now().plusHours(24);

        // Create multiple threads that create auctions simultaneously
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadNum = i;
            threads[i] = new Thread(() -> {
                auctionService.createAuction("Auction " + threadNum, "Description", 100.0, endTime);
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Add a small delay to ensure transaction completion
        Thread.sleep(100);
    }
}