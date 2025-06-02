package com.auction.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Bid
 */
public class BidDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long bidId;
    private Long auctionId;
    private String bidderUsername;
    private double bidAmount;
    private LocalDateTime bidTime;
    private boolean isWinning;

    public BidDTO() {}

    public BidDTO(Long bidId, Long auctionId, String bidderUsername,
                  double bidAmount, LocalDateTime bidTime, boolean isWinning) {
        this.bidId = bidId;
        this.auctionId = auctionId;
        this.bidderUsername = bidderUsername;
        this.bidAmount = bidAmount;
        this.bidTime = bidTime;
        this.isWinning = isWinning;
    }

    // Getters and Setters
    public Long getBidId() { return bidId; }
    public void setBidId(Long bidId) { this.bidId = bidId; }

    public Long getAuctionId() { return auctionId; }
    public void setAuctionId(Long auctionId) { this.auctionId = auctionId; }

    public String getBidderUsername() { return bidderUsername; }
    public void setBidderUsername(String bidderUsername) { this.bidderUsername = bidderUsername; }

    public double getBidAmount() { return bidAmount; }
    public void setBidAmount(double bidAmount) { this.bidAmount = bidAmount; }

    public LocalDateTime getBidTime() { return bidTime; }
    public void setBidTime(LocalDateTime bidTime) { this.bidTime = bidTime; }

    public boolean isWinning() { return isWinning; }
    public void setWinning(boolean winning) { isWinning = winning; }
}