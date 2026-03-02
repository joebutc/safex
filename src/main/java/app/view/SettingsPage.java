package app.view;

import app.AppController;
import app.model.State;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Objects;

public class SettingsPage {
    @FXML private Button homeButton;
    @FXML private ImageView homeIcon;
    @FXML private CheckBox agreeCheckBox;
    @FXML private Button acceptBtn;

    @FXML
    private void initialize() {
        acceptBtn.disableProperty().bind(agreeCheckBox.selectedProperty().not());
        homeIcon.setImage(new javafx.scene.image.Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/images/Home.png"))
        ));
    }

    @FXML
    private void handleHome() {
        AppController.updateView(State.DASHBOARD);
    }

    @FXML
    private void handleDataCollected() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText("What data is collected");
        a.setContentText("""
            \u2022 Account details (email, username)
            \u2022 Usage activity (features used, time spent)
            \u2022 Device info (OS, app version, IP range)
        """);
        a.showAndWait();
    }

    @FXML
    private void handleWhyUsed() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText("Why your data is used");
        a.setContentText("""
            We use your data to:
            \u2022 Improve app performance
            \u2022 Personalize your experience
            \u2022 Ensure platform security
            \u2022 Provide customer support
        """);
        a.showAndWait();
    }

    @FXML
    private void handleAccess() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Who can access your data");

        VBox box = new VBox(12);
        box.setPadding(new Insets(16));

        CheckBox staff = new CheckBox("Internal Support Staff");
        CheckBox analytics = new CheckBox("Analytics Team");
        CheckBox partners = new CheckBox("Trusted Partners");

        Button save = new Button("Save");
        save.setOnAction(e -> dialog.close());

        box.getChildren().addAll(
                new Label("Select who can access your data:"),
                staff, analytics, partners,
                save
        );

        Scene dialogScene = new Scene(box, 300, 240);
        AppStyles.apply(dialogScene);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    @FXML
    private void handleRetention() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("While account is active",
                "While account is active", "1 year", "6 months", "Delete immediately");
        dialog.setTitle("Data Retention");
        dialog.setHeaderText("How long should we keep your data?");
        dialog.showAndWait();
    }

    @FXML
    private void handleRights() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText("Your Rights");
        a.setContentText("""
            You can:
            \u2022 Request a copy of your data
            \u2022 Correct inaccurate info
            \u2022 Delete your account anytime
        """);
        a.showAndWait();

    }

    @FXML
    private void handleTestingPermissions() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Testing Data Permissions");

        VBox box = new VBox(16);
        box.setPadding(new Insets(24));
        box.setPrefWidth(420);

        Label header = new Label("Testing Data Permissions");
        header.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label description = new Label("Choose how SafeX can use your STI test log data:");
        description.setFont(Font.font("System", 13));
        description.setTextFill(Color.web("#475569"));
        description.setWrapText(true);

        boolean[] perms = AppController.getTestPermissions();
        int currentInterval = AppController.getReminderIntervalMonths();

        CheckBox remindersCb = new CheckBox("Send me reminders when it's time for my next STI test");
        remindersCb.setWrapText(true);
        remindersCb.setMaxWidth(380);
        remindersCb.setSelected(perms[0]);

        // Reminder frequency row
        Label intervalLabel = new Label("Remind me every:");
        intervalLabel.setFont(Font.font("System", 13));

        ComboBox<String> intervalCombo = new ComboBox<>();
        intervalCombo.getItems().addAll("Every month", "Every 3 months", "Every 6 months", "Every 12 months");
        String[] intervalOptions = {"Every month", "Every 3 months", "Every 6 months", "Every 12 months"};
        int[] intervalValues = {1, 3, 6, 12};
        // Select the option matching the current saved value
        String selectedInterval = "Every 3 months";
        for (int i = 0; i < intervalValues.length; i++) {
            if (intervalValues[i] == currentInterval) {
                selectedInterval = intervalOptions[i];
                break;
            }
        }
        intervalCombo.setValue(selectedInterval);
        intervalCombo.setMaxWidth(Double.MAX_VALUE);
        intervalCombo.disableProperty().bind(remindersCb.selectedProperty().not());

        HBox intervalRow = new HBox(12, intervalLabel, intervalCombo);
        intervalRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(intervalCombo, javafx.scene.layout.Priority.ALWAYS);
        intervalRow.disableProperty().bind(remindersCb.selectedProperty().not());

        CheckBox shareCb = new CheckBox("Allow sharing my test log with my healthcare provider");
        shareCb.setWrapText(true);
        shareCb.setMaxWidth(380);
        shareCb.setSelected(perms[1]);

        Label note = new Label("You can change these preferences at any time.");
        note.setFont(Font.font("System", 12));
        note.setTextFill(Color.web("#64748B"));

        Button saveBtn = new Button("Save Preferences");
        saveBtn.getStyleClass().add("primary-button");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> {
            int chosenInterval = 3;
            for (int i = 0; i < intervalOptions.length; i++) {
                if (intervalOptions[i].equals(intervalCombo.getValue())) {
                    chosenInterval = intervalValues[i];
                    break;
                }
            }
            AppController.saveTestPermissions(remindersCb.isSelected(), shareCb.isSelected(), chosenInterval);
            dialog.close();
        });

        box.getChildren().addAll(header, description, remindersCb, intervalRow, shareCb, note, saveBtn);

        Scene dialogScene = new Scene(box);
        AppStyles.apply(dialogScene);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    @FXML
    private void handleTreatmentPermissions() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Treatment Data Permissions");

        VBox box = new VBox(16);
        box.setPadding(new Insets(24));
        box.setPrefWidth(420);

        Label header = new Label("Treatment Data Permissions");
        header.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label description = new Label("Choose how SafeX can use your treatment plan data:");
        description.setFont(Font.font("System", 13));
        description.setTextFill(Color.web("#475569"));
        description.setWrapText(true);

        boolean[] perms = AppController.getTreatmentPermissions();

        CheckBox remindersCb = new CheckBox("Notify me when any medication is running low on supply");
        remindersCb.setWrapText(true);
        remindersCb.setMaxWidth(380);
        remindersCb.setSelected(perms[0]);

        CheckBox clinicCb = new CheckBox("Allow my healthcare provider to view and update my treatment plan");
        clinicCb.setWrapText(true);
        clinicCb.setMaxWidth(380);
        clinicCb.setSelected(perms[1]);

        Label note = new Label("Clinic access lets your doctor issue prescriptions directly through SafeX.");
        note.setFont(Font.font("System", 12));
        note.setTextFill(Color.web("#64748B"));
        note.setWrapText(true);

        Button saveBtn = new Button("Save Preferences");
        saveBtn.getStyleClass().add("primary-button");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> {
            AppController.saveTreatmentPermissions(remindersCb.isSelected(), clinicCb.isSelected());
            dialog.close();
        });

        box.getChildren().addAll(header, description, remindersCb, clinicCb, note, saveBtn);

        Scene dialogScene = new Scene(box);
        AppStyles.apply(dialogScene);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    @FXML
    private void handleReadFullTerms() {
        AppController.updateView(State.LEGAL);
    }

    @FXML
    private void handleAccept() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setHeaderText("Accepted");
        a.setContentText("You accepted the Terms & Conditions");
        a.showAndWait();

        AppController.updateView(State.DASHBOARD);
    }
}
