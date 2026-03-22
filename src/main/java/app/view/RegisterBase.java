package app.view;

import app.AppController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

public abstract class RegisterBase {
    @FXML protected VBox brandPanel;
    @FXML protected Label taglineLabel;
    @FXML protected Label titleLabel;
    @FXML protected Label subtitleLabel;
    @FXML protected TextField usernameField;
    @FXML protected PasswordField passwordField;
    @FXML protected PasswordField confirmPasswordField;
    @FXML protected Label feedbackLabel;

    @FXML
    public final void initialize() {
        confirmPasswordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleRegister();
            }
        });
        onInitialize();
    }

    protected abstract void onInitialize();
    protected abstract String getLoginType();

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showFeedback("Please fill in all fields.", false);
            return;
        }

        if (!username.matches("^[a-zA-Z]+$")) {
            showFeedback("Username must contain only letters and no spaces.", false);
            return;
        }

        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        if (!password.matches(passwordPattern)) {
            showFeedback("Password must be at least 8 characters and include uppercase, lowercase, a number, and a special character.", false);
            return;
        }

        if (!password.equals(confirm)) {
            showFeedback("Passwords do not match.", false);
            return;
        }

        boolean success = AppController.attemptRegister(username, password, getLoginType());
        if (success) {
            showFeedback("Registration successful! You can now log in.", true);
        } else {
            showFeedback("Username already exists or database error.", false);
        }
    }

    private void showFeedback(String message, boolean success) {
        feedbackLabel.setText(message);
        feedbackLabel.getStyleClass().removeAll("error-text", "success-text");
        feedbackLabel.getStyleClass().add(success ? "success-text" : "error-text");
        feedbackLabel.setVisible(true);
        feedbackLabel.setManaged(true);
    }
}
