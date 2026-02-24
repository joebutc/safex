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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class RegisterPage implements Page {

    private AppController controller;
    private LoginType loginType;

    // Updated constructor to require the LoginType
    public RegisterPage(AppController controller, LoginType loginType) {
        this.controller = controller;
        this.loginType = loginType;
    }

    // Updated fallback constructor
    public RegisterPage(LoginType loginType) {
        this.controller = null;
        this.loginType = loginType;
    }

    @Override
    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(24));

        VBox card = new VBox(14);
        card.getStyleClass().addAll("card", "card-padding");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(440);

        // Customise titles based on registration type
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
            if (e.getCode() == KeyCode.ENTER) {
                handleRegister(usernameField, passwordField, confirmPasswordField, feedbackLabel);
            }
        });

        loginLink.setOnAction(e -> {
            if (controller != null) {
                // Return the user to the correct login page based on their current path
                controller.goToLogin(loginType);
            }
        });

        card.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                userBox,
                passBox,
                confirmBox,
                feedbackLabel,
                registerBtn,
                loginLink);

        StackPane center = new StackPane(card);
        center.setPadding(new Insets(24));
        root.setCenter(center);

        return new Scene(root, AppStyles.SCENE_WIDTH, AppStyles.SCENE_HEIGHT);
    }

    private void handleRegister(TextField userField, PasswordField passField, PasswordField confirmField,
            Label feedbackLabel) {
        String username = userField.getText();
        String password = passField.getText();
        String confirm = confirmField.getText();

        // Basic Validation
        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showFeedback(feedbackLabel, "Please fill in all fields.", false);
            return;
        }

        // Username validation: No spaces, alphabet characters only
        if (!username.matches("^[a-zA-Z]+$")) {
            showFeedback(feedbackLabel, "Username must contain only letters and no spaces.", false);
            return;
        }

        // Password validation: Min length 8, numeric, upper, lower, and special
        // character
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

        // Logic for Database Insertion
        if (controller != null) {
            // Pass the loginType to the controller
            boolean success = controller.attemptRegister(username, password, loginType);
            if (success) {
                showFeedback(feedbackLabel, "Registration successful! You can now log in.", true);
            } else {
                showFeedback(feedbackLabel, "Username already exists or database error.", false);
            }
        } else {
            // Fallback for manual testing
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