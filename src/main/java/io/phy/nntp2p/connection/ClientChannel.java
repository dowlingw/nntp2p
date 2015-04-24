package io.phy.nntp2p.connection;

import io.phy.nntp2p.protocol.NntpStreamReader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ClientChannel {
    private Socket socket;
    private OutputStreamWriter osWriter;
    private NntpStreamReader reader;
    private BufferedWriter writer;

    public ClientChannel(Socket socket) throws IOException {
        this.socket = socket;

        osWriter = new OutputStreamWriter(socket.getOutputStream());

        reader = new NntpStreamReader(socket.getInputStream(), BaseConnection.NNTP_ENCODING);
        writer = new BufferedWriter(osWriter);
    }

    public Socket getSocket() {
        return socket;
    }

    public NntpStreamReader getReader() {
        return reader;
    }

    public BufferedWriter getWriter() {
        return writer;
    }
}
