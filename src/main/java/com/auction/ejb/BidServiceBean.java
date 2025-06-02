package com.auction.ejb;

import com.auction.entity.Auction;
import com.auction.entity.Bid;
import com.auction.dto.BidUpdateMessage;
import jakarta.ejb.*;
import jakarta.annotation.Resource;
import jakarta.jms.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.logging.Logger;

@Stateless
@Remote(BidServiceRemote.class)
public class BidServiceBean implements BidServiceRemote {

    private static final Logger logger = Logger.getLogger(BidServiceBean.class.getName());
    private static final double MIN_BID_INCREMENT = 5.0;

    // Fixed JNDI resource mappings for GlassFish
    @Resource(mappedName = "jms/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(mappedName = "jms/topic/BidUpdates")
    private Topic bidUpdatesTopic;

    @Override
    public boolean placeBid(Long auctionId, String username, double bidAmount) {
        logger.info(String.format("Placing bid for auction %d by user %s: $%.2f",
                auctionId, username, bidAmount));

        // Get auction from AuctionServiceBean's storage
        ConcurrentHashMap<Long, Auction> auctions = AuctionServiceBean.getAuctions();
        Auction auction = auctions.get(auctionId);

        if (auction == null) {
            logger.warning("Auction not found: " + auctionId);
            return false;
        }

        if (!auction.isActive()) {
            logger.warning("Auction is not active: " + auctionId);
            return false;
        }

        // Validate bid amount
        if (!validateBid(auctionId, bidAmount)) {
            logger.warning("Invalid bid amount: " + bidAmount);
            return false;
        }

        // Thread-safe bid placement
        synchronized (auction) {
            // Double-check validation inside synchronized block
            if (bidAmount <= auction.getCurrentHighestBid()) {
                return false;
            }

            // Create new bid
            Long bidId = auction.getNextBidId();
            Bid newBid = new Bid(bidId, auctionId, username, bidAmount);

            // Update auction with new highest bid
            auction.setCurrentHighestBid(bidAmount);
            auction.setCurrentHighestBidder(username);

            // Store bid
            auction.getBids().put(bidId, newBid);

            // Mark previous bids as not winning
            auction.getBids().values().forEach(bid -> bid.setWinning(false));
            newBid.setWinning(true);

            logger.info("Bid placed successfully: " + bidId);

            // Send JMS notification (with error handling)
            try {
                sendBidUpdateNotification(auction, newBid);
            } catch (Exception e) {
                logger.warning("Failed to send JMS notification, but bid was placed: " + e.getMessage());
                // Don't fail the bid placement if JMS fails
            }

            return true;
        }
    }

    @Override
    public List<Bid> getBidsForAuction(Long auctionId) {
        ConcurrentHashMap<Long, Auction> auctions = AuctionServiceBean.getAuctions();
        Auction auction = auctions.get(auctionId);

        if (auction == null) {
            return List.of();
        }

        return auction.getBids().values().stream()
                .sorted((b1, b2) -> b2.getBidTime().compareTo(b1.getBidTime()))
                .collect(Collectors.toList());
    }

    @Override
    public Bid getHighestBid(Long auctionId) {
        ConcurrentHashMap<Long, Auction> auctions = AuctionServiceBean.getAuctions();
        Auction auction = auctions.get(auctionId);

        if (auction == null) {
            return null;
        }

        return auction.getBids().values().stream()
                .filter(Bid::isWinning)
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean validateBid(Long auctionId, double bidAmount) {
        ConcurrentHashMap<Long, Auction> auctions = AuctionServiceBean.getAuctions();
        Auction auction = auctions.get(auctionId);

        if (auction == null) {
            return false;
        }

        // Bid must be higher than current highest bid + minimum increment
        return bidAmount > auction.getCurrentHighestBid() + MIN_BID_INCREMENT;
    }

    @Override
    public int getBidCount(Long auctionId) {
        ConcurrentHashMap<Long, Auction> auctions = AuctionServiceBean.getAuctions();
        Auction auction = auctions.get(auctionId);

        return auction != null ? auction.getBids().size() : 0;
    }

    private void sendBidUpdateNotification(Auction auction, Bid newBid) {
        try {
            if (connectionFactory == null) {
                logger.warning("ConnectionFactory is null, JMS notification skipped");
                return;
            }

            if (bidUpdatesTopic == null) {
                logger.warning("BidUpdates topic is null, JMS notification skipped");
                return;
            }

            try (Connection connection = connectionFactory.createConnection();
                 Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {

                MessageProducer producer = session.createProducer(bidUpdatesTopic);

                BidUpdateMessage updateMessage = new BidUpdateMessage(
                        auction.getAuctionId(),
                        auction.getTitle(),
                        newBid.getBidAmount(),
                        newBid.getBidderUsername(),
                        newBid.getBidTime()
                );

                ObjectMessage message = session.createObjectMessage(updateMessage);
                message.setStringProperty("auctionId", auction.getAuctionId().toString());

                producer.send(message);
                logger.info("Bid update notification sent for auction: " + auction.getAuctionId());

            }
        } catch (JMSException e) {
            logger.warning("Failed to send bid update notification: " + e.getMessage());
            // Don't rethrow - bid placement should succeed even if notification fails
        }
    }
}