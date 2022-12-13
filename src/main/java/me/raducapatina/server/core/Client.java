package me.raducapatina.server.core;

import me.raducapatina.server.data.User;

import java.net.InetSocketAddress;

public class Client {

    private User user = null;
    private InetSocketAddress address;
    private boolean isAuthenticated;

    public User getAccount() {
        return user;
    }

    public Client setAccount(User user) {
        this.user = user;
        return this;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public Client setAddress(InetSocketAddress address) {
        this.address = address;
        return this;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public Client setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
        return this;
    }
}
