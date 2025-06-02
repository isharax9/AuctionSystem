package com.auction.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Auction implements Serializable {
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
    private AtomicLong bidCounter;
    private ConcurrentHashMap<Long, Bid> bids;

    public Auction() {
        this.bidCounter = new AtomicLong(0);
        this.bids = new ConcurrentHashMap<>();
    }

    public Auction(Long auctionId, String title, String description,
                   double startingPrice, LocalDateTime endTime) {
        this();
        this.auctionId = auctionId;
        this.title = title;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentHighestBid = startingPrice;
        this.startTime = LocalDateTime.now();
        this.endTime = endTime;
        this.active = true;
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

    public ConcurrentHashMap<Long, Bid> getBids() { return bids; }
    public void setBids(ConcurrentHashMap<Long, Bid> bids) { this.bids = bids; }

    public long getNextBidId() { return bidCounter.incrementAndGet(); }
}