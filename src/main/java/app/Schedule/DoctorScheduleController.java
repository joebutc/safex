package app.Schedule;

import app.AppController;
import app.model.AppointmentSlot;
import app.model.ScheduleDatabaseHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    // Maps a time to its database ID if it is currently marked as available
    private Map<LocalTime, Integer> availableSlotsMap = new HashMap<>();

    // CSS Styles for visual feedback
    private final String STYLE_DEFAULT = "-fx-background-color: #e0e0e0; -fx-text-fill: black;";
    private final String STYLE_AVAILABLE = "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;";
    private final String STYLE_SELECTED = "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;";

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
        availableSlotsMap.clear();
        List<AppointmentSlot> slots = ScheduleDatabaseHandler.getAvailableSlotsForDate(clinicUsername,
                currentDate.toString());
        for (AppointmentSlot slot : slots) {
            LocalTime time = LocalTime.parse(slot.getTime());
            availableSlotsMap.put(time, slot.getId());
        }
    }

    private void populateTimetable() {
        timetableGrid.getChildren().clear();
        timetableGrid.getRowConstraints().clear();

        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        int intervalMinutes = 30;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        int rowNum = 0;
        LocalTime currentTime = startTime;

        while (!currentTime.isAfter(endTime)) {
            RowConstraints row = new RowConstraints();
            row.setMinHeight(50);
            timetableGrid.getRowConstraints().add(row);

            Label timeLabel = new Label(currentTime.format(formatter));
            timetableGrid.add(timeLabel, 0, rowNum);

            Button slotButton = new Button();
            slotButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            // Set initial visual state based on database
            updateButtonVisuals(slotButton, currentTime);

            // Handle selection
            final LocalTime slotTime = currentTime;
            slotButton.setOnAction(e -> handleSlotSelection(slotButton, slotTime));

            timetableGrid.add(slotButton, 1, rowNum);

            currentTime = currentTime.plusMinutes(intervalMinutes);
            rowNum++;
        }

        // Add a "Toggle Availability" control button at the very bottom
        Button toggleButton = new Button("Make Selected Slot Available / Unavailable");
        toggleButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        toggleButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
        toggleButton.setOnAction(e -> handleToggleAvailability());

        RowConstraints actionRow = new RowConstraints();
        actionRow.setMinHeight(60);
        timetableGrid.getRowConstraints().add(actionRow);
        timetableGrid.add(toggleButton, 0, rowNum, 2, 1); // Spans 2 columns
    }

    private void handleSlotSelection(Button clickedButton, LocalTime time) {
        // 1. Revert the previously selected button to its correct state
        if (currentlySelectedButton != null) {
            updateButtonVisuals(currentlySelectedButton, currentlySelectedTime);
        }

        // 2. Set the newly clicked button to the "Selected" visual state
        currentlySelectedButton = clickedButton;
        currentlySelectedTime = time;
        currentlySelectedButton.setStyle(STYLE_SELECTED);

        // Update text to show it's selected
        boolean isAlreadyAvailable = availableSlotsMap.containsKey(time);
        currentlySelectedButton
                .setText(isAlreadyAvailable ? "Selected (Currently Available)" : "Selected (Currently Unavailable)");
    }

    private void handleToggleAvailability() {
        if (currentlySelectedTime == null) {
            return; // Nothing is selected
        }

        String timeStr = currentlySelectedTime.format(DateTimeFormatter.ofPattern("HH:mm"));

        if (availableSlotsMap.containsKey(currentlySelectedTime)) {
            // It's currently available, so we want to REMOVE it
            int slotId = availableSlotsMap.get(currentlySelectedTime);
            boolean success = ScheduleDatabaseHandler.deleteSlot(slotId, clinicUsername);
            if (success) {
                availableSlotsMap.remove(currentlySelectedTime);
            }
        } else {
            // It's not available, so we want to ADD it
            boolean success = ScheduleDatabaseHandler.addAvailableSlot(clinicUsername, currentDate.toString(), timeStr);
            if (success) {
                refreshDatabaseSlots(); // Refresh to get the new ID generated by the database
            }
        }

        // Revert the button visuals to show the new state, and clear the selection
        updateButtonVisuals(currentlySelectedButton, currentlySelectedTime);
        currentlySelectedButton = null;
        currentlySelectedTime = null;
    }

    private void updateButtonVisuals(Button button, LocalTime time) {
        if (availableSlotsMap.containsKey(time)) {
            button.setStyle(STYLE_AVAILABLE);
            button.setText("Available to Patients");
        } else {
            button.setStyle(STYLE_DEFAULT);
            button.setText("Unavailable");
        }
    }

    @FXML
    private void backToCalendar(ActionEvent e) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(getClass().getResource("/fxmlFiles/monthCalenderUI.fxml")));
        Parent root = loader.load();
        CalendarController calendarController = loader.getController();
        calendarController.setAppController(appController);
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}