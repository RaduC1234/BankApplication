package me.raducapatina.server;

import me.raducapatina.server.core.ServerInstance;

public class MainServer {

    public static void main(String[] args) {
        ServerInstance serverInstance = new ServerInstance();
        serverInstance.start();
    }
}
