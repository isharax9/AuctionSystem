package com.auction.ejb;

import com.auction.entity.Auction;
import com.auction.dto.AuctionDTO;
import jakarta.ejb.*;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.logging.Logger;

@Stateless
@Remote(AuctionServiceRemote.class)
public class AuctionServiceBean implements AuctionServiceRemote {

    private static final Logger logger = Logger.getLogger(AuctionServiceBean.class.getName());

    // In-memory storage for demonstration (thread-safe)
    private static final ConcurrentHashMap<Long, Auction> auctions = new ConcurrentHashMap<>();
    private static final AtomicLong auctionIdCounter = new AtomicLong(1);

    public static void resetCounterForTesting() {
        auctionIdCounter.set(0);
        auctions.clear();
        logger.info("Counter and auction data reset for testing");
    }

    @PostConstruct
    public void init() {
        if (auctions.isEmpty()) {
            createSampleAuctions();
        }
    }

    // EXISTING METHODS - Enhanced

    @Override
    public AuctionDTO createAuction(String title, String description,
                                    double startingPrice, LocalDateTime endTime) {
        logger.info("Creating new auction: " + title);

        Long auctionId = auctionIdCounter.getAndIncrement();
        Auction auction = new Auction(auctionId, title, description, startingPrice, endTime);

        auctions.put(auctionId, auction);

        logger.info("Auction created successfully with ID: " + auctionId);
        return convertToDTO(auction);
    }

    // NEW METHOD: Enhanced auction creation with hours and minutes
    @Override
    public AuctionDTO createAuction(String title, String description,
                                    double startingPrice, int durationHours, int durationMinutes) {
        logger.info("Creating new auction with duration: " + durationHours + "h " + durationMinutes + "m");

        // Validate duration
        if (!validateAuctionDuration(durationHours, durationMinutes)) {
            logger.warning("Invalid auction duration: " + durationHours + "h " + durationMinutes + "m");
            return null;
        }

        // Calculate end time
        LocalDateTime endTime = LocalDateTime.now()
                .plusHours(durationHours)
                .plusMinutes(durationMinutes);

        return createAuction(title, description, startingPrice, endTime);
    }

    @Override
    public AuctionDTO getAuction(Long auctionId) {
        logger.info("Retrieving auction with ID: " + auctionId);
        Auction auction = auctions.get(auctionId);
        return auction != null ? convertToDTO(auction) : null;
    }

    @Override
    public List<AuctionDTO> getAllActiveAuctions() {
        logger.info("Retrieving all active auctions");

        return auctions.values().stream()
                .filter(auction -> auction.isActive() &&
                        auction.getEndTime().isAfter(LocalDateTime.now()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // NEW METHODS for auction history

    @Override
    public List<AuctionDTO> getAllCompletedAuctions() {
        logger.info("Retrieving all completed auctions");

        return auctions.values().stream()
                .filter(auction -> !auction.isActive() ||
                        auction.getEndTime().isBefore(LocalDateTime.now()))
                .map(this::ensureAuctionCompleted)
                .map(this::convertToDTO)
                .sorted((a, b) -> {
                    LocalDateTime timeA = a.getCompletedTime() != null ?
                            a.getCompletedTime() : a.getEndTime();
                    LocalDateTime timeB = b.getCompletedTime() != null ?
                            b.getCompletedTime() : b.getEndTime();
                    return timeB.compareTo(timeA); // Most recent first
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<AuctionDTO> getAuctionHistory(int page, int size) {
        logger.info("Retrieving auction history - page: " + page + ", size: " + size);

        List<AuctionDTO> allCompleted = getAllCompletedAuctions();

        int start = page * size;
        int end = Math.min(start + size, allCompleted.size());

        if (start >= allCompleted.size()) {
            return List.of();
        }

        return allCompleted.subList(start, end);
    }

    @Override
    public List<AuctionDTO> getUserWonAuctions(String username) {
        logger.info("Retrieving auctions won by user: " + username);

        return auctions.values().stream()
                .filter(auction -> !auction.isActive())
                .map(this::ensureAuctionCompleted)
                .filter(auction -> username.equals(auction.getWinnerUsername()))
                .map(this::convertToDTO)
                .sorted((a, b) -> {
                    LocalDateTime timeA = a.getCompletedTime() != null ?
                            a.getCompletedTime() : a.getEndTime();
                    LocalDateTime timeB = b.getCompletedTime() != null ?
                            b.getCompletedTime() : b.getEndTime();
                    return timeB.compareTo(timeA);
                })
                .collect(Collectors.toList());
    }

    // Enhanced close methods
    @Override
    public boolean closeAuction(Long auctionId) {
        return closeAuction(auctionId, "MANUAL_CLOSE");
    }

    @Override
    public boolean closeAuction(Long auctionId, String reason) {
        logger.info("Closing auction with ID: " + auctionId + ", reason: " + reason);

        Auction auction = auctions.get(auctionId);
        if (auction != null && auction.isActive()) {
            auction.completeAuction(reason);
            logger.info("Auction " + auctionId + " closed successfully. Winner: " +
                    auction.getWinnerUsername() + ", Final bid: $" + auction.getWinningBid());
            return true;
        }
        return false;
    }

    @Override
    public boolean cancelAuction(Long auctionId, String reason) {
        logger.info("Cancelling auction with ID: " + auctionId + ", reason: " + reason);

        Auction auction = auctions.get(auctionId);
        if (auction != null && auction.isActive()) {
            auction.completeAuction("CANCELLED: " + reason);
            logger.info("Auction " + auctionId + " cancelled successfully");
            return true;
        }
        return false;
    }

    @Override
    public boolean isAuctionActive(Long auctionId) {
        Auction auction = auctions.get(auctionId);
        if (auction == null) {
            return false;
        }

        // Auto-expire if time has passed
        if (auction.isActive() && auction.getEndTime().isBefore(LocalDateTime.now())) {
            auction.completeAuction("EXPIRED");
            return false;
        }

        return auction.isActive();
    }

    // Statistics methods
    @Override
    public int getActiveAuctionCount() {
        return (int) auctions.values().stream()
                .filter(auction -> auction.isActive() &&
                        auction.getEndTime().isAfter(LocalDateTime.now()))
                .count();
    }

    @Override
    public int getCompletedAuctionCount() {
        return (int) auctions.values().stream()
                .filter(auction -> !auction.isActive() ||
                        auction.getEndTime().isBefore(LocalDateTime.now()))
                .count();
    }

    @Override
    public int getTotalAuctionCount() {
        return auctions.size();
    }

    @Override
    public double getTotalAuctionValue() {
        return auctions.values().stream()
                .filter(auction -> !auction.isActive())
                .mapToDouble(Auction::getWinningBid)
                .sum();
    }

    // Validation method
    @Override
    public boolean validateAuctionDuration(int hours, int minutes) {
        // Calculate total minutes
        int totalMinutes = (hours * 60) + minutes;

        // Minimum 1 minute, maximum 7 days (10,080 minutes)
        int maxMinutes = 7 * 24 * 60; // 7 days in minutes

        boolean isValid = totalMinutes >= 1 && totalMinutes <= maxMinutes;

        if (!isValid) {
            logger.warning("Invalid duration: " + hours + "h " + minutes + "m (" +
                    totalMinutes + " total minutes)");
        }

        return isValid;
    }

    // Helper methods
    private Auction ensureAuctionCompleted(Auction auction) {
        // Ensure expired auctions are marked as completed
        if (auction.isActive() && auction.getEndTime().isBefore(LocalDateTime.now())) {
            auction.completeAuction("EXPIRED");
        }
        return auction;
    }

    private AuctionDTO convertToDTO(Auction auction) {
        try {
            return AuctionDTO.fromAuction(auction);
        } catch (Exception e) {
            logger.warning("Error converting auction to DTO: " + e.getMessage());
            // Return a minimal DTO if conversion fails
            return new AuctionDTO(
                    auction.getAuctionId(),
                    auction.getTitle(),
                    auction.getDescription(),
                    auction.getStartingPrice(),
                    auction.getCurrentHighestBid(),
                    auction.getCurrentHighestBidder(),
                    auction.getStartTime(),
                    auction.getEndTime(),
                    auction.isActive(),
                    0,
                    auction.getStatus(),
                    auction.getCompletedTime(),
                    auction.getWinnerUsername(),
                    auction.getWinningBid(),
                    auction.getEndReason()
            );
        }
    }

    private void createSampleAuctions() {
        // Create some sample auctions
        createAuction("Vintage Watch", "Classic 1960s Rolex", 500.0, 2, 0);
        createAuction("Antique Vase", "Ming Dynasty ceramic vase", 1000.0, 4, 30);
        createAuction("Sports Car Model", "Ferrari F40 1:18 scale", 50.0, 1, 15);

        // Create a completed sample auction for testing history
        AuctionDTO completedAuction = createAuction("Test Completed Auction",
                "This auction ended for testing", 100.0, 0, 1);
        if (completedAuction != null) {
            // Manually expire it for testing
            Auction auction = auctions.get(completedAuction.getAuctionId());
            if (auction != null) {
                auction.setEndTime(LocalDateTime.now().minusMinutes(5));
                auction.completeAuction("EXPIRED");
            }
        }
    }

    // Package-private method for accessing auctions (used by other EJBs)
    static ConcurrentHashMap<Long, Auction> getAuctions() {
        return auctions;
    }
}