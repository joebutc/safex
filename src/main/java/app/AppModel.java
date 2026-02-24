package app;

import app.model.State;

public class AppModel {
    private State state = State.Login;
    private String currentUser;
    private boolean clinicSession;

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

    public void setClinicSession(boolean clinicSession) {
        this.clinicSession = clinicSession;
    }

    public boolean isClinicSession() {
        return clinicSession;
    }
}
