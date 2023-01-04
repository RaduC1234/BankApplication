package me.raducapatina.server.command;

import me.raducapatina.server.command.core.Command;
import me.raducapatina.server.data.DatabaseManager;
import me.raducapatina.server.data.User;
import me.raducapatina.server.data.UserService;
import me.raducapatina.server.data.UserType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class UserCommand extends Command {

    private static final Logger logger = LogManager.getLogger(UserCommand.class);

    public UserCommand() {
        this.name = "user";
        this.description = "Command for basic user management.";
        this.usage = "user [delete/add] <username> | <password> <Type[STUDENT,TEACHER,ADMIN]>";
    }

    @Override
    public void execute() throws Exception {
        String args[] = input.split(" ");

        switch (args[1]) {
            case "add" -> {
                User user = new User();
                user.setUsername(args[2]);
                user.setPassword(args[3]);
                user.setType(UserType.valueOf(args[4]));

                UserService userService = DatabaseManager.getInstance().getUserService();

                if (userService.existsByUsername(user.getUsername())) {
                    logger.error("Error: User already exists.");
                    return;
                }
                userService.add(user);
                logger.info("New user created with the following properties: \n" +
                        "Username--> '" + user.getUsername() + "'\n" +
                        "Password--> '" + user.getPassword() + "'\n" +
                        "User type--> '" + user.getType().toString() + "'"
                );
            }
            case "delete" -> {
                UserService userService = DatabaseManager.getInstance().getUserService();
                if (userService.deleteByUsername((args[2]))) {
                    logger.info("User successfully deleted.");
                }
            }
            case "list" -> {
                ArrayList<User> select_a_from_user_a = (ArrayList<User>) DatabaseManager.getInstance().getEntityManager().createQuery("SELECT a FROM User a", User.class).getResultList();
                StringBuilder builder = new StringBuilder();
                for (User user : select_a_from_user_a) {
                    builder.append("Id: " + user.getId() + ". Username: " + user.getUsername() + ". Password: " + user.getPassword() + ". Type:" + user.getType() + "\n");
                }
                logger.info("\n" + builder);
            }
            default -> logger.error("Syntax error.");
        }
    }
}
