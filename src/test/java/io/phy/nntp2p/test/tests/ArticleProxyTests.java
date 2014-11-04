package io.phy.nntp2p.test.tests;

import io.phy.nntp2p.exceptions.ArticleNotFoundException;
import io.phy.nntp2p.proxy.ArticleProxy;
import org.junit.Test;

public class ArticleProxyTests {

    @Test(expected = ArticleNotFoundException.class)
    public void proxyWithoutProvidersDoesNotHaveArticle() {
        ArticleProxy proxy = new ArticleProxy();
        proxy.GetArticle("somearticlethatdoesnotexist",false);
    }

}
