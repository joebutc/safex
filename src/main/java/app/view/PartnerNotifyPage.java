package app.view;

import app.service.EmailService;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class PartnerNotifyPage implements Page {
    private static final String PILL_STYLE = "-fx-background-radius: 50; "
            + "-fx-border-radius: 50; "
            + "-fx-border-color: #cccccc; ";
    private static final double TITLE_FONT_SIZE = 36.0;
    private static final double ACTION_FONT_SIZE = 20.0;
    private static final double BODY_FONT_SIZE = 18.0;
    private static final double ROW_MIN_HEIGHT = 100.0;

    @Override
    public Scene createScene() {
        VBox root = new VBox();
        root.setSpacing(20);
        root.setPadding(new Insets(20));

        Image bellImage = loadImage("/images/Bell.png", 25, 25);
        Image plusImage = loadImage("/images/+.png", 30, 30);
        Image binImage = loadImage("/images/deleteIcon.png", 60, 60);
        Image homeImage = loadImage("/images/Home.png", 25, 25);
        List<ContactEntry> contacts = new ArrayList<>();

        HBox firstRow = new HBox();
        firstRow.setAlignment(Pos.CENTER);

        Button backButton = new Button("Back");
        backButton.setAlignment(Pos.CENTER);
        backButton.setStyle(PILL_STYLE);
        backButton.setFont(new Font(ACTION_FONT_SIZE));
        backButton.setPrefWidth(150);
        backButton.setPrefHeight(10);
        backButton.setOnAction(event -> navigateToDashboard(backButton));

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        Label title = new Label("Notify Partners");
        title.setAlignment(Pos.CENTER_RIGHT);
        title.setFont(new Font("System Bold", TITLE_FONT_SIZE));

        firstRow.getChildren().addAll(backButton, spacer1, title);

        HBox secondRow = new HBox();
        secondRow.setAlignment(Pos.TOP_LEFT);
        secondRow.setMinHeight(ROW_MIN_HEIGHT);

        Label prompt = new Label("Sends an anonymous Email to chosen partners.");
        prompt.setStyle(PILL_STYLE);
        prompt.setFont(new Font("System Bold", ACTION_FONT_SIZE));
        prompt.setPadding(new Insets(10, 0, 10, 10));
        prompt.setMaxWidth(Double.MAX_VALUE);
        prompt.setWrapText(true);

        Region spacer2 = new Region();
        spacer2.setPrefWidth(100);
        HBox.setHgrow(prompt, Priority.ALWAYS);

        Button plusButton = new Button();
        plusButton.setGraphic(new ImageView(plusImage));
        plusButton.setAlignment(Pos.CENTER);
        plusButton.setStyle(PILL_STYLE);
        plusButton.setPadding(new Insets(15));

        secondRow.getChildren().addAll(prompt, spacer2, plusButton);

        ScrollPane contactsList = new ScrollPane();
        contactsList.setMinViewportHeight(ROW_MIN_HEIGHT);
        VBox.setVgrow(contactsList, Priority.ALWAYS);

        VBox container = new VBox();
        contactsList.setContent(container);
        contactsList.setFitToWidth(true);

        plusButton.setOnAction(event -> addContactRow(container, contacts, binImage));

        HBox lastRow = new HBox();
        lastRow.setStyle("-fx-border-color: #000000;");
        lastRow.setAlignment(Pos.CENTER);
        lastRow.setPadding(new Insets(20));
        lastRow.setMinHeight(ROW_MIN_HEIGHT);

        Label disclaimerLabel = new Label("Note: The following message will be sent " +
                "anonymously to all contacts in the list above.");
        disclaimerLabel.setWrapText(true);
        disclaimerLabel.setPadding(new Insets(0, 0, 0, 10));
        disclaimerLabel.setFont(new Font(BODY_FONT_SIZE));

        HBox.setHgrow(disclaimerLabel, Priority.ALWAYS);
        disclaimerLabel.setMaxWidth(Double.MAX_VALUE);

        Button notifyButton = new Button("Notify");
        notifyButton.setFont(new Font("System Bold", ACTION_FONT_SIZE));
        notifyButton.setMinWidth(120);
        notifyButton.setGraphic(new ImageView(bellImage));
        notifyButton.setAlignment(Pos.CENTER);
        notifyButton.setStyle(PILL_STYLE);

        notifyButton.setOnAction(event -> {
            VBox confirmRoot = buildConfirmationRoot(contacts, bellImage, homeImage);
            notifyButton.getScene().setRoot(confirmRoot);
        });

        lastRow.getChildren().addAll(disclaimerLabel, notifyButton);
        root.getChildren().addAll(firstRow, secondRow, contactsList, lastRow);

        return new Scene(root);
    }

    private VBox buildConfirmationRoot(List<ContactEntry> contacts, Image bellImage, Image homeImage) {
        VBox outer = new VBox(12);
        outer.setPadding(new Insets(0, 20, 0, 20));

        Label confPageTitle = new Label("Notify Partners");
        confPageTitle.setAlignment(Pos.CENTER_RIGHT);
        confPageTitle.setMaxWidth(Double.MAX_VALUE);
        confPageTitle.setFont(new Font("System Bold", TITLE_FONT_SIZE));

        Label messageSentLabel = new Label("Ready to send notifications.");
        messageSentLabel.setAlignment(Pos.CENTER_LEFT);
        messageSentLabel.setMaxWidth(Double.MAX_VALUE);
        messageSentLabel.setPrefHeight(50);
        messageSentLabel.setPadding(new Insets(0, 0, 0, 10));
        messageSentLabel.setFont(new Font("System Bold", ACTION_FONT_SIZE));
        messageSentLabel.setStyle(PILL_STYLE + "-fx-background-color: #FFFFFF");

        VBox inner = new VBox(12);
        VBox.setVgrow(inner, Priority.ALWAYS);

        Label label1 = new Label("We'll take it from here.");
        label1.setFont(new Font("System Bold", BODY_FONT_SIZE));
        label1.setPrefHeight(50);

        Label label2 = new Label("Press \"Notify\" to send now.");
        label2.setFont(new Font("System Bold", BODY_FONT_SIZE));
        label2.setPrefHeight(50);

        Label label3 = new Label("Or return to the dashboard.");
        label3.setFont(new Font("System Bold", BODY_FONT_SIZE));
        label3.setPrefHeight(50);

        HBox sendRow = new HBox();
        sendRow.setAlignment(Pos.CENTER);
        sendRow.setPrefHeight(100.0);

        HBox homeRow = new HBox();
        homeRow.setAlignment(Pos.CENTER);
        homeRow.setPrefHeight(100.0);

        Button confirmNotifyButton = new Button("Notify");
        confirmNotifyButton.setFont(new Font("System Bold", ACTION_FONT_SIZE));
        confirmNotifyButton.setMinWidth(120);
        confirmNotifyButton.setGraphic(new ImageView(bellImage));
        confirmNotifyButton.setAlignment(Pos.CENTER);
        confirmNotifyButton.setStyle(PILL_STYLE);

        Button homeButton = new Button("Home");
        homeButton.setFont(new Font("System Bold", ACTION_FONT_SIZE));
        homeButton.setMinWidth(120);
        homeButton.setGraphic(new ImageView(homeImage));
        homeButton.setAlignment(Pos.CENTER);
        homeButton.setStyle(PILL_STYLE);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(64, 64);
        progressIndicator.setVisible(false);

        confirmNotifyButton.setOnAction(event ->
                sendNotificationsAsync(contacts, progressIndicator, confirmNotifyButton, homeButton));
        homeButton.setOnAction(event -> navigateToDashboard(homeButton));

        sendRow.getChildren().add(confirmNotifyButton);
        homeRow.getChildren().add(homeButton);

        inner.getChildren().addAll(label1, label2, sendRow, label3, homeRow, progressIndicator);
        outer.getChildren().addAll(confPageTitle, messageSentLabel, inner);
        return outer;
    }

    private void sendNotificationsAsync(
            List<ContactEntry> contacts,
            ProgressIndicator progressIndicator,
            Button confirmNotifyButton,
            Button homeButton) {

        List<Recipient> recipients = collectRecipients(contacts);
        if (recipients.isEmpty()) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "No Contacts",
                    "Add at least one contact with an email address before sending notifications."
            );
            return;
        }

        progressIndicator.setVisible(true);
        confirmNotifyButton.setDisable(true);
        homeButton.setDisable(true);

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
                            "Notification sent to " + recipient.email() + "."
                    );
                } else {
                    showAlert(
                            Alert.AlertType.ERROR,
                            "Send Failed",
                            "SafeX could not send a notification to " + recipient.email() + "."
                    );
                }
                completeSendBatch(
                        pendingCount,
                        successCount,
                        recipients.size(),
                        progressIndicator,
                        confirmNotifyButton,
                        homeButton
                );
            });

            sendTask.setOnFailed(event -> {
                showAlert(
                        Alert.AlertType.ERROR,
                        "Send Failed",
                        "SafeX could not send a notification to " + recipient.email() + "."
                );
                completeSendBatch(
                        pendingCount,
                        successCount,
                        recipients.size(),
                        progressIndicator,
                        confirmNotifyButton,
                        homeButton
                );
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
            Button confirmNotifyButton,
            Button homeButton) {

        if (pendingCount.decrementAndGet() > 0) {
            return;
        }

        progressIndicator.setVisible(false);
        confirmNotifyButton.setDisable(false);
        homeButton.setDisable(false);

        if (successCount.get() == totalRecipients) {
            showAlert(Alert.AlertType.INFORMATION, "Notifications Complete", "All notifications were sent.");
            navigateToDashboard(homeButton);
        } else {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Notifications Complete",
                    successCount.get() + " of " + totalRecipients + " notifications were sent."
            );
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
                true
        );
    }

    private void navigateToDashboard(Node sourceNode) {
        if (sourceNode.getScene() == null) {
            return;
        }
        Scene dashboardScene = new DashboardPage().createScene();
        sourceNode.getScene().setRoot(dashboardScene.getRoot());
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
