package io.phy.nntp2p.proxy;

import io.phy.nntp2p.configuration.ConnectionType;
import io.phy.nntp2p.common.Article;

public interface IArticleProvider {
    public boolean HasArticle(String messageId) throws InternalError;
    public Article GetArticle(String messageId);
    public ConnectionType ProviderType();
}
