package io.phy.nntp2p.server;

import io.phy.nntp2p.configuration.User;

public class ClientState {
    private User authenticatedUser;
    private String authinfoUser;
    private boolean quitting = false;

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    public void setAuthenticatedUser(User authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    public String getAuthinfoUser() {
        return authinfoUser;
    }

    public void setAuthinfoUser(String authinfoUser) {
        this.authinfoUser = authinfoUser;
    }

    public boolean isQuitting() {
        return quitting;
    }

    public void setQuitting(boolean quitting) {
        this.quitting = quitting;
    }
}
