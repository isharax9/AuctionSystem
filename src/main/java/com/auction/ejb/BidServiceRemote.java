package com.auction.ejb;

import com.auction.entity.Bid;
import jakarta.ejb.Remote;

import java.io.Serializable;
import java.util.List;

@Remote
public interface BidServiceRemote {
    boolean placeBid(Long auctionId, String username, double bidAmount);
    List<Bid> getBidsForAuction(Long auctionId);
    Serializable getHighestBid(Long auctionId);
    boolean validateBid(Long auctionId, double bidAmount);
    int getBidCount(Long auctionId);
}