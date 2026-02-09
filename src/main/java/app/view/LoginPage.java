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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LoginPage implements Page {

    private AppController controller;

    public LoginPage(AppController controller) {
        this.controller = controller;
    }

    @Override
    public Scene createScene() {

        // 1. Setup the Root Layout (VBox centers everything vertically)
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setSpacing(20);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #ffffff;"); // Clean white background

        // 2. Styling Constants
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
        Label titleLabel = new Label("Welcome Back");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 36));

        Label subtitleLabel = new Label("Please enter your details.");
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 18));
        subtitleLabel.setTextFill(Color.GRAY);

        // 4. Input Fields
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle(INPUT_STYLE);
        usernameField.setMaxWidth(350);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle(INPUT_STYLE);
        passwordField.setMaxWidth(350);

        // 5. Error Message Label (Hidden initially)
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);

        // 6. Login Button
        Button loginBtn = new Button("Login");
        loginBtn.setStyle(BTN_PRIMARY_STYLE);
        loginBtn.setMinWidth(200);
        loginBtn.setMinHeight(45);

        // 7. Navigation (Switch to Register)
        Button registerLink = new Button("Don't have an account? Sign Up");
        registerLink.setStyle(LINK_STYLE);

        // --- LOGIC SECTION ---

        // Action: Click Login Button
        loginBtn.setOnAction(e -> handleLogin(usernameField, passwordField, errorLabel));

        // Action: Press 'Enter' key inside password box
        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleLogin(usernameField, passwordField, errorLabel);
            }
        });

        // Go to register page
        registerLink.setOnAction(e -> {
            if (controller != null) {
                controller.goToRegister();
            } else {
                System.out.println("Debug: Controller is null, cannot navigate.");
            }
        });

        // 8. Add components to the VBox
        root.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                new Label(""), // Spacer
                usernameField,
                passwordField,
                errorLabel,
                loginBtn,
                registerLink);

        return new Scene(root, 800, 600);
    }

    // Handles login
    private void handleLogin(TextField userField, PasswordField passField, Label errorLabel) {
        String username = userField.getText();
        String password = passField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            errorLabel.setVisible(true);
            return;
        }

        if (controller != null) {
            boolean success = controller.attemptLogin(username, password);

            if (!success) {
                // login failed :/
                errorLabel.setText("Invalid username or password.");
                errorLabel.setVisible(true);
            }
        } else {
            // Testing stuff
            System.out.println("Warning: Controller is null. Running local check.");
            boolean isValid = DatabaseHandler.checkLogin(username, password);

            if (isValid) {
                System.out.println("[TEST] Login Valid (No screen switch)");
                errorLabel.setVisible(false);
            } else {
                errorLabel.setText("Invalid username or password.");
                errorLabel.setVisible(true);
            }
        }
    }

}