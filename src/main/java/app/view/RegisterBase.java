package app.view;

import app.AppController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

public abstract class RegisterBase {
    static final String PASSWORD_RULE_MESSAGE =
            "Password must be at least 8 characters and include uppercase, lowercase, a number, and a special character.";
    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    @FXML protected VBox brandPanel;
    @FXML protected Label taglineLabel;
    @FXML protected Label titleLabel;
    @FXML protected Label subtitleLabel;
    @FXML protected TextField usernameField;
    @FXML protected TextField emailField;
    @FXML protected TextField addressField;
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
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String email = emailField == null || emailField.getText() == null ? "" : emailField.getText().trim();
        String address = addressField == null || addressField.getText() == null ? "" : addressField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || address.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showFeedback("Please fill in all fields.", false);
            return;
        }

        if (!username.matches("^[a-zA-Z]+$")) {
            showFeedback("Username must contain only letters and no spaces.", false);
            return;
        }

        if (!isValidEmail(email)) {
            showFeedback("Please enter a valid email address.", false);
            return;
        }

        if (!isValidPassword(password)) {
            showFeedback(PASSWORD_RULE_MESSAGE, false);
            return;
        }

        if (!password.equals(confirm)) {
            showFeedback("Passwords do not match.", false);
            return;
        }

        boolean success = AppController.attemptRegister(
                username,
                preparePasswordForStorage(password),
                email,
                address,
                getLoginType());
        if (success) {
            showFeedback("Verification code sent. Check your email to finish registration.", true);
        } else {
            String errorMessage = AppController.getRegistrationErrorMessage();
            showFeedback(
                    errorMessage == null ? "Username or email already exists, or a database error occurred." : errorMessage,
                    false);
        }
    }

    static boolean isValidPassword(String password) {
        return password != null && password.matches(PASSWORD_PATTERN);
    }

    static boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_PATTERN);
    }

    static String preparePasswordForStorage(String password) {
        return password == null ? "" : password;
    }

    private void showFeedback(String message, boolean success) {
        feedbackLabel.setText(message);
        feedbackLabel.getStyleClass().removeAll("error-text", "success-text");
        feedbackLabel.getStyleClass().add(success ? "success-text" : "error-text");
        feedbackLabel.setVisible(true);
        feedbackLabel.setManaged(true);
    }
}
