package io.phy.nntp2p.configuration;

import io.phy.nntp2p.proxy.UserRepository;

import java.util.List;

public class Settings {
    private UserRepository userRepository;
    private List<NntpServerDetails> servers;
    private Integer listenPort;
    private Integer cacheMemoryArticleLimit;
    private String cacheDiskLocation;
    private Integer cacheDiskSizeLimit;

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<NntpServerDetails> getServers() {
        return servers;
    }

    public void setServers(List<NntpServerDetails> servers) {
        this.servers = servers;
    }

    public Integer getListenPort() {
        return listenPort;
    }

    public void setListenPort(Integer listenPort) {
        this.listenPort = listenPort;
    }

    public Integer getCacheMemoryArticleLimit() {
        return cacheMemoryArticleLimit;
    }

    public void setCacheMemoryArticleLimit(Integer cacheMemoryArticleLimit) {
        this.cacheMemoryArticleLimit = cacheMemoryArticleLimit;
    }

    public String getCacheDiskLocation() {
        return cacheDiskLocation;
    }

    public void setCacheDiskLocation(String cacheDiskLocation) {
        this.cacheDiskLocation = cacheDiskLocation;
    }

    public Integer getCacheDiskSizeLimit() {
        return cacheDiskSizeLimit;
    }

    public void setCacheDiskSizeLimit(Integer cacheDiskSizeLimit) {
        this.cacheDiskSizeLimit = cacheDiskSizeLimit;
    }
}
