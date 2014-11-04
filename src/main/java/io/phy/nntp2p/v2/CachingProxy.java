package io.phy.nntp2p.v2;

import io.phy.nntp2p.configuration.ServerConfigurationItem;

import java.io.InvalidObjectException;
import java.util.List;

/**
 * NNTP2P Main Class
 *
 * Glues all the bits together
 */
public class CachingProxy {

    private ArticleProxy proxy;

    public CachingProxy() {
        proxy = new ArticleProxy();
    }

    public void Go(List<ServerConfigurationItem> configuredUpstreams) throws InvalidObjectException {

        // Configured NNTP Servers
        for (ServerConfigurationItem config : configuredUpstreams) {
            NntpArticleProvider nntpArticleProvider = new NntpArticleProvider(config);
            proxy.RegisterProvider(nntpArticleProvider);
        }

        // TODO: Start Listening for clients
        // TODO: What now?
    }
}
