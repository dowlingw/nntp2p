package io.phy.nntp2p.connection;

import io.phy.nntp2p.commands.AuthinfoCommand;
import io.phy.nntp2p.commands.BodyCommand;
import io.phy.nntp2p.commands.ICommandImplementation;
import io.phy.nntp2p.commands.QuitCommand;
import io.phy.nntp2p.configuration.User;
import io.phy.nntp2p.exceptions.NntpUnknownCommandException;
import io.phy.nntp2p.protocol.ClientCommand;
import io.phy.nntp2p.protocol.NNTPReply;
import io.phy.nntp2p.protocol.ServerResponse;
import io.phy.nntp2p.proxy.ArticleProxy;
import io.phy.nntp2p.proxy.UserRepository;
import org.apache.jcs.access.exception.InvalidArgumentException;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Logger;

public class InboundConnection extends BaseConnection implements Runnable
{
    private ArticleProxy proxy;
    private UserRepository userRepository;

    /*
     CONNECTION STATE VARIABLES
    */
    private boolean exiting = false;
    private User authenticatedAs = null;
    private String userSpecified = null;    // TODO: Track this one better?

    protected final static Logger log = Logger.getLogger(InboundConnection.class.getName());

    public InboundConnection(Socket socket, ArticleProxy proxy, UserRepository userRepository) throws IOException {
        BindToSocket(socket);

        this.userRepository = userRepository;
        this.proxy = proxy;

        // TODO: No, really... wtf
        try {
            RegisterCommandClass(AuthinfoCommand.class);
            RegisterCommandClass(BodyCommand.class);
            RegisterCommandClass(QuitCommand.class);
        } catch (InvalidArgumentException e) {
            exiting = true;
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // Maybe we failed to initialise and need to exit before doing anythin
            if( exiting ) {
                WriteData(new ServerResponse(NNTPReply.SERVICE_TEMPORARILY_UNAVAILABLE));
            } else {
                // First thing we have to do is publish a welcome message!
                WriteData(new ServerResponse(NNTPReply.SERVER_READY_POSTING_NOT_ALLOWED));

                while(socket.isConnected() && !exiting) {
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

    // TODO: Make this bean or do it better
    // Need to get a better idea on how reflection works in Java
    private HashMap<String,ICommandImplementation> handlers = new HashMap<>();
    public void RegisterCommandClass(Class classy) throws InvalidArgumentException {
        // TODO: Check that the class implements ICommandImplementation
        if( ! ICommandImplementation.class.isAssignableFrom(classy) ) {
            throw new InvalidArgumentException();
        }
        ICommandImplementation instance = null;
        try {
            instance = (ICommandImplementation) classy.newInstance();
            handlers.put(instance.CommandName().toLowerCase(),instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register command module");
        }
    }

    private void DispatchCommand(ClientCommand command) throws IOException, NntpUnknownCommandException {

        // Is the command found?
        ICommandImplementation handler = handlers.get(command.getCommand().toLowerCase());
        if( handler == null ) {
            throw new NntpUnknownCommandException();
        }

        // Don't proceed if the command requires we authenticate
        if( handler.RequiresAuthentication() && getAuthenticatedAs() == null ) {
            WriteData(new ServerResponse(NNTPReply.AUTHENTICATION_REQUIRED));
            return;
        }

        // OK, Proceed
        handler.Handle(this,command);
    }

    // The following methods can be called from command implementations to modify connection state
    public void setExiting() {
        exiting = true;
    }

    public User getAuthenticatedAs() {
        return authenticatedAs;
    }

    public void setAuthenticatedAs(User user) {
        authenticatedAs = user;
    }


    public String getUserSpecified() {
        return userSpecified;
    }

    public void setUserSpecified(String userSpecified) {
        this.userSpecified = userSpecified;
    }

    public ArticleProxy getProxy() {
        return proxy;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }
}
