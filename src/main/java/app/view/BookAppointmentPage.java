package app.view;

import app.AppController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BookAppointmentPage {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final LocalTime START = LocalTime.of(9, 0);
    private static final LocalTime END   = LocalTime.of(17, 0);

    @FXML private DatePicker datePicker;
    @FXML private VBox slotListContainer;

    @FXML
    public void initialize() {
        datePicker.setValue(LocalDate.now());
        loadSlots();
        datePicker.valueProperty().addListener((obs, old, newVal) -> loadSlots());
    }

    private void loadSlots() {
        slotListContainer.getChildren().clear();

        LocalDate date = datePicker.getValue();
        if (date == null) {
            return;
        }

        String dateStr = date.format(DATE_FMT);
        List<String> booked = AppController.getBookedTimesForDate(dateStr);
        List<String> offered = AppController.getAvailableTimesForDate(dateStr);

        LocalTime current = START;
        while (!current.isAfter(END)) {
            String timeStr = current.format(TIME_FMT);
            boolean isBooked = booked.contains(timeStr);
            boolean isPast = date.isEqual(LocalDate.now()) && current.isBefore(LocalTime.now());
            boolean isOffered = offered.contains(timeStr);

            if (isBooked) {
                slotListContainer.getChildren().add(loadSlotRow(
                        timeStr,
                        "Booked",
                        "slot-status-booked",
                        null,
                        null,
                        "primary-button"
                ));
            } else if (isPast) {
                slotListContainer.getChildren().add(loadSlotRow(
                        timeStr,
                        "Past",
                        "slot-status-past",
                        null,
                        null,
                        "primary-button"
                ));
            } else if (isOffered) {
                final String finalTime = timeStr;
                final String finalDate = dateStr;
                slotListContainer.getChildren().add(loadSlotRow(
                        timeStr,
                        "Available",
                        "slot-status-available",
                        "Book",
                        () -> handleBook(finalDate, finalTime),
                        "primary-button"
                ));
            } else {
                slotListContainer.getChildren().add(loadSlotRow(
                        timeStr,
                        "Not Offered",
                        "slot-status-closed",
                        null,
                        null,
                        "primary-button"
                ));
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
            throw new RuntimeException("Could not load appointment slot row", e);
        }
    }

    private void handleBook(String date, String time) {
        if (AppController.bookAppointment(date, time)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Appointment Booked");
            alert.setContentText("Your appointment has been booked for " + date + " at " + time + ".");
            alert.showAndWait();
            loadSlots();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Slot Unavailable");
            alert.setContentText("This slot is not currently offered, or it was just taken. Please choose another time.");
            alert.showAndWait();
            loadSlots();
        }
    }

    @FXML
    private void handleBack() {
        AppController.goToHomeDashboard();
    }
}
