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
        // Reset the auction ID counter to initial value
        auctionIdCounter.set(0); // Assuming AtomicLong

        // Optionally clear auctions for clean test state
        auctions.clear();

        logger.info("Counter and auction data reset for testing");
    }

    @PostConstruct
    public void init() {
        // Initialize with some sample auctions for testing
        if (auctions.isEmpty()) {
            createSampleAuctions();
        }
    }

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

    @Override
    public boolean closeAuction(Long auctionId) {
        logger.info("Closing auction with ID: " + auctionId);

        Auction auction = auctions.get(auctionId);
        if (auction != null) {
            auction.setActive(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean isAuctionActive(Long auctionId) {
        Auction auction = auctions.get(auctionId);
        return auction != null && auction.isActive() &&
                auction.getEndTime().isAfter(LocalDateTime.now());
    }

    @Override
    public int getActiveAuctionCount() {
        return (int) auctions.values().stream()
                .filter(auction -> auction.isActive() &&
                        auction.getEndTime().isAfter(LocalDateTime.now()))
                .count();
    }

    // Helper method to convert Auction to AuctionDTO
    private AuctionDTO convertToDTO(Auction auction) {
        try {
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
                    auction.getBids().size()
            );
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
                    0
            );
        }
    }

    private void createSampleAuctions() {
        // Create sample auctions for testing
        createAuction("Vintage Watch", "Classic 1960s Rolex", 500.0,
                LocalDateTime.now().plusHours(2));
        createAuction("Antique Vase", "Ming Dynasty ceramic vase", 1000.0,
                LocalDateTime.now().plusHours(4));
        createAuction("Sports Car Model", "Ferrari F40 1:18 scale", 50.0,
                LocalDateTime.now().plusHours(1));
    }

    // Package-private method for accessing auctions (used by other EJBs)
    static ConcurrentHashMap<Long, Auction> getAuctions() {
        return auctions;
    }
}