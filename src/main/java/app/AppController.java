package app;

import app.model.DatabaseHandler;
import app.model.State;
import app.view.LoginPage.LoginType;

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

    public boolean attemptRegister(String username, String password, LoginType loginType) {
        if (DatabaseHandler.registerUser(username, password, loginType.name())) {
            model.setCurrentUser(username);

            if (loginType == LoginType.CLINIC) {
                model.setState(State.ClinicDashboard); // Assuming a separate dashboard
            } else {
                model.setState(State.Dashboard);
            }

            updateView();
            return true;
        }
        return false;
    }

    public void goToDashboard() {
        model.setState(State.Dashboard);
        updateView();
    }

    public void goToRegister(LoginType loginType) {
        if (loginType == LoginType.CLINIC) {
            model.setState(State.ClinicRegister);
        } else {
            model.setState(State.Register);
        }
        updateView();
    }

    public void goToRegister() {
        goToRegister(LoginType.STANDARD);
    }

    public void goToLogin(LoginType loginType) {
        model.setCurrentUser(null);
        if (loginType == LoginType.CLINIC) {
            model.setState(State.ClinicLogin);
        } else {
            model.setState(State.Login);
        }
        updateView();
    }

    public void goToLogin() {
        goToLogin(LoginType.STANDARD);
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

    public boolean attemptLogin(String username, String password, LoginType loginType) {
        if (DatabaseHandler.checkLogin(username, password, loginType.name())) {
            model.setCurrentUser(username);

            if (loginType == LoginType.CLINIC) {
                model.setState(State.ClinicDashboard);
            } else {
                model.setState(State.Dashboard);
            }

            updateView();
            return true;
        }
        return false;
    }
}