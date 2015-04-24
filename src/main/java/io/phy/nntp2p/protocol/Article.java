package io.phy.nntp2p.protocol;

import java.io.Serializable;

/**
 * A really simple Article class.
 * We don't care about interpreting the header or contents chunk - so we just store them as a chunk of data including CRLF
 */
public class  Article implements Serializable {
    private String messageId;
    private byte[] headers;
    private byte[] contents;

    public Article(String messageId, byte[] headerBytes, byte[] dataBytes) {
        this.messageId = messageId;
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

    public String getMessageId() {
        return messageId;
    }
}
