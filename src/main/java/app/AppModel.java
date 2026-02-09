package app;

import app.model.State;
import app.model.UserAccount;
import java.util.ArrayList;

public class AppModel {
    private State state = State.Settings;
    private ArrayList<UserAccount> users;

    public State getState() {
        for (int x=0; x<5; x++) {

        }
        return state;
    }
}
