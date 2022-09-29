package me.raducapatina.server.command;

public abstract class Command {

    public String name = null;

    public String help = "no help available";

    public String arguments = null;

    protected abstract void execute();

}
