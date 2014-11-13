package io.phy.nntp2p.protocol;

import org.apache.commons.net.SocketClient;

import java.util.List;

/**
 * A really simple Article class.
 * We don't care about interpreting the header or contents chunk - so we just store them as a chunk of data including CRLF
 */
public class Article {
    private String headers;
    private String contents;

    public Article() {
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public void setHeaders(List<String> lines) {
        headers = String.join(SocketClient.NETASCII_EOL, lines);
    }

    public void setContents(List<String> lines) {
        contents = String.join(SocketClient.NETASCII_EOL, lines);
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
}
