package com.auction.ejb;

import com.auction.entity.Auction;
import jakarta.annotation.*;
import jakarta.ejb.Singleton;
import jakarta.ejb.*;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Lock(LockType.READ) // Default lock type
public class AuctionManagerSingleton {

    private static final Logger logger = Logger.getLogger(AuctionManagerSingleton.class.getName());

    private ScheduledExecutorService scheduler;

    @PostConstruct
    public void initialize() {
        logger.info("Initializing Auction Manager Singleton");

        scheduler = Executors.newScheduledThreadPool(2);
        scheduleAuctionCleanup();
        scheduleAuctionStatusUpdates();

        logger.info("Auction Manager Singleton initialized successfully");
    }

    @PreDestroy
    public void cleanup() {
        logger.info("Shutting down Auction Manager Singleton");

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        logger.info("Auction Manager Singleton shutdown complete");
    }

    @Schedule(hour = "*", minute = "*/5", persistent = false)
    public void checkExpiredAuctions() {
        logger.info("Checking for expired auctions...");

        var auctions = AuctionServiceBean.getAuctions();
        LocalDateTime now = LocalDateTime.now();

        long expiredCount = auctions.values().stream()
                .filter(auction -> auction.isActive() && auction.getEndTime().isBefore(now))
                .peek(this::closeExpiredAuction)
                .count();

        if (expiredCount > 0) {
            logger.info("Processed " + expiredCount + " expired auctions");
        }
    }

    @Lock(LockType.WRITE)
    public void closeExpiredAuction(Auction auction) {
        logger.info("Closing expired auction: " + auction.getAuctionId());

        // Use the enhanced completion method
        auction.completeAuction("EXPIRED");

        // Log auction results with enhanced information
        if (auction.hasWinner()) {
            logger.info(String.format("Auction %s (#%d) won by %s with bid $%.2f. Total bids: %d",
                    auction.getTitle(),
                    auction.getAuctionId(),
                    auction.getWinnerUsername(),
                    auction.getWinningBid(),
                    auction.getTotalBidsCount()));
        } else {
            logger.info(String.format("Auction %s (#%d) ended with no bids",
                    auction.getTitle(),
                    auction.getAuctionId()));
        }
    }

    public int getSystemStatus() {
        var auctions = AuctionServiceBean.getAuctions();
        return (int) auctions.values().stream()
                .filter(auction -> auction.isActive())
                .count();
    }

    public double getTotalBidVolume() {
        var auctions = AuctionServiceBean.getAuctions();
        return auctions.values().stream()
                .flatMap(auction -> auction.getBids().values().stream())
                .mapToDouble(bid -> bid.getBidAmount())
                .sum();
    }

    // NEW METHODS for enhanced statistics
    public int getCompletedAuctionsCount() {
        var auctions = AuctionServiceBean.getAuctions();
        return (int) auctions.values().stream()
                .filter(auction -> !auction.isActive())
                .count();
    }

    public double getCompletedAuctionsValue() {
        var auctions = AuctionServiceBean.getAuctions();
        return auctions.values().stream()
                .filter(auction -> !auction.isActive())
                .mapToDouble(Auction::getWinningBid)
                .sum();
    }

    private void scheduleAuctionCleanup() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupOldAuctions();
            } catch (Exception e) {
                logger.severe("Error during auction cleanup: " + e.getMessage());
            }
        }, 0, 30, TimeUnit.MINUTES);
    }

    private void scheduleAuctionStatusUpdates() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                updateAuctionStatuses();
            } catch (Exception e) {
                logger.severe("Error during auction status update: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    private void cleanupOldAuctions() {
        var auctions = AuctionServiceBean.getAuctions();
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);

        // Alternative approach: Count before removal
        long removedCount = auctions.entrySet().stream()
                .filter(entry -> {
                    Auction auction = entry.getValue();
                    return !auction.isActive() &&
                            auction.getCompletedTime() != null &&
                            auction.getCompletedTime().isBefore(cutoff);
                })
                .count();

        // Then remove (if you need the count for logging)
        boolean anyRemoved = auctions.entrySet().removeIf(entry -> {
            Auction auction = entry.getValue();
            return !auction.isActive() &&
                    auction.getCompletedTime() != null &&
                    auction.getCompletedTime().isBefore(cutoff);
        });

        if (anyRemoved && removedCount > 0) {
            logger.info("Cleaned up " + removedCount + " old auction records");
        }
    }

    private void updateAuctionStatuses() {
        int activeAuctions = getSystemStatus();
        int completedAuctions = getCompletedAuctionsCount();
        double totalVolume = getTotalBidVolume();
        double completedValue = getCompletedAuctionsValue();

        logger.info(String.format("System Status - Active: %d, Completed: %d, " +
                        "Total Volume: $%.2f, Completed Value: $%.2f",
                activeAuctions, completedAuctions, totalVolume, completedValue));
    }
}