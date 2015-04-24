package io.phy.nntp2p.client;

import io.phy.nntp2p.client.ClientCommand;
import io.phy.nntp2p.configuration.ConnectionType;
import io.phy.nntp2p.configuration.ServerConfigurationItem;
import io.phy.nntp2p.exceptions.NntpUnknownResponseException;
import io.phy.nntp2p.protocol.*;
import io.phy.nntp2p.proxy.IArticleProvider;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class OutboundConnection implements IArticleProvider {
    private ServerConfigurationItem configuration;

    private Socket clientSocket;

    public OutboundConnection(ServerConfigurationItem configuration) {
        this.configuration = configuration;
    }

    public ServerConfigurationItem getConfiguration() {
        return configuration;
    }

    protected Socket socket;

    private static final byte[] CRLF = {0x0D, 0x0A};

    protected NntpDecoder reader;
    protected BufferedWriter writer;

    private OutputStreamWriter osWriter;

    protected boolean is_valid = true;

    protected void BindToSocket(Socket underlyingSocket) throws IOException {
        socket = underlyingSocket;

        osWriter = new OutputStreamWriter(socket.getOutputStream());

        reader = new NntpDecoder(socket.getInputStream(), StandardCharsets.UTF_8);
        writer = new BufferedWriter(osWriter);
    }

    public void WriteData(NntpProtocolMessage data) throws IOException {
        writer.write(data.ToNntpString());
        writeByteArray(CRLF);
        writer.flush();
    }

    private void writeByteArray(byte[] data) throws IOException {
        writer.flush();
        synchronized (osWriter) {
            OutputStream outputStream = socket.getOutputStream();
            synchronized (outputStream) {
                outputStream.write(data);
                outputStream.flush();
            }
        }
    }

    public boolean isValid() {
        return is_valid;
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
                ClientCommand sendUsername = new ClientCommand(NntpCommand.AUTHINFO);
                sendUsername.getArguments().add("USER");
                sendUsername.getArguments().add(configuration.getCredentials().getUsername());

                WriteData(sendUsername);
                ServerResponse sendUsernameResponse = ServerResponse.Parse(reader);

                if( sendUsernameResponse.getResponseCode() != NntpReply.PASSWORD_REQUIRED) {
                    is_valid = false;
                    return;
                }

                ClientCommand sendPassword = new ClientCommand(NntpCommand.AUTHINFO);
                sendPassword.getArguments().add("PASS");
                sendPassword.getArguments().add(configuration.getCredentials().getPassword());

                WriteData(sendPassword);
                ServerResponse sendPasswordResponse = ServerResponse.Parse(reader);

                if( sendPasswordResponse.getResponseCode() != NntpReply.AUTHENTICATION_ACCEPTED ) {
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
        ClientCommand upstreamStat = new ClientCommand(NntpCommand.STAT);
        upstreamStat.getArguments().add(String.format("<%s>",messageId));

        try {
            WriteData(upstreamStat);
            ServerResponse response = ServerResponse.Parse(reader);
            return (response.getResponseCode() == NntpReply.ARTICLE_RETRIEVED_REQUEST_TEXT_SEPARATELY);

        } catch (IOException e) {
            // TODO: Handle this
        } catch (NntpUnknownResponseException e) {
            // TODO: Handle this
        }
        return false;
    }

    @Override
    public Article GetArticle(String messageId) {
        ClientCommand upstreamStat = new ClientCommand(NntpCommand.ARTICLE);
        upstreamStat.getArguments().add(String.format("<%s>",messageId));

        try {
            WriteData(upstreamStat);
            ServerResponse response = ServerResponse.Parse(reader);
            if( response.getResponseCode() != NntpReply.ARTICLE_RETRIEVED_HEAD_AND_BODY_FOLLOW ) {
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
