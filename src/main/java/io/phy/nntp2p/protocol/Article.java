package io.phy.nntp2p.protocol;

/**
 * A really simple Article class.
 * We don't care about interpreting the header or contents chunk - so we just store them as a chunk of data including CRLF
 */
public class Article {
    private byte[] headers;
    private byte[] contents;

    public Article(byte[] headerBytes, byte[] dataBytes) {
        headers = headerBytes;
        contents = dataBytes;
    }

    public byte[] getHeaders() {
        return headers;
    }

    public void setHeaders(byte[] headers) {
        this.headers = headers;
    }

    public byte[] getContents() {
        return contents;
    }

    public void setContents(byte[] contents) {
        this.contents = contents;
    }
}
