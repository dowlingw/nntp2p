package io.phy.nntp2p.protocol;

import io.phy.nntp2p.common.Channel;
import io.phy.nntp2p.exceptions.NntpUnknownCommandException;
import io.phy.nntp2p.exceptions.NntpUnknownResponseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public final class NntpDecoder {

    private static String nntpEncoding = StandardCharsets.UTF_8.name();

    private NntpDecoder() {
        // Never to be instantiated!
    }

    public static NntpClientCommand ParseCommand(String input) throws NntpUnknownCommandException {
        String[] data = input.split(" ");
        if( data.length < 1 ) {
            // TODO: Fix this exception
            throw new IllegalThreadStateException("wahh");
        }

        NntpClientCommand clientCommand = new NntpClientCommand(data[0]);
        for (int i=1; i<data.length; i++ ) {
            clientCommand.getArguments().add(data[i]);
        }

        return clientCommand;
    }

    public static byte[] ReadMultiLine(Channel channel, boolean emptyLineTermination) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        while( true ) {
            byte[] raw = readLineBytes(channel);

            if( emptyLineTermination ) {
                if( raw.length == 0 ) {
                    break;
                }
            } else {
                if(  raw.length == 1 && raw[0] == 0x2E ) {
                    break;
                }
            }

            if( bytes.size() > 0 ) {
                bytes.write(0x0D);
                bytes.write(0x0A);
            }
            bytes.write(raw,0,raw.length);
        }

        return bytes.toByteArray();
    }

    private static ByteArrayOutputStream readByteLine(Channel channel) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        boolean prevWasCR = false;
        synchronized(channel.getReader()) {    // Shit, what do we synchronise on
            try {
                while(true) {
                    byte readByte = channel.getReader().readByte();

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

    public static byte[] readLineBytes(Channel channel) {
        return readByteLine(channel).toByteArray();
    }

    public static String readLineString(Channel channel) throws UnsupportedEncodingException {
        return readByteLine(channel).toString(nntpEncoding);
    }

    public static NntpServerReply Parse(String input) throws NntpUnknownResponseException {
        String[] data = input.split(" ");
        if( data.length < 1 ) {
            // TODO: Fix this exception
            throw new IllegalThreadStateException("wahh");
        }

        // See if the command is one we know about
        NntpServerReplyType command;
        try {
            command = NntpServerReplyType.Resolve(Integer.parseInt(data[0]));
        } catch (IllegalArgumentException e) {
            throw new NntpUnknownResponseException(data[0]);
        }

        return new NntpServerReply(command);
    }

    public static NntpServerReply Parse(Channel channel) throws NntpUnknownResponseException, UnsupportedEncodingException {
        return Parse(readLineString(channel));
    }
}
