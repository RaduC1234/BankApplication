package me.raducapatina.server.command;

import me.raducapatina.server.command.core.Command;
import me.raducapatina.server.core.ServerInstance;
import me.raducapatina.server.util.ResourceServerMessages;

public class StopCommand extends Command {

    private final ServerInstance serverInstance;

    public StopCommand(ServerInstance serverInstance) {
        this.serverInstance = serverInstance;
        this.name = "stop";
        this.usage = ResourceServerMessages.getObjectAsString("command.stop.usage"); // stop
        this.description = ResourceServerMessages.getObjectAsString("command.stop.description"); // Stops the server.
    }

    @Override
    public void execute() {
        this.serverInstance.stop();
    }
}
