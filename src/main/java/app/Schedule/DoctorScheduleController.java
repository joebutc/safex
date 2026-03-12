package app.Schedule;

import app.AppController;
import app.model.AppointmentSlot;
import app.model.ScheduleDatabaseHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DoctorScheduleController {
    private AppController appController;
    private String clinicUsername;
    private LocalDate currentDate;

    @FXML
    private GridPane timetableGrid;

    // UI State tracking
    private Button currentlySelectedButton = null;
    private LocalTime currentlySelectedTime = null;

    // Maps a time to the full slot data to distinguish available vs booked.
    private Map<LocalTime, AppointmentSlot> slotsByTime = new HashMap<>();

    // Modernised CSS Styles for visual feedback
    private final String STYLE_UNAVAILABLE = "-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-cursor: hand;";
    private final String STYLE_UNAVAILABLE_HOVER = "-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-cursor: hand;";

    private final String STYLE_AVAILABLE = "-fx-background-color: white; -fx-border-color: #10b981; -fx-border-width: 2; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #10b981; -fx-font-weight: bold; -fx-cursor: hand;";
    private final String STYLE_AVAILABLE_HOVER = "-fx-background-color: #ecfdf5; -fx-border-color: #059669; -fx-border-width: 2; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #059669; -fx-font-weight: bold; -fx-cursor: hand;";

    private final String STYLE_BOOKED = "-fx-background-color: #fffbeb; -fx-border-color: #f59e0b; -fx-border-width: 2; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #d97706; -fx-font-weight: bold; -fx-cursor: hand;";
    private final String STYLE_BOOKED_HOVER = "-fx-background-color: #fef3c7; -fx-border-color: #d97706; -fx-border-width: 2; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #b45309; -fx-font-weight: bold; -fx-cursor: hand;";

    private final String STYLE_SELECTED = "-fx-background-color: #3b82f6; -fx-border-color: #2563eb; -fx-border-width: 2; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: white; -fx-font-weight: bold; -fx-effect: dropshadow(three-pass-box, rgba(59,130,246,0.3), 5, 0, 0, 2);";

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    @FXML
    public void initialize() {
        // We leave this empty because we need the Date and Username first.
        // The setup happens in setupSchedule() instead.
    }

    /**
     * Called by the CalendarController right after loading the FXML.
     */
    public void setupSchedule(String clinicUsername, LocalDate date) {
        this.clinicUsername = clinicUsername;
        this.currentDate = date;
        refreshDatabaseSlots();
        populateTimetable();
    }

    private void refreshDatabaseSlots() {
        slotsByTime.clear();
        List<AppointmentSlot> slots = ScheduleDatabaseHandler.getSlotsForDate(clinicUsername,
                currentDate.toString());
        for (AppointmentSlot slot : slots) {
            LocalTime time = LocalTime.parse(slot.getTime());
            slotsByTime.put(time, slot);
        }
    }

    private void populateTimetable() {
        if (timetableGrid == null)
            return;

        timetableGrid.getChildren().clear();
        timetableGrid.getRowConstraints().clear();
        timetableGrid.setVgap(12); // Added gap for visual breathing room
        timetableGrid.setHgap(20);

        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        int intervalMinutes = 30;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        int rowNum = 0;
        LocalTime currentTime = startTime;

        while (!currentTime.isAfter(endTime)) {
            RowConstraints row = new RowConstraints();
            row.setMinHeight(45); // Slightly tweaked for modern proportions
            timetableGrid.getRowConstraints().add(row);

            Label timeLabel = new Label(currentTime.format(formatter));
            timeLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #475569;");
            timetableGrid.add(timeLabel, 0, rowNum);

            Button slotButton = new Button();
            slotButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            slotButton.setPrefHeight(40);

            // Set initial visual state based on database
            updateButtonVisuals(slotButton, currentTime);

            // Handle hover states and selection
            final LocalTime slotTime = currentTime;

            slotButton.setOnMouseEntered(e -> {
                if (currentlySelectedButton != slotButton) {
                    applyHoverStyle(slotButton, slotTime);
                }
            });

            slotButton.setOnMouseExited(e -> {
                if (currentlySelectedButton != slotButton) {
                    updateButtonVisuals(slotButton, slotTime);
                }
            });

            slotButton.setOnAction(e -> handleSlotSelection(slotButton, slotTime));

            timetableGrid.add(slotButton, 1, rowNum);

            currentTime = currentTime.plusMinutes(intervalMinutes);
            rowNum++;
        }
    }

    private void applyHoverStyle(Button button, LocalTime time) {
        AppointmentSlot slot = slotsByTime.get(time);
        if (slot == null) {
            button.setStyle(STYLE_UNAVAILABLE_HOVER);
        } else if (slot.isBooked()) {
            button.setStyle(STYLE_BOOKED_HOVER);
        } else {
            button.setStyle(STYLE_AVAILABLE_HOVER);
        }
    }

    private void handleSlotSelection(Button clickedButton, LocalTime time) {
        // 1. Revert the previously selected button to its correct state
        if (currentlySelectedButton != null && currentlySelectedButton != clickedButton) {
            updateButtonVisuals(currentlySelectedButton, currentlySelectedTime);
        }

        // 2. Set the newly clicked button to the "Selected" visual state
        currentlySelectedButton = clickedButton;
        currentlySelectedTime = time;
        currentlySelectedButton.setStyle(STYLE_SELECTED);

        // Update text to show it's selected, highlighting the patient's name
        AppointmentSlot slot = slotsByTime.get(time);
        if (slot == null) {
            currentlySelectedButton.setText("Selected to Open (Currently Closed)");
        } else if (slot.isBooked()) {
            String patient = (slot.getPatientUsername() == null || slot.getPatientUsername().isBlank())
                    ? "Unknown patient"
                    : slot.getPatientUsername();
            currentlySelectedButton.setText("Locked: Booked by " + patient.toUpperCase());
        } else {
            currentlySelectedButton.setText("Selected to Close (Currently Open)");
        }
    }

    private void handleToggleAvailability() {
        if (currentlySelectedTime == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a time slot first.");
            return;
        }

        String timeStr = currentlySelectedTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        boolean success;
        AppointmentSlot slot = slotsByTime.get(currentlySelectedTime);

        // Prevent modification of a booked slot
        if (slot != null && slot.isBooked()) {
            String patient = (slot.getPatientUsername() == null || slot.getPatientUsername().isBlank())
                    ? "Unknown patient"
                    : slot.getPatientUsername();
            showAlert(Alert.AlertType.INFORMATION, "Slot Already Booked",
                    "This slot has already been booked by " + patient + ". You cannot close it.");
            updateButtonVisuals(currentlySelectedButton, currentlySelectedTime);
            currentlySelectedButton = null;
            currentlySelectedTime = null;
            return;
        }

        if (slot != null) {
            // Existing unbooked slot: remove availability.
            success = ScheduleDatabaseHandler.deleteSlot(slot.getId(), clinicUsername);
        } else {
            // Missing slot row: create availability.
            success = ScheduleDatabaseHandler.addAvailableSlot(clinicUsername, currentDate.toString(), timeStr);
        }

        if (success) {
            // Bug Fix: Fully refresh the timetable grid to ensure DB sync
            refreshDatabaseSlots();
            populateTimetable();
            currentlySelectedButton = null;
            currentlySelectedTime = null;
        } else {
            showAlert(Alert.AlertType.ERROR, "Update Failed",
                    "Could not update slot availability. Please check your connection and try again.");
            updateButtonVisuals(currentlySelectedButton, currentlySelectedTime);
            currentlySelectedButton = null;
            currentlySelectedTime = null;
        }
    }

    @FXML
    private void handleConfirmAvailability() {
        handleToggleAvailability();
    }

    private void updateButtonVisuals(Button button, LocalTime time) {
        AppointmentSlot slot = slotsByTime.get(time);
        button.setWrapText(true);

        if (slot == null) {
            button.setStyle(STYLE_UNAVAILABLE);
            button.setText("Closed (Click to Open)");
            return;
        }

        if (slot.isBooked()) {
            String patient = (slot.getPatientUsername() == null || slot.getPatientUsername().isBlank())
                    ? "Unknown"
                    : slot.getPatientUsername();
            button.setStyle(STYLE_BOOKED);
            // Feature implementation: Prominently display the patient username
            button.setText("Booked by: " + patient.toUpperCase());
            return;
        }

        // Slot is open but not booked
        button.setStyle(STYLE_AVAILABLE);
        button.setText("Open for Booking (Click to Close)");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void backToCalendar(ActionEvent e) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(getClass().getResource("/fxmlFiles/monthCalenderUI.fxml")));
        Parent root = loader.load();

        CalendarController calendarController = loader.getController();
        calendarController.setAppController(appController);

        // Swap the root node instead of creating a brand new Scene
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
    }
}
