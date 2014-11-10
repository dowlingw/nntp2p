package io.phy.nntp2p.proxy;

import io.phy.nntp2p.configuration.ConnectionType;
import io.phy.nntp2p.exceptions.ArticleNotFoundException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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

    private BlockingQueue<Runnable> jobQueue;
    private ThreadPoolExecutor jobManager;

    private final long articleCheckTimeoutMillis = 500L;

    public ArticleProxy() {
        providers = new ArrayList<IArticleProvider>();

        jobQueue = new LinkedBlockingQueue<Runnable>();
        jobManager = new ThreadPoolExecutor(1,Integer.MAX_VALUE,1L, TimeUnit.HOURS,jobQueue);
    }

    public void RegisterProvider(IArticleProvider provider) {
        providers.add(provider);
    }

    public String GetArticle(String messageId, boolean clientIsCache) throws ArticleNotFoundException {
        IArticleProvider provider = resolveProvider(messageId,clientIsCache);
        if( provider == null ) {
            throw new ArticleNotFoundException();
        }

        // TODO: Stuff
        throw new NotImplementedException();
    }

    /**
     * Dispatches queries to all the registered providers
     * @param messageId
     * @param clientIsCache
     * @return
     */
    private IArticleProvider resolveProvider(String messageId, boolean clientIsCache) {
        // Generate the list of jobs to run
        List<ArticleCheckJob> jobs = new ArrayList<ArticleCheckJob>();
        for ( IArticleProvider provider : providers ) {
            jobs.add(new ArticleCheckJob(provider,messageId));
        }

        // Run ALL the jobs
        List<Future<ArticleCheckResult>> futures = new ArrayList<Future<ArticleCheckResult>>();
        try {
            futures = jobManager.invokeAll(jobs, articleCheckTimeoutMillis, TimeUnit.MILLISECONDS);

            // Providers that have the article
            List<IArticleProvider> providers = new ArrayList<>();
            for( Future<ArticleCheckResult> future : futures ) {
                if( future.isDone() && future.get().hasMessage )
                {
                    providers.add(future.get().provider);
                }
            }

            return determineProvider(providers,clientIsCache);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } // TODO: Be less shit with exceptions
        catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Implements business rules to determine which provider should be used to resolve a query
     *
     * @param providersWithArticle A list of zero or more providersWithArticle that have a given article
     * @param clientIsPeer Whether or not the calling client is a cache node
     * @return The provider to use, or null
     */
    public static IArticleProvider determineProvider(List<IArticleProvider> providersWithArticle, boolean clientIsPeer) {
        IArticleProvider provider = null;
        for( IArticleProvider p : providersWithArticle ) {
            // Short circuiting for peered caches
            if( clientIsPeer ) {
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
        public ArticleCheckResult call() throws Exception {
            // TODO: Stuff
            throw new NotImplementedException();
        }
    }
}
