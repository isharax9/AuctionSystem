package com.auction.dto;

import com.auction.entity.Auction;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Enhanced Data Transfer Object for Auction with history support
 */
public class AuctionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // Existing fields
    private Long auctionId;
    private String title;
    private String description;
    private double startingPrice;
    private double currentHighestBid;
    private String currentHighestBidder;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean active;
    private int bidCount;

    // NEW FIELDS for auction history
    private Auction.AuctionStatus status;
    private LocalDateTime completedTime;
    private String winnerUsername;
    private double winningBid;
    private String endReason;
    private boolean hasWinner;

    public AuctionDTO() {}

    // Enhanced constructor from Auction entity
    public AuctionDTO(Long auctionId, String title, String description,
                      double startingPrice, double currentHighestBid,
                      String currentHighestBidder, LocalDateTime startTime,
                      LocalDateTime endTime, boolean active, int bidCount,
                      Auction.AuctionStatus status, LocalDateTime completedTime,
                      String winnerUsername, double winningBid, String endReason) {
        this.auctionId = auctionId;
        this.title = title;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentHighestBid = currentHighestBid;
        this.currentHighestBidder = currentHighestBidder;
        this.startTime = startTime;
        this.endTime = endTime;
        this.active = active;
        this.bidCount = bidCount;
        this.status = status;
        this.completedTime = completedTime;
        this.winnerUsername = winnerUsername;
        this.winningBid = winningBid;
        this.endReason = endReason;
        this.hasWinner = winnerUsername != null && !winnerUsername.trim().isEmpty();
    }

    // Factory method to create DTO from Auction entity
    public static AuctionDTO fromAuction(Auction auction) {
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
                auction.getTotalBidsCount(),
                auction.getStatus(),
                auction.getCompletedTime(),
                auction.getWinnerUsername(),
                auction.getWinningBid(),
                auction.getEndReason()
        );
    }

    // Existing getters and setters...
    public Long getAuctionId() { return auctionId; }
    public void setAuctionId(Long auctionId) { this.auctionId = auctionId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getStartingPrice() { return startingPrice; }
    public void setStartingPrice(double startingPrice) { this.startingPrice = startingPrice; }

    public double getCurrentHighestBid() { return currentHighestBid; }
    public void setCurrentHighestBid(double currentHighestBid) {
        this.currentHighestBid = currentHighestBid;
    }

    public String getCurrentHighestBidder() { return currentHighestBidder; }
    public void setCurrentHighestBidder(String currentHighestBidder) {
        this.currentHighestBidder = currentHighestBidder;
    }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getBidCount() { return bidCount; }
    public void setBidCount(int bidCount) { this.bidCount = bidCount; }

    // NEW GETTERS AND SETTERS for history fields
    public Auction.AuctionStatus getStatus() { return status; }
    public void setStatus(Auction.AuctionStatus status) { this.status = status; }

    public LocalDateTime getCompletedTime() { return completedTime; }
    public void setCompletedTime(LocalDateTime completedTime) { this.completedTime = completedTime; }

    public String getWinnerUsername() { return winnerUsername; }
    public void setWinnerUsername(String winnerUsername) {
        this.winnerUsername = winnerUsername;
        this.hasWinner = winnerUsername != null && !winnerUsername.trim().isEmpty();
    }

    public double getWinningBid() { return winningBid; }
    public void setWinningBid(double winningBid) { this.winningBid = winningBid; }

    public String getEndReason() { return endReason; }
    public void setEndReason(String endReason) { this.endReason = endReason; }

    public boolean hasWinner() { return hasWinner; }
    public void setHasWinner(boolean hasWinner) { this.hasWinner = hasWinner; }

    // Helper method to check if auction is completed
    public boolean isCompleted() {
        return !active && completedTime != null;
    }

    // Helper method to get display status
    public String getDisplayStatus() {
        if (active) {
            return "🟢 Active";
        } else if (status != null) {
            switch (status) {
                case ENDED:
                    return "🔴 Ended";
                case EXPIRED:
                    return "⏰ Expired";
                case CANCELLED:
                    return "❌ Cancelled";
                default:
                    return "🔴 Completed";
            }
        }
        return "🔴 Ended";
    }
}