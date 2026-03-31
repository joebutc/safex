package app.view;

import app.AppController;
import app.model.State;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ClinicDashboardPage {
    @FXML private Label welcomeLabel;

    @FXML
    private void initialize() {
        String user = AppController.getCurrentUser();
        welcomeLabel.setText((user == null || user.isBlank())
                ? "Clinic dashboard"
                : "Clinic dashboard, " + user);
    }

    @FXML
    private void handleLogout() {
        AppController.updateView(State.CLINIC_LOGIN);
    }

    @FXML
    private void handleOpenSchedule() {
        AppController.updateView(State.CALENDER);
    }

    @FXML
    private void handleManageTreatments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/PatientLookupDialog.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Patient Lookup");

            Scene scene = new Scene(root);
            AppStyles.apply(scene);
            dialog.setScene(scene);
            dialog.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Could not load patient lookup dialog", e);
        }
    }

    @FXML
    private void handleLegal() {
        AppController.updateView(State.LEGAL);
    }

    @FXML
    private void handleSettingsMenu(){
        AppController.updateView(State.CLINIC_SETTING_MENU);
    }




}
