package app.view;

import app.AppController;
import app.model.State;
import app.service.EmailService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class PartnerNotifyPage {

    private static final String PILL_STYLE = "-fx-background-color: #ffffff; "
            + "-fx-background-radius: 999; "
            + "-fx-border-radius: 999; "
            + "-fx-border-color: #CBD5E1; "
            + "-fx-border-width: 1; ";
    private static final double ACTION_FONT_SIZE = 20.0;
    private static final double BODY_FONT_SIZE = 18.0;
    @FXML private Button addContactButton, notifyButton;
    @FXML private VBox contactsContainer;
    @FXML private Label promptLabel;
    private final List<ContactEntry> contacts = new ArrayList<>();
    private Image bellImage, binImage;

    @FXML
    private void initialize() {
        // Load icons
        bellImage = loadImage("/images/Bell.png", 25, 25);
        binImage = loadImage("/images/deleteIcon.png", 60, 60);

        // Style prompt and add button
        promptLabel.setFont(new Font("System Bold", ACTION_FONT_SIZE));
        promptLabel.setPadding(new Insets(10, 16, 10, 16));
        promptLabel.setStyle(PILL_STYLE);

        addContactButton.setGraphic(new ImageView(loadImage("/images/+.png", 30, 30)));
        addContactButton.setStyle(PILL_STYLE);
        addContactButton.setPadding(new Insets(12));
        addContactButton.setFocusTraversable(false);
        addContactButton.setOnAction(e -> addContactRow());

        notifyButton.setFont(new Font("System Bold", ACTION_FONT_SIZE));
        notifyButton.setGraphic(new ImageView(bellImage));
        notifyButton.setOnAction(e -> buildConfirmationCard());
    }

    @FXML
    private void handleHome() {
        AppController.updateView(State.DASHBOARD);
    }

    private void addContactRow() {
        HBox contactRow = new HBox(12);
        contactRow.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Enter contact name (Optional).");
        HBox.setHgrow(nameField, Priority.ALWAYS);

        TextField emailField = new TextField();
        emailField.setPromptText("Enter email address.");
        HBox.setHgrow(emailField, Priority.ALWAYS);

        Button deleteButton = new Button();
        ImageView binView = new ImageView(binImage);
        binView.setFitHeight(30);
        binView.setPreserveRatio(true);
        deleteButton.setGraphic(binView);
        deleteButton.setOnAction(e -> {
            contactsContainer.getChildren().remove(contactRow);
            contacts.removeIf(c -> c.row() == contactRow);
        });

        contactRow.getChildren().addAll(nameField, emailField, deleteButton);
        contactsContainer.getChildren().add(contactRow);

        contacts.add(new ContactEntry(contactRow, nameField, emailField));
    }

    private void buildConfirmationCard() {
        if (contacts.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Contacts", "Add at least one contact before sending notifications.");
            return;
        }

        List<Recipient> recipients = collectRecipients();
        if (recipients.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Emails", "Please enter valid email addresses.");
            return;
        }

        VBox confirmCard = new VBox(14);
        confirmCard.getStyleClass().addAll("card", "card-padding");
        confirmCard.setMaxWidth(520);

        Label messageSentLabel = new Label("Ready to send notifications");
        messageSentLabel.setFont(new Font("System Bold", 22));

        VBox inner = new VBox(12);
        Label label1 = new Label("We'll take it from here.");
        label1.setFont(new Font(BODY_FONT_SIZE));
        label1.getStyleClass().add("muted-text");

        Label label2 = new Label("Press \"Notify\" to send now.");
        label2.setFont(new Font(BODY_FONT_SIZE));
        label2.getStyleClass().add("muted-text");

        Button confirmNotifyButton = new Button("Notify");
        confirmNotifyButton.setFont(new Font("System Bold", ACTION_FONT_SIZE));
        confirmNotifyButton.setMinWidth(120);
        confirmNotifyButton.setGraphic(new ImageView(bellImage));
        confirmNotifyButton.getStyleClass().add("primary-button");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(64, 64);
        progressIndicator.setVisible(false);

        confirmNotifyButton.setOnAction(e -> sendNotificationsAsync(recipients, progressIndicator, confirmNotifyButton));

        inner.getChildren().addAll(label1, label2, confirmNotifyButton, progressIndicator);
        confirmCard.getChildren().addAll(messageSentLabel, inner);

        // Replace center with confirmation card
        BorderPane root = (BorderPane) contactsContainer.getScene().getRoot();
        StackPane wrapper = new StackPane(confirmCard);
        wrapper.setPadding(new Insets(20));
        root.setCenter(wrapper);
    }

    private List<Recipient> collectRecipients() {
        List<Recipient> recipients = new ArrayList<>();
        for (ContactEntry contact : contacts) {
            String name = contact.nameField().getText().trim();
            String email = contact.emailField().getText().trim();
            if (!email.isEmpty()) recipients.add(new Recipient(name, email));
        }
        return recipients;
    }

    private boolean isSyntaxValid(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email != null && email.matches(regex);
    }

    private void sendNotificationsAsync(List<Recipient> recipients, ProgressIndicator progressIndicator, Button notifyButton) {
        AtomicInteger pendingCount = new AtomicInteger(recipients.size());
        AtomicInteger successCount = new AtomicInteger(0);

        progressIndicator.setVisible(true);
        notifyButton.setDisable(true);

        for (Recipient recipient : recipients) {
            if (!isSyntaxValid(recipient.email())) {
                showAlert(Alert.AlertType.ERROR, "Invalid Email", "Email '" + recipient.email() + "' is invalid.");
                progressIndicator.setVisible(false);
                notifyButton.setDisable(false);
                return;
            }

            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() {
                    return EmailService.sendNotification(recipient.name(), recipient.email(), "a SafeX user");
                }
            };

            task.setOnSucceeded(e -> {
                if (Boolean.TRUE.equals(task.getValue())) successCount.incrementAndGet();
                completeSendBatch(pendingCount, successCount, recipients.size(), progressIndicator, notifyButton);
            });

            task.setOnFailed(e -> completeSendBatch(pendingCount, successCount, recipients.size(), progressIndicator, notifyButton));

            Thread t = new Thread(task, "safex-email-send");
            t.setDaemon(true);
            t.start();
        }
    }

    private void completeSendBatch(AtomicInteger pendingCount, AtomicInteger successCount, int total, ProgressIndicator indicator, Button notifyButton) {
        if (pendingCount.decrementAndGet() > 0) return;

        indicator.setVisible(false);
        notifyButton.setDisable(false);

        if (successCount.get() == total) {
            showAlert(Alert.AlertType.INFORMATION, "Notifications Complete", "All notifications sent.");
            goToDashboard();
        } else {
            showAlert(Alert.AlertType.WARNING, "Notifications Complete", successCount.get() + " of " + total + " sent.");
        }
    }

    private void goToDashboard() {
        AppController.updateView(State.DASHBOARD);
    }

    private Image loadImage(String path, double width, double height) {
        return new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)), width, height, true, true);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private record ContactEntry(HBox row, TextField nameField, TextField emailField) {}
    private record Recipient(String name, String email) {}
}
