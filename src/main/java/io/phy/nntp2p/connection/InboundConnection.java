package io.phy.nntp2p.connection;

import io.phy.nntp2p.Application;
import io.phy.nntp2p.configuration.ConnectionType;
import io.phy.nntp2p.exceptions.NntpUnknownCommandException;
import io.phy.nntp2p.protocol.ClientCommand;
import io.phy.nntp2p.protocol.NNTPCommand;
import io.phy.nntp2p.protocol.NNTPReply;
import io.phy.nntp2p.protocol.ServerResponse;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class InboundConnection extends BaseConnection implements Runnable
{
    private Application parent;
    private boolean isPeer;

    protected final static Logger log = Logger.getLogger(InboundConnection.class.getName());

    public InboundConnection(Socket socket, Application application) throws IOException {
        super(socket);

        parent = application;
        isPeer = false;
    }

    @Override
    public void run() {
        try {
            log.info("Server thread running for socket: "+socket.toString());

            // First thing we have to do is publish a welcome message!
            WriteData(new ServerResponse(NNTPReply.SERVER_READY_POSTING_NOT_ALLOWED));

            while(socket.isConnected()) {
                ClientCommand command = null;
                String rawInput = reader.readLine();
                if( rawInput == null ) { break; }
                try {
                    command = ClientCommand.Parse(rawInput);
                } catch (NntpUnknownCommandException e) {
                    log.info("Unknown unknown command: "+rawInput);
                    WriteData(new ServerResponse(NNTPReply.COMMAND_NOT_RECOGNIZED));
                    continue;
                }
                DispatchCommand(command);
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

        log.info("Socket closed: "+socket);
    }

    private void DispatchCommand(ClientCommand command) throws IOException {
        // Are they a peered cache?
        if( command.getCommand().equals(NNTPCommand.PEER) ) {
            log.info("Client recognised as a downstream cache peer: "+socket);
            isPeer = true;
            WriteData(new ServerResponse(NNTPReply.SERVER_READY_POSTING_NOT_ALLOWED));
            return;
        }

        if( command.getCommand().equals(NNTPCommand.ARTICLE)) {
            // Do some validation over the article
            if( command.getArguments().size() > 1 ) {
                log.fine("Invalid ARTICLE request: "+command.ToNntpString());
                WriteData(new ServerResponse(NNTPReply.COMMAND_SYNTAX_ERROR));
                return;
            }

            // Get a conncetion from upstream
            OutboundConnection connection = GetConnection(new ConnectionType[]{ConnectionType.Primary});
            if( connection != null ) {
                log.fine("No upstream connection available to serve request");
                WriteData(new ServerResponse(NNTPReply.X_NNTP2P_NOUPSTREAM_AVAILABLE));
                return;
            }

            // Perform the lookup on the upstream connection
            if ( command.getArguments().size() == 0 ) {
                // Handle the case where client has selected current article
                connection.GetArticle();
            } else {
                connection.GetArticle(command.getArguments().get(0));
            }
            // TODO: Handle the various exceptions that can be thrown from the client
        }

        log.info("Received command: "+command.ToNntpString());
    }

    /**
     * Return an OutboundConnection instance from the pool.
     *
     * Accepts a list of connection types in order, the method will attempt to borrow a single connection
     * from these types in order. Caller is responsible for returning connection to the pool
     */
    private OutboundConnection GetConnection( ConnectionType[] typePriority ) {
        for ( ConnectionType type : typePriority ) {
            try {
                OutboundConnection poolConnection = parent.getOutboundPeers().borrowObject(type);
                if( poolConnection != null ) {
                    return poolConnection;
                }
            } catch (Exception e) {
                // Do nothing
            }
        }

        return null;
    }
}
