package io.phy.nntp2p.commands;

import io.phy.nntp2p.connection.InboundConnection;
import io.phy.nntp2p.protocol.ClientCommand;

import java.io.IOException;

public interface ICommandImplementation {
    String CommandName();
    boolean RequiresAuthentication();
    void Handle(InboundConnection connection, ClientCommand command) throws IOException;
}
