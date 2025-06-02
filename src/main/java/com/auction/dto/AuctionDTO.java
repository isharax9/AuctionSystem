package com.auction.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Auction
 * Lightweight version for web layer communication
 */
public class AuctionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

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

    public AuctionDTO() {}

    // Constructor from Auction entity
    public AuctionDTO(Long auctionId, String title, String description,
                      double startingPrice, double currentHighestBid,
                      String currentHighestBidder, LocalDateTime startTime,
                      LocalDateTime endTime, boolean active, int bidCount) {
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
    }

    // Getters and Setters
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
}