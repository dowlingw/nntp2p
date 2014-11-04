package io.phy.nntp2p.proxy.provider.nntp;

import io.phy.nntp2p.configuration.ServerConfigurationItem;
import io.phy.nntp2p.connection.OutboundConnection;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class NntpConnectionFactory implements PooledObjectFactory<OutboundConnection> {

    public NntpConnectionFactory(ServerConfigurationItem config) {
        throw new NotImplementedException();
    }

    @Override
    public PooledObject<OutboundConnection> makeObject() throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public void destroyObject(PooledObject<OutboundConnection> outboundConnectionPooledObject) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public boolean validateObject(PooledObject<OutboundConnection> outboundConnectionPooledObject) {
        throw new NotImplementedException();
    }

    @Override
    public void activateObject(PooledObject<OutboundConnection> outboundConnectionPooledObject) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public void passivateObject(PooledObject<OutboundConnection> outboundConnectionPooledObject) throws Exception {
        throw new NotImplementedException();
    }
}
