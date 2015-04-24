package io.phy.nntp2p.proxy.provider.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.phy.nntp2p.common.Article;
import io.phy.nntp2p.configuration.ConnectionType;
import io.phy.nntp2p.proxy.provider.IArticleCache;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.io.*;
import java.sql.*;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class LocalCache extends CacheLoader<String, Article> implements IArticleCache {

    private Integer cacheMemoryArticleLimit;
    private final String cacheDiskLocation;
    private final Integer cacheDiskSizeLimit;

    private static final String SQL_INSERT_CACHE = "INSERT INTO articles (message_id,file_name,file_size_bytes,insert_time) VALUES(?,?,?,CURRENT_TIMESTAMP)";
    private static final String SQL_CACHE_QUERY = "SELECT file_name FROM articles WHERE message_id=?";

    private Connection conn;
    private LoadingCache<String, Article> memoryCache;

    protected final static Logger log = Logger.getLogger(LocalCache.class.getName());

    public LocalCache(Integer cacheMemoryArticleLimit, String cacheDiskLocation, Integer cacheDiskSizeLimit) {
        this.cacheMemoryArticleLimit = cacheMemoryArticleLimit;
        this.cacheDiskLocation = cacheDiskLocation;
        this.cacheDiskSizeLimit = cacheDiskSizeLimit;

        memoryCache = CacheBuilder.newBuilder()
                .maximumSize(cacheMemoryArticleLimit)
                .expireAfterWrite(1, TimeUnit.DAYS)
                .build(this);
    }

    @Override
    public boolean HasArticle(String messageId) throws InternalError {
        try {
            PreparedStatement getCacheItem = GetQueryPS();
            getCacheItem.setString(1,messageId);
            ResultSet resultSet = getCacheItem.executeQuery();
            boolean result = false;
            while(resultSet.next()) {
                result = true;
            }
            resultSet.close();
            return result;
        } catch (SQLException e) {
        }
        return false;
    }

    @Override
    public Article GetArticle(String messageId) {
        try {
            return memoryCache.get(messageId);
        } catch (ExecutionException e) {
            return null;
        }
    }

    @Override
    public ConnectionType ProviderType() {
        return ConnectionType.LocalCache;
    }

    @Override
    public void CacheArticle(Article article) {
        // Don't put it in if we already have it!
        if( HasArticle(article.getMessageId()) ) {
            return;
        }

        UUID uuid = UUID.nameUUIDFromBytes(article.getMessageId().getBytes());

        // Write the file to disk
        File destFile = FilePath(uuid.toString());
        try {
            // Write the file to disk
            FileOutputStream foos = new FileOutputStream(destFile);
            ObjectOutputStream writer = new ObjectOutputStream(foos);
            writer.writeObject(article);
            writer.close();
        } catch (IOException e) {
            log.severe(String.format("Failed to write article to disk cache; messageId=%s file=%s",article.getMessageId(),destFile.toString()));
            return;
        }

        // Grab the size of the file we just wrote
        Long filesize = destFile.length();

        // Insert record to the database
        try {
            PreparedStatement insertCacheItem = GetInsertPS();
            insertCacheItem.setString(1,article.getMessageId());
            insertCacheItem.setString(2, uuid.toString());
            insertCacheItem.setInt(3, filesize.intValue());
            insertCacheItem.executeUpdate();
            conn.commit();  // Because setting autocommit to true apparently isn't good enough
        } catch (SQLException e) {
            log.severe(String.format("Failed to write article housekeeping, deleting object; messageId=%s file=%s",article.getMessageId(),destFile.toString()));
            destFile.delete();
            return;
        }

        // At this point the object is cached!
    }

    public void initialise() throws Exception {
        // Create or open the Derby database
        //Properties dbProps = new Properties();
        Properties dbProps = System.getProperties();
        dbProps.setProperty("derby.system.home", cacheDiskLocation);

        conn = DriverManager.getConnection("jdbc:derby:db;create=true", dbProps);
        conn.setAutoCommit(true);

        // Run all the database migrations
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));
        Liquibase lb = new Liquibase("schema.xml",new ClassLoaderResourceAccessor(),database);
        lb.forceReleaseLocks(); // Only safe because we're using Derby DB
        lb.update((Contexts)null);

        // Statements are prepared as required because they will be used concurrently
    }

    private PreparedStatement GetInsertPS() throws SQLException {
        PreparedStatement statement = conn.prepareStatement(SQL_INSERT_CACHE);
        statement.closeOnCompletion();
        return statement;
    }

    private PreparedStatement GetQueryPS() throws SQLException {
        PreparedStatement statement = conn.prepareStatement(SQL_CACHE_QUERY);
        statement.closeOnCompletion();
        return statement;
    }

    @Override
    public Article load(String messageId) throws Exception {
        PreparedStatement getCacheItem = GetQueryPS();

        getCacheItem.setString(1,messageId);
        ResultSet resultSet = getCacheItem.executeQuery();
        while(resultSet.next()) {
            String uuid = resultSet.getString("file_name");

            File filePath = FilePath(uuid);
            if( ! filePath.isFile() ) {
                continue;
            }

            resultSet.close();
            FileInputStream fis = new FileInputStream(filePath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            return  (Article) ois.readObject();
        }
        resultSet.close();
        throw new Exception(); // TODO: Better?
    }

    private File FilePath(String fileName) {
        return new File(cacheDiskLocation,fileName);
    }
}
