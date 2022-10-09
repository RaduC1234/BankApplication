package me.raducapatina.server.command.core;

import me.raducapatina.server.util.Log;
import me.raducapatina.server.util.ResourceServerMessages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandHandler {

    private ExecutorService service = Executors.newSingleThreadExecutor();
    private List<Command> commands;

    public CommandHandler() {
        commands = new ArrayList<>();
    }

    public CommandHandler addCommand(Command command) {
        commands.add(command);
        return this;
    }

    public CommandHandler addCommands(Command... commands) {
        for (Command command : commands)
            addCommand(command);
        return this;
    }

    public CommandHandler listen() {
        service.execute(() -> {
            Thread.currentThread().setName("CommandHandler");

            String consoleLine;

            while (true) {

                consoleLine = ConsoleInput.readLine();

                boolean commandFound = false;

                for (Command command : commands) {
                    if (consoleLine.startsWith(command.name)) {
                        commandFound = true;
                        command.arguments = consoleLine.replace(command.name, "");
                        command.execute();
                    }
                }
                if (consoleLine.startsWith("help")) {
                    StringBuilder row = new StringBuilder();
                    for (Command command : commands) {
                        row.append(command.name).append(" -> ").append(command.usage).append("\n");
                    }
                    Log.info(ResourceServerMessages.getObjectAsString("command.help.listAll").replace("{0}", row));
                    continue;
                }

                if (!commandFound) {
                    Log.warn(ResourceServerMessages.getObjectAsString("command.help.unknown"));
                }

            }
        });
        return null;
    }

    public void stop() {
        service.shutdownNow();
    }

    public boolean isStarted() {
        return !service.isTerminated();
    }

    public List<Command> getCommands() {
        return commands;
    }

    private static class ConsoleInput {

        static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        public static String readLine() {
            try {
                return reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
