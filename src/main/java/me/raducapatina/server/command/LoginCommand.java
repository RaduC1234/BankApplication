package me.raducapatina.server.command;

import me.raducapatina.server.command.core.Command;

public class LoginCommand extends Command {

    public LoginCommand() {
    }

    @Override
    public void execute() {
        String[] args = this.arguments.split(" ");
    }
}
