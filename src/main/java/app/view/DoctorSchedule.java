package app.view;

import app.AppController;
import app.model.State;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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

    @FXML private VBox timetableListContainer;
    @FXML private Button confirmButton;

    private final Set<String> offeredTimes = new LinkedHashSet<>();
    private String selectedDate;

    @FXML
    public void initialize() {
        selectedDate = AppController.getSelectedDate();
        offeredTimes.clear();
        if (selectedDate != null) {
            offeredTimes.addAll(AppController.getAvailableTimesForDate(selectedDate));
        }
        populateTimetable();
    }

    private void populateTimetable() {
        timetableListContainer.getChildren().clear();

        if (selectedDate == null) {
            return;
        }

        List<String> bookedTimes = AppController.getBookedTimesForDate(selectedDate);
        LocalDate selectedLocalDate = parseSelectedDate(selectedDate);
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);

        LocalTime current = startTime;
        while (!current.isAfter(endTime)) {
            String timeStr = current.format(TIME_FMT);
            if (bookedTimes.contains(timeStr)) {
                timetableListContainer.getChildren().add(
                        loadSlotRow(timeStr, "Booked", "slot-status-booked", null, null, "primary-button")
                );
            } else if (isPastSlot(selectedLocalDate, current)) {
                timetableListContainer.getChildren().add(
                        loadSlotRow(timeStr, "Past", "slot-status-past", null, null, "primary-button")
                );
            } else if (offeredTimes.contains(timeStr)) {
                timetableListContainer.getChildren().add(
                        loadSlotRow(timeStr, "Offered", "slot-status-offered",
                                "Remove", () -> toggleOffered(timeStr), "secondary-button")
                );
            } else {
                timetableListContainer.getChildren().add(
                        loadSlotRow(timeStr, "Closed", "slot-status-closed",
                                "Offer", () -> toggleOffered(timeStr), "primary-button")
                );
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
            offeredTimes.remove(time);
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
        alert.showAndWait();
    }

    @FXML
    private void backToCalendar() {
        AppController.updateView(State.CALENDER);
    }
}
