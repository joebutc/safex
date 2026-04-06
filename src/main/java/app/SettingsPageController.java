package app;

import app.model.State;
import javafx.fxml.FXML;

public class SettingsPageController {

    @FXML
    private void handleBackToDashboard() {
        AppController.goToHomeDashboard();
    }

    @FXML
    private void handleSettings() {
        AppController.updateView(State.SETTINGS);
    }
}
