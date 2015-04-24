package io.phy.nntp2p.protocol;

import java.io.*;
import java.nio.charset.Charset;

public class NntpDecoder extends DataInputStream {

    private String nntpEncoding;

    public NntpDecoder(InputStream stream, Charset nntpEncoding) {
        super(stream);
        this.nntpEncoding = nntpEncoding.name();
    }

    private ByteArrayOutputStream readByteLine() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        boolean prevWasCR = false;
        synchronized(this) {
            try {
                while(true) {
                    byte readByte = this.readByte();

                    boolean thisIsCR = (readByte == 0x0D);
                    boolean thisIsLF = (readByte == 0x0A);

                    if( prevWasCR ) {
                        if(thisIsLF) {
                            break;
                        }
                        bytes.write(0x0D);
                    }

                    if( ! thisIsCR ) {
                        bytes.write(readByte);
                    }
                    prevWasCR = thisIsCR;
                }
            } catch (IOException e) {
                // Do nothing, we've reached the end of what we're getting
            }
        }
        return bytes;
    }

    public byte[] readLineBytes() {
        return readByteLine().toByteArray();
    }

    public String readLineString() throws UnsupportedEncodingException {
        return readByteLine().toString(nntpEncoding);
    }
}
