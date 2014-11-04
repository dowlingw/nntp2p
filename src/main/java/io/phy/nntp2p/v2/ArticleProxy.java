package io.phy.nntp2p.v2;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Article Proxy
 *
 * This coordinates works from cache clients and determines what
 * provider should be used.
 *
 * Upon receiving a GetArticle() request, fans out a HasArticle()
 * request to all providers and waits until all responses come back,
 * or a predefined timeout (whichever occurs first).
 *
 * Then uses a selection algorithm to determine which provider to use.
 * Generally this select in the following order of preference:
 *  - Cache Provider
 *  - Primary Provider (unless client is cache)
 *  - Backup Provider (unless client is cache)
 *
 *  Over time this class can be refactored to represent more complex
 *  caching strategies.
 *
 */
public class ArticleProxy {
    private List<IArticleProvider> providers;

    public ArticleProxy() {
        providers = new ArrayList<IArticleProvider>();
    }

    public void RegisterProvider(IArticleProvider provider) {
        providers.add(provider);
    }

    public String GetArticle(String messageId, boolean clientIsCache) {
        // TODO: Implement
        throw new NotImplementedException();
    }
}
