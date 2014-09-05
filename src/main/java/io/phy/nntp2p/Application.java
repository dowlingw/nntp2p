package io.phy.nntp2p;

import io.phy.nntp2p.configuration.ConnectionType;
import io.phy.nntp2p.connection.InboundConnection;
import io.phy.nntp2p.connection.OutboundConnection;
import io.phy.nntp2p.configuration.ServerConfigurationItem;
import io.phy.nntp2p.pool.OutboundConnectionPoolFactory;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

public class Application {

    private GenericKeyedObjectPool<ConnectionType,OutboundConnection> outboundPeers;

    protected final static Logger log = Logger.getLogger(Application.class.getName());

    public Application(List<ServerConfigurationItem> outboundPeerConfiguration)
    {
        // Create the factory and pool for outbound connections
        OutboundConnectionPoolFactory factory = new OutboundConnectionPoolFactory(outboundPeerConfiguration);
        outboundPeers = new GenericKeyedObjectPool<ConnectionType, OutboundConnection>(factory);
        outboundPeers.setMaxTotal(factory.getAggregateMaximumNumberConnections());
    }

    public void RunApplication() throws IOException {
        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        ServerSocket listenSocket = factory.createServerSocket(119);

        // Spawn a new thread for each incoming connection
        while (true) {
            Socket socket = listenSocket.accept();
            log.info("Accepted new connection: "+socket.toString());
            new Thread(new InboundConnection(socket,this)).start();
        }
    }

    public GenericKeyedObjectPool<ConnectionType, OutboundConnection> getOutboundPeers() {
        return outboundPeers;
    }
}