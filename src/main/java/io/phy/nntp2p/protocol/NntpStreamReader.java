package io.phy.nntp2p.protocol;

import java.io.*;

public class NntpStreamReader extends DataInputStream {

    private String nntpEncoding;

    public NntpStreamReader(InputStream stream, String nntpEncoding) {
        super(stream);
        this.nntpEncoding = nntpEncoding;
    }

    private ByteArrayOutputStream readByteLine() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        boolean prevWasCR = false;
        synchronized(this) {
            try {
                while(true) {
                    int readByte = this.readUnsignedByte();

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
