package io.phy.nntp2p.connection;

import io.phy.nntp2p.configuration.User;
import io.phy.nntp2p.exceptions.ArticleNotFoundException;
import io.phy.nntp2p.exceptions.NntpUnknownCommandException;
import io.phy.nntp2p.protocol.Article;
import io.phy.nntp2p.protocol.ClientCommand;
import io.phy.nntp2p.protocol.NNTPReply;
import io.phy.nntp2p.protocol.ServerResponse;
import io.phy.nntp2p.proxy.ArticleProxy;
import io.phy.nntp2p.proxy.UserRepository;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

public class InboundConnection extends BaseConnection implements Runnable
{
    private ArticleProxy proxy;

    private boolean listening = true;

    private UserRepository userRepository;
    private User authenticatedAs;

    private String userSpecified;   // TODO: Can't we store state better?

    protected final static Logger log = Logger.getLogger(InboundConnection.class.getName());

    public InboundConnection(Socket socket, ArticleProxy proxy, UserRepository userRepository) throws IOException {
        BindToSocket(socket);

        this.userRepository = userRepository;
        this.proxy = proxy;

        // Start off as not authenticated
        authenticatedAs = null;
    }

    @Override
    public void run() {
        try {
            // First thing we have to do is publish a welcome message!
            WriteData(new ServerResponse(NNTPReply.SERVER_READY_POSTING_NOT_ALLOWED));

            while(socket.isConnected() && listening) {
                ClientCommand command = null;
                String rawInput = reader.readLineString();
                if( rawInput == null ) { break; }
                try {
                    command = ClientCommand.Parse(rawInput);
                    DispatchCommand(command);
                } catch (NntpUnknownCommandException e) {
                    log.info("Unknown unknown command: "+rawInput);
                    WriteData(new ServerResponse(NNTPReply.COMMAND_NOT_RECOGNIZED));
                    continue;
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

        // TODO: Commands and case sensitivity
        // Sab sends lowercase authinfo user

        // Commands that do not require authentication
        switch (command.getCommand()) {
            case QUIT:
                cmdQuit();
                break;

            case AUTHINFO:
                cmdAuthInfo(command);
                break;
        }

        // TODO: Really need to start implementing command classes
        // Then we can register them and make these decisions intelligently
        // Also, that will allow us to respond to things like CAPABILITIES

        // Require the user authenticate
        if( authenticatedAs == null ) {
            WriteData(new ServerResponse(NNTPReply.AUTHENTICATION_REQUIRED));
            return;
        }

        // Commands that DO require authentication
        switch (command.getCommand()) {
            case BODY:
                cmdBody(command);
                break;

            default:
                throw new NntpUnknownCommandException();
        }
    }

    private void cmdAuthInfo(ClientCommand command) throws IOException {
        // RFC 4643
        if( authenticatedAs != null ) {
            WriteData(new ServerResponse(NNTPReply.COMMAND_UNAVAILABLE));
            return;
        }

        // Check we got the right number of arguments
        List<String> args = command.getArguments();
        if( args.size() != 2 ) {
            WriteData(new ServerResponse(NNTPReply.COMMAND_SYNTAX_ERROR));
            return;
        }

        if( args.get(0).equalsIgnoreCase("USER") ) {
            userSpecified = args.get(1);
            WriteData(new ServerResponse(NNTPReply.PASSWORD_REQUIRED));
            return;
        }

        if( args.get(0).equalsIgnoreCase("USER") ) {
            if( userSpecified == null ) {
                WriteData(new ServerResponse(NNTPReply.AUTH_OUT_OF_SEQUENCE));
                return;
            }

            User user = userRepository.authenticate(userSpecified,args.get(1));
            if( user != null ) {
                authenticatedAs = user;
                WriteData(new ServerResponse(NNTPReply.AUTHENTICATION_ACCEPTED));
            } else {
                WriteData(new ServerResponse(NNTPReply.AUTH_REJECTED));
            }

            return;
        }

        // If we hit here, it's invalid!!!
        WriteData(new ServerResponse(NNTPReply.COMMAND_NOT_RECOGNIZED));
    }

    private void cmdQuit() throws IOException {
        listening = false;
        WriteData(new ServerResponse(NNTPReply.CLOSING_CONNECTION));
    }

    private void cmdBody(ClientCommand command) throws IOException {
        // Do some validation over the article
        if( command.getArguments().size() > 1 ) {
            log.fine("Invalid ARTICLE request: "+command.ToNntpString());
            WriteData(new ServerResponse(NNTPReply.COMMAND_SYNTAX_ERROR));
            return;
        }

        String messageId = command.getArguments().get(0);
        Article articleData;
        try {
            articleData = proxy.GetArticle(messageId,authenticatedAs);
        } catch (ArticleNotFoundException e) {
            log.fine("ARTICLE not found: " + messageId);
            WriteData(new ServerResponse(NNTPReply.NO_SUCH_ARTICLE_FOUND));
            return;
        }
        if (articleData == null) {
            log.fine("BODY got back NULL: " + messageId);
        }

        // TODO: Have hit a case here where articleData is null
        ServerResponse response = new ServerResponse(NNTPReply.ARTICLE_RETRIEVED_BODY_FOLLOWS);
        response.addArg(0);
        response.addArg(messageId);

        WriteData(response);
        WriteArticleBody(articleData);
    }
}
