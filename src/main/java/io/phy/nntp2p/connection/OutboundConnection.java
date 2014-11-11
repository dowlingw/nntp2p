package io.phy.nntp2p.connection;

import io.phy.nntp2p.configuration.ConnectionType;
import io.phy.nntp2p.configuration.ServerConfigurationItem;
import io.phy.nntp2p.exceptions.NntpUnknownCommandException;
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

        // TODO: HandShake & Authentication
    }

    @Override
    public boolean HasArticle(String messageId) throws InternalError {
        ClientCommand upstreamStat = new ClientCommand(NNTPCommand.STAT);
        upstreamStat.getArguments().add(messageId);

        try {
            WriteData(upstreamStat);
            String rawResponse = reader.readLine();
            if( rawResponse == null ) {
                // TODO: Handle better
                return false;
            }

            ServerResponse response = ServerResponse.Parse(rawResponse);
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
