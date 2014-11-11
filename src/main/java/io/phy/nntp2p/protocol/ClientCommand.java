package io.phy.nntp2p.protocol;

import io.phy.nntp2p.exceptions.NntpUnknownCommandException;

import java.util.ArrayList;
import java.util.List;

public class ClientCommand implements NntpProtocolMessage {
    private NNTPCommand command;
    private List<String> arguments;

    public ClientCommand(NNTPCommand command) {
        arguments = new ArrayList<String>();
        this.command = command;
    }

    public NNTPCommand getCommand() {
        return command;
    }

    public void setCommand(NNTPCommand command) {
        this.command = command;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public String ToNntpString() {
        String retString = command.name();

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

        // See if the command is one we know about
        NNTPCommand command;
        try {
            command = NNTPCommand.valueOf(data[0]);
        } catch (IllegalArgumentException e) {
            throw new NntpUnknownCommandException();
        }

        ClientCommand clientCommand = new ClientCommand(command);
        for (int i=1; i<data.length; i++ ) {
            clientCommand.arguments.add(data[i]);
        }

        return clientCommand;
    }
}