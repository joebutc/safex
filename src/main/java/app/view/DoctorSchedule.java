package app.view;

import app.AppController;
import app.model.State;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DoctorSchedule {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private VBox timetableListContainer;
    @FXML
    private Button confirmButton;
    @FXML
    private ComboBox<String> startTimeCombo;
    @FXML
    private ComboBox<String> endTimeCombo;

    private final Set<String> offeredTimes = new LinkedHashSet<>();
    private String selectedDate;

    // Default hours if nothing is saved in the database yet
    private LocalTime workingStart = LocalTime.of(9, 0);
    private LocalTime workingEnd = LocalTime.of(17, 0);

    @FXML
    public void initialize() {
        populateTimeDropdowns();
        loadSavedWorkingHours();

        selectedDate = AppController.getSelectedDate();
        offeredTimes.clear();

        if (selectedDate != null) {
            List<String> explicitlySavedSlots = AppController.getAvailableTimesForDate(selectedDate);

            // If the database has no saved slots for this day at all, auto-populate using
            // default working hours
            if (explicitlySavedSlots.isEmpty()) {
                autoFillDefaultHours();
            } else {
                offeredTimes.addAll(explicitlySavedSlots);
            }
        }

        populateTimetable();
    }

    private void populateTimeDropdowns() {
        List<String> times = new ArrayList<>();
        LocalTime time = LocalTime.of(0, 0);
        LocalTime endOfDay = LocalTime.of(23, 30);

        while (!time.isAfter(endOfDay)) {
            times.add(time.format(TIME_FMT));
            if (time.equals(endOfDay))
                break;
            time = time.plusMinutes(30);
        }

        startTimeCombo.getItems().setAll(times);
        endTimeCombo.getItems().setAll(times);
    }

    private void loadSavedWorkingHours() {
        String savedStart = AppController.getClinicDefaultStartTime();
        String savedEnd = AppController.getClinicDefaultEndTime();

        if (savedStart != null && !savedStart.isEmpty()) {
            workingStart = LocalTime.parse(savedStart, TIME_FMT);
        }
        if (savedEnd != null && !savedEnd.isEmpty()) {
            workingEnd = LocalTime.parse(savedEnd, TIME_FMT);
        }

        startTimeCombo.setValue(workingStart.format(TIME_FMT));
        endTimeCombo.setValue(workingEnd.format(TIME_FMT));
    }

    @FXML
    private void handleSaveWorkingHours() {
        String newStart = startTimeCombo.getValue();
        String newEnd = endTimeCombo.getValue();

        workingStart = LocalTime.parse(newStart, TIME_FMT);
        workingEnd = LocalTime.parse(newEnd, TIME_FMT);

        AppController.saveClinicDefaultWorkingHours(newStart, newEnd);
        AppController.applyWorkingHoursToAllFutureDates(newStart, newEnd);

        offeredTimes.clear();
        autoFillDefaultHours();
        populateTimetable();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Working Hours Saved");
        alert.setContentText("Your default working hours have been updated to " + newStart + " - " + newEnd
                + ".\n\nAll future days have been updated. Don't forget to click 'Confirm Daily Changes' to save today's schedule.");
        alert.showAndWait();
    }

    private void autoFillDefaultHours() {
        LocalTime current = workingStart;
        while (!current.isAfter(workingEnd)) {
            offeredTimes.add(current.format(TIME_FMT));
            current = current.plusMinutes(30);
        }
    }

    private void populateTimetable() {
        timetableListContainer.getChildren().clear();

        if (selectedDate == null) {
            return;
        }

        List<String> bookedTimes = AppController.getBookedTimesForDate(selectedDate);
        LocalDate selectedLocalDate = parseSelectedDate(selectedDate);

        LocalTime current = workingStart;
        while (!current.isAfter(workingEnd)) {
            String timeStr = current.format(TIME_FMT);

            if (bookedTimes.contains(timeStr)) {
                timetableListContainer.getChildren().add(
                        loadSlotRow(timeStr, "Booked", "slot-status-booked", null, null, "primary-button"));
            } else if (isPastSlot(selectedLocalDate, current)) {
                timetableListContainer.getChildren().add(
                        loadSlotRow(timeStr, "Past", "slot-status-past", null, null, "primary-button"));
            } else if (offeredTimes.contains(timeStr)) {
                timetableListContainer.getChildren().add(
                        loadSlotRow(timeStr, "Offered", "slot-status-offered",
                                "Remove", () -> toggleOffered(timeStr), "secondary-button"));
            } else {
                timetableListContainer.getChildren().add(
                        loadSlotRow(timeStr, "Closed", "slot-status-closed",
                                "Offer", () -> toggleOffered(timeStr), "primary-button"));
            }
            current = current.plusMinutes(30);
        }
    }

    private Parent loadSlotRow(String time, String status, String statusStyleClass,
            String actionLabel, Runnable action, String actionStyleClass) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/ScheduleSlotRow.fxml"));
            Parent row = loader.load();
            ScheduleSlotRowController controller = loader.getController();
            controller.configure(time, status, statusStyleClass, actionLabel, action, actionStyleClass);
            return row;
        } catch (IOException e) {
            throw new RuntimeException("Could not load clinic schedule row", e);
        }
    }

    private void toggleOffered(String time) {
        if (offeredTimes.contains(time)) {
            offeredTimes.remove(time); // Typo fixed here!
        } else {
            offeredTimes.add(time);
        }
        populateTimetable();
    }

    private LocalDate parseSelectedDate(String date) {
        try {
            return LocalDate.parse(date, DATE_FMT);
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isPastSlot(LocalDate date, LocalTime time) {
        if (date == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return date.isBefore(today) || (date.isEqual(today) && time.isBefore(LocalTime.now()));
    }

    @FXML
    private void handleConfirm() {
        if (selectedDate == null) {
            return;
        }

        boolean ok = AppController.saveAvailableTimesForDate(selectedDate, new ArrayList<>(offeredTimes));
        Alert alert = new Alert(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setHeaderText(ok ? "Availability Updated" : "Update Failed");
        alert.setContentText(ok
                ? "The offered appointment slots for " + selectedDate + " have been saved."
                : "SafeX could not save the offered appointment slots.");

        // This halts the code until the user clicks "OK" on the alert box
        alert.showAndWait();

        // NEW: If the save was successful, automatically go back to the calendar
        if (ok) {
            backToCalendar();
        }
    }

    @FXML
    private void backToCalendar() {
        AppController.updateView(State.CALENDER);
    }
}