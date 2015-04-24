package io.phy.nntp2p.protocol;

import io.phy.nntp2p.common.Article;
import io.phy.nntp2p.common.Channel;

import java.io.IOException;
import java.io.OutputStream;

public final class NntpEncoder {

    private NntpEncoder() {
        // Never to be instantiated!
    }

    private static final byte[] CRLF = {0x0D, 0x0A};

    private static void WriteData(Channel channel, String str) throws IOException {
        channel.getWriter().write(str);
        writeByteArray(channel,CRLF);
        channel.getWriter().flush();
    }

    public static void WriteData(Channel channel, NntpClientCommand data) throws IOException {
        WriteData(channel, ToNntpString(data));
    }

    public static void WriteServerReply(Channel channel, NntpServerReply reply) throws IOException {
        WriteData(channel, ToNntpString(reply));
    }

    public static void WriteServerReply(Channel channel, NntpServerReplyType reply) throws IOException {
        WriteData(channel, ToNntpString(new NntpServerReply(reply)));
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

    public static String ToNntpString(NntpClientCommand cmd) {
        String retString = cmd.getCommand();

        for (String arg : cmd.getArguments()) {
            retString += " " + arg;
        }

        return retString;
    }

    public static String ToNntpString(NntpServerReply response) {
        String s = response.getResponseCode().responseCode.toString();
        if( ! response.getSections().isEmpty() ) {
            s += " " + String.join(" ",response.getSections());
        }
        return s;
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
