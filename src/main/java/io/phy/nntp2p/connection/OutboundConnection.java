package io.phy.nntp2p.connection;

import io.phy.nntp2p.configuration.ServerConfigurationItem;

public class OutboundConnection {
    private ServerConfigurationItem configuration;

    public OutboundConnection(ServerConfigurationItem configuration) {
        this.configuration = configuration;
    }

    public ServerConfigurationItem getConfiguration() {
        return configuration;
    }
}
