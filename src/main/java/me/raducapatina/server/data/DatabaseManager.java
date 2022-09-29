package me.raducapatina.server.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.raducapatina.server.core.ServerInstance;
import me.raducapatina.server.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Represents the placeholder class for the nonSQL database
 */
public class DatabaseManager {

    /**
     * @throws IllegalStateException if an account with this username already exists.
     */
    public static Account createNewAccountAndUpload(String username, String password, String ownerName, Account.MerchantType type, boolean sysAdmin) throws IllegalStateException{
        File file = new File(ServerInstance.databasePath + "\\" + username + ".json");

        if (file.exists()) {
            throw new IllegalStateException("Cannot create new user with username: \"" + username + "\" Reason: Username already exists.");
        }
        Account account = new Account(
                username,
                password,
                ownerName,
                String.valueOf(UUID.randomUUID()),
                type,
                sysAdmin
        );

        try {
            new ObjectMapper().writeValue(file, account);
        } catch (IOException e) {
            Log.error(e.getMessage());
        }

        return account;
    }

    /**
     * @throws IllegalStateException if an account with this username already exists.
     */
    public static void uploadNewAccount(Account account) throws IllegalStateException {
        File file = new File(ServerInstance.databasePath + "\\" + account.getUsername() + ".json");

        if (file.exists()) {
            throw new IllegalStateException("Cannot create new user with username: \"" + account.getUsername() + "\" Reason: Username already exists.");
        }

        try {
            new ObjectMapper().writeValue(file, account);
        } catch (IOException e) {
            Log.error(e.getMessage());
        }
    }

    /**
     * The method directly overrides the current user file with a new one.
     * @throws IllegalStateException if the instance with the given username does not exist.
     */
    public static void updateInstanceToDatabase(Account account) throws IllegalStateException {
        File file = new File(ServerInstance.databasePath + "\\" + account.getUsername() + ".json");

        if (file.exists()) {
            throw new IllegalStateException("Cannot update database. Reason: Username: \"" + account.getUsername() + "\" does not exist.");
        }

        try {
            new ObjectMapper().writeValue(file, account);
        } catch (IOException e) {
            Log.error(e.getMessage());
        }
    }

    /**
     * @throws IllegalStateException if the instance with the given username does not exist.
     * @return requested data or null if {@link IOException} is thrown.
     */
    public static Account getInstanceFromDatabase(String username) throws IllegalStateException {
        File file = new File(ServerInstance.databasePath + "\\" + username + ".json");
        if(!file.exists()) {
            throw new IllegalStateException("Cannot update database. Reason: Username: \"" + username + "\" does not exist.");
        }

        try {
            return new ObjectMapper().readValue(file, Account.class);
        } catch (IOException e) {
            Log.error(e.getMessage());
        }
        return null;
    }
}
