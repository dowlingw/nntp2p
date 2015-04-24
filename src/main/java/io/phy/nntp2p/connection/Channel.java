package io.phy.nntp2p.connection;

import io.phy.nntp2p.protocol.NntpDecoder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Channel {
    private Socket socket;
    private OutputStreamWriter osWriter;
    private NntpDecoder reader;
    private BufferedWriter writer;

    public Channel(Socket socket) throws IOException {
        this.socket = socket;

        osWriter = new OutputStreamWriter(socket.getOutputStream());

        reader = new NntpDecoder(socket.getInputStream(), StandardCharsets.UTF_8);
        writer = new BufferedWriter(osWriter);
    }

    public Socket getSocket() {
        return socket;
    }

    public NntpDecoder getReader() {
        return reader;
    }

    public BufferedWriter getWriter() {
        return writer;
    }
}
