package com.auction.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BidUpdateMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long auctionId;
    private String auctionTitle;
    private double bidAmount;
    private String bidderUsername;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime bidTime;

    public BidUpdateMessage() {}

    public BidUpdateMessage(Long auctionId, String auctionTitle, double bidAmount,
                            String bidderUsername, LocalDateTime bidTime) {
        this.auctionId = auctionId;
        this.auctionTitle = auctionTitle;
        this.bidAmount = bidAmount;
        this.bidderUsername = bidderUsername;
        this.bidTime = bidTime;
    }

    // Getters and Setters
    public Long getAuctionId() { return auctionId; }
    public void setAuctionId(Long auctionId) { this.auctionId = auctionId; }

    public String getAuctionTitle() { return auctionTitle; }
    public void setAuctionTitle(String auctionTitle) { this.auctionTitle = auctionTitle; }

    public double getBidAmount() { return bidAmount; }
    public void setBidAmount(double bidAmount) { this.bidAmount = bidAmount; }

    public String getBidderUsername() { return bidderUsername; }
    public void setBidderUsername(String bidderUsername) { this.bidderUsername = bidderUsername; }

    public LocalDateTime getBidTime() { return bidTime; }
    public void setBidTime(LocalDateTime bidTime) { this.bidTime = bidTime; }

    @Override
    public String toString() {
        return String.format("BidUpdate{auction=%d, title='%s', amount=%.2f, bidder='%s', time=%s}",
                auctionId, auctionTitle, bidAmount, bidderUsername, bidTime);
    }
}