package app;

import app.model.State;

public class AppModel {
    private State state = State.Login;
    private String currentUser;

    public State getState() {
        return state;
    }

    public void setState(State newState) {
        this.state = newState;
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    public String getCurrentUser() {
        return currentUser;
    }
}
