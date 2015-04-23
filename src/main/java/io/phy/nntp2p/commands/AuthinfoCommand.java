package io.phy.nntp2p.commands;

import io.phy.nntp2p.configuration.User;
import io.phy.nntp2p.connection.ConnectionState;
import io.phy.nntp2p.connection.InboundConnection;
import io.phy.nntp2p.protocol.ClientCommand;
import io.phy.nntp2p.protocol.NNTPReply;
import io.phy.nntp2p.protocol.ServerResponse;

import java.io.IOException;
import java.util.List;

public class AuthinfoCommand implements ICommandImplementation {
    @Override
    public String CommandName() {
        return "AUTHINFO";
    }

    @Override
    public boolean RequiresAuthentication() {
        return false;
    }

    @Override
    public void Handle(InboundConnection socket, ConnectionState state, ClientCommand command) throws IOException {
        // RFC 4643
        if( state.getAuthenticatedUser() != null ) {
            socket.WriteData(new ServerResponse(NNTPReply.COMMAND_UNAVAILABLE));
            return;
        }

        // Check we got the right number of arguments
        List<String> args = command.getArguments();
        if( args.size() != 2 ) {
            socket.WriteData(new ServerResponse(NNTPReply.COMMAND_SYNTAX_ERROR));
            return;
        }

        if( args.get(0).equalsIgnoreCase("USER") ) {
            state.setAuthinfoUser(args.get(1));
            socket.WriteData(new ServerResponse(NNTPReply.PASSWORD_REQUIRED));
            return;
        }

        if( args.get(0).equalsIgnoreCase("PASS") ) {
            if( state.getAuthinfoUser() == null ) {
                socket.WriteData(new ServerResponse(NNTPReply.AUTH_OUT_OF_SEQUENCE));
                return;
            }

            User user = socket.getUserRepository().authenticate(state.getAuthinfoUser(), args.get(1));
            if( user != null ) {
                state.setAuthenticatedUser(user);
                socket.WriteData(new ServerResponse(NNTPReply.AUTHENTICATION_ACCEPTED));
            } else {
                socket.WriteData(new ServerResponse(NNTPReply.AUTH_REJECTED));
            }

            return;
        }

        // If we hit here, it's invalid!!!
        socket.WriteData(new ServerResponse(NNTPReply.COMMAND_NOT_RECOGNIZED));
    }
}
