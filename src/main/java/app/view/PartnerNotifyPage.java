package app.view;

import app.AppController;
import app.service.EmailService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PartnerNotifyPage {

    @FXML private Button addContactButton;
    @FXML private Button notifyButton;
    @FXML private VBox contactsContainer;
    @FXML private Label promptLabel;

    private final List<ContactEntry> contacts = new ArrayList<>();

    @FXML
    private void initialize() {
    }

    @FXML
    private void handleAddContact() {
        addContactRow();
    }

    @FXML
    private void handleNotify() {
        buildConfirmationCard();
    }

    @FXML
    private void handleHome() {
        AppController.goToHomeDashboard();
    }

    private void addContactRow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/PartnerContactRow.fxml"));
            Parent row = loader.load();
            PartnerContactRowController controller = loader.getController();

            controller.setOnDelete(() -> {
                contactsContainer.getChildren().remove(row);
                contacts.removeIf(c -> c.row() == row);
            });

            contactsContainer.getChildren().add(row);
            contacts.add(new ContactEntry(row, controller));
        } catch (IOException e) {
            throw new RuntimeException("Could not load partner contact row", e);
        }
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

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/PartnerNotifyConfirm.fxml"));
            Parent confirmCard = loader.load();
            PartnerNotifyConfirmController controller = loader.getController();
            controller.configure(() -> sendNotificationsAsync(
                    recipients,
                    controller.getProgressIndicator(),
                    controller.getNotifyButton()
            ));

            BorderPane root = (BorderPane) contactsContainer.getScene().getRoot();
            StackPane wrapper = new StackPane(confirmCard);
            wrapper.getStyleClass().add("card-padding");
            root.setCenter(wrapper);
        } catch (IOException e) {
            throw new RuntimeException("Could not load partner confirmation card", e);
        }
    }

    private List<Recipient> collectRecipients() {
        List<Recipient> recipients = new ArrayList<>();
        for (ContactEntry contact : contacts) {
            String name = contact.controller().getNameValue().trim();
            String email = contact.controller().getEmailValue().trim();
            if (!email.isEmpty()) {
                recipients.add(new Recipient(name, email));
            }
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
                if (Boolean.TRUE.equals(task.getValue())) {
                    successCount.incrementAndGet();
                }
                completeSendBatch(pendingCount, successCount, recipients.size(), progressIndicator, notifyButton);
            });

            task.setOnFailed(e -> completeSendBatch(pendingCount, successCount, recipients.size(), progressIndicator, notifyButton));

            Thread thread = new Thread(task, "safex-email-send");
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void completeSendBatch(AtomicInteger pendingCount, AtomicInteger successCount, int total,
                                   ProgressIndicator indicator, Button notifyButton) {
        if (pendingCount.decrementAndGet() > 0) {
            return;
        }

        indicator.setVisible(false);
        notifyButton.setDisable(false);

        if (successCount.get() == total) {
            showAlert(Alert.AlertType.INFORMATION, "Notifications Complete", "All notifications sent.");
            AppController.goToHomeDashboard();
        } else {
            showAlert(Alert.AlertType.WARNING, "Notifications Complete", successCount.get() + " of " + total + " sent.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private record ContactEntry(Parent row, PartnerContactRowController controller) {}

    private record Recipient(String name, String email) {}
}
