package com.auction.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.websocket.server.ServerContainer;
import com.auction.websocket.AuctionWebSocketEndpoint;
import java.util.logging.Logger;

@WebListener
public class WebSocketConfigurator implements ServletContextListener {

    private static final Logger logger = Logger.getLogger(WebSocketConfigurator.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            ServerContainer serverContainer = (ServerContainer) sce.getServletContext()
                    .getAttribute("jakarta.websocket.server.ServerContainer");

            if (serverContainer != null) {
                serverContainer.addEndpoint(AuctionWebSocketEndpoint.class);
                logger.info("WebSocket endpoint registered successfully: " + AuctionWebSocketEndpoint.class.getSimpleName());
            } else {
                logger.severe("ServerContainer not found - WebSocket not supported");
            }
        } catch (Exception e) {
            logger.severe("Failed to register WebSocket endpoint: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("WebSocket configurator destroyed");
    }
}