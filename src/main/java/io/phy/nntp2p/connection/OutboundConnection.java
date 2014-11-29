package io.phy.nntp2p.connection;

import io.phy.nntp2p.configuration.ConnectionType;
import io.phy.nntp2p.configuration.ServerConfigurationItem;
import io.phy.nntp2p.exceptions.NntpUnknownResponseException;
import io.phy.nntp2p.protocol.*;
import io.phy.nntp2p.proxy.IArticleProvider;

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
                is_valid = false;
                return;
            }

            // Always attempt authentication
            if( configuration.getCredentials() != null ) {
                ClientCommand sendUsername = new ClientCommand(NNTPCommand.AUTHINFO);
                sendUsername.getArguments().add("USER");
                sendUsername.getArguments().add(configuration.getCredentials().getUsername());

                WriteData(sendUsername);
                ServerResponse sendUsernameResponse = ServerResponse.Parse(reader);

                if( sendUsernameResponse.getResponseCode() != NNTPReply.PASSWORD_REQUIRED) {
                    is_valid = false;
                    return;
                }

                ClientCommand sendPassword = new ClientCommand(NNTPCommand.AUTHINFO);
                sendPassword.getArguments().add("PASS");
                sendPassword.getArguments().add(configuration.getCredentials().getPassword());

                WriteData(sendPassword);
                ServerResponse sendPasswordResponse = ServerResponse.Parse(reader);

                if( sendPasswordResponse.getResponseCode() != NNTPReply.AUTHENTICATION_ACCEPTED ) {
                    is_valid = false;
                    return;                }
            }

        } catch (NntpUnknownResponseException e) {
            is_valid = false;
            return;
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
    public Article GetArticle(String messageId) {
        ClientCommand upstreamStat = new ClientCommand(NNTPCommand.ARTICLE);
        upstreamStat.getArguments().add(messageId);

        try {
            WriteData(upstreamStat);
            ServerResponse response = ServerResponse.Parse(reader);
            if( response.getResponseCode() != NNTPReply.ARTICLE_RETRIEVED_HEAD_AND_BODY_FOLLOW ) {
                // TODO: No no no
                return null;
            }

            // Read headers
            byte[] headerBytes = ServerResponse.ReadMultiLine(reader,true);
            byte[] dataBytes = ServerResponse.ReadMultiLine(reader,false);

            return new Article(messageId, headerBytes,dataBytes);

        } catch (IOException e) {
            // TODO: Handle this
        } catch (NntpUnknownResponseException e) {
            // TODO: Handle this
        }
        return null;
    }

    @Override
    public ConnectionType ProviderType() {
        return configuration.getConnectionType();
    }
}
