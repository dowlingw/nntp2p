package io.phy.nntp2p.test.tests;

import io.phy.nntp2p.configuration.ConnectionType;
import io.phy.nntp2p.exceptions.ArticleNotFoundException;
import io.phy.nntp2p.proxy.ArticleProxy;
import io.phy.nntp2p.proxy.IArticleProvider;
import io.phy.nntp2p.test.impl.DummyArticleProvider;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ArticleProxyTests {

    @Test(expected = ArticleNotFoundException.class)
    public void proxyWithoutProvidersDoesNotHaveArticle() throws ArticleNotFoundException {
        ArticleProxy proxy = new ArticleProxy();
        proxy.GetArticle("somearticlethatdoesnotexist",false);
    }

    @Test
    public void determineProviderDoesNotSelectRemoteCacheForPeer() {
        DummyArticleProvider remoteCache = new DummyArticleProvider(ConnectionType.RemoteCache);
        List<IArticleProvider> peersWithArticle = new ArrayList<>();
        peersWithArticle.add(remoteCache);

        IArticleProvider provider = ArticleProxy.determineProvider(peersWithArticle,true);

        Assert.assertNotEquals(provider,remoteCache);
    }

    @Test
    public void determineProviderSelectsLocalCacheOverRemoteCache() {
        DummyArticleProvider localCache = new DummyArticleProvider(ConnectionType.LocalCache);
        DummyArticleProvider remoteCache = new DummyArticleProvider(ConnectionType.RemoteCache);

        List<IArticleProvider> peersWithArticle = new ArrayList<>();
        peersWithArticle.add(localCache);
        peersWithArticle.add(remoteCache);

        IArticleProvider provider = ArticleProxy.determineProvider(peersWithArticle,false);

        Assert.assertEquals(provider, localCache);
    }

    @Test
    public void determineProviderSelectsRemoteCacheOverPrimary() {
        DummyArticleProvider remoteCache = new DummyArticleProvider(ConnectionType.RemoteCache);
        DummyArticleProvider primarySource = new DummyArticleProvider(ConnectionType.Primary);

        List<IArticleProvider> peersWithArticle = new ArrayList<>();
        peersWithArticle.add(primarySource);
        peersWithArticle.add(remoteCache);

        IArticleProvider provider = ArticleProxy.determineProvider(peersWithArticle,false);

        Assert.assertEquals(provider,remoteCache);
    }


    @Test
    public void determineProviderSelectsPrimaryOverBackup() {
        DummyArticleProvider primaryPeer = new DummyArticleProvider(ConnectionType.Primary);
        DummyArticleProvider backupSource = new DummyArticleProvider(ConnectionType.Backup);

        List<IArticleProvider> peersWithArticle = new ArrayList<>();
        peersWithArticle.add(primaryPeer);
        peersWithArticle.add(backupSource);

        IArticleProvider provider = ArticleProxy.determineProvider(peersWithArticle,false);

        Assert.assertEquals(provider,primaryPeer);
    }
}
