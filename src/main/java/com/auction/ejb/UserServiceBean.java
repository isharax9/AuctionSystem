package com.auction.ejb;

import com.auction.entity.User;
import com.auction.entity.Auction;
import javax.ejb.*;
import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.logging.Logger;

@Stateful
@Remote(UserServiceRemote.class)
@StatefulTimeout(value = 30, unit = TimeUnit.MINUTES)
public class UserServiceBean implements UserServiceRemote {

    private static final Logger logger = Logger.getLogger(UserServiceBean.class.getName());

    // In-memory storage for demonstration (thread-safe)
    private static final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    // Session-specific user context
    private String currentUsername;
    private LocalDateTime sessionStartTime;

    @PostConstruct
    public void init() {
        sessionStartTime = LocalDateTime.now();
        logger.info("User service session initialized at: " + sessionStartTime);

        // Initialize with sample users if empty
        if (users.isEmpty()) {
            createSampleUsers();
        }
    }

    @PreDestroy
    public void cleanup() {
        logger.info("User service session ending for user: " + currentUsername);
        if (currentUsername != null) {
            updateUserActivity(currentUsername);
        }
    }

    @Override
    public User registerUser(String username, String email) {
        logger.info("Registering new user: " + username);

        if (users.containsKey(username)) {
            logger.warning("User already exists: " + username);
            return null;
        }

        User newUser = new User(username, email);
        users.put(username, newUser);

        // Set current session user
        this.currentUsername = username;

        logger.info("User registered successfully: " + username);
        return newUser;
    }

    @Override
    public User getUserByUsername(String username) {
        logger.info("Retrieving user: " + username);
        return users.get(username);
    }

    @Override
    public boolean authenticateUser(String username) {
        logger.info("Authenticating user: " + username);

        User user = users.get(username);
        if (user != null && user.isActive()) {
            this.currentUsername = username;
            updateUserActivity(username);
            logger.info("User authenticated successfully: " + username);
            return true;
        }

        logger.warning("Authentication failed for user: " + username);
        return false;
    }

    @Override
    public List<User> getAllActiveUsers() {
        logger.info("Retrieving all active users");

        return users.values().stream()
                .filter(User::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public boolean updateUserActivity(String username) {
        User user = users.get(username);
        if (user != null) {
            user.setLastActivity(LocalDateTime.now());
            logger.info("Updated activity for user: " + username);
            return true;
        }
        return false;
    }

    @Override
    public boolean deactivateUser(String username) {
        logger.info("Deactivating user: " + username);

        User user = users.get(username);
        if (user != null) {
            user.setActive(false);
            logger.info("User deactivated: " + username);
            return true;
        }
        return false;
    }

    @Override
    public int getActiveUserCount() {
        return (int) users.values().stream()
                .filter(User::isActive)
                .count();
    }

    @Override
    public List<User> getUsersInAuction(Long auctionId) {
        logger.info("Getting users participating in auction: " + auctionId);

        // Get auction and extract bidders
        var auctions = AuctionServiceBean.getAuctions();
        Auction auction = auctions.get(auctionId);

        if (auction == null) {
            return List.of();
        }

        return auction.getBids().values().stream()
                .map(bid -> bid.getBidderUsername())
                .distinct()
                .map(users::get)
                .filter(user -> user != null && user.isActive())
                .collect(Collectors.toList());
    }

    // Session-specific methods
    public String getCurrentUsername() {
        return currentUsername;
    }

    public LocalDateTime getSessionStartTime() {
        return sessionStartTime;
    }

    public long getSessionDurationMinutes() {
        return java.time.Duration.between(sessionStartTime, LocalDateTime.now()).toMinutes();
    }

    private void createSampleUsers() {
        // Create sample users for testing
        registerUser("john_doe", "john@example.com");
        registerUser("jane_smith", "jane@example.com");
        registerUser("bob_wilson", "bob@example.com");
        registerUser("alice_brown", "alice@example.com");

        // Reset current username after creating samples
        this.currentUsername = null;

        logger.info("Sample users created");
    }

    // Package-private method for accessing users (used by other EJBs)
    static ConcurrentHashMap<String, User> getUsers() {
        return users;
    }
}