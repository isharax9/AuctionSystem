package com.auction.websocket;

import com.auction.dto.BidUpdateMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

@ServerEndpoint(value = "/auction-updates/{auctionId}")
public class AuctionWebSocketEndpoint {

    private static final Logger logger = Logger.getLogger(AuctionWebSocketEndpoint.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Store sessions by auction ID
    private static final ConcurrentHashMap<Long, CopyOnWriteArraySet<Session>> auctionSessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("auctionId") String auctionIdStr) {
        try {
            Long auctionId = Long.parseLong(auctionIdStr);
            logger.info("WebSocket connection opened for auction: " + auctionId + ", session: " + session.getId());

            // Add session to the auction's session set
            auctionSessions.computeIfAbsent(auctionId, k -> new CopyOnWriteArraySet<>()).add(session);

            // Store auction ID in session for later use
            session.getUserProperties().put("auctionId", auctionId);

            // Send initial connection confirmation
            session.getBasicRemote().sendText("{\"type\":\"connection\",\"message\":\"Connected to auction " + auctionId + "\",\"auctionId\":" + auctionId + "}");

            logger.info("Total sessions for auction " + auctionId + ": " + auctionSessions.get(auctionId).size());

        } catch (NumberFormatException e) {
            logger.severe("Invalid auction ID: " + auctionIdStr);
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Invalid auction ID"));
            } catch (IOException ex) {
                logger.severe("Failed to close session: " + ex.getMessage());
            }
        } catch (IOException e) {
            logger.warning("Failed to send connection confirmation: " + e.getMessage());
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        Long auctionId = (Long) session.getUserProperties().get("auctionId");
        logger.info("Received message from session " + session.getId() + " for auction " + auctionId + ": " + message);

        // Send back a heartbeat response
        try {
            session.getBasicRemote().sendText("{\"type\":\"heartbeat\",\"timestamp\":" + System.currentTimeMillis() + "}");
        } catch (IOException e) {
            logger.warning("Failed to send heartbeat: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        Long auctionId = (Long) session.getUserProperties().get("auctionId");
        logger.info("WebSocket connection closed for auction: " + auctionId + ", session: " + session.getId() + ", reason: " + closeReason.getReasonPhrase());

        if (auctionId != null) {
            // Remove session from auction's session set
            CopyOnWriteArraySet<Session> sessions = auctionSessions.get(auctionId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    auctionSessions.remove(auctionId);
                    logger.info("No more sessions for auction " + auctionId + ", removed from map");
                } else {
                    logger.info("Remaining sessions for auction " + auctionId + ": " + sessions.size());
                }
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        Long auctionId = (Long) session.getUserProperties().get("auctionId");
        logger.severe("WebSocket error for auction " + auctionId + ", session " + session.getId() + ": " + throwable.getMessage());
        throwable.printStackTrace();

        if (auctionId != null) {
            // Clean up session on error
            CopyOnWriteArraySet<Session> sessions = auctionSessions.get(auctionId);
            if (sessions != null) {
                sessions.remove(session);
            }
        }
    }

    // Static method to broadcast bid updates to all connected clients for an auction
    public static void broadcastBidUpdate(BidUpdateMessage bidUpdate) {
        Long auctionId = bidUpdate.getAuctionId();
        CopyOnWriteArraySet<Session> sessions = auctionSessions.get(auctionId);

        logger.info("Attempting to broadcast bid update for auction: " + auctionId);
        logger.info("Available auction sessions: " + auctionSessions.keySet());

        if (sessions == null || sessions.isEmpty()) {
            logger.info("No WebSocket sessions for auction: " + auctionId);
            return;
        }

        try {
            String jsonMessage = objectMapper.writeValueAsString(bidUpdate);
            String webSocketMessage = "{\"type\":\"bidUpdate\",\"data\":" + jsonMessage + "}";

            logger.info("Broadcasting to " + sessions.size() + " sessions for auction: " + auctionId);
            logger.info("Message: " + webSocketMessage);

            int successCount = 0;
            int failCount = 0;

            // Send to all connected sessions for this auction
            sessions.removeIf(session -> {
                try {
                    if (session.isOpen()) {
                        session.getBasicRemote().sendText(webSocketMessage);
                        logger.info("Sent message to session: " + session.getId());
                        return false; // Keep session
                    } else {
                        logger.info("Session " + session.getId() + " is closed, removing");
                        return true; // Remove closed session
                    }
                } catch (Exception e) {
                    logger.warning("Failed to send message to session " + session.getId() + ": " + e.getMessage());
                    return true; // Remove failed session
                }
            });

            logger.info("Broadcast completed for auction " + auctionId);

        } catch (Exception e) {
            logger.severe("Failed to broadcast bid update: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to get active session count for an auction
    public static int getActiveSessionCount(Long auctionId) {
        CopyOnWriteArraySet<Session> sessions = auctionSessions.get(auctionId);
        int count = sessions != null ? sessions.size() : 0;
        logger.info("Active session count for auction " + auctionId + ": " + count);
        return count;
    }

    // Debug method to get all auction sessions
    public static void logAllSessions() {
        logger.info("=== WebSocket Session Debug ===");
        logger.info("Total auctions with sessions: " + auctionSessions.size());
        for (Long auctionId : auctionSessions.keySet()) {
            int sessionCount = auctionSessions.get(auctionId).size();
            logger.info("Auction " + auctionId + ": " + sessionCount + " sessions");
        }
        logger.info("===============================");
    }
}