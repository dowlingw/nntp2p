package io.phy.nntp2p.connection;

import io.phy.nntp2p.protocol.NntpProtocolMessage;
import org.apache.commons.net.SocketClient;
import org.apache.commons.net.io.CRLFLineReader;

import java.io.*;
import java.net.Socket;

public abstract class BaseConnection {
    protected Socket socket;

    private static final String NNTP_ENCODING = "UTF-8";

    protected BufferedReader reader;
    protected BufferedWriter writer;

    public BaseConnection(Socket underlyingSocket) throws IOException {
        socket = underlyingSocket;

        // Wire up the
        reader = new CRLFLineReader(new InputStreamReader(socket.getInputStream(), NNTP_ENCODING));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void WriteData(NntpProtocolMessage data) throws IOException {
        writer.write(data.ToNntpString());
        writer.write(SocketClient.NETASCII_EOL);
        writer.flush();
    }
}
