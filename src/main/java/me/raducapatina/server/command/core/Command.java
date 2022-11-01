package me.raducapatina.server.command.core;

public abstract class Command {

    public String name = null;

    public String usage = "no usage available";

    public String description = "no description available";

    public String input = null;

    public abstract void execute() throws Exception;

}
