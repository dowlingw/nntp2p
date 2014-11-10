package io.phy.nntp2p.proxy;

import io.phy.nntp2p.configuration.ConnectionType;

public interface IArticleProvider {
    public boolean HasArticle(String messageId) throws InternalError;
    public String GetArticle(String messageId);
    public ConnectionType ProviderType();
}
