package io.phy.nntp2p.connection;

import io.phy.nntp2p.protocol.Article;
import io.phy.nntp2p.protocol.NntpProtocolMessage;
import io.phy.nntp2p.protocol.NntpStreamReader;

import java.io.*;
import java.net.Socket;

public abstract class BaseConnection {
    protected Socket socket;

    protected static final String NNTP_ENCODING = "UTF-8";
    private static final byte[] CRLF = {0x0D, 0x0A};

    protected NntpStreamReader reader;
    protected BufferedWriter writer;

    private OutputStreamWriter osWriter;

    protected boolean is_valid = true;

    protected void BindToSocket(Socket underlyingSocket) throws IOException {
        socket = underlyingSocket;

        osWriter = new OutputStreamWriter(socket.getOutputStream());

        reader = new NntpStreamReader(socket.getInputStream(), NNTP_ENCODING);
        writer = new BufferedWriter(osWriter);
    }

    public void WriteData(NntpProtocolMessage data) throws IOException {
        writer.write(data.ToNntpString());
        writeByteArray(CRLF);
        writer.flush();
    }

    public void WriteArticleHead(Article data, boolean continues) throws IOException {
        writeByteArray(data.getHeaders());
        writeByteArray(CRLF);

        if( ! continues ) {
            writer.write(".");
            writeByteArray(CRLF);
        }
        writer.flush();
    }

    public void WriteArticleBody(Article data) throws IOException {
        writeByteArray(data.getContents());
        writeByteArray(CRLF);
        writer.write(".");
        writeByteArray(CRLF);
        writer.flush();
    }

    private void writeByteArray(byte[] data) throws IOException {
        writer.flush();
        synchronized (osWriter) {
            OutputStream outputStream = socket.getOutputStream();
            synchronized (outputStream) {
                outputStream.write(data);
                outputStream.flush();
            }
        }
    }

    public boolean isValid() {
        return is_valid;
    }
}
