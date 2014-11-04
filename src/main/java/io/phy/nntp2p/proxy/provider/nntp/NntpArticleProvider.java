package io.phy.nntp2p.proxy.provider.nntp;

import io.phy.nntp2p.configuration.ServerConfigurationItem;
import io.phy.nntp2p.connection.OutboundConnection;
import io.phy.nntp2p.proxy.IArticleProvider;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.io.InvalidObjectException;

/**
 * This provider manages a single NNTP configuration.
 *
 * Maintains a pool of at least 2 connections to upstream provider
 *
 * GetArticle() requests will be able to consume up to n-1 connections.
 * This ensures a minimum of 1 connection available at all times to answer HasArticle() requests.
 *
 * First pass:
 *  - Connection pool and borrow objects directly
 *  Later
 *  - Queue requests
 *
 */
public class NntpArticleProvider implements IArticleProvider {

    private final static long MAX_POOL_WAIT_TIME = 100L;

    private ServerConfigurationItem _config;
    private GenericObjectPool<OutboundConnection> _pool;

    public NntpArticleProvider(ServerConfigurationItem config) throws InvalidObjectException {
        _config = config;

        // Validate the configuration
        if( config.getMaxConnections() < 2 ) {
            throw new InvalidObjectException("Must have at least 2 connections");
        }

        // Configure and create connection pool
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(_config.getMaxConnections());
        poolConfig.setTestOnBorrow(true);
        poolConfig.setMaxWaitMillis(MAX_POOL_WAIT_TIME);

        _pool = new GenericObjectPool<OutboundConnection>( new NntpConnectionFactory(_config), poolConfig );
    }

    @Override
    public boolean HasArticle(String messageId) throws InternalError {
        OutboundConnection connection = null;
        boolean hasArticle = false;

        try {
            connection = _pool.borrowObject();
            hasArticle = connection.HasArticle(messageId);
        } catch (Exception e) {
            throw new InternalError();
        } finally {
            if( connection != null ) {
                _pool.returnObject(connection);
            }
        }

        return hasArticle;
    }

    @Override
    public String GetArticle(String messageId) {
        OutboundConnection connection = null;
        String article = null;

        // TODO: Be less shit
        if ( _pool.getNumIdle() < 2 ) {
            throw new InternalError("Failed");
        }

        // TODO: Edge case exists where # idle changes between querying it and actually borrowing an object
        try {
            connection = _pool.borrowObject();
            article = connection.GetArticle(messageId);
        } catch (Exception e) {
            throw new InternalError();
        } finally {
            if( connection != null ) {
                _pool.returnObject(connection);
            }
        }

        return article;
    }
}
