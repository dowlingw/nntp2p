package io.phy.nntp2p.proxy;

import io.phy.nntp2p.configuration.ConnectionType;
import io.phy.nntp2p.configuration.User;
import io.phy.nntp2p.exceptions.ArticleNotFoundException;
import io.phy.nntp2p.protocol.Article;
import io.phy.nntp2p.proxy.provider.IArticleCache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

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
    private List<IArticleCache> caches;

    private BlockingQueue<Runnable> jobQueue;
    private ThreadPoolExecutor jobManager;

    // TODO: Figure out something sensible
    private final long articleCheckTimeoutMillis = 10000L;

    protected final static Logger log = Logger.getLogger(ArticleProxy.class.getName());

    public ArticleProxy() {
        providers = new ArrayList<>();
        caches = new ArrayList<>();

        jobQueue = new LinkedBlockingQueue<>();
        jobManager = new ThreadPoolExecutor(1,Integer.MAX_VALUE,1L, TimeUnit.HOURS,jobQueue);
    }

    public void RegisterCache(IArticleCache cache) {
        caches.add(cache);
    }

    public void RegisterProvider(IArticleProvider provider) {
        providers.add(provider);
    }

    public Article GetArticle(String messageId, User authenticatedUser) throws ArticleNotFoundException {
        IArticleProvider provider = resolveProvider(messageId, authenticatedUser);
        if( provider == null ) {
            log.info(String.format("No provider found with messageId=%s",messageId));
            throw new ArticleNotFoundException();
        }

        // TODO: Only local caches?
        Article article = provider.GetArticle(messageId);
        if( article != null ) {
            for (IArticleCache cache : caches) {
                if( ! cache.HasArticle(messageId) ) {
                    cache.CacheArticle(article);
                }
            }
        }

        return article;
    }

    /**
     * Dispatches queries to all the registered providers
     * @param messageId
     * @param authenticatedUser
     * @return
     */
    private IArticleProvider resolveProvider(String messageId, User authenticatedUser) {
        // Generate the list of jobs to run
        List<ArticleCheckJob> jobs = new ArrayList<>();
        for ( IArticleProvider provider : providers ) {
            jobs.add(new ArticleCheckJob(provider,messageId));
        }
        for (IArticleCache cache : caches ) {
            jobs.add(new ArticleCheckJob(cache,messageId));
        }

        // Run ALL the jobs
        List<Future<ArticleCheckResult>> futures;
        try {
            futures = jobManager.invokeAll(jobs, articleCheckTimeoutMillis, TimeUnit.MILLISECONDS);

            // Providers that have the article
            List<IArticleProvider> providers = new ArrayList<>();
            for( Future<ArticleCheckResult> future : futures ) {
                if( future.isDone() && !future.isCancelled() && future.get().hasMessage )
                {
                    providers.add(future.get().provider);
                }
            }

            return determineProvider(providers,authenticatedUser);

        } catch (InterruptedException e) {
            // TODO: Handle this exception
        }
        catch (ExecutionException e) {
            // TODO: Handle this exception
        }

        return null;
    }

    /**
     * Implements business rules to determine which provider should be used to resolve a query
     *
     * @param providersWithArticle A list of zero or more providersWithArticle that have a given article
     * @param authenticatedUser Whether or not the calling client is a cache node
     * @return The provider to use, or null
     */
    public static IArticleProvider determineProvider(List<IArticleProvider> providersWithArticle, User authenticatedUser) {
        IArticleProvider provider = null;
        for( IArticleProvider p : providersWithArticle ) {
            // Short circuiting for peered caches
            if( authenticatedUser.isCache() ) {
                if( p.ProviderType() == ConnectionType.LocalCache) {
                    provider = p;
                }
                continue;
            }

            if( provider == null ) {
                provider = p;
                continue;
            }

            if( p.ProviderType().ordinal() < provider.ProviderType().ordinal() ) {
                provider = p;
            }
        }

        return provider;
    }

    private class ArticleCheckResult {
        public IArticleProvider provider;
        public String messageId;
        public Boolean hasMessage;
    }

    private class ArticleCheckJob implements Callable<ArticleCheckResult> {
        private IArticleProvider provider;
        private String messageId;

        private ArticleCheckJob(IArticleProvider provider, String messageId) {
            this.provider = provider;
            this.messageId = messageId;
        }

        public IArticleProvider getProvider() {
            return provider;
        }

        public void setProvider(IArticleProvider provider) {
            this.provider = provider;
        }

        @Override
        public ArticleCheckResult call() {
            // TODO: This just looks wrong
            ArticleCheckResult result = new ArticleCheckResult();
            result.messageId = messageId;
            result.provider = provider;
            result.hasMessage = provider.HasArticle(messageId);

            return result;
        }
    }
}
