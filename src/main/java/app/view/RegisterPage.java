package app.view;

import app.AppController;
import app.model.State;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class RegisterPage {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label feedbackLabel;

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        // Basic Validation
        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showFeedback("Please fill in all fields.", false);
            return;
        }

        // Username validation: No spaces, alphabet characters only
        if (!username.matches("^[a-zA-Z]+$")) {
            showFeedback("Username must contain only letters and no spaces.", false);
            return;
        }

        // Password validation: Min length 8, numeric, upper, lower, and special character
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        if (!password.matches(passwordPattern)) {
            showFeedback("Password must be at least 8 characters and include uppercase, lowercase, a number, and a special character.", false);
            return;
        }

        if (!password.equals(confirm)) {
            showFeedback("Passwords do not match.", false);
            return;
        }

        // Logic for Database Insertion
        boolean success = AppController.attemptRegister(username, password);
        if (success) {
            showFeedback("Registration successful! You can now log in.", true);
        } else {
            showFeedback("Username already exists or database error.", false);
        }
    }

    @FXML
    private void handleLoginLink() {
        AppController.updateView(State.LOGIN);
    }

    @FXML
    private void handleEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleRegister();
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
