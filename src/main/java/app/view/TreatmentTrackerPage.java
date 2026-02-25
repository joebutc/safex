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
        // Low-stock badge
        Label stockBadge = new Label(t.isLowStock() ? "LOW STOCK" : "");
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

        // Quantity / days remaining
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

        // Action buttons
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

    // editTarget == null → add mode; non-null → edit/prefill mode
    void showAddDialog(TreatmentEntry editTarget) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(editTarget == null ? "Add Medication" : "Edit Medication");

        VBox form = new VBox(10);
        form.setPadding(new Insets(24));
        form.setPrefWidth(440);

        Label heading = new Label(editTarget == null ? "Add a New Medication" : "Edit Medication");
        heading.setFont(Font.font("System", FontWeight.BOLD, 16));
        heading.setTextFill(Color.web("#0F172A"));

        // Medication name
        Label nameL = fieldLabel("Medication Name");
        ComboBox<String> nameCombo = new ComboBox<>();
        nameCombo.getItems().addAll(MEDICATION_PRESETS);
        nameCombo.setEditable(true);
        nameCombo.setValue(editTarget != null ? editTarget.getMedicationName() : MEDICATION_PRESETS.get(0));
        nameCombo.setMaxWidth(Double.MAX_VALUE);

        // Dose
        Label doseL = fieldLabel("Dose (e.g. 1 tablet, 200 mg)");
        TextField doseField = new TextField(editTarget != null ? editTarget.getDose() : "");
        doseField.setPromptText("e.g. 1 tablet");

        // Frequency
        Label freqL = fieldLabel("Frequency");
        ComboBox<String> freqCombo = new ComboBox<>();
        freqCombo.getItems().addAll(FREQUENCY_OPTIONS);
        freqCombo.setValue(editTarget != null ? editTarget.getFrequency() : "Once daily");
        freqCombo.setMaxWidth(Double.MAX_VALUE);

        // Quantity
        Label qtyL = fieldLabel("Current Supply (number of doses)");
        TextField qtyField = new TextField(editTarget != null
                ? String.valueOf((int) editTarget.getQuantityRemaining()) : "30");
        qtyField.setPromptText("e.g. 30");

        // Low-stock threshold
        Label threshL = fieldLabel("Low-stock alert when below (days of supply)");
        TextField threshField = new TextField(editTarget != null
                ? String.valueOf(editTarget.getLowStockThreshold()) : "7");
        threshField.setPromptText("e.g. 7");

        // Start date
        Label dateL = fieldLabel("Start Date");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        if (editTarget != null) {
            try {
                datePicker.setValue(LocalDate.parse(editTarget.getStartDate(),
                        DateTimeFormatter.ofPattern("dd MMM yyyy")));
            } catch (Exception ignored) {}
        }
        datePicker.setMaxWidth(Double.MAX_VALUE);

        // Notes
        Label notesL = fieldLabel("Notes (optional)");
        TextArea notesField = new TextArea(editTarget != null ? editTarget.getNotes() : "");
        notesField.setPromptText("e.g. Take with food");
        notesField.setPrefRowCount(2);
        notesField.setWrapText(true);

        // Error
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

            if (editTarget != null) {
                controller.updateTreatmentQuantity(editTarget.getId(), qty);
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
