package app.view;

import app.AppController;
import app.util.PdfOpener;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;

public class PrivacyOnboardingController {

    @FXML private ScrollPane scrollPane;

    @FXML private CheckBox staffCheckBox;
    @FXML private CheckBox analyticsCheckBox;
    @FXML private CheckBox partnersCheckBox;

    @FXML private CheckBox testRemindersCheckBox;
    @FXML private ComboBox<String> reminderIntervalCombo;
    @FXML private CheckBox shareWithDoctorCheckBox;

    @FXML private CheckBox treatmentRemindersCheckBox;
    @FXML private CheckBox clinicAccessCheckBox;

    @FXML private CheckBox agreeCheckBox;
    @FXML private CheckBox privacyPolicyCheckBox;
    @FXML private Button continueBtn;

    @FXML
    private void initialize() {
        reminderIntervalCombo.setItems(FXCollections.observableArrayList(
                "1 month", "2 months", "3 months", "6 months", "12 months"
        ));
        reminderIntervalCombo.setValue("3 months");

        continueBtn.disableProperty().bind(
                agreeCheckBox.selectedProperty().not()
                        .or(privacyPolicyCheckBox.selectedProperty().not())
        );

        scrollPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null || scrollPane.getContent() == null) {
                return;
            }

            var content = scrollPane.getContent();
            var listenerRef = new Object() {
                javafx.beans.value.ChangeListener<javafx.geometry.Bounds> listener;
            };
            listenerRef.listener = (o, oldBounds, newBounds) -> {
                if (newBounds == null || newBounds.getHeight() <= 0) {
                    return;
                }
                Platform.runLater(() -> {
                    scrollPane.setHvalue(scrollPane.getHmin());
                    scrollPane.setVvalue(scrollPane.getVmin());
                    content.requestFocus();
                });
                content.layoutBoundsProperty().removeListener(listenerRef.listener);
            };
            content.layoutBoundsProperty().addListener(listenerRef.listener);

            Platform.runLater(() -> {
                scrollPane.setHvalue(scrollPane.getHmin());
                scrollPane.setVvalue(scrollPane.getVmin());
            });
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

        AppController.saveDataAccessPermissions(
                staffCheckBox.isSelected(),
                analyticsCheckBox.isSelected(),
                partnersCheckBox.isSelected()
        );

        int intervalMonths = parseIntervalMonths(reminderIntervalCombo.getValue());
        AppController.saveTestPermissions(
                testRemindersCheckBox.isSelected(),
                shareWithDoctorCheckBox.isSelected(),
                intervalMonths
        );

        AppController.saveTreatmentPermissions(
                treatmentRemindersCheckBox.isSelected(),
                clinicAccessCheckBox.isSelected()
        );

        AppController.acceptTerms();
        AppController.goToHomeDashboard();
    }

    private int parseIntervalMonths(String value) {
        if (value == null) return 3;
        return switch (value) {
            case "1 month" -> 1;
            case "2 months" -> 2;
            case "3 months" -> 3;
            case "6 months" -> 6;
            case "12 months" -> 12;
            default -> 3;
        };
    }
}
