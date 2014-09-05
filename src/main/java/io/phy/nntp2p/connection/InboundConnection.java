package io.phy.nntp2p.connection;

import io.phy.nntp2p.Application;
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

        log.info("Received command: "+command.ToNntpString());
    }
}
