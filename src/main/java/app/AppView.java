package app;

import app.model.State;
import javafx.stage.Stage;
import app.view.DashboardPage;
import app.view.LoginPage;
import app.view.Page;
import app.view.SettingsPage;

public class AppView {
    private Stage primaryStage;

    public void updateView(State state) {
        Page page = switch (state) {
            case Login -> new LoginPage();
            case Dashboard -> new DashboardPage();
            case Settings -> new SettingsPage();
            default -> throw new IllegalStateException("Invalid state: " + state);
        };
        primaryStage.setScene(page.createScene());
        primaryStage.show();
        page.onEnter();
    }

    public AppView(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}
