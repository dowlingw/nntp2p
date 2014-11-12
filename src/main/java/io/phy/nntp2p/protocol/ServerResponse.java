package io.phy.nntp2p.protocol;

import io.phy.nntp2p.exceptions.NntpUnknownResponseException;

import java.io.BufferedReader;
import java.io.IOException;

public class ServerResponse implements NntpProtocolMessage {
    private NNTPReply responseCode;

    public ServerResponse(NNTPReply command) {
        this.responseCode = command;
    }

    @Override
    public String ToNntpString() {
        return String.valueOf(responseCode);
    }

    public NNTPReply getResponseCode() {
        return this.responseCode;
    }

    public static ServerResponse Parse(String input) throws NntpUnknownResponseException {
        String[] data = input.split(" ");
        if( data.length < 1 ) {
            // TODO: Fix this exception
            throw new IllegalThreadStateException("wahh");
        }

        // See if the command is one we know about
        NNTPReply command;
        try {
            command = NNTPReply.Resolve(Integer.parseInt(data[0]));
        } catch (IllegalArgumentException e) {
            throw new NntpUnknownResponseException();
        }

        return new ServerResponse(command);
    }

    public static ServerResponse Parse(BufferedReader reader) throws NntpUnknownResponseException, IOException {
        return ServerResponse.Parse(reader.readLine());
    }
}
