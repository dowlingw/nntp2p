package io.phy.nntp2p.connection;

import io.phy.nntp2p.configuration.ConnectionType;
import io.phy.nntp2p.configuration.ServerConfigurationItem;
import io.phy.nntp2p.exceptions.NntpUnknownResponseException;
import io.phy.nntp2p.protocol.ClientCommand;
import io.phy.nntp2p.protocol.NNTPCommand;
import io.phy.nntp2p.protocol.NNTPReply;
import io.phy.nntp2p.protocol.ServerResponse;
import io.phy.nntp2p.proxy.IArticleProvider;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;

public class OutboundConnection extends BaseConnection implements IArticleProvider {
    private ServerConfigurationItem configuration;

    private Socket clientSocket;

    public OutboundConnection(ServerConfigurationItem configuration) {
        this.configuration = configuration;
    }

    public ServerConfigurationItem getConfiguration() {
        return configuration;
    }

    public void Connect() throws IOException {
        SocketFactory factory = configuration.isUseSsl() ? SSLSocketFactory.getDefault() : SocketFactory.getDefault();
        clientSocket = factory.createSocket(configuration.getHostname(),configuration.getPort());

        // If SSL, do a handshake
        if( configuration.isUseSsl() ) {
            ((SSLSocket)clientSocket).startHandshake();
        }

        BindToSocket(clientSocket);
        try {

            // Read server advertisement
            ServerResponse advertisement = ServerResponse.Parse(reader);
            if( ! advertisement.getResponseCode().isPositiveCompletion() ) {
                // TODO: Invalidate connection
            }

            // Always attempt authentication
            if( configuration.getCredentials() != null ) {
                ClientCommand sendUsername = new ClientCommand(NNTPCommand.AUTHINFO);
                sendUsername.getArguments().add("USER");
                sendUsername.getArguments().add(configuration.getCredentials().getUsername());

                WriteData(sendUsername);
                ServerResponse sendUsernameResponse = ServerResponse.Parse(reader);

                // TODO: Support AUTHINFO fully
                if( sendUsernameResponse.getResponseCode() != NNTPReply.MORE_AUTH_INFO_REQUIRED ) {
                    // TODO: Invalidate connection
                }

                ClientCommand sendPassword = new ClientCommand(NNTPCommand.AUTHINFO);
                sendPassword.getArguments().add("PASS");
                sendPassword.getArguments().add(configuration.getCredentials().getPassword());

                WriteData(sendPassword);
                ServerResponse sendPasswordResponse = ServerResponse.Parse(reader);

                // TODO: Support AUTHINFO properly
                if( sendPasswordResponse.getResponseCode() != NNTPReply.AUTHENTICATION_ACCEPTED ) {
                    // TODO: Invalidate connection
                }
            }

        } catch (NntpUnknownResponseException e) {
            // TODO: Invalidate connection
        }
    }

    @Override
    public boolean HasArticle(String messageId) throws InternalError {
        ClientCommand upstreamStat = new ClientCommand(NNTPCommand.STAT);
        upstreamStat.getArguments().add(messageId);

        try {
            WriteData(upstreamStat);
            ServerResponse response = ServerResponse.Parse(reader);
            return (response.getResponseCode() == NNTPReply.ARTICLE_RETRIEVED_REQUEST_TEXT_SEPARATELY);

        } catch (IOException e) {
            // TODO: Handle this
        } catch (NntpUnknownResponseException e) {
            // TODO: Handle this
        }
        return false;
    }

    @Override
    public String GetArticle(String messageId) {
        throw new NotImplementedException();
    }

    @Override
    public ConnectionType ProviderType() {
        return configuration.getConnectionType();
    }
}
