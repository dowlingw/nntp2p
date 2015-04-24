package io.phy.nntp2p;

import io.phy.nntp2p.configuration.*;
import io.phy.nntp2p.proxy.UserRepository;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SettingBuilder {

    @Autowired
    XMLConfiguration xmlConfiguration;

    public Settings ReadSettings() throws ConfigurationException {
        Settings settings = new Settings();

        settings.setUserRepository(readUsers());
        settings.setServers(readServers());

        settings.setListenPort(xmlConfiguration.getInt("proxy.listenport", 119));
        settings.setCacheDiskLocation(xmlConfiguration.getString("proxy.diskpath"));
        settings.setCacheDiskSizeLimit(xmlConfiguration.getInt("proxy.disklimit"));
        settings.setCacheMemoryArticleLimit(xmlConfiguration.getInt("proxy.memlimit"));

        return settings;
    }

    private UserRepository readUsers() throws ConfigurationException {
        UserRepository users = new UserRepository();

        List<HierarchicalConfiguration> fields = xmlConfiguration.configurationsAt("users.user");
        for (HierarchicalConfiguration raw : fields) {
            boolean isCache = raw.getString("[@type]","").toLowerCase().equals("cache");
            String username = raw.getString("[@username]","");
            String password = raw.getString("[@password]","");

            // Required!
            if( username.isEmpty() || password.isEmpty() ) {
                throw new ConfigurationException("User entries must have all fields completed");
            }

            users.AddUser(new User(username,password,isCache));
        }

        return users;
    }

    private List<NntpServerDetails> readServers() throws ConfigurationException {
        List<NntpServerDetails> peers = new ArrayList<>();

        List<HierarchicalConfiguration> fields = xmlConfiguration.configurationsAt("servers.server");
        for (HierarchicalConfiguration raw : fields) {

            String rawType = raw.getString("[@type]", "primary");
            ConnectionType type = FromString(rawType);

            String host = raw.getString("[@host]");
            Integer port = raw.getInt("[@port]",119);
            Boolean ssl = raw.getBoolean("[@ssl]",false);
            Integer maxconn = raw.getInt("[@connections]",2);

            String username = raw.getString("credentials.username","");
            String password = raw.getString("credentials.password","");

            // Validate that the connection type was set
            if( type == null ) {
                throw new ConfigurationException("Connection type not specified for a server");
            }

            // Credentials are optional
            PasswordCredential pwd = null;
            if( ! username.isEmpty() && ! password.isEmpty() ) {
                pwd = new PasswordCredential(username,password);
            }

            peers.add(new NntpServerDetails(host, port, ssl, maxconn, type, pwd));
        }

        return peers;
    }

    private static ConnectionType FromString(String rawType) {
        ConnectionType type = null;
        switch (rawType.toLowerCase()) {
            case "primary":
                type = ConnectionType.Primary;
                break;

            case "backup":
                type = ConnectionType.Backup;
                break;

            case "cache":
                type = ConnectionType.RemoteCache;
                break;
        }

        return type;
    }
}
