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

    public enum LoginType {
        STANDARD,
        CLINIC
    }

    private AppController controller;
    private LoginType loginType;

    public LoginPage(AppController controller, LoginType loginType) {
        this.controller = controller;
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

        String greetingText = (loginType == LoginType.CLINIC) ? "Clinic Portal" : "Welcome back";
        Label titleLabel = new Label(greetingText);
        titleLabel.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 30));

        String subtitleText = (loginType == LoginType.CLINIC) ? "Sign in to manage your clinic."
                : "Sign in to continue.";
        Label subtitleLabel = new Label(subtitleText);
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

        String registerText = (loginType == LoginType.CLINIC) ? "Register a new clinic" : "Create an account";
        Button registerLink = new Button(registerText);
        registerLink.getStyleClass().add("link-button");

        // NEW: The toggle button to switch between portal types
        String switchPortalText = (loginType == LoginType.STANDARD) ? "Are you a clinic? Clinic Portal"
                : "Are you a patient? Patient Portal";
        Button switchPortalLink = new Button(switchPortalText);
        switchPortalLink.getStyleClass().add("link-button");

        loginBtn.setOnAction(e -> handleLogin(usernameField, passwordField, errorLabel));
        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleLogin(usernameField, passwordField, errorLabel);
            }
        });

        registerLink.setOnAction(e -> {
            if (controller != null) {
                controller.goToRegister(loginType);
            }
        });

        // NEW: Action event to tell the controller to swap the login page
        switchPortalLink.setOnAction(e -> {
            if (controller != null) {
                LoginType oppositeType = (loginType == LoginType.STANDARD) ? LoginType.CLINIC : LoginType.STANDARD;
                controller.goToLogin(oppositeType);
            }
        });

        card.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                userBox,
                passBox,
                errorLabel,
                loginBtn,
                registerLink,
                switchPortalLink // Added to the layout
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
            boolean success = controller.attemptLogin(username, password, loginType);
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
            boolean isValid = DatabaseHandler.checkLogin(username, password, loginType.name());
            if (isValid) {
                System.out.println("[TEST] Login Valid for " + loginType.name() + " (No screen switch)");
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