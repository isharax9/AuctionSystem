package com.auction.ejb;

import com.auction.entity.User;
import jakarta.ejb.Remote;

import java.util.List;

@Remote
public interface UserServiceRemote {
    User registerUser(String username, String email, String password);
    User getUserByUsername(String username);
    boolean authenticateUser(String username, String password);
    boolean isUserAdmin(String username);
    List<User> getAllActiveUsers();
    boolean updateUserActivity(String username);
    boolean deactivateUser(String username);
    int getActiveUserCount();
    List<User> getUsersInAuction(Long auctionId);
    boolean changePassword(String username, String oldPassword, String newPassword);
    boolean resetPassword(String username, String newPassword);
}