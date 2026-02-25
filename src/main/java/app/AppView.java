package app;

import app.model.State;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import app.view.DashboardPage;
import app.view.ClinicDashboardPage;
import app.view.DoctorSchedule;
import app.view.LoginPage;
import app.view.LoginPage.LoginType;
import app.view.Page;
import app.view.RegisterPage;
import app.view.PartnerNotifyPage;
import app.view.SettingsPage;
import app.view.LegalOnboardPage;
import app.view.StiTestTrackerPage;
import app.view.TreatmentTrackerPage;
import app.view.ClinicPatientTreatmentPage;
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

        try {
            Page page = switch (state) {
                case Login -> new LoginPage(controller, LoginType.STANDARD);
                case ClinicLogin -> new LoginPage(controller, LoginType.CLINIC);
                case Register -> new RegisterPage(controller, LoginType.STANDARD);
                case ClinicRegister -> new RegisterPage(controller, LoginType.CLINIC);
                case Dashboard -> new DashboardPage(controller);
                case ClinicDashboard -> new ClinicDashboardPage(controller);
                case ClinicSchedule -> new DoctorSchedule(controller);
                case Notify -> new PartnerNotifyPage(controller);
                case Settings -> new SettingsPage(controller);
                case LegalOnboard -> new LegalOnboardPage(controller);
                case StiTestTracker -> new StiTestTrackerPage(controller);
                case TreatmentTracker -> new TreatmentTrackerPage(controller);
                case ClinicPatientTreatment -> new ClinicPatientTreatmentPage(controller);
            };
            Scene scene = page.createScene();
            AppStyles.apply(scene);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception exception) {
            exception.printStackTrace();
            primaryStage.setScene(buildErrorScene(state, exception));
            primaryStage.show();
        }
    }

    private Scene buildErrorScene(State state, Exception exception) {
        Label title = new Label("SafeX could not render this page.");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        Label details = new Label(
                "State: " + state + "\n"
                        + "Error: " + exception.getClass().getSimpleName() + "\n"
                        + "Message: " + (exception.getMessage() == null ? "(no message)" : exception.getMessage())
        );
        details.setWrapText(true);

        VBox root = new VBox(12, title, details);
        root.setPadding(new Insets(24));
        root.setAlignment(Pos.TOP_LEFT);
        return new Scene(root, AppStyles.SCENE_WIDTH, AppStyles.SCENE_HEIGHT);
    }
}
