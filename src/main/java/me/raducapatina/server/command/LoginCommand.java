package me.raducapatina.server.command;

import me.raducapatina.server.command.core.ICommand;

public class LoginCommand extends ICommand {

    public LoginCommand() {
    }

    @Override
    public void execute() {
        String[] args = this.input.split(" ");
    }
}
