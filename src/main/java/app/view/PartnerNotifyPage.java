package app.view;

import app.AppController;
import app.service.EmailService;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class PartnerNotifyPage implements Page {
    private static final String PILL_STYLE = "-fx-background-color: #ffffff; "
            + "-fx-background-radius: 999; "
            + "-fx-border-radius: 999; "
            + "-fx-border-color: #CBD5E1; "
            + "-fx-border-width: 1; ";
    private static final double ACTION_FONT_SIZE = 20.0;
    private static final double BODY_FONT_SIZE = 18.0;
    private static final double ROW_MIN_HEIGHT = 100.0;

    private final AppController controller;

    public PartnerNotifyPage(AppController controller) {
        this.controller = controller;
    }

    public PartnerNotifyPage() {
        this(null);
    }

    @Override
    public Scene createScene() {
        BorderPane pageRoot = new BorderPane();

        Image bellImage = loadImage("/images/Bell.png", 25, 25);
        Image plusImage = loadImage("/images/+.png", 30, 30);
        Image binImage = loadImage("/images/deleteIcon.png", 60, 60);
        Image homeImage = loadImage("/images/Home.png", 25, 25);
        List<ContactEntry> contacts = new ArrayList<>();

        Button homeButton = buildHomeButton(homeImage);

        Label title = new Label("Notify Partners");
        title.getStyleClass().add("app-header-title");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        HBox header = new HBox(10, homeButton, title, headerSpacer);
        header.getStyleClass().add("app-header");
        header.setAlignment(Pos.CENTER_LEFT);
        pageRoot.setTop(header);

        VBox content = new VBox(18);
        content.setPadding(new Insets(20));
        content.setFillWidth(true);

        HBox secondRow = new HBox(12);
        secondRow.setAlignment(Pos.TOP_LEFT);

        Label prompt = new Label("Send anonymous email notifications to chosen partners.");
        prompt.setStyle(PILL_STYLE);
        prompt.setFont(new Font("System Bold", ACTION_FONT_SIZE));
        prompt.setPadding(new Insets(10, 16, 10, 16));
        prompt.setMaxWidth(Double.MAX_VALUE);
        prompt.setWrapText(true);
        HBox.setHgrow(prompt, Priority.ALWAYS);

        Button plusButton = new Button();
        plusButton.setGraphic(new ImageView(plusImage));
        plusButton.setAlignment(Pos.CENTER);
        plusButton.setStyle(PILL_STYLE);
        plusButton.setPadding(new Insets(12));
        plusButton.setFocusTraversable(false);

        secondRow.getChildren().addAll(prompt, plusButton);

        ScrollPane contactsList = new ScrollPane();
        contactsList.setMinViewportHeight(ROW_MIN_HEIGHT);
        contactsList.setFitToWidth(true);
        contactsList.setPannable(true);
        contactsList.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(contactsList, Priority.ALWAYS);

        VBox contactsContainer = new VBox(12);
        contactsContainer.setPadding(new Insets(12));
        contactsList.setContent(contactsContainer);

        plusButton.setOnAction(event -> addContactRow(contactsContainer, contacts, binImage));

        HBox lastRow = new HBox(16);
        lastRow.getStyleClass().add("card");
        lastRow.setAlignment(Pos.CENTER_LEFT);
        lastRow.setPadding(new Insets(18));

        Label disclaimerLabel = new Label(
                "Note: the following message will be sent anonymously to all contacts in the list above.");
        disclaimerLabel.getStyleClass().add("muted-text");
        disclaimerLabel.setWrapText(true);
        disclaimerLabel.setFont(new Font(BODY_FONT_SIZE));
        HBox.setHgrow(disclaimerLabel, Priority.ALWAYS);
        disclaimerLabel.setMaxWidth(Double.MAX_VALUE);

        Button notifyButton = new Button("Notify");
        notifyButton.getStyleClass().add("primary-button");
        notifyButton.setFont(new Font("System Bold", ACTION_FONT_SIZE));
        notifyButton.setGraphic(new ImageView(bellImage));
        notifyButton.setMinWidth(140);

        notifyButton.setOnAction(event -> {
            VBox confirmCard = buildConfirmationRoot(contacts, bellImage);
            StackPane wrapper = new StackPane(confirmCard);
            wrapper.setPadding(new Insets(20));
            pageRoot.setCenter(wrapper);
        });

        lastRow.getChildren().addAll(disclaimerLabel, notifyButton);
        content.getChildren().addAll(secondRow, contactsList, lastRow);
        pageRoot.setCenter(content);

        return new Scene(pageRoot, AppStyles.SCENE_WIDTH, AppStyles.SCENE_HEIGHT);
    }

    private VBox buildConfirmationRoot(List<ContactEntry> contacts, Image bellImage) {
        VBox outer = new VBox(14);
        outer.getStyleClass().addAll("card", "card-padding");
        outer.setMaxWidth(520);
        outer.setAlignment(Pos.TOP_LEFT);

        Label messageSentLabel = new Label("Ready to send notifications");
        messageSentLabel.setFont(new Font("System Bold", 22));

        VBox inner = new VBox(12);
        VBox.setVgrow(inner, Priority.ALWAYS);

        Label label1 = new Label("We'll take it from here.");
        label1.setFont(new Font(BODY_FONT_SIZE));
        label1.getStyleClass().add("muted-text");

        Label label2 = new Label("Press \"Notify\" to send now.");
        label2.setFont(new Font(BODY_FONT_SIZE));
        label2.getStyleClass().add("muted-text");

        HBox sendRow = new HBox();
        sendRow.setAlignment(Pos.CENTER);
        sendRow.setPrefHeight(100.0);

        Button confirmNotifyButton = new Button("Notify");
        confirmNotifyButton.setFont(new Font("System Bold", ACTION_FONT_SIZE));
        confirmNotifyButton.setMinWidth(120);
        confirmNotifyButton.setGraphic(new ImageView(bellImage));
        confirmNotifyButton.setAlignment(Pos.CENTER);
        confirmNotifyButton.getStyleClass().add("primary-button");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(64, 64);
        progressIndicator.setVisible(false);

        confirmNotifyButton
                .setOnAction(event -> sendNotificationsAsync(contacts, progressIndicator, confirmNotifyButton));

        sendRow.getChildren().add(confirmNotifyButton);

        inner.getChildren().addAll(label1, label2, sendRow, progressIndicator);
        outer.getChildren().addAll(messageSentLabel, inner);
        return outer;
    }

    private boolean isSyntaxValid(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email != null && email.matches(regex);
    }

    private void sendNotificationsAsync(
            List<ContactEntry> contacts,
            ProgressIndicator progressIndicator,
            Button confirmNotifyButton) {

        List<Recipient> recipients = collectRecipients(contacts);
        if (recipients.isEmpty()) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "No Contacts",
                    "Add at least one contact with an email address before sending notifications.");
            return;
        }
        for (Recipient recipient : recipients) {
            if (!isSyntaxValid(recipient.email())) {
                showAlert(
                        Alert.AlertType.ERROR,
                        "Invalid Email Address",
                        "The email '" + recipient.email() + "' is not formatted correctly.");
                return;
            }
        }

        progressIndicator.setVisible(true);
        confirmNotifyButton.setDisable(true);

        AtomicInteger pendingCount = new AtomicInteger(recipients.size());
        AtomicInteger successCount = new AtomicInteger(0);

        for (Recipient recipient : recipients) {
            Task<Boolean> sendTask = new Task<>() {
                @Override
                protected Boolean call() {
                    return EmailService.sendNotification(recipient.name(), recipient.email(), "a SafeX user");
                }
            };

            sendTask.setOnSucceeded(event -> {
                boolean sent = Boolean.TRUE.equals(sendTask.getValue());
                if (sent) {
                    successCount.incrementAndGet();
                    showAlert(
                            Alert.AlertType.INFORMATION,
                            "Notification Sent",
                            "Notification sent to " + recipient.email() + ".");
                } else {
                    showAlert(
                            Alert.AlertType.ERROR,
                            "Send Failed",
                            "SafeX could not send a notification to " + recipient.email() + ".");
                }
                completeSendBatch(
                        pendingCount,
                        successCount,
                        recipients.size(),
                        progressIndicator,
                        confirmNotifyButton);
            });

            sendTask.setOnFailed(event -> {
                showAlert(
                        Alert.AlertType.ERROR,
                        "Send Failed",
                        "SafeX could not send a notification to " + recipient.email() + ".");
                completeSendBatch(
                        pendingCount,
                        successCount,
                        recipients.size(),
                        progressIndicator,
                        confirmNotifyButton);
            });

            Thread emailThread = new Thread(sendTask, "safex-email-send");
            emailThread.setDaemon(true);
            emailThread.start();
        }
    }

    private void completeSendBatch(
            AtomicInteger pendingCount,
            AtomicInteger successCount,
            int totalRecipients,
            ProgressIndicator progressIndicator,
            Button confirmNotifyButton) {

        if (pendingCount.decrementAndGet() > 0) {
            return;
        }

        progressIndicator.setVisible(false);
        confirmNotifyButton.setDisable(false);

        if (successCount.get() == totalRecipients) {
            showAlert(Alert.AlertType.INFORMATION, "Notifications Complete", "All notifications were sent.");
            goToDashboard();
        } else {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Notifications Complete",
                    successCount.get() + " of " + totalRecipients + " notifications were sent.");
        }
    }

    private List<Recipient> collectRecipients(List<ContactEntry> contacts) {
        List<Recipient> recipients = new ArrayList<>();
        for (ContactEntry contact : contacts) {
            String email = contact.emailField().getText() == null ? "" : contact.emailField().getText().trim();

            if (email.isBlank()) {
                continue;
            }

            String name = contact.nameField().getText() == null ? "" : contact.nameField().getText().trim();
            recipients.add(new Recipient(name, email));
        }
        return recipients;
    }

    private void addContactRow(VBox container, List<ContactEntry> contacts, Image binImage) {
        HBox contactRow = new HBox(12);
        contactRow.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Enter contact name (Optional).");
        HBox.setHgrow(nameField, Priority.ALWAYS);
        nameField.setMaxHeight(Double.MAX_VALUE);

        TextField emailField = new TextField();
        emailField.setPromptText("Enter email address.");
        HBox.setHgrow(emailField, Priority.ALWAYS);
        emailField.setMaxHeight(Double.MAX_VALUE);

        Button deleteButton = new Button();
        ImageView binView = new ImageView(binImage);
        binView.setFitHeight(30);
        binView.setPreserveRatio(true);
        deleteButton.setGraphic(binView);
        deleteButton.setOnAction(event -> {
            container.getChildren().remove(contactRow);
            contacts.removeIf(contact -> contact.row().equals(contactRow));
        });

        contactRow.getChildren().addAll(nameField, emailField, deleteButton);
        container.getChildren().add(contactRow);
        contacts.add(new ContactEntry(contactRow, nameField, emailField));
    }

    private Image loadImage(String path, double width, double height) {
        return new Image(
                Objects.requireNonNull(getClass().getResourceAsStream(path)),
                width,
                height,
                true,
                true);
    }

    private Button buildHomeButton(Image homeImage) {
        ImageView icon = new ImageView(homeImage);
        icon.setFitWidth(28);
        icon.setFitHeight(28);
        icon.setPreserveRatio(true);

        Button homeButton = new Button();
        homeButton.setGraphic(icon);
        homeButton.getStyleClass().add("icon-button");
        homeButton.setFocusTraversable(false);
        homeButton.setOnAction(event -> goToDashboard());
        return homeButton;
    }

    private void goToDashboard() {
        if (controller == null) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Navigation Unavailable",
                    "Unable to return to the dashboard because the controller is not available.");
            return;
        }
        controller.goToDashboard();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void onEnter() {
        Page.super.onEnter();
    }

    private record ContactEntry(HBox row, TextField nameField, TextField emailField) {
    }

    private record Recipient(String name, String email) {
    }
}
