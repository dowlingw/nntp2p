package io.phy.nntp2p.protocol;

import io.phy.nntp2p.connection.Channel;

import java.io.IOException;
import java.io.OutputStream;

public final class NntpEncoder {

    private static final byte[] CRLF = {0x0D, 0x0A};

    public static void WriteData(Channel channel, NntpProtocolMessage data) throws IOException {
        channel.getWriter().write(data.ToNntpString());
        writeByteArray(channel,CRLF);
        channel.getWriter().flush();
    }
    public static void WriteServerReply(Channel channel, NNTPReply reply) throws IOException {
        WriteData(channel, new ServerResponse(reply));
    }

    public static void WriteArticleBody(Channel channel, Article data) throws IOException {
        writeByteArray(channel,data.getContents());
        writeByteArray(channel,CRLF);
        channel.getWriter().write(".");
        writeByteArray(channel,CRLF);
        channel.getWriter().flush();
    }

    private static void writeByteArray(Channel channel, byte[] data) throws IOException {
        channel.getWriter().flush();
        synchronized (channel.getWriter()) {
            OutputStream outputStream = channel.getSocket().getOutputStream();
            synchronized (outputStream) {
                outputStream.write(data);
                outputStream.flush();
            }
        }
    }

    public static void WriteArticleHead(Channel channel, Article data, boolean continues) throws IOException {
        writeByteArray(channel,data.getHeaders());
        writeByteArray(channel,CRLF);

        if( ! continues ) {
            channel.getWriter().write(".");
            writeByteArray(channel,CRLF);
        }
        channel.getWriter().flush();
    }
}
