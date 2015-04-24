package io.phy.nntp2p.client;

import io.phy.nntp2p.common.Article;
import io.phy.nntp2p.configuration.ConnectionType;
import io.phy.nntp2p.configuration.ServerConfigurationItem;
import io.phy.nntp2p.common.Channel;
import io.phy.nntp2p.exceptions.NntpUnknownResponseException;
import io.phy.nntp2p.protocol.*;
import io.phy.nntp2p.proxy.IArticleProvider;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;

public class OutboundConnection implements IArticleProvider {
    private ServerConfigurationItem configuration;
    private Channel channel;
    protected boolean is_valid = true;

    public OutboundConnection(ServerConfigurationItem configuration) {
        this.configuration = configuration;
    }

    public ServerConfigurationItem getConfiguration() {
        return configuration;
    }

    public boolean isValid() {
        return is_valid;
    }

    public void Connect() throws IOException {
        SocketFactory factory = configuration.isUseSsl() ? SSLSocketFactory.getDefault() : SocketFactory.getDefault();
        Socket clientSocket = factory.createSocket(configuration.getHostname(), configuration.getPort());

        // If SSL, do a handshake
        if( configuration.isUseSsl() ) {
            ((SSLSocket)clientSocket).startHandshake();
        }

        channel = new Channel(clientSocket);
        try {

            // Read server advertisement
            NntpServerReply advertisement = NntpDecoder.Parse(channel);
            if( ! advertisement.getResponseCode().isPositiveCompletion() ) {
                is_valid = false;
                return;
            }

            // Always attempt authentication
            if( configuration.getCredentials() != null ) {
                NntpClientCommand sendUsername = new NntpClientCommand(NntpClientCommandType.AUTHINFO);
                sendUsername.getArguments().add("USER");
                sendUsername.getArguments().add(configuration.getCredentials().getUsername());

                NntpEncoder.WriteData(channel,sendUsername);
                NntpServerReply sendUsernameResponse = NntpDecoder.Parse(channel);

                if( sendUsernameResponse.getResponseCode() != NntpServerReplyType.PASSWORD_REQUIRED) {
                    is_valid = false;
                    return;
                }

                NntpClientCommand sendPassword = new NntpClientCommand(NntpClientCommandType.AUTHINFO);
                sendPassword.getArguments().add("PASS");
                sendPassword.getArguments().add(configuration.getCredentials().getPassword());

                NntpEncoder.WriteData(channel, sendPassword);
                NntpServerReply sendPasswordResponse = NntpDecoder.Parse(channel);

                if( sendPasswordResponse.getResponseCode() != NntpServerReplyType.AUTHENTICATION_ACCEPTED ) {
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
        NntpClientCommand request = new NntpClientCommand(NntpClientCommandType.STAT);
        request.getArguments().add(String.format("<%s>",messageId));

        try {
            NntpEncoder.WriteData(channel,request);
            NntpServerReply response = NntpDecoder.Parse(channel);
            return (response.getResponseCode() == NntpServerReplyType.ARTICLE_RETRIEVED_REQUEST_TEXT_SEPARATELY);

        } catch (IOException e) {
            // TODO: Handle this
        } catch (NntpUnknownResponseException e) {
            // TODO: Handle this
        }
        return false;
    }

    @Override
    public Article GetArticle(String messageId) {
        NntpClientCommand request = new NntpClientCommand(NntpClientCommandType.ARTICLE);
        request.getArguments().add(String.format("<%s>",messageId));

        try {
            NntpEncoder.WriteData(channel, request);
            NntpServerReply response = NntpDecoder.Parse(channel);
            if( response.getResponseCode() != NntpServerReplyType.ARTICLE_RETRIEVED_HEAD_AND_BODY_FOLLOW ) {
                // TODO: No no no
                return null;
            }

            // Read headers
            byte[] headerBytes = NntpDecoder.ReadMultiLine(channel, true);
            byte[] dataBytes = NntpDecoder.ReadMultiLine(channel, false);

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
