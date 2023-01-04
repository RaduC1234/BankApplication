package me.raducapatina.server.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.raducapatina.server.data.DatabaseManager;
import me.raducapatina.server.data.User;

import java.net.SocketAddress;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Client {

    // id
    private SocketAddress address;
    private User user = null;
    private boolean isAuthenticated = false;

    /**
     * @throws Exception if the user does not exit in the database. Very unlikely.
     */
    public void reloadUser() throws Exception {
        if(user == null) {
            return;
        }
            this.user = DatabaseManager.getInstance().getUserService().findById(user.getId());
    }
}
