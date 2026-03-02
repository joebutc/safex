package app.view;

import app.AppController;
import app.model.State;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Objects;

public class DashboardPage {
    @FXML private Label welcomeLabel;
    @FXML private FlowPane cardContainer;
    @FXML private Button bellButton;

    @FXML
    private void initialize() {
        welcomeLabel.setText(buildWelcomeText());
        buildCards();
        buildBellIcon();
    }

    private String buildWelcomeText() {
        String user = AppController.getCurrentUser();
        if (user == null || user.isBlank()) {
            return "Welcome back";
        }
        return "Welcome back, " + user;
    }

    private void buildBellIcon() {
        Image bellImage = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/images/Bell.png")),
                22, 22, true, true);

        ImageView bellView = new ImageView(bellImage);

        List<String> notifications = AppController.getPendingNotifications();

        int count = notifications.size();

        StackPane bellWithBadge;

        if (count > 0) {
            Label badgeLabel = new Label(count > 9 ? "9+" : String.valueOf(count));
            badgeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 8px; -fx-font-weight: bold;");

            StackPane badge = new StackPane(badgeLabel);
            badge.setPrefSize(15, 15);
            badge.setStyle("-fx-background-color: #DC2626; -fx-background-radius: 999;");
            badge.setTranslateX(9);
            badge.setTranslateY(-9);

            bellWithBadge = new StackPane(bellView, badge);
        } else {
            bellWithBadge = new StackPane(bellView);
        }

        bellButton.setGraphic(bellWithBadge);
    }

    @FXML
    private void handleLogout() {
        AppController.updateView(State.LOGIN);
    }

    @FXML
    private void handleNotifications() {
        List<String> notifications = AppController.getPendingNotifications();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notifications");

        if (notifications.isEmpty()) {
            alert.setHeaderText("No new notifications");
            alert.setContentText("You're all up to date.");
        } else {
            alert.setHeaderText(notifications.size() + " notification(s)");
            alert.setContentText(String.join("\n\n", notifications));
        }

        alert.showAndWait();
    }

    private void buildCards() {
        cardContainer.getChildren().addAll(
                buildCard("Notify Partners", "Send anonymous notifications.", "Start", () -> AppController.updateView(State.NOTIFY)),
                buildCard("Book an STI Test", "Check eligibility for testing.", "Check", () -> AppController.updateView(State.STI_BOOKING)),
                buildCard("Track Your Tests", "Log and review STI tests.", "View", () -> AppController.updateView(State.STI_TRACKER)),
                buildCard("My Treatment Plan", "Track medications and supply.", "Open", () -> AppController.updateView(State.TREATMENTS)),
                buildCard("Privacy & Settings", "Manage your privacy settings.", "Open", () -> AppController.updateView(State.SETTNGS)),
                buildCard("Terms & Conditions", "Review privacy policy.", "View", () -> AppController.updateView(State.LEGAL))
        );
    }

    private VBox buildCard(String titleText,
                           String descriptionText,
                           String buttonText,
                           Runnable action) {

        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setPrefWidth(400);
        card.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 18;
                -fx-border-radius: 18;
                -fx-border-color: #E5E7EB;
                """);

        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label description = new Label(descriptionText);
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: #475569;");

        Button button = new Button(buttonText);
        button.getStyleClass().add("primary-button");
        button.setOnAction(e -> action.run());

        card.getChildren().addAll(title, description, button);
        return card;
    }
}
