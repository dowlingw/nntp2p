package io.phy.nntp2p.proxy.provider.nntp;

import io.phy.nntp2p.configuration.ServerConfigurationItem;
import io.phy.nntp2p.connection.OutboundConnection;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class NntpConnectionFactory extends BasePooledObjectFactory<OutboundConnection> {

    private ServerConfigurationItem config;

    public NntpConnectionFactory(ServerConfigurationItem config) {
        this.config = config;
    }

    @Override
    public OutboundConnection create() throws Exception {
        return new OutboundConnection(config);
    }

    @Override
    public PooledObject<OutboundConnection> wrap(OutboundConnection outboundConnection) {
        return new DefaultPooledObject<>(outboundConnection);
    }
}
