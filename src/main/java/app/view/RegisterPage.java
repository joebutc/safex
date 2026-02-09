package app.view;

import app.AppController;
import app.model.DatabaseHandler;
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

    public RegisterPage(AppController controller) {
        this.controller = controller;
    }

    public RegisterPage() {
        this.controller = null;
    }

    @Override
    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(24));

        VBox card = new VBox(14);
        card.getStyleClass().addAll("card", "card-padding");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(440);

        Label titleLabel = new Label("Create account");
        titleLabel.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 28));

        Label subtitleLabel = new Label("Join our secure community.");
        subtitleLabel.getStyleClass().add("muted-text");
        subtitleLabel.setWrapText(true);

        Label userLabel = new Label("Username");
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

        Button registerBtn = new Button("Create account");
        registerBtn.getStyleClass().add("primary-button");
        registerBtn.setDefaultButton(true);
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setMinHeight(42);

        Button loginLink = new Button("Already have an account? Log in");
        loginLink.getStyleClass().add("link-button");

        registerBtn.setOnAction(e -> handleRegister(usernameField, passwordField, confirmPasswordField, feedbackLabel));
        confirmPasswordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleRegister(usernameField, passwordField, confirmPasswordField, feedbackLabel);
            }
        });

        loginLink.setOnAction(e -> {
            if (controller != null) {
                controller.goToLogin();
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
                loginLink
        );

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

        if (!password.equals(confirm)) {
            showFeedback(feedbackLabel, "Passwords do not match.", false);
            return;
        }

        // Logic for Database Insertion
        if (controller != null) {
            boolean success = controller.attemptRegister(username, password);
            if (success) {
                showFeedback(feedbackLabel, "Registration successful! You can now log in.", true);
            } else {
                showFeedback(feedbackLabel, "Username already exists or database error.", false);
            }
        } else {
            // Fallback for manual testing (assuming DatabaseHandler has a register method)
            System.out.println("Warning: Controller is null. Calling DatabaseHandler directly.");
            boolean success = DatabaseHandler.registerUser(username, password);
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
