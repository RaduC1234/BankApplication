package me.raducapatina.server.command;

import me.raducapatina.server.command.core.Command;
import me.raducapatina.server.core.ServerInstance;
import me.raducapatina.server.util.ResourceServerMessages;

public class StopCommand extends Command {

    private final ServerInstance serverInstance;

    public StopCommand(ServerInstance serverInstance) {
        this.serverInstance = serverInstance;
        this.name = "stop";
        this.usage = "stop";
        this.description = "Stopping server";
    }

    @Override
    public void execute() {
        this.serverInstance.stop();
    }
}
