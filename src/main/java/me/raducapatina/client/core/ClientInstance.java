package me.raducapatina.client.core;

public class ClientInstance {

    private String username = null;
    private String password = null;

    public ClientInstance() {
    }

    public ClientInstance(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void start() {
        if (username != null && password != null) {
            // skip login screen
        }
        // start normal application

    }
}
