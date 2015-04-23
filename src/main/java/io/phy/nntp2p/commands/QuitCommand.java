package io.phy.nntp2p.commands;

import io.phy.nntp2p.connection.ConnectionState;
import io.phy.nntp2p.connection.InboundConnection;
import io.phy.nntp2p.protocol.ClientCommand;
import io.phy.nntp2p.protocol.NNTPReply;
import io.phy.nntp2p.protocol.ServerResponse;

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
    public void Handle(InboundConnection socket, ConnectionState state, ClientCommand command) throws IOException {
        state.setQuitting(true);
        socket.WriteData(new ServerResponse(NNTPReply.CLOSING_CONNECTION));
    }
}
