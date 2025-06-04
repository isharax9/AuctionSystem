package com.auction.ejb;

import com.auction.dto.BidUpdateMessage;
import com.auction.websocket.AuctionWebSocketEndpoint;
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
        logger.info("=== BidNotificationMDB.onMessage() CALLED ===");
        try {
            logger.info("Received bid update message of type: " + message.getClass().getName());

            if (message instanceof ObjectMessage) {
                ObjectMessage objectMessage = (ObjectMessage) message;
                Object obj = objectMessage.getObject();
                logger.info("ObjectMessage contains: " + obj.getClass().getName());

                if (obj instanceof BidUpdateMessage) {
                    BidUpdateMessage bidUpdate = (BidUpdateMessage) obj;
                    logger.info("Processing BidUpdateMessage: " + bidUpdate.toString());
                    handleBidUpdate(bidUpdate);
                } else {
                    logger.warning("Received unexpected message type: " + obj.getClass().getName());
                }
            } else {
                logger.warning("Received non-ObjectMessage: " + message.getClass().getName());
            }

        } catch (JMSException e) {
            logger.severe("Error processing bid update message: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.severe("Unexpected error processing message: " + e.getMessage());
            e.printStackTrace();
        }
        logger.info("=== BidNotificationMDB.onMessage() COMPLETED ===");
    }

    private void handleBidUpdate(BidUpdateMessage bidUpdate) {
        logger.info("=== handleBidUpdate() START ===");
        logger.info("Processing bid update: " + bidUpdate.toString());

        // 1. Send REAL WebSocket notifications
        sendRealTimeNotification(bidUpdate);

        // 2. Simulate email notifications
        simulateEmailNotification(bidUpdate);

        // 3. Log analytics
        simulateAnalyticsLogging(bidUpdate);

        logger.info("=== handleBidUpdate() END ===");
    }

    private void sendRealTimeNotification(BidUpdateMessage bidUpdate) {
        logger.info("=== sendRealTimeNotification() START ===");
        try {
            // Log WebSocket session status
            AuctionWebSocketEndpoint.logAllSessions();

            // Get active session count
            int activeConnections = AuctionWebSocketEndpoint.getActiveSessionCount(bidUpdate.getAuctionId());

            logger.info(String.format("REAL-TIME PUSH: Broadcasting to %d WebSocket connections for auction %d - New bid $%.2f by %s",
                    activeConnections, bidUpdate.getAuctionId(), bidUpdate.getBidAmount(), bidUpdate.getBidderUsername()));

            // Broadcast to all connected WebSocket clients
            AuctionWebSocketEndpoint.broadcastBidUpdate(bidUpdate);

            logger.info("WebSocket broadcast completed");

        } catch (Exception e) {
            logger.severe("Failed to send real-time WebSocket notification: " + e.getMessage());
            e.printStackTrace();
        }
        logger.info("=== sendRealTimeNotification() END ===");
    }

    private void simulateEmailNotification(BidUpdateMessage bidUpdate) {
        logger.info(String.format("EMAIL NOTIFICATION: Previous bidders notified for auction %d",
                bidUpdate.getAuctionId()));
    }

    private void simulateAnalyticsLogging(BidUpdateMessage bidUpdate) {
        logger.info(String.format("ANALYTICS: Bid event - Auction: %d, Amount: $%.2f, Time: %s",
                bidUpdate.getAuctionId(), bidUpdate.getBidAmount(), bidUpdate.getBidTime()));
    }
}