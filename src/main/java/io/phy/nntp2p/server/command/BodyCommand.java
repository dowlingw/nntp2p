package io.phy.nntp2p.server.command;

import io.phy.nntp2p.connection.Channel;
import io.phy.nntp2p.connection.ConnectionState;
import io.phy.nntp2p.exceptions.ArticleNotFoundException;
import io.phy.nntp2p.protocol.*;
import io.phy.nntp2p.proxy.ArticleProxy;

import java.io.IOException;
import java.util.logging.Logger;

public class BodyCommand implements ICommandImplementation {

    private ArticleProxy proxy;
    protected final static Logger log = Logger.getLogger(BodyCommand.class.getName());

    public BodyCommand(ArticleProxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public String CommandName() {
        return "BODY";
    }

    @Override
    public boolean RequiresAuthentication() {
        return true;
    }

    @Override
    public void Handle(Channel channel, ConnectionState state, ClientCommand command) throws IOException {
        // Do some validation over the article
        if( command.getArguments().size() > 1 ) {
            log.fine("Invalid ARTICLE request: " + command.ToNntpString());
            NntpEncoder.WriteServerReply(channel, NNTPReply.COMMAND_SYNTAX_ERROR);
            return;
        }

        // TODO: We only really support one variant of BODY, we should properly support the others
        String messageId = command.getArguments().get(0);
        if( ! messageId.startsWith("<") || ! messageId.endsWith(">") ) {
            NntpEncoder.WriteServerReply(channel, NNTPReply.COMMAND_UNAVAILABLE);
            return;
        }

        // TODO: This is a hack!
        messageId = messageId.substring(1,messageId.length()-1);

        Article articleData;
        try {
            articleData = proxy.GetArticle(messageId, state.getAuthenticatedUser());
        } catch (ArticleNotFoundException e) {
            log.fine("ARTICLE not found: " + messageId);
            NntpEncoder.WriteServerReply(channel, NNTPReply.NO_SUCH_ARTICLE_FOUND);
            return;
        }
        if (articleData == null) {
            log.fine("BODY got back NULL: " + messageId);
        }

        // TODO: Have hit a case here where articleData is null
        ServerResponse response = new ServerResponse(NNTPReply.ARTICLE_RETRIEVED_BODY_FOLLOWS);
        response.addArg(0);
        response.addArg(messageId);

        NntpEncoder.WriteData(channel, response);
        NntpEncoder.WriteArticleBody(channel, articleData);
    }
}
