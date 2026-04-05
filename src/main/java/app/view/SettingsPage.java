package app.view;

import app.AppController;
import app.model.State;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class SettingsPage {
    @FXML private Button homeButton;
    @FXML private ImageView homeIcon;
    @FXML private CheckBox agreeCheckBox;
    @FXML private Button acceptBtn;

    @FXML
    private void initialize() {
        if (acceptBtn != null && agreeCheckBox != null) {
            acceptBtn.disableProperty().bind(agreeCheckBox.selectedProperty().not());
        }
    }

    @FXML
    private void handleHome() {
        AppController.goToHomeDashboard();
    }

    @FXML
    private void handleDataCollected() {
        showDialog("Your Personal Data", "/scenes/MyData.fxml");
    }

    @FXML
    private void handleSettingsMenu(){
        String userType = AppController.getCurrentLoginType();
        if ("CLINIC".equals(userType)) {
            AppController.goToHomeDashboard();
        } else {
            AppController.updateView(State.SETTINGS_MENU);
        }

    }

    @FXML
    private void handleWhyUsed() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText("Why your data is used");
        a.setContentText("""
            We use your data to:
            • Improve app performance
            • Personalize your experience
            • Ensure platform security
            • Provide customer support
        """);
        a.showAndWait();
    }

    @FXML
    private void handleAccess() {
        showDialog("Who can access your data", "/scenes/DataAccessDialog.fxml");
    }

    @FXML
    private void handleRetention() {
        String current = AppController.getDataRetention();
        ChoiceDialog<String> dialog = new ChoiceDialog<>(current,
                "30 days", "90 days", "180 days", "While account is active");
        dialog.setTitle("Data Retention");
        dialog.setHeaderText("How long should we keep your health data?");
        dialog.setContentText("STI tests and treatments older than this will be deleted.");
        dialog.showAndWait().ifPresent(AppController::saveDataRetention);
    }

    @FXML
    private void handleRights() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText("Your Rights");
        a.setContentText("""
            You can:
            • Request a copy of your data
            • Correct inaccurate info
            • Delete your account anytime
        """);
        a.showAndWait();

    }

    @FXML
    private void handleTestingPermissions() {
        showDialog("Testing Data Permissions", "/scenes/TestPermissionsDialog.fxml");
    }

    @FXML
    private void handleTreatmentPermissions() {
        showDialog("Treatment Data Permissions", "/scenes/TreatmentPermissionsDialog.fxml");
    }

    @FXML
    private void handleReadFullTerms() {
        AppController.updateView(State.LEGAL);
    }

    @FXML
    private void handleAccept() {
        if (agreeCheckBox != null && !agreeCheckBox.isSelected()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Please confirm terms acceptance");
            alert.setContentText("Tick the checkbox to confirm you have read and accepted the Terms & Conditions.");
            alert.showAndWait();
            return;
        }
        AppController.acceptTerms();
        AppController.goToHomeDashboard();
    }

    private void showDialog(String title, String resourcePath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(title);

            Scene dialogScene = new Scene(root);
            AppStyles.apply(dialogScene);
            dialog.setScene(dialogScene);
            dialog.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Could not load dialog " + resourcePath, e);
        }
    }
}
