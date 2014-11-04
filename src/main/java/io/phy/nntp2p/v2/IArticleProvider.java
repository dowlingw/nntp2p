package io.phy.nntp2p.v2;

public interface IArticleProvider {
    public boolean HasArticle(String messageId);
    public String GetArticle(String messageId);
}
