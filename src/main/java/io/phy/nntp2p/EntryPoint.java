package io.phy.nntp2p;

import io.phy.nntp2p.configuration.ConnectionType;
import io.phy.nntp2p.configuration.PasswordCredential;
import io.phy.nntp2p.configuration.ServerConfigurationItem;
import io.phy.nntp2p.configuration.User;
import io.phy.nntp2p.proxy.UserRepository;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class EntryPoint {
    protected final static Logger log = Logger.getLogger(EntryPoint.class.getName());

    public static void main(String [] args) throws IOException {
        List<ServerConfigurationItem> peers = new ArrayList<>();
        UserRepository users = new UserRepository();

        // Load us some configuration
        XMLConfiguration config;
        try {
            config = new XMLConfiguration("config.xml");
        } catch (ConfigurationException e) {
            log.severe(e.getMessage());
            return;
        }

        // Let's grab server configurations
        List<HierarchicalConfiguration> fields = config.configurationsAt("servers.server");
        for (HierarchicalConfiguration raw : fields) {

            String rawType = raw.getString("[@type]","primary");
            ConnectionType type = FromString(rawType);

            String host = raw.getString("[@host]");
            Integer port = raw.getInt("[@port]",119);
            Boolean ssl = raw.getBoolean("[@ssl]",false);
            Integer maxconn = raw.getInt("[@connections]",2);

            String username = raw.getString("credentials.username","");
            String password = raw.getString("credentials.password","");

            // Validate that the connection type was set
            if( type == null ) {
                log.severe("Connection type not specified for a server");
                return;
            }

            // Credentials are optional
            PasswordCredential pwd = null;
            if( ! username.isEmpty() && ! password.isEmpty() ) {
                pwd = new PasswordCredential(username,password);
            }

            peers.add(new ServerConfigurationItem(host, port, ssl, maxconn, type, pwd));
        }

        // And now let's grab cache users!
        fields = config.configurationsAt("users.user");
        for (HierarchicalConfiguration raw : fields) {
            boolean isCache = raw.getString("[@type]","").toLowerCase().equals("cache");
            String username = raw.getString("[@username]","");
            String password = raw.getString("[@password]","");

            // Required!
            if( username.isEmpty() || password.isEmpty() ) {
                log.severe("User entries must have all fields completed");
                return;
            }

            users.AddUser(new User(username,password,isCache));
        }

        // Oh, and the listen port... that's kind of important
        Integer listenPort = config.getInt("proxy.listenport",119);

        // Spin up the application
        Application app = new Application(listenPort,users,peers);
        app.RunApplication();
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
