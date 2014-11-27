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

    private ArticleProxy proxy;
    private UserRepository users;

    public Application(UserRepository users, List<ServerConfigurationItem> outboundPeerConfiguration) throws InvalidObjectException {
        this.users = users;

        // Configured NNTP Servers
        proxy = new ArticleProxy();
        for (ServerConfigurationItem config : outboundPeerConfiguration) {
            NntpArticleProvider nntpArticleProvider = new NntpArticleProvider(config);
            proxy.RegisterProvider(nntpArticleProvider);
        }

        // Register the local cache
        LocalCache localCache = new LocalCache();
        proxy.RegisterCache(localCache);
    }

    public void RunApplication() throws IOException {
        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        ServerSocket listenSocket = factory.createServerSocket(1337);

        // Spawn a new thread for each incoming connection
        while (true) {
            Socket socket = listenSocket.accept();
            new Thread(new InboundConnection(socket,proxy,users)).start();
        }
    }
}