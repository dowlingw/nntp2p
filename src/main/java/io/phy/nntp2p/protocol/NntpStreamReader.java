package io.phy.nntp2p.protocol;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class NntpStreamReader extends DataInputStream {

    private String nntpEncoding;

    public NntpStreamReader(InputStream stream, String nntpEncoding) {
        super(stream);
        this.nntpEncoding = nntpEncoding;
    }

    private ByteArrayOutputStream readByteLine() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        byte readByte;
        boolean prevWasCR = false;
        synchronized(this) {
            while((readByte = this.readByte()) != -1) {
                boolean thisIsCR = (readByte == 0x0D);
                boolean thisIsLF = (readByte == 0x0A);

                if( prevWasCR ) {
                    if(thisIsLF) {
                        return bytes;
                    }
                    bytes.write(0x0D);
                }

                if( ! thisIsCR ) {
                    bytes.write(readByte);
                }
                prevWasCR = thisIsCR;
            }
        }
        return bytes;
    }

    public byte[] readLineBytes() throws IOException {
        return readByteLine().toByteArray();
    }

    public String readLineString() throws IOException {
        return readByteLine().toString(nntpEncoding);
    }
}
