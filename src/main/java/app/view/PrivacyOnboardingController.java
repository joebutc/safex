package app.view;

import app.AppController;
import app.model.State;
import app.util.PdfOpener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;

public class PrivacyOnboardingController {

    @FXML private ScrollPane scrollPane;
    @FXML private CheckBox agreeCheckBox;
    @FXML private CheckBox privacyPolicyCheckBox;
    @FXML private Button continueBtn;

    @FXML
    private void initialize() {
        continueBtn.disableProperty().bind(
                agreeCheckBox.selectedProperty().not()
                        .or(privacyPolicyCheckBox.selectedProperty().not())
        );

        scrollPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    scrollPane.setVvalue(0);
                    scrollPane.setHvalue(0);
                });
            }
        });
    }

    @FXML
    private void openTermsPdf() {
        PdfOpener.openResourcePdf("tcs.pdf");
    }

    @FXML
    private void openPrivacyPdf() {
        PdfOpener.openResourcePdf("privacy.pdf");
    }

    @FXML
    private void handleContinue() {
        if (!agreeCheckBox.isSelected() || !privacyPolicyCheckBox.isSelected()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Please accept the terms");
            alert.setContentText("You must accept both the Terms & Conditions and Privacy Policy to continue.");
            alert.showAndWait();
            return;
        }

        AppController.updateView(State.PERSONA_SELECTION);
    }
}
