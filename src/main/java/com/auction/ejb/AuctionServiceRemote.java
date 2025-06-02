package com.auction.ejb;

import com.auction.dto.AuctionDTO;
import com.auction.entity.Auction;
import jakarta.ejb.Remote;

import java.time.LocalDateTime;
import java.util.List;

@Remote
public interface AuctionServiceRemote {
    AuctionDTO createAuction(String title, String description, double startingPrice, LocalDateTime endTime);
    AuctionDTO getAuction(Long auctionId);
    List<AuctionDTO> getAllActiveAuctions();
    boolean closeAuction(Long auctionId);
    boolean isAuctionActive(Long auctionId);
    int getActiveAuctionCount();
}