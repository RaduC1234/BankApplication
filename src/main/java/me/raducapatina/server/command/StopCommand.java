package me.raducapatina.server.command;

import me.raducapatina.server.command.core.ICommand;
import me.raducapatina.server.core.ServerInstance;

public class StopCommand extends ICommand {

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
