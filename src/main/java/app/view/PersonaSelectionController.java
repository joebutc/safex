package app.view;

import app.AppController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class PersonaSelectionController {

    @FXML private VBox anonymousCard;
    @FXML private VBox balancedCard;
    @FXML private VBox connectedCard;
    @FXML private Button continueBtn;

    private String selectedPersona = null;

    @FXML
    private void selectAnonymous() {
        selectCard("anonymous");
    }

    @FXML
    private void selectBalanced() {
        selectCard("balanced");
    }

    @FXML
    private void selectConnected() {
        selectCard("connected");
    }

    private void selectCard(String persona) {
        selectedPersona = persona;

        anonymousCard.getStyleClass().remove("persona-card-selected");
        balancedCard.getStyleClass().remove("persona-card-selected");
        connectedCard.getStyleClass().remove("persona-card-selected");

        switch (persona) {
            case "anonymous" -> anonymousCard.getStyleClass().add("persona-card-selected");
            case "balanced" -> balancedCard.getStyleClass().add("persona-card-selected");
            case "connected" -> connectedCard.getStyleClass().add("persona-card-selected");
        }

        continueBtn.setDisable(false);
    }

    @FXML
    private void handleContinue() {
        if (selectedPersona == null) {
            return;
        }

        switch (selectedPersona) {
            case "anonymous" -> applyAnonymousSettings();
            case "balanced" -> applyBalancedSettings();
            case "connected" -> applyConnectedSettings();
        }

        AppController.acceptTerms();
        AppController.goToHomeDashboard();
    }

    private void applyAnonymousSettings() {
        AppController.saveDataAccessPermissions(false, false, false);
        AppController.saveTestPermissions(false, false, 3);
        AppController.saveTreatmentPermissions(false, false);
        AppController.saveDataRetention("30 days");
    }

    private void applyBalancedSettings() {
        AppController.saveDataAccessPermissions(true, false, false);
        AppController.saveTestPermissions(true, false, 3);
        AppController.saveTreatmentPermissions(true, false);
        AppController.saveDataRetention("90 days");
    }

    private void applyConnectedSettings() {
        AppController.saveDataAccessPermissions(true, true, true);
        AppController.saveTestPermissions(true, true, 3);
        AppController.saveTreatmentPermissions(true, true);
        AppController.saveDataRetention("180 days");
    }
}
