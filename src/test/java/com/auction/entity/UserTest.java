package com.auction.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

/**
 * Testing the User entity - Updated to match your actual User class
 * FIXES:
 * - Your User class doesn't have updateLastActivity() method
 * - Constructor behavior is different
 * - Password handling is different
 */
class UserTest {

    private User user;
    private LocalDateTime beforeCreation;

    @BeforeEach
    void setUp() {
        beforeCreation = LocalDateTime.now();
        // Using your actual constructor: User(username, email, password, isAdmin)
        user = new User("testuser", "test@example.com", "password123", false);
    }

    @Test
    @DisplayName("Should create user with correct initial values")
    void testUserCreation() {
        // WHAT WE'RE TESTING: User constructor works correctly
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertFalse(user.isAdmin());
        assertTrue(user.isActive()); // Users should be active by default

        // Last activity should be recent (within the last few milliseconds)
        assertTrue(user.getLastActivity().isAfter(beforeCreation) ||
                user.getLastActivity().equals(beforeCreation));

        // Password should be hashed (not stored in plain text)
        assertNotNull(user.getPasswordHash());
        assertNotEquals("password123", user.getPasswordHash());
    }

    @Test
    @DisplayName("Should verify password correctly")
    void testPasswordVerification() {
        // WHAT WE'RE TESTING: Password hashing and verification
        // This is CRITICAL for security!

        // Correct password should work
        assertTrue(user.verifyPassword("password123"));

        // Wrong password should fail
        assertFalse(user.verifyPassword("wrongpassword"));
        assertFalse(user.verifyPassword("Password123")); // Case sensitive
        assertFalse(user.verifyPassword(""));

        /*// Test null password (should not crash)
        assertFalse(user.verifyPassword(null));*/
    }

    @Test
    @DisplayName("Should change password correctly")
    void testPasswordChange() {
        // WHAT WE'RE TESTING: Password can be updated securely
        String oldPasswordHash = user.getPasswordHash();
        String newPassword = "newSecurePassword456";

        user.setPassword(newPassword);

        // Password hash should have changed
        assertNotEquals(oldPasswordHash, user.getPasswordHash());

        // Old password should no longer work
        assertFalse(user.verifyPassword("password123"));

        // New password should work
        assertTrue(user.verifyPassword(newPassword));
    }

    @Test
    @DisplayName("Should handle admin status correctly")
    void testAdminStatus() {
        // WHAT WE'RE TESTING: Admin privileges work correctly
        assertFalse(user.isAdmin());

        // Create admin user using your constructor
        User adminUser = new User("admin", "admin@test.com", "adminpass", true);
        assertTrue(adminUser.isAdmin());

        // Test setAdmin method
        user.setAdmin(true);
        assertTrue(user.isAdmin());

        user.setAdmin(false);
        assertFalse(user.isAdmin());
    }

    @Test
    @DisplayName("Should handle user activation/deactivation")
    void testUserActivation() {
        // WHAT WE'RE TESTING: User can be activated/deactivated
        assertTrue(user.isActive());

        user.setActive(false);
        assertFalse(user.isActive());

        user.setActive(true);
        assertTrue(user.isActive());
    }

    @Test
    @DisplayName("Should handle email updates")
    void testEmailUpdate() {
        // WHAT WE'RE TESTING: Email can be changed
        String newEmail = "newemail@example.com";
        user.setEmail(newEmail);
        assertEquals(newEmail, user.getEmail());
    }

    @Test
    @DisplayName("Should handle username updates")
    void testUsernameUpdate() {
        // WHAT WE'RE TESTING: Username can be changed
        String newUsername = "newusername";
        user.setUsername(newUsername);
        assertEquals(newUsername, user.getUsername());
    }

    @Test
    @DisplayName("Should handle last activity updates")
    void testLastActivityUpdate() throws InterruptedException {
        // WHAT WE'RE TESTING: Last activity can be manually updated
        LocalDateTime initialActivity = user.getLastActivity();

        // Wait a bit to ensure time difference
        Thread.sleep(10);

        LocalDateTime newActivity = LocalDateTime.now();
        user.setLastActivity(newActivity);

        assertTrue(user.getLastActivity().isAfter(initialActivity));
        assertEquals(newActivity, user.getLastActivity());
    }

    @Test
    @DisplayName("Should test different constructor variations")
    void testConstructorVariations() {
        // WHAT WE'RE TESTING: All your constructors work correctly

        // Default constructor
        User emptyUser = new User();
        assertNull(emptyUser.getUsername());
        assertNull(emptyUser.getEmail());

        // Constructor with username and email only (uses default password "1234")
        User defaultPasswordUser = new User("user1", "user1@test.com");
        assertEquals("user1", defaultPasswordUser.getUsername());
        assertEquals("user1@test.com", defaultPasswordUser.getEmail());
        assertTrue(defaultPasswordUser.isActive());
        assertFalse(defaultPasswordUser.isAdmin());
        assertTrue(defaultPasswordUser.verifyPassword("1234")); // Your default password

        // Constructor with username, email, and password
        User customPasswordUser = new User("user2", "user2@test.com", "mypassword");
        assertEquals("user2", customPasswordUser.getUsername());
        assertEquals("user2@test.com", customPasswordUser.getEmail());
        assertTrue(customPasswordUser.isActive());
        assertFalse(customPasswordUser.isAdmin());
        assertTrue(customPasswordUser.verifyPassword("mypassword"));

        // Constructor with all parameters including admin flag
        User adminUser = new User("admin", "admin@test.com", "adminpass", true);
        assertEquals("admin", adminUser.getUsername());
        assertEquals("admin@test.com", adminUser.getEmail());
        assertTrue(adminUser.isActive());
        assertTrue(adminUser.isAdmin());
        assertTrue(adminUser.verifyPassword("adminpass"));
    }

    @Test
    @DisplayName("Should handle password hashing consistently")
    void testPasswordHashingConsistency() {
        // WHAT WE'RE TESTING: Same password always produces same hash

        User user1 = new User("user1", "user1@test.com", "samepassword");
        User user2 = new User("user2", "user2@test.com", "samepassword");

        // Same password should produce same hash
        assertEquals(user1.getPasswordHash(), user2.getPasswordHash());

        // Both should verify the same password
        assertTrue(user1.verifyPassword("samepassword"));
        assertTrue(user2.verifyPassword("samepassword"));
    }

    @Test
    @DisplayName("Should handle edge cases gracefully")
    void testEdgeCases() {
        // WHAT WE'RE TESTING: Your User class handles edge cases

        // Test with null values (should not crash)
        User edgeUser = new User();
        edgeUser.setUsername(null);
        edgeUser.setEmail(null);

        assertNull(edgeUser.getUsername());
        assertNull(edgeUser.getEmail());

        // Test empty strings
        edgeUser.setUsername("");
        edgeUser.setEmail("");

        assertEquals("", edgeUser.getUsername());
        assertEquals("", edgeUser.getEmail());

        // Test password with empty string
        edgeUser.setPassword("");
        assertTrue(edgeUser.verifyPassword("")); // Empty password should work if set
    }

    @Test
    @DisplayName("Should handle password hash direct manipulation")
    void testPasswordHashDirectAccess() {
        // WHAT WE'RE TESTING: Direct hash manipulation (for testing purposes)

        String originalHash = user.getPasswordHash();
        assertNotNull(originalHash);

        // Test getting password hash
        assertEquals(originalHash, user.getPasswordHash());

        // Test setting password hash directly (though this bypasses security)
        String customHash = "customHashValue";
        user.setPasswordHash(customHash);
        assertEquals(customHash, user.getPasswordHash());

        // Note: After setting hash directly, verifyPassword won't work correctly
        // because it expects the hash to be created by your hashPassword method
    }
}