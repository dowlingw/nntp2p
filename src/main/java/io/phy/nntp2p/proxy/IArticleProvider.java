package io.phy.nntp2p.proxy;

public interface IArticleProvider {
    public boolean HasArticle(String messageId) throws InternalError;
    public String GetArticle(String messageId);
}
