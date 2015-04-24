package io.phy.nntp2p.server.command;

import io.phy.nntp2p.common.Channel;
import io.phy.nntp2p.server.ClientState;
import io.phy.nntp2p.protocol.NntpClientCommand;
import io.phy.nntp2p.protocol.NntpServerReplyType;
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
    public void Handle(Channel channel, ClientState state, NntpClientCommand command) throws IOException {
        state.setQuitting(true);
        NntpEncoder.WriteServerReply(channel, NntpServerReplyType.CLOSING_CONNECTION);

    }
}
