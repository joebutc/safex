package app.view;

import app.AppController;
import app.model.DatabaseHandler;
import app.view.LoginPage.LoginType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Objects;

public class RegisterPage implements Page {

    private AppController controller;
    private LoginType loginType;

    public RegisterPage(AppController controller, LoginType loginType) {
        this.controller = controller;
        this.loginType = loginType;
    }

    public RegisterPage(LoginType loginType) {
        this.controller = null;
        this.loginType = loginType;
    }

    @Override
    public Scene createScene() {
        BorderPane root = new BorderPane();

        // ── Left: dark branding panel ────────────────────────────────────────
        String tagline = (loginType == LoginType.CLINIC)
                ? "Secure patient care\nstarts here."
                : "Your health, your privacy,\nyour control.";
        root.setLeft(buildBrandPanel(tagline));

        // ── Right: register form ─────────────────────────────────────────────
        VBox card = new VBox(14);
        card.getStyleClass().addAll("card", "card-padding");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(400);

        String titleText = (loginType == LoginType.CLINIC) ? "Register your clinic" : "Create account";
        Label titleLabel = new Label(titleText);
        titleLabel.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 28));

        String subtitleText = (loginType == LoginType.CLINIC) ? "Set up your clinic portal."
                : "Join our secure community.";
        Label subtitleLabel = new Label(subtitleText);
        subtitleLabel.getStyleClass().add("muted-text");
        subtitleLabel.setWrapText(true);

        String userLabelText = (loginType == LoginType.CLINIC) ? "Clinic ID / Username" : "Username";
        Label userLabel = new Label(userLabelText);
        userLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");
        usernameField.setMaxWidth(Double.MAX_VALUE);
        VBox userBox = new VBox(4, userLabel, usernameField);
        userBox.setMaxWidth(Double.MAX_VALUE);

        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Choose a password");
        passwordField.setMaxWidth(Double.MAX_VALUE);
        VBox passBox = new VBox(4, passwordLabel, passwordField);
        passBox.setMaxWidth(Double.MAX_VALUE);

        Label confirmLabel = new Label("Confirm password");
        confirmLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");
        confirmPasswordField.setMaxWidth(Double.MAX_VALUE);
        VBox confirmBox = new VBox(4, confirmLabel, confirmPasswordField);
        confirmBox.setMaxWidth(Double.MAX_VALUE);

        Label feedbackLabel = new Label();
        feedbackLabel.setWrapText(true);
        feedbackLabel.setVisible(false);
        feedbackLabel.setManaged(false);

        String registerBtnText = (loginType == LoginType.CLINIC) ? "Register clinic" : "Create account";
        Button registerBtn = new Button(registerBtnText);
        registerBtn.getStyleClass().add("primary-button");
        registerBtn.setDefaultButton(true);
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setMinHeight(42);

        String loginLinkText = (loginType == LoginType.CLINIC) ? "Already registered? Log in to clinic"
                : "Already have an account? Log in";
        Button loginLink = new Button(loginLinkText);
        loginLink.getStyleClass().add("link-button");

        registerBtn.setOnAction(e -> handleRegister(usernameField, passwordField, confirmPasswordField, feedbackLabel));
        confirmPasswordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER)
                handleRegister(usernameField, passwordField, confirmPasswordField, feedbackLabel);
        });
        loginLink.setOnAction(e -> {
            if (controller != null) controller.goToLogin(loginType);
        });

        card.getChildren().addAll(titleLabel, subtitleLabel, userBox, passBox,
                confirmBox, feedbackLabel, registerBtn, loginLink);

        StackPane formArea = new StackPane(card);
        formArea.setPadding(new Insets(32));
        formArea.setStyle("-fx-background-color: #F8FAFC;");
        root.setCenter(formArea);

        return new Scene(root, AppStyles.SCENE_WIDTH, AppStyles.SCENE_HEIGHT);
    }

    // ── Brand panel ──────────────────────────────────────────────────────────

    private VBox buildBrandPanel(String taglineText) {
        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setPrefWidth(310);
        panel.setMinWidth(310);
        panel.setMaxWidth(310);
        panel.setPadding(new Insets(48, 28, 48, 28));
        panel.setStyle("-fx-background-color: #0A0A0A;");

        try {
            Image logo = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream("/images/safex_logo.png")),
                    254, 200, true, true);
            ImageView logoView = new ImageView(logo);
            logoView.setPreserveRatio(true);
            panel.getChildren().add(logoView);
        } catch (Exception ignored) {
            Label fallback = new Label("SafeX");
            fallback.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 36));
            fallback.setTextFill(Color.WHITE);
            panel.getChildren().add(fallback);
        }

        Label tagline = new Label(taglineText);
        tagline.setFont(Font.font("System", 13));
        tagline.setTextFill(Color.web("#94A3B8"));
        tagline.setWrapText(true);
        tagline.setMaxWidth(240);
        panel.getChildren().add(tagline);

        return panel;
    }

    // ── Register logic ───────────────────────────────────────────────────────

    private void handleRegister(TextField userField, PasswordField passField, PasswordField confirmField,
            Label feedbackLabel) {
        String username = userField.getText();
        String password = passField.getText();
        String confirm = confirmField.getText();

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showFeedback(feedbackLabel, "Please fill in all fields.", false);
            return;
        }

        if (!username.matches("^[a-zA-Z]+$")) {
            showFeedback(feedbackLabel, "Username must contain only letters and no spaces.", false);
            return;
        }

        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        if (!password.matches(passwordPattern)) {
            showFeedback(feedbackLabel,
                    "Password must be at least 8 characters and include uppercase, lowercase, a number, and a special character.",
                    false);
            return;
        }

        if (!password.equals(confirm)) {
            showFeedback(feedbackLabel, "Passwords do not match.", false);
            return;
        }

        if (controller != null) {
            boolean success = controller.attemptRegister(username, password, loginType);
            if (success) {
                showFeedback(feedbackLabel, "Registration successful! You can now log in.", true);
            } else {
                showFeedback(feedbackLabel, "Username already exists or database error.", false);
            }
        } else {
            System.out.println("Warning: Controller is null. Calling DatabaseHandler directly.");
            boolean success = DatabaseHandler.registerUser(username, password, loginType.name());
            if (success) {
                showFeedback(feedbackLabel, "User registered successfully.", true);
            } else {
                showFeedback(feedbackLabel, "Registration failed.", false);
            }
        }
    }

    private void showFeedback(Label label, String message, boolean success) {
        label.setText(message);
        label.getStyleClass().removeAll("error-text", "success-text");
        label.getStyleClass().add(success ? "success-text" : "error-text");
        label.setVisible(true);
        label.setManaged(true);
    }
}
