package io.phy.nntp2p.configuration;

/**
 * Represents the configuration of an upstream server
 */
public class ServerConfigurationItem {
    private String hostname;
    private Integer port;
    private boolean useSsl;
    private Integer maxConnections;
    private ConnectionType connectionType;

    private PasswordCredential credentials;

    public ServerConfigurationItem(String hostname, Integer port, boolean useSsl, Integer maxConnections, ConnectionType connectionType) {
        this.hostname = hostname;
        this.port = port;
        this.useSsl = useSsl;
        this.maxConnections = maxConnections;
        this.connectionType = connectionType;
    }

    public ServerConfigurationItem(String hostname, Integer port, boolean useSsl, Integer maxConnections, ConnectionType connectionType, PasswordCredential credential) {
        this.hostname = hostname;
        this.port = port;
        this.useSsl = useSsl;
        this.maxConnections = maxConnections;
        this.connectionType = connectionType;
        this.credentials = credential;
    }

    public String getHostname() {
        return hostname;
    }

    public Integer getPort() {
        return port;
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public PasswordCredential getCredentials() {
        return credentials;
    }

    public void setCredentials(PasswordCredential credentials) {
        this.credentials = credentials;
    }
}
