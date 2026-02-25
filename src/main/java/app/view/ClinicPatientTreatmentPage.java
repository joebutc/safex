package app.view;

import app.AppController;
import app.model.TreatmentEntry;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class ClinicPatientTreatmentPage implements Page {

    private final AppController controller;
    private VBox listContainer;
    private String patientUsername;

    public ClinicPatientTreatmentPage(AppController controller) {
        this.controller = controller;
    }

    @Override
    public Scene createScene() {
        patientUsername = controller != null ? controller.getClinicPatientUsername() : "";

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F8FAFC;");
        root.setMinSize(900, 650);
        root.setTop(buildHeader());

        VBox content = new VBox(16);
        content.setPadding(new Insets(24));

        // Patient info + permission status row
        HBox patientRow = buildPatientInfoRow();

        // Check clinic access permission
        boolean[] perms = controller != null
                ? controller.getPatientTreatmentPermissions(patientUsername)
                : new boolean[]{false, false};
        boolean hasAccess = perms[1]; // allow_clinic_access

        if (!hasAccess) {
            Label warn = new Label(
                    "This patient has not granted clinic access to their treatment plan. "
                    + "They can enable this in Privacy & Settings → Treatment Data Permissions.");
            warn.setWrapText(true);
            warn.setFont(Font.font("System", 13));
            warn.setTextFill(Color.web("#B45309"));
            warn.setStyle("-fx-background-color: #FEF3C7; -fx-padding: 12; -fx-background-radius: 8;");
            content.getChildren().addAll(patientRow, warn);
        } else {
            Button prescribeBtn = new Button("+ Issue Prescription");
            prescribeBtn.getStyleClass().add("primary-button");
            prescribeBtn.setMinHeight(40);
            prescribeBtn.setOnAction(e -> showPrescribeDialog(null));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox topRow = new HBox(12, patientRow, spacer, prescribeBtn);
            topRow.setAlignment(Pos.CENTER_LEFT);

            listContainer = new VBox(14);
            listContainer.setAlignment(Pos.TOP_LEFT);
            listContainer.setPadding(new Insets(0, 0, 16, 0));

            ScrollPane scroll = new ScrollPane(listContainer);
            scroll.setFitToWidth(true);
            scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
            VBox.setVgrow(scroll, Priority.ALWAYS);

            content.getChildren().addAll(topRow, scroll);
            VBox.setVgrow(content, Priority.ALWAYS);
            refresh();
        }

        root.setCenter(content);
        return new Scene(root, AppStyles.SCENE_WIDTH, AppStyles.SCENE_HEIGHT);
    }

    private HBox buildPatientInfoRow() {
        Label patientLabel = new Label("Patient:  ");
        patientLabel.setFont(Font.font("System", 13));
        patientLabel.setTextFill(Color.web("#475569"));

        Label patientName = new Label(patientUsername != null ? patientUsername : "—");
        patientName.setFont(Font.font("System", FontWeight.BOLD, 14));
        patientName.setTextFill(Color.web("#0F172A"));

        HBox row = new HBox(4, patientLabel, patientName);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox buildHeader() {
        Image homeImage = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/images/Home.png")), 25, 25, true, true);
        ImageView icon = new ImageView(homeImage);
        icon.setFitWidth(28);
        icon.setFitHeight(28);

        Button homeBtn = new Button();
        homeBtn.setGraphic(icon);
        homeBtn.getStyleClass().add("icon-button");
        homeBtn.setFocusTraversable(false);
        homeBtn.setOnAction(e -> { if (controller != null) controller.goToDashboard(); });

        Label title = new Label("Patient Treatment Plan");
        title.getStyleClass().add("app-header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(10, homeBtn, title, spacer);
        header.getStyleClass().add("app-header");
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private void refresh() {
        if (listContainer == null || controller == null) return;
        listContainer.getChildren().clear();

        List<TreatmentEntry> treatments = controller.getPatientTreatmentsForClinic(patientUsername);

        if (treatments.isEmpty()) {
            Label empty = new Label("No active treatments on record for this patient.");
            empty.setTextFill(Color.web("#94A3B8"));
            empty.setFont(Font.font("System", 14));
            empty.setPadding(new Insets(20, 0, 0, 4));
            listContainer.getChildren().add(empty);
            return;
        }

        for (TreatmentEntry t : treatments) {
            listContainer.getChildren().add(buildTreatmentCard(t));
        }
    }

    private VBox buildTreatmentCard(TreatmentEntry t) {
        Label nameLabel = new Label(t.getMedicationName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.web("#0F172A"));

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

        VBox infoBox = new VBox(4, nameLabel, doseFreq, qtyLabel, startLabel);
        if (t.getNotes() != null && !t.getNotes().isBlank()) {
            Label notes = new Label("Note: " + t.getNotes());
            notes.setFont(Font.font("System", 12));
            notes.setTextFill(Color.web("#64748B"));
            notes.setWrapText(true);
            infoBox.getChildren().add(notes);
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
                if (bt == ButtonType.YES && controller != null) {
                    controller.deleteTreatmentForPatient(t.getId(), patientUsername);
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
        form.setPrefWidth(440);

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

        Label doseL = fieldLabel("Dose (e.g. 1 tablet, 200 mg)");
        TextField doseField = new TextField(editTarget != null ? editTarget.getDose() : "");
        doseField.setPromptText("e.g. 1 tablet");

        Label freqL = fieldLabel("Frequency");
        ComboBox<String> freqCombo = new ComboBox<>();
        freqCombo.getItems().addAll(TreatmentTrackerPage.FREQUENCY_OPTIONS);
        freqCombo.setValue(editTarget != null ? editTarget.getFrequency() : "Once daily");
        freqCombo.setMaxWidth(Double.MAX_VALUE);

        Label qtyL = fieldLabel("Dispensed quantity (number of doses)");
        TextField qtyField = new TextField(editTarget != null
                ? String.valueOf((int) editTarget.getQuantityRemaining()) : "30");
        qtyField.setPromptText("e.g. 30");

        Label threshL = fieldLabel("Low-stock alert threshold (days of supply)");
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
            String dose = doseField.getText().trim();
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

            if (name.isBlank() || dose.isBlank() || datePicker.getValue() == null) {
                errorLabel.setText("Please fill in all required fields.");
                errorLabel.setVisible(true);
                return;
            }

            String date = datePicker.getValue().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));

            boolean ok;
            if (editTarget != null) {
                ok = controller.updateTreatmentPlanForPatient(
                        editTarget.getId(), patientUsername, name, dose, freq, qty, thresh, notes);
            } else {
                ok = controller.addTreatmentForPatient(
                        patientUsername, name, dose, freq, qty, thresh, date, notes);
            }

            if (ok) {
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
                doseL, doseField,
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

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        l.setTextFill(Color.web("#374151"));
        return l;
    }

    @Override
    public void onEnter() {}
}
