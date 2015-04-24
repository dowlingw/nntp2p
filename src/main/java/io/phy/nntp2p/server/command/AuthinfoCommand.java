package io.phy.nntp2p.server.command;

import io.phy.nntp2p.configuration.User;
import io.phy.nntp2p.connection.Channel;
import io.phy.nntp2p.connection.ConnectionState;
import io.phy.nntp2p.protocol.ClientCommand;
import io.phy.nntp2p.protocol.NNTPReply;
import io.phy.nntp2p.protocol.NntpEncoder;
import io.phy.nntp2p.proxy.UserRepository;

import java.io.IOException;
import java.util.List;

public class AuthinfoCommand implements ICommandImplementation {

    private UserRepository repository;

    public AuthinfoCommand(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public String CommandName() {
        return "AUTHINFO";
    }

    @Override
    public boolean RequiresAuthentication() {
        return false;
    }

    @Override
    public void Handle(Channel channel, ConnectionState state, ClientCommand command) throws IOException {
        // RFC 4643
        if( state.getAuthenticatedUser() != null ) {
            NntpEncoder.WriteServerReply(channel, NNTPReply.COMMAND_UNAVAILABLE);
            return;
        }

        // Check we got the right number of arguments
        List<String> args = command.getArguments();
        if( args.size() != 2 ) {
            NntpEncoder.WriteServerReply(channel, NNTPReply.COMMAND_SYNTAX_ERROR);
            return;
        }

        if( args.get(0).equalsIgnoreCase("USER") ) {
            state.setAuthinfoUser(args.get(1));
            NntpEncoder.WriteServerReply(channel, NNTPReply.PASSWORD_REQUIRED);
            return;
        }

        if( args.get(0).equalsIgnoreCase("PASS") ) {
            if( state.getAuthinfoUser() == null ) {
                NntpEncoder.WriteServerReply(channel, NNTPReply.AUTH_OUT_OF_SEQUENCE);
                return;
            }

            User user = repository.authenticate(state.getAuthinfoUser(), args.get(1));
            if( user != null ) {
                state.setAuthenticatedUser(user);
                NntpEncoder.WriteServerReply(channel, NNTPReply.AUTHENTICATION_ACCEPTED);
            } else {
                NntpEncoder.WriteServerReply(channel, NNTPReply.AUTH_REJECTED);
            }

            return;
        }

        // If we hit here, it's invalid!!!
        NntpEncoder.WriteServerReply(channel, NNTPReply.COMMAND_NOT_RECOGNIZED);
    }
}
