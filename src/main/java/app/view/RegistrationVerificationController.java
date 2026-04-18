package app.view;

import app.AppController;
import app.model.State;
import app.service.RecoveryService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;

public class RegistrationVerificationController {
    @FXML private Label subtitleLabel;
    @FXML private TextField otpField;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        if (!AppController.hasPendingRegistration()) {
            Platform.runLater(() -> AppController.updateView(AppController.getPendingRegistrationReturnState()));
            return;
        }

        if (subtitleLabel != null && AppController.getPendingRegistrationEmail() != null) {
            subtitleLabel.setText("We sent a 6-digit verification code to "
                    + AppController.getPendingRegistrationEmail()
                    + ". Enter it below to finish creating your account.");
        }

        if (otpField != null) {
            otpField.setTextFormatter(new TextFormatter<String>(
                    change -> change.getControlNewText().matches("\\d{0,6}") ? change : null));
            otpField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    handleVerifyRegistration();
                }
            });
        }
    }

    @FXML
    private void handleVerifyRegistration() {
        String code = otpField == null ? "" : otpField.getText().trim();
        if (!code.matches("\\d{6}")) {
            showMessage("Enter the 6-digit code from your email.", false);
            return;
        }

        if (!AppController.completePendingRegistration(code)) {
            showMessage("The verification code is invalid, expired, or the account could not be created.", false);
            return;
        }
    }

    @FXML
    private void handleResendCode() {
        if (!AppController.resendPendingRegistrationCode()) {
            showMessage("We couldn't resend the verification code right now. Please try again.", false);
            return;
        }
        showMessage("A new verification code was sent. It expires in "
                + RecoveryService.OTP_EXPIRY_MINUTES + " minutes.", true);
    }

    @FXML
    private void handleBackToRegister() {
        State returnState = AppController.getPendingRegistrationReturnState();
        AppController.cancelPendingRegistration();
        AppController.updateView(returnState);
    }

    private void showMessage(String message, boolean success) {
        if (statusLabel == null) {
            return;
        }

        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("error-text", "success-text");
        statusLabel.getStyleClass().add(success ? "success-text" : "error-text");
        statusLabel.setManaged(true);
        statusLabel.setVisible(true);
    }
}
