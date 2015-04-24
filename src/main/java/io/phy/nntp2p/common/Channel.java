package io.phy.nntp2p.common;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Channel {
    private Socket socket;
    private OutputStreamWriter osWriter;
    private DataInputStream reader;
    private BufferedWriter writer;

    public Channel(Socket socket) throws IOException {
        this.socket = socket;

        osWriter = new OutputStreamWriter(socket.getOutputStream());

        reader = new DataInputStream(socket.getInputStream());
        writer = new BufferedWriter(osWriter);
    }

    public Socket getSocket() {
        return socket;
    }

    public DataInputStream getReader() {
        return reader;
    }

    public BufferedWriter getWriter() {
        return writer;
    }
}
