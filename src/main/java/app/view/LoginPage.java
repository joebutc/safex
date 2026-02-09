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

public class LoginPage implements Page {

    private AppController controller;

    public LoginPage(AppController controller) {
        this.controller = controller;
    }

    @Override
    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(24));

        VBox card = new VBox(14);
        card.getStyleClass().addAll("card", "card-padding");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(440);

        Label titleLabel = new Label("Welcome back");
        titleLabel.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 30));

        Label subtitleLabel = new Label("Sign in to continue.");
        subtitleLabel.getStyleClass().add("muted-text");
        subtitleLabel.setWrapText(true);

        Label userLabel = new Label("Username");
        userLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setMaxWidth(Double.MAX_VALUE);

        VBox userBox = new VBox(4, userLabel, usernameField);
        userBox.setMaxWidth(Double.MAX_VALUE);

        Label passLabel = new Label("Password");
        passLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setMaxWidth(Double.MAX_VALUE);

        VBox passBox = new VBox(4, passLabel, passwordField);
        passBox.setMaxWidth(Double.MAX_VALUE);

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-text");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        Button loginBtn = new Button("Log in");
        loginBtn.getStyleClass().add("primary-button");
        loginBtn.setDefaultButton(true);
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setMinHeight(42);

        Button registerLink = new Button("Create an account");
        registerLink.getStyleClass().add("link-button");

        loginBtn.setOnAction(e -> handleLogin(usernameField, passwordField, errorLabel));
        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleLogin(usernameField, passwordField, errorLabel);
            }
        });

        registerLink.setOnAction(e -> {
            if (controller != null) {
                controller.goToRegister();
            }
        });

        card.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                userBox,
                passBox,
                errorLabel,
                loginBtn,
                registerLink
        );

        StackPane center = new StackPane(card);
        center.setPadding(new Insets(24));
        root.setCenter(center);

        return new Scene(root, AppStyles.SCENE_WIDTH, AppStyles.SCENE_HEIGHT);
    }

    private void handleLogin(TextField userField, PasswordField passField, Label errorLabel) {
        String username = userField.getText();
        String password = passField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            return;
        }

        if (controller != null) {
            boolean success = controller.attemptLogin(username, password);
            if (!success) {
                errorLabel.setText("Invalid username or password.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            } else {
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
            }
        } else {
            System.out.println("Warning: Controller is null. Running local check.");
            boolean isValid = DatabaseHandler.checkLogin(username, password);
            if (isValid) {
                System.out.println("[TEST] Login Valid (No screen switch)");
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
            } else {
                errorLabel.setText("Invalid username or password.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            }
        }
    }
}
