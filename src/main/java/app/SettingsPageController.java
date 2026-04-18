package app;

import app.model.State;
import app.view.AppStyles;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class SettingsPageController {
    @FXML
    private VBox manageLocationsCard;

    @FXML
    public void initialize() {
        if (manageLocationsCard != null) {
            boolean isClinic = "CLINIC".equals(AppController.getCurrentLoginType());
            manageLocationsCard.setVisible(isClinic);
            manageLocationsCard.setManaged(isClinic);
        }
    }

    @FXML
    private void handleBackToDashboard() {
        AppController.goToHomeDashboard();
    }

    @FXML
    private void handleSettings() {
        AppController.updateView(State.SETTINGS);
    }

    @FXML
    private void handleLegal() {
        AppController.updateView(State.LEGAL);
    }

    @FXML
    private void handleManageLocations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/ClinicLocationDialog.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Manage Supported Areas");

            Scene dialogScene = new Scene(root);
            AppStyles.apply(dialogScene);
            dialog.setScene(dialogScene);
            dialog.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Could not load ClinicLocationDialog.fxml", e);
        }
    }
}
