package app.view;

import app.AppController;
import app.model.State;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class DashboardPage {
    @FXML private Label welcomeLabel;
    @FXML private FlowPane cardContainer;
    @FXML private Button bellButton;
    @FXML private Button settingButton;
    @FXML private StackPane dashboardRoot;

    private VBox notificationContainer;
    private static boolean hasShownLoginReminder = false;

    @FXML
    private void initialize() {
        welcomeLabel.setText(buildWelcomeText());
        buildBellIcon();

        if (!hasShownLoginReminder) {
            addBannerToScreen("Daily Reminder", "Don't forget to take your medication today.");
            String stiReminder = AppController.getReminderMessage();
            if (stiReminder != null && stiReminder.startsWith("You haven't logged any STI tests yet")) {
                addBannerToScreen("Test Reminder", stiReminder);
            }
            hasShownLoginReminder = true;
        }

        processSystemAlerts();
    }

    @FXML
    private void handleSettingsMenu() {
        AppController.updateView(State.SETTINGS);
    }

    private void processSystemAlerts() {
        for (String alertMessage : AppController.getPendingNotifications()) {
            if (alertMessage.startsWith("You haven't logged any STI tests yet")) {
                continue;
            }
            addBannerToScreen("System Alert", alertMessage);
        }
    }

    private String buildWelcomeText() {
        String user = AppController.getCurrentUser();
        return (user == null || user.isBlank()) ? "Welcome back" : "Welcome back, " + user;
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
            badgeLabel.getStyleClass().add("notification-badge-label");
            StackPane badge = new StackPane(badgeLabel);
            badge.setPrefSize(15, 15);
            badge.getStyleClass().add("notification-badge");
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
        hasShownLoginReminder = false;
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

    @FXML private void handleNotifyPartners() { AppController.updateView(State.NOTIFY); }
    @FXML private void handleStiBooking() { AppController.updateView(State.STI_BOOKING); }
    @FXML private void handleBookAppointment() { AppController.updateView(State.BOOK_APPOINTMENT); }
    @FXML private void handleStiTracker() { AppController.updateView(State.STI_TRACKER); }
    @FXML private void handleTreatmentTracker() { AppController.updateView(State.TREATMENT_TRACKER); }

    private void addBannerToScreen(String title, String message) {
        if (notificationContainer == null) {
            notificationContainer = new VBox(10);
            notificationContainer.setAlignment(Pos.TOP_CENTER);
            notificationContainer.setPickOnBounds(false);
            notificationContainer.setPadding(new Insets(20, 0, 0, 0));
            notificationContainer.getStyleClass().add("notification-stack");
            dashboardRoot.getChildren().add(notificationContainer);
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/reminderBanner.fxml"));
            Node banner = loader.load();
            app.NotificationBannerController controller = loader.getController();
            controller.setTitle(title);
            controller.setMessage(message);
            controller.setOnCloseCallback(() -> {
                TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), banner);
                slideOut.setByY(-50);
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), banner);
                fadeOut.setToValue(0);
                ParallelTransition popOut = new ParallelTransition(slideOut, fadeOut);
                popOut.setOnFinished(_ -> notificationContainer.getChildren().remove(banner));
                popOut.play();
            });
            banner.setTranslateY(-50);
            banner.setOpacity(0);
            notificationContainer.getChildren().add(banner);
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), banner);
            slideIn.setToY(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), banner);
            fadeIn.setToValue(1);
            new ParallelTransition(slideIn, fadeIn).play();
        } catch (IOException e) {
            System.err.println("Failed to load notification banner FXML.");
            e.printStackTrace();
        }
    }
}
