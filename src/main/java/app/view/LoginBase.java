package app.view;

import app.AppController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.input.KeyCode;

import java.util.Objects;

public abstract class LoginBase {
    @FXML protected VBox brandPanel;
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
