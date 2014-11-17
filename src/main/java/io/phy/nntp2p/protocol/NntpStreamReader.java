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
//        // ORIGINAL
//        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//
//        byte readByte;
//        boolean prevWasCR = false;
//        synchronized(this) {
//            for(; (readByte = this.readByte()) != -1;) {
//                if(prevWasCR && readByte == 10) {
//                    return bytes;
//                }
//
//                bytes.write(readByte);
//                prevWasCR = (readByte == 13);
//            }
//        }
//        return bytes;


        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        byte readByte;
        boolean prevWasCR = false;
        synchronized(this) {
            int bc = 0;
            while((readByte = this.readByte()) != -1) {
                // Temp debugginglah
                if( bc >= 82 ) {
                    System.out.println("I'm being naughty!");
                }
                bc++;
                if( prevWasCR ) {
                    if(readByte == 0x0A) {
                        return bytes;
                    }
                    bytes.write(0x0D);
                }

                prevWasCR = (readByte == 0x0D);
                if( ! prevWasCR ) {
                    bytes.write(readByte);
                }
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
