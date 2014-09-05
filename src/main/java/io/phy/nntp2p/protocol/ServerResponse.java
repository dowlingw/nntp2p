package io.phy.nntp2p.protocol;

public class ServerResponse implements NntpProtocolMessage {
    private Integer command;

    public ServerResponse(Integer command) {
        this.command = command;
    }

    @Override
    public String ToNntpString() {
        return String.valueOf(command);
    }
}
