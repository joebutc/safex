package app.view;

import app.AppController;
import app.model.DatabaseHandler;
import app.model.State;
import app.service.EmailService;
import app.service.RecoveryService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class AccountRecoveryController {
    @FXML private TextField emailField;
    @FXML private TextField otpField;
    @FXML private VBox otpStepBox;
    @FXML private Label recoveryStatusLabel;

    @FXML private Label resetSubtitleLabel;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label resetStatusLabel;

    @FXML
    private void initialize() {
        initialiseRecoveryPage();
        initialiseResetPage();
    }

    private void initialiseRecoveryPage() {
        if (emailField != null) {
            emailField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    handleSendCode();
                }
            });
        }

        if (otpField != null) {
            otpField.setTextFormatter(new TextFormatter<String>(
                    change -> change.getControlNewText().matches("\\d{0,6}") ? change : null));
            otpField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    handleVerifyCode();
                }
            });
        }
    }

    private void initialiseResetPage() {
        if (newPasswordField == null || confirmPasswordField == null) {
            return;
        }

        if (AppController.getRecoveryEmail() == null || AppController.getRecoveryAccountType() == null) {
            AppController.startRecoveryFlow(AppController.getRecoveryReturnState());
            return;
        }

        if (resetSubtitleLabel != null) {
            resetSubtitleLabel.setText("Create a new password for " + AppController.getRecoveryEmail() + ".");
        }

        confirmPasswordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleResetPassword();
            }
        });
    }

    @FXML
    private void handleSendCode() {
        String email = normalizeEmail(emailField == null ? null : emailField.getText());
        if (!RegisterBase.isValidEmail(email)) {
            showMessage(recoveryStatusLabel, "Please enter a valid email address.", false);
            return;
        }

        String accountType = DatabaseHandler.getAccountTypeByEmail(email);
        if (accountType == null) {
            showMessage(recoveryStatusLabel, "We couldn't find an account for that email address.", false);
            return;
        }

        String code = RecoveryService.generateRecoveryCode();
        if (!DatabaseHandler.storeRecoveryCode(email, code)) {
            showMessage(recoveryStatusLabel, "We couldn't generate a recovery code. Please try again.", false);
            return;
        }

        if (!EmailService.sendRecoveryCode(email, code)) {
            DatabaseHandler.clearRecoveryCode(email);
            showMessage(recoveryStatusLabel, "We couldn't send the email right now. Please try again.", false);
            return;
        }

        AppController.setRecoveryContext(email, accountType);
        setOtpModeVisible(true);
        showMessage(
                recoveryStatusLabel,
                "We sent a 6-digit code to " + email + ". It expires in " + RecoveryService.OTP_EXPIRY_MINUTES
                        + " minutes.",
                true);
    }

    @FXML
    private void handleVerifyCode() {
        String email = AppController.getRecoveryEmail();
        if (email == null) {
            showMessage(recoveryStatusLabel, "Request a recovery code before entering the OTP.", false);
            return;
        }

        String code = otpField == null ? "" : otpField.getText().trim();
        if (!code.matches("\\d{6}")) {
            showMessage(recoveryStatusLabel, "Enter the 6-digit code sent to your email.", false);
            return;
        }

        if (!DatabaseHandler.verifyRecoveryCode(email, code)) {
            showMessage(recoveryStatusLabel, "The recovery code is invalid or has expired.", false);
            return;
        }

        AppController.goToResetPassword();
    }

    @FXML
    private void handleResetPassword() {
        String email = AppController.getRecoveryEmail();
        if (email == null || AppController.getRecoveryAccountType() == null) {
            AppController.startRecoveryFlow(AppController.getRecoveryReturnState());
            return;
        }

        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword.isBlank() || confirmPassword.isBlank()) {
            showMessage(resetStatusLabel, "Please fill in all fields.", false);
            return;
        }

        if (!RegisterBase.isValidPassword(newPassword)) {
            showMessage(resetStatusLabel, RegisterBase.PASSWORD_RULE_MESSAGE, false);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showMessage(resetStatusLabel, "Passwords do not match.", false);
            return;
        }

        if (!DatabaseHandler.updatePasswordByEmail(email, RegisterBase.preparePasswordForStorage(newPassword))) {
            showMessage(resetStatusLabel, "We couldn't update your password. Please try again.", false);
            return;
        }

        State returnState = AppController.getRecoveryReturnState();
        showMessage(resetStatusLabel, "Password updated. Redirecting you to sign in.", true);

        PauseTransition pause = new PauseTransition(Duration.seconds(1.2));
        pause.setOnFinished(event -> {
            AppController.clearRecoveryContext();
            AppController.updateView(returnState);
        });
        pause.play();
    }

    @FXML
    private void handleBackToLogin() {
        State returnState = AppController.getRecoveryReturnState();
        AppController.clearRecoveryContext();
        AppController.updateView(returnState);
    }

    private void setOtpModeVisible(boolean visible) {
        if (otpStepBox != null) {
            otpStepBox.setVisible(visible);
            otpStepBox.setManaged(visible);
        }
    }

    private void showMessage(Label label, String message, boolean success) {
        if (label == null) {
            return;
        }

        label.setText(message);
        label.getStyleClass().removeAll("error-text", "success-text");
        label.getStyleClass().add(success ? "success-text" : "error-text");
        label.setVisible(true);
        label.setManaged(true);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
