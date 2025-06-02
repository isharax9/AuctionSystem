package com.auction.ejb;

import com.auction.entity.Bid;
import javax.ejb.Remote;
import java.util.List;

@Remote
public interface BidServiceRemote {
    boolean placeBid(Long auctionId, String username, double bidAmount);
    List<Bid> getBidsForAuction(Long auctionId);
    Bid getHighestBid(Long auctionId);
    boolean validateBid(Long auctionId, double bidAmount);
    int getBidCount(Long auctionId);
}