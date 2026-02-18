package app;

import app.model.DatabaseHandler;
import app.model.State;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public final class AppController {
    private static boolean initialised = false;
    private static Stage primaryStage;
    private static String currentUser;

    private AppController() {
    }

    public static void init(State state) {
        if (initialised) {
            System.err.println("AppController already initialised");
            return;
        }
        Parent root = loadRoot(state);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        initialised = true;
    }

    public static void updateView(State state) {
        assertInitialised();
        if (state == State.LOGIN) {
            currentUser = null;
        }
        Parent root = loadRoot(state);
        primaryStage.getScene().setRoot(root);
        primaryStage.show();
    }

    // Called by Log In Page
    public static boolean attemptLogin(String username, String password) {
        assertInitialised();
        if (DatabaseHandler.checkLogin(username, password)) {
            currentUser = username;
            updateView(State.DASHBOARD);
            return true;
        }
        return false;
    }

    public static boolean attemptRegister(String username, String password) {
        assertInitialised();
        if (DatabaseHandler.registerUser(username, password)) {
            currentUser = username;
            updateView(State.DASHBOARD);
            return true;
        }
        return false;
    }

    public static String getCurrentUser() {
        assertInitialised();
        return currentUser;
    }

    private static Parent loadRoot(State state) {
        try {
            FXMLLoader loader = new FXMLLoader(AppController.class.getResource(state.getFxmlPath()));
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Could not load FXML for " + state, e);
        }
    }

    private static void assertInitialised() {
        if (!initialised) {
            throw new RuntimeException("AppController not initialised");
        }
    }

//    public void updateView() {
//        view.updateView(model.getState());
//    }
//
//    public String getCurrentUser() {
//        return model.getCurrentUser();
//    }
//
//    public boolean attemptRegister(String username, String password) {
//        if (DatabaseHandler.registerUser(username, password)) {
//            model.setCurrentUser(username);
//            model.setState(State.Dashboard);
//            updateView();
//            return true;
//        }
//        return false;
//    }
//
//    public void goToDashboard() {
//        model.setState(State.Dashboard);
//        updateView();
//    }
//
//    public void goToRegister() {
//        model.setState(State.Register);
//        updateView();
//    }
//
//    public void goToLogin() {
//        model.setCurrentUser(null);
//        model.setState(State.Login);
//        updateView();
//    }
//
//    public void goToNotify() {
//        model.setState(State.Notify);
//        updateView();
//    }
//
//    public void goToSettings() {
//        model.setState(State.Settings);
//        updateView();
//    }
//
//    public void goToLegalOnboard() {
//        model.setState(State.LegalOnboard);
//        updateView();
//    }
//
//    // Called by Log In Page
//    public boolean attemptLogin(String username, String password) {
//        if (DatabaseHandler.checkLogin(username, password)) {
//            model.setCurrentUser(username);
//            model.setState(State.Dashboard);
//            updateView();
//            return true;
//        }
//        return false;
//    }
    
}
