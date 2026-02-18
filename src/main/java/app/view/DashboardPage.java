package app.view;

import app.AppController;
import app.model.State;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class DashboardPage {
    @FXML private Label welcomeLabel;

    @FXML
    private void initialize() {
        String user = AppController.getCurrentUser();
        if (user == null || user.isBlank()) {
            welcomeLabel.setText("Welcome back");
        } else {
            welcomeLabel.setText("Welcome back, " + user);
        }
    }

    @FXML
    private void handleLogout() {
        AppController.updateView(State.LOGIN);
    }

    @FXML
    private void goToNotify() {
        AppController.updateView(State.NOTIFY);
    }

    @FXML
    private void goToSettings() {
        AppController.updateView(State.SETTNGS);
    }

    @FXML
    private void goToLegal() {
        AppController.updateView(State.LEGAL);
    }

    @FXML
    private void openResources() {
        try {
            if (!Desktop.isDesktopSupported()) {
                showAlert("Desktop browsing is not supported.");
                return;
            }
            Desktop.getDesktop().browse(
                    new URI("https://www.nhs.uk/conditions/sexually-transmitted-infections-stis/")
            );
        } catch (URISyntaxException | IOException e) {
            showAlert("Unable to open NHS resources page.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
