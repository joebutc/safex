package app.view;

import app.AppController;
import app.model.DatabaseHandler;
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

public class TreatmentTrackerPage implements Page {

    static final List<String> MEDICATION_PRESETS = List.of(
            "PrEP (Tenofovir/Emtricitabine)",
            "PEP (Post-Exposure Prophylaxis)",
            "Antiretroviral Therapy (ART)",
            "Doxycycline",
            "Azithromycin",
            "Metronidazole",
            "Penicillin",
            "Ceftriaxone",
            "Other"
    );

    static final List<String> FREQUENCY_OPTIONS = List.of(
            "Once daily", "Twice daily", "Three times daily", "Weekly", "As needed"
    );

    static final List<String> DOSE_UNITS = List.of(
            // Solid / oral
            "tablet(s)", "capsule(s)", "lozenge(s)", "sachet(s)",
            // Weight / volume measures
            "mg", "mcg", "g", "ml", "mmol", "mEq",
            // Injectables / biological
            "IU", "units",
            // Topical / inhaled / other forms
            "patch(es)", "puff(s)", "spray(s)", "drop(s)", "suppository/suppositories",
            // Catch-all
            "Other (type below)..."
    );

    private final AppController controller;
    private VBox listContainer;

    public TreatmentTrackerPage(AppController controller) {
        this.controller = controller;
    }

    @Override
    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F8FAFC;");
        root.setMinSize(900, 650);
        root.setTop(buildHeader());

        VBox content = new VBox(16);
        content.setPadding(new Insets(24));

        Label subtitle = new Label(
                "Track your medications, dose schedules, and supply levels. You'll be notified when stock runs low.");
        subtitle.setFont(Font.font("System", 13));
        subtitle.setTextFill(Color.web("#475569"));
        subtitle.setWrapText(true);

        Button addBtn = new Button("+ Add Medication");
        addBtn.getStyleClass().add("primary-button");
        addBtn.setMinHeight(40);
        addBtn.setOnAction(e -> showAddDialog(null));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(12, subtitle, spacer, addBtn);
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
        root.setCenter(content);

        refresh();
        return new Scene(root, AppStyles.SCENE_WIDTH, AppStyles.SCENE_HEIGHT);
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

        Label title = new Label("My Treatment Plan");
        title.getStyleClass().add("app-header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(10, homeBtn, title, spacer);
        header.getStyleClass().add("app-header");
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    void refresh() {
        listContainer.getChildren().clear();
        if (controller == null) return;

        List<TreatmentEntry> treatments = controller.getTreatments();
        if (treatments.isEmpty()) {
            Label empty = new Label(
                    "No medications added yet. Click '+ Add Medication' to log your first treatment.");
            empty.setWrapText(true);
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

    VBox buildTreatmentCard(TreatmentEntry t) {
        return t.isClinicIssued() ? buildClinicCard(t) : buildPatientCard(t);
    }

    // ── Clinic-issued card ───────────────────────────────────────────────────
    // Read-only: Log Dose + Refill + Note to Clinic only.
    // Blue left accent + light blue tint distinguishes it from self-entered cards.

    private VBox buildClinicCard(TreatmentEntry t) {
        Label badge = new Label("Prescribed by your doctor");
        badge.setFont(Font.font("System", FontWeight.SEMI_BOLD, 10));
        badge.setPadding(new Insets(3, 9, 3, 9));
        badge.setTextFill(Color.web("#1D4ED8"));
        badge.setStyle("-fx-background-color: #DBEAFE; -fx-background-radius: 999;");

        Label stockBadge = new Label("LOW STOCK");
        stockBadge.setFont(Font.font("System", FontWeight.BOLD, 10));
        stockBadge.setPadding(new Insets(3, 9, 3, 9));
        stockBadge.setTextFill(Color.WHITE);
        stockBadge.setStyle("-fx-background-color: #DC2626; -fx-background-radius: 999;");
        stockBadge.setVisible(t.isLowStock());
        stockBadge.setManaged(t.isLowStock());

        Label nameLabel = new Label(t.getMedicationName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.web("#1E3A8A"));

        HBox nameRow = new HBox(10, nameLabel, badge, stockBadge);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label doseFreq = new Label(t.getDose() + "  ·  " + t.getFrequency());
        doseFreq.setFont(Font.font("System", 13));
        doseFreq.setTextFill(Color.web("#3B4E6B"));

        Label startedLabel = new Label("Prescribed: " + t.getStartDate());
        startedLabel.setFont(Font.font("System", 12));
        startedLabel.setTextFill(Color.web("#64748B"));

        String qtyText;
        if (t.dosesPerDay() > 0) {
            int days = (int) Math.ceil(t.daysRemaining());
            qtyText = String.format("Supply: %.0f doses  (approx. %d day%s remaining)",
                    t.getQuantityRemaining(), days, days == 1 ? "" : "s");
        } else {
            qtyText = String.format("Supply: %.0f doses  (as needed — no auto-calculation)",
                    t.getQuantityRemaining());
        }
        Label qtyLabel = new Label(qtyText);
        qtyLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        qtyLabel.setTextFill(t.isLowStock() ? Color.web("#DC2626") : Color.web("#1E3A8A"));

        VBox infoBox = new VBox(4, nameRow, doseFreq, startedLabel, qtyLabel);

        if (t.getNotes() != null && !t.getNotes().isBlank()) {
            Label clinicNotes = new Label("Clinical note: " + t.getNotes());
            clinicNotes.setFont(Font.font("System", 12));
            clinicNotes.setTextFill(Color.web("#3B4E6B"));
            clinicNotes.setWrapText(true);
            infoBox.getChildren().add(clinicNotes);
        }

        if (t.getPatientNote() != null && !t.getPatientNote().isBlank()) {
            Label noteDisplay = new Label("Your note to clinic: " + t.getPatientNote());
            noteDisplay.setFont(Font.font("System", 12));
            noteDisplay.setTextFill(Color.web("#6B21A8"));
            noteDisplay.setStyle(
                    "-fx-background-color: #F3E8FF; -fx-padding: 6 8 6 8; -fx-background-radius: 6;");
            noteDisplay.setWrapText(true);
            infoBox.getChildren().add(noteDisplay);
        }

        Button logDoseBtn = new Button("Log Dose");
        logDoseBtn.getStyleClass().add("primary-button");
        logDoseBtn.setMinHeight(32);
        logDoseBtn.setDisable(t.getQuantityRemaining() <= 0);
        logDoseBtn.setOnAction(e -> {
            double newQty = Math.max(0, t.getQuantityRemaining() - 1);
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Log 1 dose of " + t.getMedicationName() + "?\n"
                            + String.format("Remaining: %.0f → %.0f doses", t.getQuantityRemaining(), newQty),
                    ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);
            confirm.setTitle("Log Dose");
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES && controller != null) {
                    controller.updateTreatmentQuantity(t.getId(), newQty);
                    refresh();
                }
            });
        });

        Button refillBtn = new Button("Refill");
        refillBtn.getStyleClass().add("secondary-button");
        refillBtn.setMinHeight(32);
        refillBtn.setOnAction(e -> showRefillDialog(t));

        Button noteBtn = new Button("Note to Clinic");
        noteBtn.getStyleClass().add("secondary-button");
        noteBtn.setMinHeight(32);
        noteBtn.setOnAction(e -> showNoteToClinicDialog(t));

        HBox buttons = new HBox(8, logDoseBtn, refillBtn, noteBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox cardInner = new VBox(10, infoBox, buttons);
        cardInner.setPadding(new Insets(14, 16, 14, 14));
        cardInner.setStyle("-fx-background-color: #EFF6FF; -fx-background-radius: 0 10 10 0;");
        HBox.setHgrow(cardInner, Priority.ALWAYS);

        Region accent = new Region();
        accent.setMinWidth(5);
        accent.setPrefWidth(5);
        accent.setStyle("-fx-background-color: #2563EB; -fx-background-radius: 10 0 0 10;");

        HBox cardRow = new HBox(0, accent, cardInner);
        cardRow.setStyle(
                "-fx-background-radius: 12;"
                + " -fx-border-color: #BFDBFE;"
                + " -fx-border-radius: 12;"
                + " -fx-border-width: 1;"
                + " -fx-effect: dropshadow(gaussian, rgba(37,99,235,0.08), 12, 0.1, 0, 3);");

        return new VBox(cardRow);
    }

    // ── Patient self-entered card ────────────────────────────────────────────
    // Fully editable: Log Dose + Refill + Edit + Remove.

    private VBox buildPatientCard(TreatmentEntry t) {
        Label stockBadge = new Label("LOW STOCK");
        stockBadge.setFont(Font.font("System", FontWeight.BOLD, 10));
        stockBadge.setPadding(new Insets(2, 8, 2, 8));
        stockBadge.setTextFill(Color.WHITE);
        stockBadge.setStyle("-fx-background-color: #DC2626; -fx-background-radius: 999;");
        stockBadge.setVisible(t.isLowStock());
        stockBadge.setManaged(t.isLowStock());

        Label nameLabel = new Label(t.getMedicationName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.web("#0F172A"));

        HBox nameRow = new HBox(10, nameLabel, stockBadge);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label doseFreq = new Label(t.getDose() + "  ·  " + t.getFrequency());
        doseFreq.setFont(Font.font("System", 13));
        doseFreq.setTextFill(Color.web("#475569"));

        Label startedLabel = new Label("Started: " + t.getStartDate());
        startedLabel.setFont(Font.font("System", 12));
        startedLabel.setTextFill(Color.web("#64748B"));

        String qtyText;
        if (t.dosesPerDay() > 0) {
            int days = (int) Math.ceil(t.daysRemaining());
            qtyText = String.format("Supply: %.0f doses  (approx. %d day%s remaining)",
                    t.getQuantityRemaining(), days, days == 1 ? "" : "s");
        } else {
            qtyText = String.format("Supply: %.0f doses  (as needed — no auto-calculation)",
                    t.getQuantityRemaining());
        }
        Label qtyLabel = new Label(qtyText);
        qtyLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        qtyLabel.setTextFill(t.isLowStock() ? Color.web("#DC2626") : Color.web("#0F172A"));

        VBox infoBox = new VBox(4, nameRow, doseFreq, startedLabel, qtyLabel);

        if (t.getNotes() != null && !t.getNotes().isBlank()) {
            Label notes = new Label("Note: " + t.getNotes());
            notes.setFont(Font.font("System", 12));
            notes.setTextFill(Color.web("#64748B"));
            notes.setWrapText(true);
            infoBox.getChildren().add(notes);
        }

        Button logDoseBtn = new Button("Log Dose");
        logDoseBtn.getStyleClass().add("primary-button");
        logDoseBtn.setMinHeight(32);
        logDoseBtn.setDisable(t.getQuantityRemaining() <= 0);
        logDoseBtn.setOnAction(e -> {
            double newQty = Math.max(0, t.getQuantityRemaining() - 1);
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Log 1 dose of " + t.getMedicationName() + "?\n"
                            + String.format("Remaining: %.0f → %.0f doses", t.getQuantityRemaining(), newQty),
                    ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);
            confirm.setTitle("Log Dose");
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES && controller != null) {
                    controller.updateTreatmentQuantity(t.getId(), newQty);
                    refresh();
                }
            });
        });

        Button refillBtn = new Button("Refill");
        refillBtn.getStyleClass().add("secondary-button");
        refillBtn.setMinHeight(32);
        refillBtn.setOnAction(e -> showRefillDialog(t));

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("secondary-button");
        editBtn.setMinHeight(32);
        editBtn.setOnAction(e -> showAddDialog(t));

        Button deleteBtn = new Button("Remove");
        deleteBtn.getStyleClass().add("secondary-button");
        deleteBtn.setMinHeight(32);
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Remove " + t.getMedicationName() + " from your treatment plan?",
                    ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);
            confirm.setTitle("Remove Medication");
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES && controller != null) {
                    controller.deleteTreatment(t.getId());
                    refresh();
                }
            });
        });

        HBox buttons = new HBox(8, logDoseBtn, refillBtn, editBtn, deleteBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10, infoBox, buttons);
        card.setPadding(new Insets(16));
        String borderColor = t.isLowStock() ? "#FCA5A5" : "#E5E7EB";
        String borderWidth = t.isLowStock() ? "2" : "1";
        card.setStyle("-fx-background-color: #FFFFFF;"
                + " -fx-background-radius: 12;"
                + " -fx-border-color: " + borderColor + ";"
                + " -fx-border-radius: 12;"
                + " -fx-border-width: " + borderWidth + ";");
        return card;
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────

    private void showRefillDialog(TreatmentEntry t) {
        TextInputDialog dlg = new TextInputDialog("30");
        dlg.setTitle("Refill Medication");
        dlg.setHeaderText("Add doses to " + t.getMedicationName());
        dlg.setContentText("Number of doses to add:");
        dlg.showAndWait().ifPresent(val -> {
            try {
                double add = Double.parseDouble(val.trim());
                if (add <= 0) return;
                controller.updateTreatmentQuantity(t.getId(), t.getQuantityRemaining() + add);
                refresh();
            } catch (NumberFormatException ignored) {
                new Alert(Alert.AlertType.ERROR, "Please enter a valid number.", ButtonType.OK).showAndWait();
            }
        });
    }

    private void showNoteToClinicDialog(TreatmentEntry t) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Note to Clinic");

        VBox form = new VBox(12);
        form.setPadding(new Insets(24));
        form.setPrefWidth(420);

        Label heading = new Label("Note to your clinic");
        heading.setFont(Font.font("System", FontWeight.BOLD, 15));
        heading.setTextFill(Color.web("#0F172A"));

        Label medInfo = new Label("Regarding: " + t.getMedicationName());
        medInfo.setFont(Font.font("System", 13));
        medInfo.setTextFill(Color.web("#475569"));

        Label noteL = fieldLabel("Your message (e.g. side effects, questions, refill request)");
        TextArea noteArea = new TextArea(t.getPatientNote() != null ? t.getPatientNote() : "");
        noteArea.setPromptText("e.g. Experiencing nausea, please advise on timing");
        noteArea.setPrefRowCount(4);
        noteArea.setWrapText(true);

        Button saveBtn = new Button("Save Note");
        saveBtn.getStyleClass().add("primary-button");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setMinHeight(40);
        saveBtn.setOnAction(e -> {
            if (controller != null) controller.updatePatientNote(t.getId(), noteArea.getText().trim());
            dialog.close();
            refresh();
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary-button");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setOnAction(e -> dialog.close());

        HBox btns = new HBox(10, cancelBtn, saveBtn);
        HBox.setHgrow(saveBtn, Priority.ALWAYS);
        HBox.setHgrow(cancelBtn, Priority.ALWAYS);

        form.getChildren().addAll(heading, medInfo, noteL, noteArea, btns);
        Scene sc = new Scene(form);
        AppStyles.apply(sc);
        dialog.setScene(sc);
        dialog.showAndWait();
    }

    // editTarget == null → add mode; non-null → edit/prefill mode
    void showAddDialog(TreatmentEntry editTarget) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(editTarget == null ? "Add Medication" : "Edit Medication");

        VBox form = new VBox(10);
        form.setPadding(new Insets(24));
        form.setPrefWidth(460);

        Label heading = new Label(editTarget == null ? "Add a New Medication" : "Edit Medication");
        heading.setFont(Font.font("System", FontWeight.BOLD, 16));
        heading.setTextFill(Color.web("#0F172A"));

        Label nameL = fieldLabel("Medication Name");
        ComboBox<String> nameCombo = new ComboBox<>();
        nameCombo.getItems().addAll(MEDICATION_PRESETS);
        nameCombo.setEditable(true);
        nameCombo.setValue(editTarget != null ? editTarget.getMedicationName() : MEDICATION_PRESETS.get(0));
        nameCombo.setMaxWidth(Double.MAX_VALUE);

        // Split dose: amount field + unit picker
        Label doseL = fieldLabel("Dose per administration");
        String[] doseParts = parseDoseString(editTarget != null ? editTarget.getDose() : "");
        TextField doseAmountField = new TextField(doseParts[0]);
        doseAmountField.setPromptText("e.g. 1, 200, 0.5");

        ComboBox<String> doseUnitCombo = new ComboBox<>();
        doseUnitCombo.getItems().addAll(DOSE_UNITS);
        doseUnitCombo.setEditable(true);
        doseUnitCombo.setValue(!doseParts[1].isBlank() ? doseParts[1] : DOSE_UNITS.get(0));
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
        freqCombo.getItems().addAll(FREQUENCY_OPTIONS);
        freqCombo.setValue(editTarget != null ? editTarget.getFrequency() : "Once daily");
        freqCombo.setMaxWidth(Double.MAX_VALUE);

        Label qtyL = fieldLabel("Current Supply (number of doses)");
        TextField qtyField = new TextField(editTarget != null
                ? String.valueOf((int) editTarget.getQuantityRemaining()) : "30");
        qtyField.setPromptText("e.g. 30");

        Label threshL = fieldLabel("Low stock alert when below (days of supply)");
        TextField threshField = new TextField(editTarget != null
                ? String.valueOf(editTarget.getLowStockThreshold()) : "7");
        threshField.setPromptText("e.g. 7");

        Label dateL = fieldLabel("Start Date");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        if (editTarget != null) {
            try {
                datePicker.setValue(LocalDate.parse(editTarget.getStartDate(),
                        DateTimeFormatter.ofPattern("dd MMM yyyy")));
            } catch (Exception ignored) {}
        }
        datePicker.setMaxWidth(Double.MAX_VALUE);

        Label notesL = fieldLabel("Notes (optional)");
        TextArea notesField = new TextArea(editTarget != null ? editTarget.getNotes() : "");
        notesField.setPromptText("e.g. Take with food");
        notesField.setPrefRowCount(2);
        notesField.setWrapText(true);

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web("#DC2626"));
        errorLabel.setFont(Font.font("System", 12));
        errorLabel.setVisible(false);

        Button saveBtn = new Button(editTarget == null ? "Add Medication" : "Save Changes");
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

            if (editTarget != null) {
                DatabaseHandler.updateTreatmentPlan(editTarget.getId(), editTarget.getUsername(),
                        name, dose, freq, qty, thresh, notes);
            } else {
                controller.addTreatment(name, dose, freq, qty, thresh, date, notes);
            }
            dialog.close();
            refresh();
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

    /** Splits "1 tablet(s)" → ["1", "tablet(s)"], "200 mg" → ["200", "mg"]. */
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

    @Override
    public void onEnter() {}
}
