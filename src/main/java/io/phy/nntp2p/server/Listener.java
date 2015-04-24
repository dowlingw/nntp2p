package io.phy.nntp2p.server;

import io.phy.nntp2p.common.Channel;
import io.phy.nntp2p.configuration.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Component
public class Listener implements CommandLineRunner {

    @Autowired
    Settings settings;

    @Autowired
    ProxyServer server;

    @Override
    public void run(String... strings) throws Exception {
        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        ServerSocket listenSocket = factory.createServerSocket(settings.getListenPort());

        while (true) {
            Socket socket = listenSocket.accept();
            new Thread(new ProxyServerWrapper(server, socket)).start();
        }
    }

    private class ProxyServerWrapper implements Runnable {
        private ProxyServer server;
        private Channel socket;
        private ProxyServerWrapper(ProxyServer server, Socket socket) throws IOException {
            this.server = server;
            this.socket = new Channel(socket);
        }

        @Override
        public void run() {
            server.run(socket);
        }
    }

}