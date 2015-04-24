package io.phy.nntp2p.server.command;

import io.phy.nntp2p.connection.Channel;
import io.phy.nntp2p.connection.ConnectionState;
import io.phy.nntp2p.protocol.ClientCommand;
import io.phy.nntp2p.protocol.NNTPReply;
import io.phy.nntp2p.protocol.NntpEncoder;

import java.io.IOException;

public class QuitCommand implements ICommandImplementation {
    @Override
    public String CommandName() {
        return "QUIT";
    }

    @Override
    public boolean RequiresAuthentication() {
        return false;
    }

    @Override
    public void Handle(Channel channel, ConnectionState state, ClientCommand command) throws IOException {
        state.setQuitting(true);
        NntpEncoder.WriteServerReply(channel, NNTPReply.CLOSING_CONNECTION);

    }
}
