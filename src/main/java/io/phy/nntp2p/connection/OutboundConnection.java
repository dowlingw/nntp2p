package io.phy.nntp2p.connection;

import io.phy.nntp2p.configuration.ServerConfigurationItem;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;

public class OutboundConnection {
    private ServerConfigurationItem configuration;

    private Socket clientSocket;

    public OutboundConnection(ServerConfigurationItem configuration) {
        this.configuration = configuration;
    }

    public ServerConfigurationItem getConfiguration() {
        return configuration;
    }

    public void Connect() throws IOException {
        SocketFactory factory = configuration.isUseSsl() ? SSLSocketFactory.getDefault() : SocketFactory.getDefault();
        clientSocket = factory.createSocket(configuration.getHostname(),configuration.getPort());

        // If SSL, do a handshake
        if( configuration.isUseSsl() ) {
            ((SSLSocket)clientSocket).startHandshake();
        }
    }

    public void GetArticle() {
        throw new NotImplementedException();
    }

    public void GetArticle(String s) {
        throw new NotImplementedException();
    }
}
