package io.phy.nntp2p.server.command;

import io.phy.nntp2p.common.Channel;
import io.phy.nntp2p.server.ClientState;
import io.phy.nntp2p.protocol.NntpClientCommand;

import java.io.IOException;

public interface ICommandImplementation {
    String CommandName();
    boolean RequiresAuthentication();
    void Handle(Channel channel, ClientState state, NntpClientCommand command) throws IOException;
}
