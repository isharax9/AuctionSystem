package com.auction.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Auction implements Serializable {
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

    // NEW FIELDS for auction history management
    private AuctionStatus status;
    private LocalDateTime completedTime;
    private String winnerUsername;
    private double winningBid;
    private int totalBidsCount;
    private String endReason; // "EXPIRED", "MANUAL_CLOSE", "CANCELLED"

    // Remove non-serializable fields from main class
    private transient AtomicLong bidCounter;
    private transient ConcurrentHashMap<Long, Bid> bids;

    // Status enumeration for auction lifecycle
    public enum AuctionStatus {
        ACTIVE("Active"),
        ENDED("Ended"),
        CANCELLED("Cancelled"),
        EXPIRED("Expired");

        private final String displayName;

        AuctionStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public Auction() {
        this.status = AuctionStatus.ACTIVE;
        this.endReason = null;
        this.completedTime = null;
        initializeTransientFields();
    }

    public Auction(Long auctionId, String title, String description,
                   double startingPrice, LocalDateTime endTime) {
        this.auctionId = auctionId;
        this.title = title;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentHighestBid = startingPrice;
        this.startTime = LocalDateTime.now();
        this.endTime = endTime;
        this.active = true;
        this.status = AuctionStatus.ACTIVE;
        this.endReason = null;
        this.completedTime = null;
        this.totalBidsCount = 0;
        this.winnerUsername = null;
        this.winningBid = startingPrice;
        initializeTransientFields();
    }

    // Initialize transient fields after deserialization
    private void initializeTransientFields() {
        if (this.bidCounter == null) {
            this.bidCounter = new AtomicLong(this.totalBidsCount);
        }
        if (this.bids == null) {
            this.bids = new ConcurrentHashMap<>();
        }
    }

    // Method to complete auction
    public void completeAuction(String reason) {
        this.active = false;
        this.completedTime = LocalDateTime.now();
        this.endReason = reason;
        this.winnerUsername = this.currentHighestBidder;
        this.winningBid = this.currentHighestBid;
        this.totalBidsCount = this.bids != null ? this.bids.size() : 0;

        // Set status based on reason
        switch (reason) {
            case "EXPIRED":
                this.status = AuctionStatus.EXPIRED;
                break;
            case "CANCELLED":
                this.status = AuctionStatus.CANCELLED;
                break;
            default:
                this.status = AuctionStatus.ENDED;
        }
    }

    // Check if auction has a winner
    public boolean hasWinner() {
        return winnerUsername != null && !winnerUsername.trim().isEmpty();
    }

    // Custom serialization methods
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        // Update totalBidsCount before serialization
        if (this.bids != null) {
            this.totalBidsCount = this.bids.size();
        }
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        initializeTransientFields();
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

    public ConcurrentHashMap<Long, Bid> getBids() {
        if (bids == null) initializeTransientFields();
        return bids;
    }

    public void setBids(ConcurrentHashMap<Long, Bid> bids) { this.bids = bids; }

    public long getNextBidId() {
        if (bidCounter == null) initializeTransientFields();
        return bidCounter.incrementAndGet();
    }

    // NEW GETTERS AND SETTERS for history fields
    public AuctionStatus getStatus() { return status; }
    public void setStatus(AuctionStatus status) { this.status = status; }

    public LocalDateTime getCompletedTime() { return completedTime; }
    public void setCompletedTime(LocalDateTime completedTime) { this.completedTime = completedTime; }

    public String getWinnerUsername() { return winnerUsername; }
    public void setWinnerUsername(String winnerUsername) { this.winnerUsername = winnerUsername; }

    public double getWinningBid() { return winningBid; }
    public void setWinningBid(double winningBid) { this.winningBid = winningBid; }

    public int getTotalBidsCount() {
        return this.bids != null ? this.bids.size() : this.totalBidsCount;
    }
    public void setTotalBidsCount(int totalBidsCount) { this.totalBidsCount = totalBidsCount; }

    public String getEndReason() { return endReason; }
    public void setEndReason(String endReason) { this.endReason = endReason; }
}