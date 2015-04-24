package io.phy.nntp2p.protocol;

import io.phy.nntp2p.exceptions.NntpUnknownResponseException;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ServerResponse implements NntpProtocolMessage {
    private NntpReply responseCode;
    private List<String> sections = new ArrayList<>();

    public ServerResponse(NntpReply command) {
        this.responseCode = command;
    }

    public void addArg(String arg) {
        sections.add(arg);
    }

    public void addArg(Integer arg) {
        sections.add(arg.toString());
    }

    @Override
    public String ToNntpString() {
        String s = responseCode.responseCode.toString();
        if( ! sections.isEmpty() ) {
            s += " " + String.join(" ",sections);
        }
        return s;
    }

    public NntpReply getResponseCode() {
        return this.responseCode;
    }

    public static ServerResponse Parse(String input) throws NntpUnknownResponseException {
        String[] data = input.split(" ");
        if( data.length < 1 ) {
            // TODO: Fix this exception
            throw new IllegalThreadStateException("wahh");
        }

        // See if the command is one we know about
        NntpReply command;
        try {
            command = NntpReply.Resolve(Integer.parseInt(data[0]));
        } catch (IllegalArgumentException e) {
            throw new NntpUnknownResponseException();
        }

        return new ServerResponse(command);
    }

    public static ServerResponse Parse(NntpDecoder reader) throws NntpUnknownResponseException, UnsupportedEncodingException {
        return ServerResponse.Parse(reader.readLineString());
    }

    public static byte[] ReadMultiLine(NntpDecoder reader, boolean emptyLineTermination) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        while( true ) {
            byte[] raw = reader.readLineBytes();

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
}
