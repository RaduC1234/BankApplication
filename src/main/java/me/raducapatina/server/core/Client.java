package me.raducapatina.server.core;

import me.raducapatina.server.data.Account;

import java.net.InetAddress;

public class Client {

    private Account account = null;
    private InetAddress address;
    private boolean isAuthenticated;

    public Account getAccount() {
        return account;
    }

    public Client setAccount(Account account) {
        this.account = account;
        return this;
    }

    public InetAddress getAddress() {
        return address;
    }

    public Client setAddress(InetAddress address) {
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
