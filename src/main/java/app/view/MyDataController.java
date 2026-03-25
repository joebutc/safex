package app.view;

import app.AppController;
import app.model.StiTestEntry;
import app.model.TreatmentEntry;
import app.model.UserDataExport;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MyDataController {
    @FXML private VBox dataContainer;
    @FXML private Button closeButton;

    @FXML
    private void initialize() {
        UserDataExport export = AppController.getUserDataExport();

        // Account section
        addSectionHeader("Account");
        addField("Username", export.getUsername());
        addField("Terms Accepted", export.getTermsAcceptedDate() != null
                ? export.getTermsAcceptedDate() : "Not yet accepted");

        // Privacy settings
        addSectionHeader("Privacy Settings");
        addField("Data Retention", export.getDataRetention());
        boolean[] dataAccess = export.getDataAccessPermissions();
        if (dataAccess != null) {
            addField("Allow Internal Staff", dataAccess[0] ? "Yes" : "No");
            addField("Allow Analytics", dataAccess[1] ? "Yes" : "No");
            addField("Allow Trusted Partners", dataAccess[2] ? "Yes" : "No");
        }

        // Test permissions
        addSectionHeader("Test Permissions");
        boolean[] testPerms = export.getTestPermissions();
        if (testPerms != null) {
            addField("Allow Reminders", testPerms[0] ? "Yes" : "No");
            addField("Share with Doctor", testPerms[1] ? "Yes" : "No");
            addField("Reminder Interval", export.getReminderInterval() + " months");
        }

        // Treatment permissions
        addSectionHeader("Treatment Permissions");
        boolean[] treatPerms = export.getTreatmentPermissions();
        if (treatPerms != null) {
            addField("Allow Treatment Reminders", treatPerms[0] ? "Yes" : "No");
            addField("Allow Clinic Access", treatPerms[1] ? "Yes" : "No");
        }

        // STI test history
        addSectionHeader("STI Test History (" + (export.getStiTests() != null ? export.getStiTests().size() : 0) + " records)");
        if (export.getStiTests() != null && !export.getStiTests().isEmpty()) {
            for (StiTestEntry test : export.getStiTests()) {
                addTestEntry(test);
            }
        } else {
            addEmptyMessage("No STI tests logged.");
        }

        // Treatment history
        addSectionHeader("Treatment History (" + (export.getTreatments() != null ? export.getTreatments().size() : 0) + " records)");
        if (export.getTreatments() != null && !export.getTreatments().isEmpty()) {
            for (TreatmentEntry treatment : export.getTreatments()) {
                addTreatmentEntry(treatment);
            }
        } else {
            addEmptyMessage("No treatments logged.");
        }
    }

    private void addSectionHeader(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 14));
        label.setTextFill(Color.web("#0F172A"));
        VBox.setMargin(label, new Insets(8, 0, 4, 0));
        dataContainer.getChildren().add(label);
    }

    private void addField(String name, String value) {
        Label label = new Label(name + ": " + (value != null ? value : "N/A"));
        label.setFont(Font.font("System", 13));
        label.setTextFill(Color.web("#334155"));
        label.setWrapText(true);
        dataContainer.getChildren().add(label);
    }

    private void addTestEntry(StiTestEntry test) {
        VBox card = new VBox(2);
        card.setPadding(new Insets(8));
        card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 6;");

        Label main = new Label(test.getTestDate() + " - " + test.getStiType() + ": " + test.getResult());
        main.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        main.setTextFill(Color.web("#0F172A"));
        card.getChildren().add(main);

        if (test.getNotes() != null && !test.getNotes().isBlank()) {
            Label notes = new Label("Notes: " + test.getNotes());
            notes.setFont(Font.font("System", 11));
            notes.setTextFill(Color.web("#64748B"));
            notes.setWrapText(true);
            card.getChildren().add(notes);
        }

        dataContainer.getChildren().add(card);
    }

    private void addTreatmentEntry(TreatmentEntry t) {
        VBox card = new VBox(2);
        card.setPadding(new Insets(8));
        card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 6;");

        String status = t.isActive() ? "Active" : "Inactive";
        String source = t.isClinicIssued() ? "Clinic" : "Self";
        Label main = new Label(t.getMedicationName() + " - " + t.getDose() + " (" + status + ", " + source + ")");
        main.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        main.setTextFill(Color.web("#0F172A"));
        card.getChildren().add(main);

        Label details = new Label("Started: " + t.getStartDate() + " | Frequency: " + t.getFrequency());
        details.setFont(Font.font("System", 11));
        details.setTextFill(Color.web("#64748B"));
        card.getChildren().add(details);

        dataContainer.getChildren().add(card);
    }

    private void addEmptyMessage(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", 12));
        label.setTextFill(Color.web("#94A3B8"));
        dataContainer.getChildren().add(label);
    }

    @FXML
    private void handleExportCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Personal Data");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        chooser.setInitialFileName("safex-data-export.csv");
        File file = chooser.showSaveDialog(closeButton.getScene().getWindow());
        if (file != null) {
            boolean success = AppController.exportUserDataToCsv(file);
            if (success) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setHeaderText("Export Successful");
                alert.setContentText("Your data has been exported to:\n" + file.getAbsolutePath());
                alert.showAndWait();
            } else {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR);
                alert.setHeaderText("Export Failed");
                alert.setContentText("Could not export data. Please try again.");
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void handleViewAccessLog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/AccessLog.fxml"));
            Parent root = loader.load();
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Data Access Log");
            Scene dialogScene = new Scene(root);
            AppStyles.apply(dialogScene);
            dialog.setScene(dialogScene);
            dialog.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException("Could not load AccessLog", e);
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) closeButton.getScene().getWindow()).close();
    }
}
