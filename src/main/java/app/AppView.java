package app;

import app.model.State;
import javafx.stage.Stage;
import app.view.DashboardPage;
import app.view.LoginPage;
import app.view.Page;
import app.view.RegisterPage;
import app.view.PartnerNotifyPage;
import app.view.SettingsPage;
import app.view.LegalOnboardPage;

public class AppView {
    private Stage primaryStage;
    private AppController controller;

    public AppView(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setController(AppController controller) {
        this.controller = controller;
    }

    public void updateView(State state) {
        Page page = switch (state) {
            // 'this' refers to the instance of the class this method belongs to
            case Login -> new LoginPage(controller);
            case Register -> new RegisterPage(controller);
            case Dashboard -> new DashboardPage(controller);
            case Notify -> new PartnerNotifyPage();
            case Settings -> new SettingsPage();
            case LegalOnboard -> new LegalOnboardPage();
            default -> throw new IllegalStateException("Invalid state: " + state);
        };
        primaryStage.setScene(page.createScene());
        primaryStage.show();
    }
}
