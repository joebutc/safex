package app.view;

import app.AppController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;

public abstract class LoginBase {
    @FXML protected VBox brandPanel;
    @FXML protected Label taglineLabel;
    @FXML protected Label titleLabel;
    @FXML protected Label subtitleLabel;
    @FXML protected TextField usernameField;
    @FXML protected PasswordField passwordField;
    @FXML protected Label errorLabel;

    @FXML
    public final void initialize() {
        passwordField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
        onInitialize();
    }

    protected abstract void onInitialize();
    protected abstract String getLoginType(); // "STANDARD" or "CLINIC"

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        boolean success = AppController.attemptLogin(username, password, getLoginType());
        if (!success) {
            showError("Invalid username or password.");
        } else {
            hideError();
        }
    }

    protected void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    protected void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
