package io.phy.nntp2p.proxy;

import io.phy.nntp2p.configuration.ConnectionType;
import io.phy.nntp2p.common.Article;

public interface IArticleProvider {
    boolean HasArticle(String messageId) throws InternalError;
    Article GetArticle(String messageId);
    ConnectionType ProviderType();
}
