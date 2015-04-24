package io.phy.nntp2p;

import io.phy.nntp2p.commands.AuthinfoCommand;
import io.phy.nntp2p.commands.BodyCommand;
import io.phy.nntp2p.commands.QuitCommand;
import io.phy.nntp2p.configuration.ServerConfigurationItem;
import io.phy.nntp2p.connection.ClientChannel;
import io.phy.nntp2p.connection.ProxyServer;
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

public class Application {

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

        ProxyServer server = new ProxyServer(proxy,users);
        server.RegisterCommandHandler(new AuthinfoCommand(users));
        server.RegisterCommandHandler(new BodyCommand(proxy));
        server.RegisterCommandHandler(new QuitCommand());

        // Spawn a new thread for each incoming connection
        while (true) {
            Socket socket = listenSocket.accept();
            new Thread(new ProxyServerWrapper(server, socket)).start();
        }
    }

    private class ProxyServerWrapper implements Runnable {
        private ProxyServer server;
        private ClientChannel socket;

        private ProxyServerWrapper(ProxyServer server, Socket socket) throws IOException {
            this.server = server;
            this.socket = new ClientChannel(socket);
        }

        @Override
        public void run() {
            server.run(socket);
        }
    }
}