package com.auction.ejb;

import com.auction.dto.AuctionDTO;
import jakarta.ejb.Remote;

import java.time.LocalDateTime;
import java.util.List;

@Remote
public interface AuctionServiceRemote {
    // Existing methods
    AuctionDTO createAuction(String title, String description, double startingPrice, LocalDateTime endTime);
    AuctionDTO getAuction(Long auctionId);
    List<AuctionDTO> getAllActiveAuctions();
    boolean closeAuction(Long auctionId);
    boolean isAuctionActive(Long auctionId);
    int getActiveAuctionCount();

    // NEW METHODS for enhanced functionality

    // Enhanced auction creation with hours and minutes
    AuctionDTO createAuction(String title, String description, double startingPrice, int durationHours, int durationMinutes);

    // Auction history management
    List<AuctionDTO> getAllCompletedAuctions();
    List<AuctionDTO> getAuctionHistory(int page, int size);
    List<AuctionDTO> getUserWonAuctions(String username);

    // Enhanced auction operations
    boolean closeAuction(Long auctionId, String reason);
    boolean cancelAuction(Long auctionId, String reason);

    // Statistics
    int getCompletedAuctionCount();
    int getTotalAuctionCount();
    double getTotalAuctionValue();

    // Validation
    boolean validateAuctionDuration(int hours, int minutes);
}