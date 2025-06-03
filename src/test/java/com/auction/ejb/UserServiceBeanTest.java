package com.auction.ejb;

import com.auction.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UPDATED UserServiceBeanTest - Fixed to match your User class implementation
 */
class UserServiceBeanTest {

    private UserServiceBean userService;
    private ConcurrentHashMap<String, User> originalUsers;

    @BeforeEach
    void setUp() throws Exception {
        userService = new UserServiceBean();

        // Access static users field
        Field usersField = UserServiceBean.class.getDeclaredField("users");
        usersField.setAccessible(true);
        originalUsers = (ConcurrentHashMap<String, User>) usersField.get(null);
        originalUsers.clear();

        // Manually call @PostConstruct
        userService.init();
    }

    @AfterEach
    void tearDown() {
        originalUsers.clear();
    }

    @Test
    @DisplayName("Should register new user successfully")
    void testRegisterUser() {
        // WHAT WE'RE TESTING: User registration works

        User result = userService.registerUser("newuser", "user@test.com", "password123");

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("user@test.com", result.getEmail());
        assertTrue(result.isActive());
        assertFalse(result.isAdmin());

        // Verify password was hashed correctly using your User class
        assertTrue(result.verifyPassword("password123"));
        // Verify it's actually hashed (not plain text)
        assertNotEquals("password123", result.getPasswordHash());
    }

    @Test
    @DisplayName("Should prevent duplicate username registration")
    void testDuplicateUsernameRegistration() {
        // WHAT WE'RE TESTING: Business rule - usernames must be unique

        userService.registerUser("testuser", "user1@test.com", "password123");

        // Try to register same username again
        User duplicate = userService.registerUser("testuser", "user2@test.com", "differentpassword");

        assertNull(duplicate); // Should fail
    }

    @Test
    @DisplayName("Should prevent duplicate email registration")
    void testDuplicateEmailRegistration() {
        // WHAT WE'RE TESTING: Business rule - emails must be unique

        userService.registerUser("user1", "test@example.com", "password123");

        // Try to register same email again
        User duplicate = userService.registerUser("user2", "test@example.com", "differentpassword");

        assertNull(duplicate); // Should fail
    }

    @Test
    @DisplayName("Should authenticate user with correct credentials")
    void testSuccessfulAuthentication() {
        // WHAT WE'RE TESTING: Login works with correct credentials

        userService.registerUser("testuser", "test@example.com", "password123");

        assertTrue(userService.authenticateUser("testuser", "password123"));
        // Test email authentication if your service supports it
        // assertTrue(userService.authenticateUser("test@example.com", "password123"));
    }

    @Test
    @DisplayName("Should reject authentication with wrong credentials")
    void testFailedAuthentication() {
        // WHAT WE'RE TESTING: Security - wrong passwords are rejected

        userService.registerUser("testuser", "test@example.com", "password123");

        assertFalse(userService.authenticateUser("testuser", "wrongpassword"));
        assertFalse(userService.authenticateUser("nonexistentuser", "password123"));
        assertFalse(userService.authenticateUser("testuser", ""));
        assertFalse(userService.authenticateUser("testuser", null));
    }

    @Test
    @DisplayName("Should change password with correct old password")
    void testSuccessfulPasswordChange() {
        // WHAT WE'RE TESTING: Password change security

        userService.registerUser("testuser", "test@example.com", "oldpassword");

        boolean result = userService.changePassword("testuser", "oldpassword", "newpassword123");

        assertTrue(result);

        // Old password should no longer work
        assertFalse(userService.authenticateUser("testuser", "oldpassword"));

        // New password should work
        assertTrue(userService.authenticateUser("testuser", "newpassword123"));
    }

    @Test
    @DisplayName("Should reject password change with wrong old password")
    void testFailedPasswordChange() {
        // WHAT WE'RE TESTING: Security - can't change password without knowing current one

        userService.registerUser("testuser", "test@example.com", "password123");

        boolean result = userService.changePassword("testuser", "wrongoldpassword", "newpassword");

        assertFalse(result);

        // Original password should still work
        assertTrue(userService.authenticateUser("testuser", "password123"));
    }

    @Test
    @DisplayName("Should reject short passwords")
    void testPasswordLengthValidation() {
        // WHAT WE'RE TESTING: Password complexity rules

        User result = userService.registerUser("testuser", "test@example.com", "123"); // Too short
        assertNull(result);

        // Test password change with short password
        userService.registerUser("validuser", "valid@example.com", "validpassword");
        boolean changeResult = userService.changePassword("validuser", "validpassword", "123");
        assertFalse(changeResult);
    }

    @Test
    @DisplayName("Should check admin status correctly")
    void testAdminStatusCheck() {
        // WHAT WE'RE TESTING: Admin privilege checking

        // Regular user
        userService.registerUser("regularuser", "user@test.com", "password123");
        assertFalse(userService.isUserAdmin("regularuser"));

        // Check default admin (created in init())
        assertTrue(userService.isUserAdmin("admin@auction.com"));
    }

    @Test
    @DisplayName("Should verify default users created in init()")
    void testInitialUsersCreation() {
        // WHAT WE'RE TESTING: Your init() method creates expected users

        // Check admin user exists
        assertTrue(userService.authenticateUser("admin@auction.com", "11010001"));
        assertTrue(userService.isUserAdmin("admin@auction.com"));

        // Check sample users exist with default password "1234"
        assertTrue(userService.authenticateUser("john_doe", "1234"));
        assertTrue(userService.authenticateUser("jane_smith", "1234"));
        assertTrue(userService.authenticateUser("bob_wilson", "1234"));
        assertTrue(userService.authenticateUser("alice_brown", "1234"));

        // Verify they are not admins
        assertFalse(userService.isUserAdmin("john_doe"));
        assertFalse(userService.isUserAdmin("jane_smith"));
    }

    @Test
    @DisplayName("Should get all active users")
    void testGetAllActiveUsers() {
        // WHAT WE'RE TESTING: User listing functionality

        userService.registerUser("user1", "user1@test.com", "password123");
        userService.registerUser("user2", "user2@test.com", "password123");

        List<User> activeUsers = userService.getAllActiveUsers();

        // Should include our 2 users + default admin + sample users from init()
        assertTrue(activeUsers.size() >= 2);

        // All returned users should be active
        for (User user : activeUsers) {
            assertTrue(user.isActive());
        }
    }

    @Test
    @DisplayName("Should deactivate user successfully")
    void testUserDeactivation() {
        // WHAT WE'RE TESTING: User deactivation functionality

        userService.registerUser("testuser", "test@example.com", "password123");

        boolean result = userService.deactivateUser("testuser");
        assertTrue(result);

        // User should not be able to authenticate when deactivated
        assertFalse(userService.authenticateUser("testuser", "password123"));

        // User should not appear in active users list
        List<User> activeUsers = userService.getAllActiveUsers();
        boolean foundDeactivatedUser = activeUsers.stream()
                .anyMatch(user -> "testuser".equals(user.getUsername()));
        assertFalse(foundDeactivatedUser);
    }

    @Test
    @DisplayName("Should count active users correctly")
    void testActiveUserCount() {
        // WHAT WE'RE TESTING: User counting logic

        int initialCount = userService.getActiveUserCount();

        userService.registerUser("user1", "user1@test.com", "password123");
        userService.registerUser("user2", "user2@test.com", "password123");

        assertEquals(initialCount + 2, userService.getActiveUserCount());

        // Deactivate one user
        userService.deactivateUser("user1");

        assertEquals(initialCount + 1, userService.getActiveUserCount());
    }
}