package io.phy.nntp2p.proxy;

import io.phy.nntp2p.configuration.User;

import java.util.HashMap;

public class UserRepository {
    private HashMap<String,User> users;

    public UserRepository() {
        users = new HashMap<>();
    }

    public void AddUser(User user) {
        users.put(user.getUsername().toLowerCase(),user);
    }

    // TODO: Support password crypting
    // TODO: be more explicit about username format
    public User authenticate(String userSpecified, String password) {
        User user = users.get(userSpecified.toLowerCase());
        if( user != null ) {
            if( user.getPassword().equals(password) ) {
                return user;
            }
        }

        return null;
    }
}
