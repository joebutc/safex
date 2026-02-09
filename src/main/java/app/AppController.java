package app;

import app.model.DatabaseHandler;
import app.model.State;

public class AppController {
    private final AppModel model;
    private final AppView view;

    public AppController(AppModel model, AppView view) {
        this.model = model;
        this.view = view;
    }

    public void updateView() {
        view.updateView(model.getState());
    }

    public String getCurrentUser() {
        return model.getCurrentUser();
    }

    public boolean attemptRegister(String username, String password) {
        if (DatabaseHandler.registerUser(username, password)) {
            model.setCurrentUser(username);
            model.setState(State.Dashboard);
            updateView();
            return true;
        }
        return false;
    }

    public void goToDashboard() {
        model.setState(State.Dashboard);
        updateView();
    }

    public void goToRegister() {
        model.setState(State.Register);
        updateView();
    }

    public void goToLogin() {
        model.setCurrentUser(null);
        model.setState(State.Login);
        updateView();
    }

    public void goToNotify() {
        model.setState(State.Notify);
        updateView();
    }

    public void goToSettings() {
        model.setState(State.Settings);
        updateView();
    }

    public void goToLegalOnboard() {
        model.setState(State.LegalOnboard);
        updateView();
    }

    // Called by Log In Page
    public boolean attemptLogin(String username, String password) {
        if (DatabaseHandler.checkLogin(username, password)) {
            model.setCurrentUser(username);
            model.setState(State.Dashboard);
            updateView();
            return true;
        }
        return false;
    }
    
}
