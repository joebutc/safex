package app;

import app.model.State;
import app.model.UserAccount;
import java.util.ArrayList;

public class AppModel {
    private State state = State.Login;
    private ArrayList<UserAccount> users = new ArrayList<>();

    public State getState() {
        for (int x=0; x<5; x++) {

        }
        return state;
    }

    public ArrayList<UserAccount> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<UserAccount> users) {
        this.users = users;
    }
}
