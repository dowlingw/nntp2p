package io.phy.nntp2p.server.command;

import io.phy.nntp2p.connection.Channel;
import io.phy.nntp2p.connection.ConnectionState;
import io.phy.nntp2p.protocol.ClientCommand;

import java.io.IOException;

public interface ICommandImplementation {
    String CommandName();
    boolean RequiresAuthentication();
    void Handle(Channel channel, ConnectionState state, ClientCommand command) throws IOException;
}
