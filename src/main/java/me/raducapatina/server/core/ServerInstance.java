package me.raducapatina.server.core;

import me.raducapatina.server.command.CommandHandler;
import me.raducapatina.server.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ServerInstance {

    public static String databasePath = "database";

    private final int port;
    private volatile List<Client> connectedClients;
    private CommandHandler commandHandler;

    public ServerInstance(int port) {
        this.port = port;
        this.connectedClients = new ArrayList<>(0);
    }

    public void start() {
        Log.info("Starting server...");

        this.commandHandler = new CommandHandler();
    }
}
