package io.phy.nntp2p;

import io.phy.nntp2p.connection.InboundConnection;
import io.phy.nntp2p.configuration.ServerConfigurationItem;
import io.phy.nntp2p.proxy.ArticleProxy;
import io.phy.nntp2p.proxy.UserRepository;
import io.phy.nntp2p.proxy.provider.cache.LocalCache;
import io.phy.nntp2p.proxy.provider.nntp.NntpArticleProvider;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

public class Application {

    protected final static Logger log = Logger.getLogger(Application.class.getName());

    // TODO: If not DI, some kind of configuration source
    private Integer listenPort;
    private ArticleProxy proxy;
    private UserRepository users;

    public Application(LocalCache cache, Integer listenPort, UserRepository users, List<ServerConfigurationItem> outboundPeerConfiguration) throws InvalidObjectException {
        this.users = users;
        this.listenPort = listenPort;

        // Configured NNTP Servers
        proxy = new ArticleProxy();
        for (ServerConfigurationItem config : outboundPeerConfiguration) {
            NntpArticleProvider nntpArticleProvider = new NntpArticleProvider(config);
            proxy.RegisterProvider(nntpArticleProvider);
        }

        // Register the local cache
        proxy.RegisterCache(cache);
    }

    public void RunApplication() throws IOException {
        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        ServerSocket listenSocket = factory.createServerSocket(listenPort);

        // Spawn a new thread for each incoming connection
        while (true) {
            Socket socket = listenSocket.accept();
            new Thread(new InboundConnection(socket,proxy,users)).start();
        }
    }
}