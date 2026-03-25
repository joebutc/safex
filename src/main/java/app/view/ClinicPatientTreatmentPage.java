package app.view;

import app.AppController;
import app.model.TreatmentEntry;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClinicPatientTreatmentPage {

    private String patientUsername;

    @FXML
    private Button homeButton;
    @FXML
    private Button prescribeButton;

    @FXML
    private Label patientNameLabel;
    @FXML
    private Label accessWarning;

    @FXML
    private VBox listContainer;
    @FXML
    private HBox topRow;

    @FXML
    private void initialize() {
        this.patientUsername = AppController.getClinicPatientUsername();

        patientNameLabel.setText(patientUsername);

        homeButton.setOnAction(e -> AppController.goToHomeDashboard());
        prescribeButton.setOnAction(e -> showPrescribeDialog(null));

        boolean[] perms = AppController.getPatientTreatmentPermissions(patientUsername);
        boolean hasAccess = perms[1];

        if (!hasAccess) {
            accessWarning.setText(
                    "This patient has not granted clinic access to their treatment plan. "
                            + "They can enable this in Privacy & Settings → Treatment Data Permissions."
            );
            accessWarning.setVisible(true);
            accessWarning.setManaged(true);
            topRow.setVisible(false);
            topRow.setManaged(false);
        } else {
            AppController.logClinicAccess(patientUsername, "VIEW_TREATMENTS", null);
            refresh();
        }
    }

    private void refresh() {
        listContainer.getChildren().clear();

        List<TreatmentEntry> treatments = AppController.getPatientTreatmentsForClinic(patientUsername);

        if (treatments.isEmpty()) {
            Label empty = new Label("No active treatments on record for this patient.");
            empty.setTextFill(Color.web("#94A3B8"));
            empty.setFont(Font.font("System", 14));
            listContainer.getChildren().add(empty);
            return;
        }

        for (TreatmentEntry t : treatments) {
            listContainer.getChildren().add(buildTreatmentCard(t));
        }
    }

    private VBox buildTreatmentCard(TreatmentEntry t) {
        // Source badge
        Label sourceBadge = new Label(t.isClinicIssued() ? "Clinic prescribed" : "Self reported");
        sourceBadge.setFont(Font.font("System", FontWeight.SEMI_BOLD, 10));
        sourceBadge.setPadding(new Insets(3, 9, 3, 9));
        if (t.isClinicIssued()) {
            sourceBadge.setTextFill(Color.web("#1D4ED8"));
            sourceBadge.setStyle("-fx-background-color: #DBEAFE; -fx-background-radius: 999;");
        } else {
            sourceBadge.setTextFill(Color.web("#374151"));
            sourceBadge.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 999;");
        }

        Label nameLabel = new Label(t.getMedicationName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.web("#0F172A"));

        HBox nameRow = new HBox(10, nameLabel, sourceBadge);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label doseFreq = new Label(t.getDose() + "  ·  " + t.getFrequency());
        doseFreq.setFont(Font.font("System", 13));
        doseFreq.setTextFill(Color.web("#475569"));

        String qtyText;
        if (t.dosesPerDay() > 0) {
            int days = (int) Math.ceil(t.daysRemaining());
            qtyText = String.format("Supply: %.0f doses  (approx. %d day%s remaining)",
                    t.getQuantityRemaining(), days, days == 1 ? "" : "s");
        } else {
            qtyText = String.format("Supply: %.0f doses  (as needed)", t.getQuantityRemaining());
        }
        Label qtyLabel = new Label(qtyText);
        qtyLabel.setFont(Font.font("System", 13));
        qtyLabel.setTextFill(t.isLowStock() ? Color.web("#DC2626") : Color.web("#374151"));

        Label startLabel = new Label("Started: " + t.getStartDate());
        startLabel.setFont(Font.font("System", 12));
        startLabel.setTextFill(Color.web("#64748B"));

        VBox infoBox = new VBox(4, nameRow, doseFreq, qtyLabel, startLabel);

        if (t.getNotes() != null && !t.getNotes().isBlank()) {
            Label notes = new Label("Clinical note: " + t.getNotes());
            notes.setFont(Font.font("System", 12));
            notes.setTextFill(Color.web("#64748B"));
            notes.setWrapText(true);
            infoBox.getChildren().add(notes);
        }

        // Show patient's note to clinic if present
        if (t.getPatientNote() != null && !t.getPatientNote().isBlank()) {
            Label patientNote = new Label("Patient's note: " + t.getPatientNote());
            patientNote.setFont(Font.font("System", 12));
            patientNote.setTextFill(Color.web("#6B21A8"));
            patientNote.setStyle(
                    "-fx-background-color: #F3E8FF; -fx-padding: 6 8 6 8; -fx-background-radius: 6;");
            patientNote.setWrapText(true);
            infoBox.getChildren().add(patientNote);
        }

        Button editBtn = new Button("Update Prescription");
        editBtn.getStyleClass().add("primary-button");
        editBtn.setMinHeight(32);
        editBtn.setOnAction(e -> showPrescribeDialog(t));

        Button removeBtn = new Button("Discontinue");
        removeBtn.getStyleClass().add("secondary-button");
        removeBtn.setMinHeight(32);
        removeBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Discontinue " + t.getMedicationName() + " for patient " + patientUsername + "?",
                    ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);
            confirm.setTitle("Discontinue Treatment");
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) {
                    AppController.deleteTreatmentForPatient(t.getId(), patientUsername);
                    AppController.logClinicAccess(patientUsername, "DISCONTINUE_TREATMENT",
                            t.getMedicationName() + " - " + t.getDose());
                    refresh();
                }
            });
        });

        HBox buttons = new HBox(8, editBtn, removeBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10, infoBox, buttons);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: #FFFFFF;"
                + " -fx-background-radius: 12;"
                + " -fx-border-color: #E5E7EB;"
                + " -fx-border-radius: 12;"
                + " -fx-border-width: 1;");
        return card;
    }

    // editTarget == null → new prescription; non-null → update existing
    private void showPrescribeDialog(TreatmentEntry editTarget) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(editTarget == null ? "Issue Prescription" : "Update Prescription");

        VBox form = new VBox(10);
        form.setPadding(new Insets(24));
        form.setPrefWidth(460);

        Label heading = new Label(editTarget == null
                ? "Issue New Prescription for " + patientUsername
                : "Update Prescription for " + patientUsername);
        heading.setFont(Font.font("System", FontWeight.BOLD, 15));
        heading.setTextFill(Color.web("#0F172A"));
        heading.setWrapText(true);

        Label nameL = fieldLabel("Medication");
        ComboBox<String> nameCombo = new ComboBox<>();
        nameCombo.getItems().addAll(TreatmentTrackerPage.MEDICATION_PRESETS);
        nameCombo.setEditable(true);
        nameCombo.setValue(editTarget != null ? editTarget.getMedicationName()
                : TreatmentTrackerPage.MEDICATION_PRESETS.get(0));
        nameCombo.setMaxWidth(Double.MAX_VALUE);

        // Split dose: amount + unit
        Label doseL = fieldLabel("Dose per administration");
        String[] doseParts = parseDoseString(editTarget != null ? editTarget.getDose() : "");
        TextField doseAmountField = new TextField(doseParts[0]);
        doseAmountField.setPromptText("e.g. 1, 200, 0.5");

        ComboBox<String> doseUnitCombo = new ComboBox<>();
        doseUnitCombo.getItems().addAll(TreatmentTrackerPage.DOSE_UNITS);
        doseUnitCombo.setEditable(true);
        doseUnitCombo.setValue(!doseParts[1].isBlank() ? doseParts[1] : TreatmentTrackerPage.DOSE_UNITS.get(0));
        doseUnitCombo.setMaxWidth(Double.MAX_VALUE);
        doseUnitCombo.setPromptText("Select or type unit");
        // "Other (type below)..." clears the editor so the user can type a custom unit
        doseUnitCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if ("Other (type below)...".equals(newVal)) {
                doseUnitCombo.getEditor().clear();
                doseUnitCombo.getEditor().requestFocus();
            }
        });

        HBox doseRow = new HBox(8, doseAmountField, doseUnitCombo);
        HBox.setHgrow(doseAmountField, Priority.SOMETIMES);
        HBox.setHgrow(doseUnitCombo, Priority.ALWAYS);

        Label freqL = fieldLabel("Frequency");
        ComboBox<String> freqCombo = new ComboBox<>();
        freqCombo.getItems().addAll(TreatmentTrackerPage.FREQUENCY_OPTIONS);
        freqCombo.setValue(editTarget != null ? editTarget.getFrequency() : "Once daily");
        freqCombo.setMaxWidth(Double.MAX_VALUE);

        Label qtyL = fieldLabel("Dispensed quantity (number of doses)");
        TextField qtyField = new TextField(editTarget != null
                ? String.valueOf((int) editTarget.getQuantityRemaining()) : "30");
        qtyField.setPromptText("e.g. 30");

        Label threshL = fieldLabel("Low stock alert threshold (days of supply)");
        TextField threshField = new TextField(editTarget != null
                ? String.valueOf(editTarget.getLowStockThreshold()) : "7");
        threshField.setPromptText("e.g. 7");

        Label dateL = fieldLabel("Prescription Date");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);

        Label notesL = fieldLabel("Clinical notes (optional)");
        TextArea notesField = new TextArea(editTarget != null ? editTarget.getNotes() : "");
        notesField.setPromptText("e.g. Take with food, monitor liver function");
        notesField.setPrefRowCount(2);
        notesField.setWrapText(true);

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web("#DC2626"));
        errorLabel.setFont(Font.font("System", 12));
        errorLabel.setVisible(false);

        Button saveBtn = new Button(editTarget == null ? "Issue Prescription" : "Save Changes");
        saveBtn.getStyleClass().add("primary-button");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setMinHeight(40);
        saveBtn.setOnAction(e -> {
            String name = nameCombo.getValue() == null ? "" : nameCombo.getValue().trim();
            String amount = doseAmountField.getText().trim();
            String unit = doseUnitCombo.getValue() == null ? "" : doseUnitCombo.getValue().trim();
            String dose = unit.isBlank() ? amount : amount + " " + unit;
            String freq = freqCombo.getValue();
            String notes = notesField.getText().trim();

            double qty;
            int thresh;
            try {
                qty = Double.parseDouble(qtyField.getText().trim());
                thresh = Integer.parseInt(threshField.getText().trim());
            } catch (NumberFormatException ex) {
                errorLabel.setText("Quantity and threshold must be valid numbers.");
                errorLabel.setVisible(true);
                return;
            }

            if (name.isBlank() || amount.isBlank() || datePicker.getValue() == null) {
                errorLabel.setText("Please fill in all required fields.");
                errorLabel.setVisible(true);
                return;
            }

            String date = datePicker.getValue().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));

            boolean ok;
            if (editTarget != null) {
                ok = AppController.updateTreatmentPlanForPatient(
                        editTarget.getId(), patientUsername, name, dose, freq, qty, thresh, notes);
            } else {
                ok = AppController.addTreatmentForPatient(
                        patientUsername, name, dose, freq, qty, thresh, date, notes);
            }

            if (ok) {
                AppController.logClinicAccess(patientUsername,
                        editTarget == null ? "ADD_TREATMENT" : "UPDATE_TREATMENT",
                        name + " - " + dose);
                dialog.close();
                refresh();
            } else {
                errorLabel.setText("Failed to save. Please try again.");
                errorLabel.setVisible(true);
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary-button");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setOnAction(e -> dialog.close());

        HBox btns = new HBox(10, cancelBtn, saveBtn);
        HBox.setHgrow(saveBtn, Priority.ALWAYS);
        HBox.setHgrow(cancelBtn, Priority.ALWAYS);

        form.getChildren().addAll(heading,
                nameL, nameCombo,
                doseL, doseRow,
                freqL, freqCombo,
                qtyL, qtyField,
                threshL, threshField,
                dateL, datePicker,
                notesL, notesField,
                errorLabel, btns);

        Scene sc = new Scene(new ScrollPane(form));
        AppStyles.apply(sc);
        dialog.setScene(sc);
        dialog.showAndWait();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static String[] parseDoseString(String dose) {
        if (dose == null || dose.isBlank()) return new String[]{"", ""};
        int space = dose.indexOf(' ');
        if (space < 0) return new String[]{dose, ""};
        return new String[]{dose.substring(0, space), dose.substring(space + 1)};
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        l.setTextFill(Color.web("#374151"));
        return l;
    }

}
