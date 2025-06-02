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

        // Initialize scheduler for auction management tasks
        scheduler = Executors.newScheduledThreadPool(2);

        // Schedule periodic auction cleanup
        scheduleAuctionCleanup();

        // Schedule auction status updates
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

        auctions.values().stream()
                .filter(auction -> auction.isActive() && auction.getEndTime().isBefore(now))
                .forEach(this::closeExpiredAuction);
    }

    @Lock(LockType.WRITE)
    public void closeExpiredAuction(Auction auction) {
        logger.info("Closing expired auction: " + auction.getAuctionId());

        auction.setActive(false);

        // Log auction results
        if (auction.getCurrentHighestBidder() != null) {
            logger.info(String.format("Auction %s won by %s with bid $%.2f",
                    auction.getTitle(),
                    auction.getCurrentHighestBidder(),
                    auction.getCurrentHighestBid()));
        } else {
            logger.info("Auction " + auction.getTitle() + " ended with no bids");
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
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);

        auctions.entrySet().removeIf(entry -> {
            Auction auction = entry.getValue();
            return !auction.isActive() && auction.getEndTime().isBefore(cutoff);
        });
    }

    private void updateAuctionStatuses() {
        // This method can be used for real-time status updates
        // Currently just logs system statistics
        int activeAuctions = getSystemStatus();
        double totalVolume = getTotalBidVolume();

        logger.info(String.format("System Status - Active Auctions: %d, Total Bid Volume: $%.2f",
                activeAuctions, totalVolume));
    }
}