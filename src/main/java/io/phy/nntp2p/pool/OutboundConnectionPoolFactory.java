package io.phy.nntp2p.pool;

import io.phy.nntp2p.configuration.ConnectionType;
import io.phy.nntp2p.connection.OutboundConnection;
import io.phy.nntp2p.configuration.ServerConfigurationItem;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A very dangerous and naive pool factory that enforces our configuration
 *
 * The GenericKeyedObjectPool<K,V> handles thread safety for calling into this.
 * So we SHOULD be ok to do the bare minimum... hopefully.
 *
 * This keeps a map of configuration objects and the number of objects the factory has spawned for each one.
 * Requesting a new object by type will read the configuration and spin up any object if required.
 *
 */
public class OutboundConnectionPoolFactory extends BaseKeyedPooledObjectFactory<ConnectionType,OutboundConnection> {

    private HashMap<ServerConfigurationItem,ConfigurationState> track;

    public OutboundConnectionPoolFactory(List<ServerConfigurationItem> peers) {
        track = new HashMap<ServerConfigurationItem, ConfigurationState>(peers.size());
        for (ServerConfigurationItem peer: peers)
        {
            track.put(peer,new ConfigurationState());
        }
    }

    public Integer getAggregateMaximumNumberConnections() {
        Integer numConnections = 0;
        for( Map.Entry<ServerConfigurationItem,ConfigurationState> entry : track.entrySet() ) {
            numConnections += entry.getKey().getMaxConnections();
        }

        return numConnections;
    }

    public Integer getAggregateMaximumNumberConnections(ConnectionType connectionType) {
        Integer numConnections = 0;
        for( Map.Entry<ServerConfigurationItem,ConfigurationState> entry : track.entrySet() ) {
            if( entry.getKey().getConnectionType() == connectionType ) {
                numConnections += entry.getKey().getMaxConnections();
            }
        }

        return numConnections;
    }

    @Override
    public OutboundConnection create(ConnectionType connectionType) throws Exception {
        // Find a matching configuration we can use
        ServerConfigurationItem configurationToUse = null;
        for( Map.Entry<ServerConfigurationItem,ConfigurationState> entry : track.entrySet() ) {
            if( entry.getKey().getConnectionType() == connectionType ) {
                if( entry.getValue().getNumberAssignedConnections() < entry.getKey().getMaxConnections() ) {
                    configurationToUse = entry.getKey();
                }
            }
        }

        if( configurationToUse == null ) {
            throw new Exception("Configuration does not allow any more connections of the type requested");
        }

        track.get(configurationToUse).incrementNumberAssignedConnections();
        OutboundConnection connection = new OutboundConnection(configurationToUse);
        connection.Connect();

        return connection;
    }

    @Override
    public PooledObject<OutboundConnection> wrap(OutboundConnection outboundConnection) {
        return new DefaultPooledObject<OutboundConnection>(outboundConnection);
    }

    @Override
    public void destroyObject(ConnectionType key, PooledObject<OutboundConnection> p) {
        ServerConfigurationItem config = p.getObject().getConfiguration();
        track.get(config).decrementNumberAssignedConnections();
    }
}

class ConfigurationState {
    private Integer numberAssignedConnections;

    public ConfigurationState() {
        numberAssignedConnections = 0;
    }

    public Integer getNumberAssignedConnections() {
        return numberAssignedConnections;
    }

    public void incrementNumberAssignedConnections() {
        numberAssignedConnections++;
    }

    public void decrementNumberAssignedConnections() {
        numberAssignedConnections--;
    }
}