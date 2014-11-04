package io.phy.nntp2p.v2;

import io.phy.nntp2p.configuration.ServerConfigurationItem;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.InvalidObjectException;

/**
* This provider manages a single NNTP configuration.
*
* Requires that the configuration has at least 2 connections configured.
* Keeps a single thread dedicated to answering HasArticle() requests.
* Uses all remaining threads for retrieving articles.
*
*/
public class NntpArticleProvider implements IArticleProvider {

    private ServerConfigurationItem _config;

    public NntpArticleProvider(ServerConfigurationItem config) throws InvalidObjectException {
        _config = config;

        // Validate the configuration
        if( config.getMaxConnections() < 2 ) {
            throw new InvalidObjectException("Must have at least 2 connections");
        }

        // TODO: Connection Pool
    }

    @Override
    public boolean HasArticle(String messageId) {
        // TODO: Implement
        throw new NotImplementedException();
    }

    @Override
    public String GetArticle(String messageId) {
        // TODO: Implement
        throw new NotImplementedException();
    }
}
