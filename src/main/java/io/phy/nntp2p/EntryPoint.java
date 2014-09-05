package io.phy.nntp2p;

import io.phy.nntp2p.configuration.ConnectionType;
import io.phy.nntp2p.configuration.PasswordCredential;
import io.phy.nntp2p.configuration.ServerConfigurationItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EntryPoint {
    public static void main(String [] args) throws IOException {
        // We're using a dummy configuration for now
        List<ServerConfigurationItem> peers = new ArrayList<ServerConfigurationItem>();
        peers.add(new ServerConfigurationItem("someprovider.com",563,true, 10, ConnectionType.Primary, new PasswordCredential("user","password")));

        // Spin up the application
        Application app = new Application(peers);
        app.RunApplication();
    }
}
