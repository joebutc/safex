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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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

        // 1. Setup the Root Layout
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setSpacing(15);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #ffffff;");

        // 2. Styling Constants (Matching Login Page)
        String INPUT_STYLE = "-fx-background-radius: 50; " +
                "-fx-border-radius: 50; " +
                "-fx-border-color: #cccccc; " +
                "-fx-padding: 12 20 12 20; " +
                "-fx-font-size: 14px;";

        String BTN_PRIMARY_STYLE = "-fx-background-radius: 50; " +
                "-fx-background-color: #000000; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 16px;";

        String LINK_STYLE = "-fx-background-color: transparent; " +
                "-fx-text-fill: #666666; " +
                "-fx-underline: true; " +
                "-fx-font-size: 14px;";

        // 3. Title Section
        Label titleLabel = new Label("Create Account");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 36));

        Label subtitleLabel = new Label("Join our secure community.");
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 18));
        subtitleLabel.setTextFill(Color.GRAY);

        // 4. Input Fields
        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a Username");
        usernameField.setStyle(INPUT_STYLE);
        usernameField.setMaxWidth(350);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Choose a Password");
        passwordField.setStyle(INPUT_STYLE);
        passwordField.setMaxWidth(350);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.setStyle(INPUT_STYLE);
        confirmPasswordField.setMaxWidth(350);

        // 5. Feedback Label
        Label feedbackLabel = new Label();
        feedbackLabel.setWrapText(true);
        feedbackLabel.setMaxWidth(350);
        feedbackLabel.setVisible(false);

        // 6. Buttons
        Button registerBtn = new Button("Sign Up");
        registerBtn.setStyle(BTN_PRIMARY_STYLE);
        registerBtn.setMinWidth(200);
        registerBtn.setMinHeight(45);

        Button loginLink = new Button("Already have an account? Log In");
        loginLink.setStyle(LINK_STYLE);

        // --- LOGIC SECTION ---

        registerBtn.setOnAction(e -> handleRegister(usernameField, passwordField, confirmPasswordField, feedbackLabel));

        loginLink.setOnAction(e -> {
            if (controller != null) {
                // Assuming your controller has a method to show the login page
                // controller.showLoginPage();
            }
        });

        // 7. Add components
        root.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                new Label(""),
                usernameField,
                passwordField,
                confirmPasswordField,
                feedbackLabel,
                registerBtn,
                loginLink);

        return new Scene(root, 800, 600);
    }

    private void handleRegister(TextField userField, PasswordField passField, PasswordField confirmField,
            Label feedbackLabel) {
        String username = userField.getText();
        String password = passField.getText();
        String confirm = confirmField.getText();

        // Basic Validation
        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            feedbackLabel.setText("Please fill in all fields.");
            feedbackLabel.setTextFill(Color.RED);
            feedbackLabel.setVisible(true);
            return;
        }

        if (!password.equals(confirm)) {
            feedbackLabel.setText("Passwords do not match.");
            feedbackLabel.setTextFill(Color.RED);
            feedbackLabel.setVisible(true);
            return;
        }

        // Logic for Database Insertion
        if (controller != null) {
            boolean success = controller.attemptRegister(username, password);
            if (success) {
                feedbackLabel.setText("Registration successful! You can now log in.");
                feedbackLabel.setTextFill(Color.GREEN);
            } else {
                feedbackLabel.setText("Username already exists or database error.");
                feedbackLabel.setTextFill(Color.RED);
            }
            feedbackLabel.setVisible(true);
        } else {
            // Fallback for manual testing (assuming DatabaseHandler has a register method)
            System.out.println("Warning: Controller is null. Calling DatabaseHandler directly.");
            boolean success = DatabaseHandler.registerUser(username, password);
            if (success) {
                feedbackLabel.setText("User registered successfully.");
                feedbackLabel.setTextFill(Color.GREEN);
            } else {
                feedbackLabel.setText("Registration failed.");
                feedbackLabel.setTextFill(Color.RED);
            }
            feedbackLabel.setVisible(true);
        }
    }
}