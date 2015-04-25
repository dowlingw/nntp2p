package io.phy.nntp2p;

import io.phy.nntp2p.configuration.NntpServerDetails;
import io.phy.nntp2p.configuration.Settings;
import io.phy.nntp2p.proxy.ArticleProxy;
import io.phy.nntp2p.proxy.provider.cache.LocalCache;
import io.phy.nntp2p.proxy.provider.nntp.NntpArticleProvider;
import io.phy.nntp2p.server.ProxyServer;
import io.phy.nntp2p.server.command.AuthinfoCommand;
import io.phy.nntp2p.server.command.BodyCommand;
import io.phy.nntp2p.server.command.QuitCommand;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.InvalidObjectException;

@SpringBootApplication
public class EntryPoint {
    public static void main(String[] args) {
        SpringApplication.run(EntryPoint.class, args);
    }

    @Value( "${settings.location}" )
    private String settingsLocation;

    @Bean
    public XMLConfiguration xmlConfiguration() throws ConfigurationException {
        return new XMLConfiguration(settingsLocation);
    }

    @Bean
    public Settings settings(SettingBuilder settingBuilder) throws ConfigurationException {
        return settingBuilder.ReadSettings();
    }

    @Bean
    public ArticleProxy articleProxy(Settings settings, LocalCache localCache) throws InvalidObjectException {
        ArticleProxy proxy = new ArticleProxy();
        for (NntpServerDetails config : settings.getServers()) {
            proxy.RegisterProvider(new NntpArticleProvider(config));
        }

        proxy.RegisterCache(localCache);

        return proxy;
    }

    @Bean
    public ProxyServer proxyServer(ArticleProxy articleProxy,Settings settings) {
        ProxyServer server = new ProxyServer();
        server.RegisterCommandHandler(new AuthinfoCommand(settings.getUserRepository()));
        server.RegisterCommandHandler(new BodyCommand(articleProxy));
        server.RegisterCommandHandler(new QuitCommand());

        return server;
    }

}
