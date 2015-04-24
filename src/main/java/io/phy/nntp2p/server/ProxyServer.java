package io.phy.nntp2p.server;

import io.phy.nntp2p.common.Channel;
import io.phy.nntp2p.exceptions.NntpUnknownCommandException;
import io.phy.nntp2p.protocol.NntpClientCommand;
import io.phy.nntp2p.protocol.NntpDecoder;
import io.phy.nntp2p.protocol.NntpEncoder;
import io.phy.nntp2p.protocol.NntpServerReplyType;
import io.phy.nntp2p.server.command.ICommandImplementation;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

// This is a replacement for InboundConnection, aimed at
// separation of concerns. Calls to Run() should be
// idempotent so that the same ProxyServer instance can
// be used for serving all requests.
public class ProxyServer {
    private Map<String,ICommandImplementation> handlers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    protected final static Logger log = Logger.getLogger(ProxyServer.class.getName());

    public void RegisterCommandHandler(ICommandImplementation handler) {
        handlers.put(handler.CommandName(),handler);
    }

    public void run(Channel channel) {
        ClientState state = new ClientState();

        try {
            // Maybe we failed to initialise and need to exit before doing anythin
            if( state.isQuitting() ) {
                NntpEncoder.WriteServerReply(channel, NntpServerReplyType.SERVICE_TEMPORARILY_UNAVAILABLE);
            } else {
                // First thing we have to do is publish a welcome message!
                NntpEncoder.WriteServerReply(channel, NntpServerReplyType.SERVER_READY_POSTING_NOT_ALLOWED);


                while(channel.getSocket().isConnected() && !state.isQuitting()) {
                    NntpClientCommand command = null;
                    String rawInput = NntpDecoder.readLineString(channel);
                    if( rawInput == null ) { break; }
                    try {
                        command = NntpDecoder.ParseCommand(rawInput);
                        DispatchCommand(channel,state,command);
                    } catch (NntpUnknownCommandException e) {
                        log.info("Unknown command: " + rawInput);
                        NntpEncoder.WriteServerReply(channel, NntpServerReplyType.COMMAND_NOT_RECOGNIZED);
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if( channel.getSocket().isConnected() ) {
                try {
                    // TODO: Move this into ClientSocket
                    channel.getSocket().close();
                } catch (IOException e) {
                    // Do nothing
                }
            }
        }
    }

    private void DispatchCommand(Channel channel, ClientState state, NntpClientCommand command) throws IOException, NntpUnknownCommandException {
        ICommandImplementation handler = handlers.get(command.getCommand());
        if( handler == null ) {
            throw new NntpUnknownCommandException();
        }

        // Don't proceed if the command requires we authenticate
        if( handler.RequiresAuthentication() && state.getAuthenticatedUser() == null ) {
            NntpEncoder.WriteServerReply(channel, NntpServerReplyType.AUTHENTICATION_REQUIRED);
            return;
        }

        // OK, Proceed
        handler.Handle(channel,state,command);
    }
}
