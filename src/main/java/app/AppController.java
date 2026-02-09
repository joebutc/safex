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

    // Called from view
    public void updateView() {
        view.updateView(model.getState());
    }

    public boolean attemptRegister(String username, String password) {
        // Attempt to add the user to the database
        if (DatabaseHandler.registerUser(username, password)) {
            // Automatically log them in or redirect to login page
            model.setCurrentUser(username);
            model.setState(State.Dashboard); // Or keep them on login to verify
            updateView();
            return true;
        }
        // Return false if the username is taken or a database error occurred
        return false;
    }

    public void goToRegister() {
        model.setState(State.Register);
        updateView();
    }

    public void goToLogin() {
        model.setState(State.Login);
        view.updateView(State.Login);
    }

    // Called by Log In Page
    public boolean attemptLogin(String username, String password) {
        if (DatabaseHandler.checkLogin(username, password)) {
            model.setCurrentUser(username);
            model.setState(State.Dashboard);
            updateView(); // (Switches to dashboard when we make it)
            return true;
        }
        return false;
    }
}