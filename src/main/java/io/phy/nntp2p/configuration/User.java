package io.phy.nntp2p.configuration;

public class User {
    private String username;
    private String password;
    private boolean isCache;


    public User(String username, String password, boolean isCache) {
        this.username = username;
        this.password = password;
        this.isCache = isCache;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isCache() {
        return isCache;
    }

    public void setCache(boolean isCache) {
        this.isCache = isCache;
    }
}
