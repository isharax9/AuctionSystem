package com.auction.ejb;

import com.auction.entity.Auction;
import jakarta.ejb.Remote;

import java.time.LocalDateTime;
import java.util.List;

@Remote
public interface AuctionServiceRemote {
    Auction createAuction(String title, String description, double startingPrice, LocalDateTime endTime);
    Auction getAuction(Long auctionId);
    List<Auction> getAllActiveAuctions();
    boolean closeAuction(Long auctionId);
    boolean isAuctionActive(Long auctionId);
    int getActiveAuctionCount();
}