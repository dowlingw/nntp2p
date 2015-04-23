package io.phy.nntp2p.connection;

import io.phy.nntp2p.commands.AuthinfoCommand;
import io.phy.nntp2p.commands.BodyCommand;
import io.phy.nntp2p.commands.ICommandImplementation;
import io.phy.nntp2p.commands.QuitCommand;
import io.phy.nntp2p.exceptions.NntpUnknownCommandException;
import io.phy.nntp2p.protocol.ClientCommand;
import io.phy.nntp2p.protocol.NNTPReply;
import io.phy.nntp2p.protocol.ServerResponse;
import io.phy.nntp2p.proxy.ArticleProxy;
import io.phy.nntp2p.proxy.UserRepository;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

public class InboundConnection extends BaseConnection implements Runnable
{
    private ArticleProxy proxy;
    private UserRepository userRepository;

    private ConnectionState state = new ConnectionState();
    private Map<String,ICommandImplementation> handlers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    protected final static Logger log = Logger.getLogger(InboundConnection.class.getName());

    public InboundConnection(Socket socket, ArticleProxy proxy, UserRepository userRepository) throws IOException {
        BindToSocket(socket);

        this.userRepository = userRepository;
        this.proxy = proxy;

        // TODO: No, really... wtf
        RegisterCommandClass(new AuthinfoCommand());
        RegisterCommandClass(new BodyCommand());
        RegisterCommandClass(new QuitCommand());
    }

    private void RegisterCommandClass(ICommandImplementation command) {
        handlers.put(command.CommandName(),command);
    }

    @Override
    public void run() {
        try {
            // Maybe we failed to initialise and need to exit before doing anythin
            if( state.isQuitting() ) {
                WriteData(new ServerResponse(NNTPReply.SERVICE_TEMPORARILY_UNAVAILABLE));
            } else {
                // First thing we have to do is publish a welcome message!
                WriteData(new ServerResponse(NNTPReply.SERVER_READY_POSTING_NOT_ALLOWED));

                while(socket.isConnected() && !state.isQuitting()) {
                    ClientCommand command = null;
                    String rawInput = reader.readLineString();
                    if( rawInput == null ) { break; }
                    try {
                        command = ClientCommand.Parse(rawInput);
                        DispatchCommand(command);
                    } catch (NntpUnknownCommandException e) {
                        log.info("Unknown command: "+rawInput);
                        WriteData(new ServerResponse(NNTPReply.COMMAND_NOT_RECOGNIZED));
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if( socket.isConnected() ) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Do nothing
                }
            }
        }
    }

    private void DispatchCommand(ClientCommand command) throws IOException, NntpUnknownCommandException {

        // Is the command found?
        ICommandImplementation handler = handlers.get(command.getCommand().toLowerCase());
        if( handler == null ) {
            throw new NntpUnknownCommandException();
        }

        // Don't proceed if the command requires we authenticate
        if( handler.RequiresAuthentication() && state.getAuthenticatedUser() == null ) {
            WriteData(new ServerResponse(NNTPReply.AUTHENTICATION_REQUIRED));
            return;
        }

        // OK, Proceed
        handler.Handle(this,state,command);
    }

    public ArticleProxy getProxy() {
        return proxy;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }
}
