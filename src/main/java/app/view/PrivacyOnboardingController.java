package app.view;

import app.AppController;
import app.model.State;
import app.util.PdfOpener;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class PrivacyOnboardingController {

    @FXML private ScrollPane scrollPane;
    @FXML private CheckBox agreeCheckBox;
    @FXML private CheckBox privacyPolicyCheckBox;
    @FXML private Button continueBtn;

    private final ChangeListener<Number> initialTopGuard = (obs, oldVal, newVal) -> {
        if (newVal.doubleValue() > scrollPane.getVmin() + 1e-6) {
            Platform.runLater(this::scrollToTop);
        }
    };

    @FXML
    private void initialize() {
        continueBtn.disableProperty().bind(
                agreeCheckBox.selectedProperty().not()
                        .or(privacyPolicyCheckBox.selectedProperty().not())
        );

        scrollPane.vvalueProperty().addListener(initialTopGuard);

        scrollPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    scrollToTop();
                    Platform.runLater(this::scrollToTop);
                });
            }
        });

        scrollPane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> releaseInitialTopGuard());
        scrollPane.addEventFilter(ScrollEvent.SCROLL, e -> releaseInitialTopGuard());
        scrollPane.addEventFilter(KeyEvent.KEY_PRESSED, e -> releaseInitialTopGuard());
    }

    private void scrollToTop() {
        scrollPane.setHvalue(scrollPane.getHmin());
        scrollPane.setVvalue(scrollPane.getVmin());
    }

    private void releaseInitialTopGuard() {
        scrollPane.vvalueProperty().removeListener(initialTopGuard);
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
