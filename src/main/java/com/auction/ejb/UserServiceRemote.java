package com.auction.ejb;

import com.auction.entity.User;
import javax.ejb.Remote;
import java.util.List;

@Remote
public interface UserServiceRemote {
    User registerUser(String username, String email);
    User getUserByUsername(String username);
    boolean authenticateUser(String username);
    List<User> getAllActiveUsers();
    boolean updateUserActivity(String username);
    boolean deactivateUser(String username);
    int getActiveUserCount();
    List<User> getUsersInAuction(Long auctionId);
}