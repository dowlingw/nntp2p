package io.phy.nntp2p.protocol;

import java.util.ArrayList;
import java.util.List;

public class NntpClientCommand {
    private String command;
    private List<String> arguments = new ArrayList<>();

    public NntpClientCommand(String command) {
        this.command = command;
    }

    public NntpClientCommand(NntpClientCommandType command) {
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
}