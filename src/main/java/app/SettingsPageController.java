package app;

import app.model.State;
import javafx.fxml.FXML;

public class SettingsPageController {

    @FXML
    private void handleBackToDashboard() {
        AppController.goToHomeDashboard(); // Assuming this is your state name
    }

    @FXML private void handleSettings() {
        AppController.updateView(State.SETTINGS);
    }

    @FXML private void handleLegal() {
        AppController.updateView(State.LEGAL);
    }

}
