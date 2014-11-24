package io.phy.nntp2p.proxy.provider.cache;

import io.phy.nntp2p.configuration.ConnectionType;
import io.phy.nntp2p.protocol.Article;
import io.phy.nntp2p.proxy.provider.IArticleCache;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.control.CompositeCacheManager;

import java.util.Properties;

public class LocalCache implements IArticleCache {

    private CompositeCacheManager ccm;
    private JCS cache;

    private final static String CACHE_REGION_NAME = "articles";

    public LocalCache() {
        Properties props = new Properties();
        props.setProperty("jcs.default.cacheattributes.UseMemoryShrinker","true");
        props.setProperty("jcs.default.cacheattributes.MaxObjects","1000");
        props.setProperty("jcs.default","");

        // Create new JCS instance
        ccm = CompositeCacheManager.getUnconfiguredInstance();
        ccm.configure(props);

        try {
            cache = JCS.getInstance(CACHE_REGION_NAME);
        } catch (CacheException e) {
            // TODO: Be less shit
            throw new RuntimeException();
        }
    }

    @Override
    public boolean HasArticle(String messageId) throws InternalError {
        return (GetArticle(messageId) != null);
    }

    @Override
    public Article GetArticle(String messageId) {
        return (Article) cache.get(messageId);
    }

    @Override
    public ConnectionType ProviderType() {
        return ConnectionType.LocalCache;
    }

    @Override
    public void CacheArticle(Article article) {
        try {
            cache.put(article.getMessageId(),article);
        } catch (CacheException e) {
            // Do nothing
            // TODO: Do nothing better
        }
    }
}
