package me.raducapatina.server;

import me.raducapatina.server.core.ServerInstance;

public class MainServer {

    public static int PORT = 8080;

    public static void main(String[] args) {
        ServerInstance serverInstance = new ServerInstance(8080);
        serverInstance.start();
    }
}
