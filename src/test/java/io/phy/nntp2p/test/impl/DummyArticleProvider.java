package io.phy.nntp2p.test.impl;

import io.phy.nntp2p.configuration.ConnectionType;
import io.phy.nntp2p.protocol.Article;
import io.phy.nntp2p.proxy.IArticleProvider;

public class DummyArticleProvider implements IArticleProvider {
    private ConnectionType connectionType;

    public DummyArticleProvider(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    @Override
    public boolean HasArticle(String messageId) throws InternalError {
        return false;
    }

    @Override
    public Article GetArticle(String messageId) {
        return null;
    }

    @Override
    public ConnectionType ProviderType() {
        return connectionType;
    }
}
