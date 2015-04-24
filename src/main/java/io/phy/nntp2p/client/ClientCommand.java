package io.phy.nntp2p.client;

import io.phy.nntp2p.exceptions.NntpUnknownCommandException;
import io.phy.nntp2p.protocol.NntpCommand;
import io.phy.nntp2p.protocol.NntpProtocolMessage;

import java.util.ArrayList;
import java.util.List;

public class ClientCommand implements NntpProtocolMessage {
    private String command;
    private List<String> arguments = new ArrayList<>();

    public ClientCommand(String command) {
        this.command = command;
    }

    public ClientCommand(NntpCommand command) {
        this.command = command.name();
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public String ToNntpString() {
        String retString = command;

        for (String arg : arguments) {
            retString += " " + arg;
        }

        return retString;
    }

    public static ClientCommand Parse(String input) throws NntpUnknownCommandException {
        String[] data = input.split(" ");
        if( data.length < 1 ) {
            // TODO: Fix this exception
            throw new IllegalThreadStateException("wahh");
        }

        ClientCommand clientCommand = new ClientCommand(data[0]);
        for (int i=1; i<data.length; i++ ) {
            clientCommand.arguments.add(data[i]);
        }

        return clientCommand;
    }
}