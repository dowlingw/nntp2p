package io.phy.nntp2p.connection;

import io.phy.nntp2p.configuration.ConnectionType;
import io.phy.nntp2p.configuration.ServerConfigurationItem;
import io.phy.nntp2p.exceptions.NntpUnknownResponseException;
import io.phy.nntp2p.protocol.*;
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

            Article article = new Article();

            // Read headers
            while ( true ) {
                // Read until we get end of headers
                String rawLine = reader.readLine();
                if( rawLine.length() == 0 ) {
                    break;
                }

                String[] split = rawLine.split(": ", 2);
                if (split.length == 2 ) {
                    // Using guava multimap so don't have to worry about key collisions
                    article.getHeaders().put(split[0],split[1]);
                } else {
                    // TODO: Handle header folding
                }
            }

            // TODO: Refactor into multi-line data block handler
            // TODO: Read BODY
            throw new NotImplementedException();


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
