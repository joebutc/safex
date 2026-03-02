package app.view;

import app.AppController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Objects;

public abstract class RegisterBase {
    @FXML protected VBox brandPanel;
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

    protected void buildBrandPanel(String tagline) {
        try {
            Image logo = new Image(
                    Objects.requireNonNull(
                            getClass().getResourceAsStream("/images/safex_logo.png")),
                    254, 200, true, true);

            ImageView logoView = new ImageView(logo);
            logoView.setPreserveRatio(true);
            brandPanel.getChildren().add(logoView);

        } catch (Exception ignored) {
            Label fallback = new Label("SafeX");
            fallback.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 36));
            fallback.setTextFill(Color.WHITE);
            brandPanel.getChildren().add(fallback);
        }

        Label taglineLabel = new Label(tagline);
        taglineLabel.setFont(Font.font("System", 13));
        taglineLabel.setTextFill(Color.web("#94A3B8"));
        taglineLabel.setWrapText(true);
        taglineLabel.setMaxWidth(240);

        brandPanel.getChildren().add(taglineLabel);
    }

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
