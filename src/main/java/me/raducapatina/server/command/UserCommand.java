package me.raducapatina.server.command;

import me.raducapatina.server.command.core.Command;
import me.raducapatina.server.data.Account;
import me.raducapatina.server.data.DatabaseManager;
import me.raducapatina.server.util.Log;
import me.raducapatina.server.util.ResourceServerMessages;
import me.raducapatina.server.util.ResourceServerProperties;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;

public class UserCommand extends Command {

    public UserCommand() {
        this.name = "user";
        this.description = ResourceServerMessages.getObjectAsString("command.user.description");
        this.usage = ResourceServerMessages.getObjectAsString("command.user.usage");
    }

    @Override
    public void execute() throws Exception {
        String[] args = input.split(" ");
        switch (args[1]) {
            case "add": {
                DatabaseManager.createNewAccountAndUpload(
                        args[2],
                        args[3],
                        args[4],
                        Account.MerchantType.valueOf(args[5]), //TODO: fix default state
                        Boolean.getBoolean(args[6]));
                Log.info(ResourceServerMessages.getObjectAsString("command.runSuccess"));
                break;
            }
            case "delete": {
                try {
                    DatabaseManager.deleteUser(args[2]);
                    Log.info(ResourceServerMessages.getObjectAsString("command.runSuccess"));
                } catch (FileNotFoundException exception) {
                    Log.error(ResourceServerMessages.getObjectAsString("command.user.notExists"));
                }
                break;
            }
            case "update": {
                Account account = DatabaseManager.getInstanceFromDatabase(args[2]);
                if (!args[3].equalsIgnoreCase("D"))
                    account.setPassword(args[3]);
                if (!args[3].equalsIgnoreCase("D"))
                    account.setOwnerName(args[4]);
                if (!args[3].equalsIgnoreCase("D"))
                    account.setMerchantType(Account.MerchantType.valueOf(args[5])); //TODO: fix default state
                if (!args[3].equalsIgnoreCase("D"))
                    account.setSysAdmin(Boolean.getBoolean(args[6]));
                break;
            }

            case "list" : {
                File mainDir = new File(ResourceServerProperties.getInstance().getObject("databasePath").toString());
                String[] files = mainDir.list();
                if(files.length == 0){
                    Log.info(ResourceServerMessages.getObjectAsString("command.noUsersInDatabase"));
                }
                for(String file : files) {
                    Log.info("User -> " + DatabaseManager.getInstanceFromDatabase(file.replace(".json", "")).getUsername());
                }
                break;
            }
            default:
                Log.error(ResourceServerMessages.getObjectAsString("command.syntaxError"));
        }
    }
}
