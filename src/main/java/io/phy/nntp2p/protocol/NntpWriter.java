package io.phy.nntp2p.protocol;

import io.phy.nntp2p.connection.ClientChannel;

import java.io.IOException;
import java.io.OutputStream;

public final class NntpWriter {

    private static final byte[] CRLF = {0x0D, 0x0A};

    public static void WriteData(ClientChannel channel, NntpProtocolMessage data) throws IOException {
        channel.getWriter().write(data.ToNntpString());
        writeByteArray(channel,CRLF);
        channel.getWriter().flush();
    }
    public static void WriteServerReply(ClientChannel channel, NNTPReply reply) throws IOException {
        WriteData(channel, new ServerResponse(reply));
    }

    public static void WriteArticleBody(ClientChannel channel, Article data) throws IOException {
        writeByteArray(channel,data.getContents());
        writeByteArray(channel,CRLF);
        channel.getWriter().write(".");
        writeByteArray(channel,CRLF);
        channel.getWriter().flush();
    }

    private static void writeByteArray(ClientChannel channel, byte[] data) throws IOException {
        channel.getWriter().flush();
        synchronized (channel.getWriter()) {
            OutputStream outputStream = channel.getSocket().getOutputStream();
            synchronized (outputStream) {
                outputStream.write(data);
                outputStream.flush();
            }
        }
    }

    public static void WriteArticleHead(ClientChannel channel, Article data, boolean continues) throws IOException {
        writeByteArray(channel,data.getHeaders());
        writeByteArray(channel,CRLF);

        if( ! continues ) {
            channel.getWriter().write(".");
            writeByteArray(channel,CRLF);
        }
        channel.getWriter().flush();
    }
}
