package io.phy.nntp2p;

import io.phy.nntp2p.configuration.ConnectionType;
import io.phy.nntp2p.configuration.PasswordCredential;
import io.phy.nntp2p.configuration.ServerConfigurationItem;
import io.phy.nntp2p.configuration.User;
import io.phy.nntp2p.proxy.UserRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EntryPoint {
    public static void main(String [] args) throws IOException {
        // We're using a dummy configuration for now
        List<ServerConfigurationItem> peers = new ArrayList<ServerConfigurationItem>();
        peers.add(new ServerConfigurationItem("someprovider.com",563,true, 10, ConnectionType.Primary, new PasswordCredential("user","password")));

        // Dummy Users for now
        UserRepository users = new UserRepository();
        users.AddUser(new User("username","password",false));

        // Spin up the application
        Application app = new Application(users,peers);
        app.RunApplication();
    }
}
