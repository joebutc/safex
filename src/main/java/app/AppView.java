package app;

import app.model.State;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import app.view.DashboardPage;
import app.view.LoginPage;
import app.view.LoginPage.LoginType;
import app.view.Page;
import app.view.RegisterPage;
import app.view.PartnerNotifyPage;
import app.view.SettingsPage;
import app.view.LegalOnboardPage;
// Import Clinic dashboard
import app.view.AppStyles;

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
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> updateView(state));
            return;
        }

        Page page = switch (state) {
            case Login -> new LoginPage(controller, LoginType.STANDARD);
            case ClinicLogin -> new LoginPage(controller, LoginType.CLINIC);
            case Register -> new RegisterPage(controller, LoginType.STANDARD);
            case ClinicRegister -> new RegisterPage(controller, LoginType.CLINIC);
            case Dashboard -> new DashboardPage(controller);
            case ClinicDashboard -> new DashboardPage(controller);
            case Notify -> new PartnerNotifyPage(controller);
            case Settings -> new SettingsPage(controller);
            case LegalOnboard -> new LegalOnboardPage(controller);
        };

        Scene scene = page.createScene();
        AppStyles.apply(scene);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}