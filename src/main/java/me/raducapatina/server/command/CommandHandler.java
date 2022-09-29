package me.raducapatina.server.command;

import me.raducapatina.server.util.Log;

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

    public CommandHandler addCommand(Command command){
        commands.add(command);
        return this;
    }

    public CommandHandler addCommands(Command... commands) {
        for(Command command : commands)
            addCommand(command);
        return this;
    }

    public CommandHandler listen() {
        service.execute(() -> {
            Thread.currentThread().setName("CommandHandler");

            String consoleLine;

            while (true) {

                try {
                    consoleLine = ConsoleInput.readLine();

                    boolean commandFound = false;

                    for (Command command : commands) {
                        if (consoleLine.startsWith(command.name)) {
                            commandFound = true;
                            command.arguments = consoleLine.replace(command.name , "");
                            command.execute();
                        }
                    }
                    if(consoleLine.startsWith("help")){
                        StringBuilder help = new StringBuilder();
                        for(Command command : commands){
                            help.append(command.name).append(" -> ").append(command.help).append("\n");
                        }
                        Log.info("Here are the syntax's of all commands:\n" + help);
                        continue;
                    }

                    if(!commandFound) {
                        Log.warn("Unknown command, run help for a full list of commands");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        return null;
    }
    public void terminate(){
        service.shutdownNow();
    }

    private static class ConsoleInput {

        static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        public static String readLine() throws IOException {
            return reader.readLine();
        }
    }
}
