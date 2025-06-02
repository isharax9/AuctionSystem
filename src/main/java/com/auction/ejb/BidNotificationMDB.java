package com.auction.ejb;

import com.auction.dto.BidUpdateMessage;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.ObjectMessage;
import java.util.logging.Logger;

@MessageDriven(name = "BidNotificationMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup",
                propertyValue = "jms/topic/BidUpdates"),
        @ActivationConfigProperty(propertyName = "destinationType",
                propertyValue = "jakarta.jms.Topic"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode",
                propertyValue = "Auto-acknowledge")
})
public class BidNotificationMDB implements MessageListener {

    private static final Logger logger = Logger.getLogger(BidNotificationMDB.class.getName());

    @Override
    public void onMessage(Message message) {
        try {
            logger.info("Received bid update message");

            if (message instanceof ObjectMessage) {
                ObjectMessage objectMessage = (ObjectMessage) message;
                Object obj = objectMessage.getObject();

                if (obj instanceof BidUpdateMessage) {
                    handleBidUpdate((BidUpdateMessage) obj);
                } else {
                    logger.warning("Received unexpected message type: " + obj.getClass().getName());
                }
            } else {
                logger.warning("Received non-ObjectMessage: " + message.getClass().getName());
            }

        } catch (JMSException e) {
            logger.severe("Error processing bid update message: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Unexpected error processing message: " + e.getMessage());
        }
    }

    private void handleBidUpdate(BidUpdateMessage bidUpdate) {
        logger.info("Processing bid update: " + bidUpdate.toString());

        // In a real application, this would:
        // 1. Update WebSocket connections to push real-time updates
        // 2. Send email notifications to outbid users
        // 3. Update caching layers
        // 4. Log analytics data
        // 5. Trigger other business processes

        // For demonstration, we'll simulate these operations
        simulateRealTimeNotification(bidUpdate);
        simulateEmailNotification(bidUpdate);
        simulateAnalyticsLogging(bidUpdate);
    }

    private void simulateRealTimeNotification(BidUpdateMessage bidUpdate) {
        // Simulate pushing update to WebSocket clients
        logger.info(String.format("WEBSOCKET PUSH: Auction %d - New bid $%.2f by %s",
                bidUpdate.getAuctionId(), bidUpdate.getBidAmount(), bidUpdate.getBidderUsername()));

        // In production, this would integrate with:
        // - WebSocket endpoints
        // - Server-Sent Events (SSE)
        // - Push notification services
    }

    private void simulateEmailNotification(BidUpdateMessage bidUpdate) {
        // Simulate sending email to previous highest bidder
        logger.info(String.format("EMAIL NOTIFICATION: Previous bidders notified for auction %d",
                bidUpdate.getAuctionId()));

        // In production, this would:
        // - Query database for previous bidders
        // - Send personalized email notifications
        // - Handle email delivery failures
    }

    private void simulateAnalyticsLogging(BidUpdateMessage bidUpdate) {
        // Simulate logging analytics data
        logger.info(String.format("ANALYTICS: Bid event - Auction: %d, Amount: $%.2f, Time: %s",
                bidUpdate.getAuctionId(), bidUpdate.getBidAmount(), bidUpdate.getBidTime()));

        // In production, this would:
        // - Store data in analytics database
        // - Update real-time dashboards
        // - Trigger business intelligence processes
    }
}