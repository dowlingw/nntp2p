package io.phy.nntp2p.commands;

import io.phy.nntp2p.connection.InboundConnection;
import io.phy.nntp2p.exceptions.ArticleNotFoundException;
import io.phy.nntp2p.protocol.Article;
import io.phy.nntp2p.protocol.ClientCommand;
import io.phy.nntp2p.protocol.NNTPReply;
import io.phy.nntp2p.protocol.ServerResponse;

import java.io.IOException;
import java.util.logging.Logger;

public class BodyCommand implements ICommandImplementation {
    protected final static Logger log = Logger.getLogger(BodyCommand.class.getName());

    @Override
    public String CommandName() {
        return "BODY";
    }

    @Override
    public boolean RequiresAuthentication() {
        return true;
    }

    @Override
    public void Handle(InboundConnection connection, ClientCommand command) throws IOException {
        // Do some validation over the article
        if( command.getArguments().size() > 1 ) {
            log.fine("Invalid ARTICLE request: "+command.ToNntpString());
            connection.WriteData(new ServerResponse(NNTPReply.COMMAND_SYNTAX_ERROR));
            return;
        }

        // TODO: We only really support one variant of BODY, we should properly support the others
        String messageId = command.getArguments().get(0);
        if( ! messageId.startsWith("<") || ! messageId.endsWith(">") ) {
            connection.WriteData(new ServerResponse(NNTPReply.COMMAND_UNAVAILABLE));
            return;
        }

        // TODO: This is a hack!
        messageId = messageId.substring(1,messageId.length()-1);

        Article articleData;
        try {
            articleData = connection.getProxy().GetArticle(messageId,connection.getAuthenticatedAs());
        } catch (ArticleNotFoundException e) {
            log.fine("ARTICLE not found: " + messageId);
            connection.WriteData(new ServerResponse(NNTPReply.NO_SUCH_ARTICLE_FOUND));
            return;
        }
        if (articleData == null) {
            log.fine("BODY got back NULL: " + messageId);
        }

        // TODO: Have hit a case here where articleData is null
        ServerResponse response = new ServerResponse(NNTPReply.ARTICLE_RETRIEVED_BODY_FOLLOWS);
        response.addArg(0);
        response.addArg(messageId);

        connection.WriteData(response);
        connection.WriteArticleBody(articleData);
    }
}
