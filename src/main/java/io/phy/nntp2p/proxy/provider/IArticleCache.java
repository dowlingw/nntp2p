package io.phy.nntp2p.proxy.provider;

import io.phy.nntp2p.protocol.Article;
import io.phy.nntp2p.proxy.IArticleProvider;

public interface IArticleCache extends IArticleProvider {
    void CacheArticle(Article article);
}
