package me.raducapatina.server.core;

import me.raducapatina.server.data.Account;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class Client {

    private Account account = null;
    private InetSocketAddress address;
    private boolean isAuthenticated;

    public Account getAccount() {
        return account;
    }

    public Client setAccount(Account account) {
        this.account = account;
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
