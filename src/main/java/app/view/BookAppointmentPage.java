package app.view;

import app.AppController;
import app.model.ClinicAccount;
import app.service.ClinicService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BookAppointmentPage {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final LocalTime START = LocalTime.of(9, 0);
    private static final LocalTime END = LocalTime.of(17, 0);

    @FXML
    private ComboBox<String> clinicSelector;
    @FXML
    private DatePicker datePicker;
    @FXML
    private VBox slotListContainer;

    private final ClinicService clinicService = new ClinicService();

    @FXML
    public void initialize() {
        clinicSelector.getItems().setAll(AppController.getClinicUsernames());
        if (!clinicSelector.getItems().isEmpty()) {
            clinicSelector.setValue(clinicSelector.getItems().get(0));
        }
        datePicker.setValue(LocalDate.now());
        loadSlots();

        clinicSelector.valueProperty().addListener((obs, old, newVal) -> loadSlots());
        datePicker.valueProperty().addListener((obs, old, newVal) -> loadSlots());
    }

    /**
     * Pre-selects a specific clinic and locks the combo box to show it immediately.
     * Use this when navigating from the STI Decision Tree.
     */
    public void setPreselectedClinic(String clinicUsername) {
        if (clinicSelector.getItems().contains(clinicUsername)) {
            clinicSelector.setValue(clinicUsername);
        }
    }

    /**
     * Triggers a postcode popup. Sorts the combo box so local clinics appear
     * at the top and auto-selects the nearest one.
     * Use this when navigating normally from the home dashboard.
     */
    public void promptForLocationAndSort() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Find Local Clinics");
        dialog.setHeaderText("Let's find the clinics closest to you.");
        dialog.setContentText("Please enter your postcode:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(postcode -> {
            List<ClinicAccount> localClinics = clinicService.findClinicsNear(postcode);

            if (!localClinics.isEmpty()) {
                // Extract just the usernames of the local clinics
                List<String> localUsernames = localClinics.stream()
                        .map(ClinicAccount::getUsername)
                        .collect(Collectors.toList());

                // Get all clinics and remove the local ones so we don't have duplicates
                List<String> allUsernames = new ArrayList<>(AppController.getClinicUsernames());
                allUsernames.removeAll(localUsernames);

                // Build a new sorted list: Local clinics at the top, followed by the rest
                List<String> sortedUsernames = new ArrayList<>(localUsernames);
                sortedUsernames.addAll(allUsernames);

                clinicSelector.getItems().setAll(sortedUsernames);
                clinicSelector.setValue(localUsernames.get(0)); // Auto-select the first local match
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No Local Clinics");
                alert.setHeaderText(null);
                alert.setContentText(
                        "We couldn't find any specific partner clinics in your immediate area. Showing all available clinics.");
                alert.showAndWait();
            }
        });
    }

    private void loadSlots() {
        slotListContainer.getChildren().clear();

        String clinicUsername = clinicSelector.getValue();
        LocalDate date = datePicker.getValue();
        if (date == null || clinicUsername == null || clinicUsername.isBlank()) {
            return;
        }

        String dateStr = date.format(DATE_FMT);
        List<String> booked = AppController.getBookedTimesForDate(dateStr, clinicUsername);
        List<String> offered = AppController.getAvailableTimesForDate(dateStr, clinicUsername);

        LocalTime current = START;
        while (!current.isAfter(END)) {
            String timeStr = current.format(TIME_FMT);
            boolean isBooked = booked.contains(timeStr);
            boolean isPast = date.isEqual(LocalDate.now()) && current.isBefore(LocalTime.now());
            boolean isOffered = offered.contains(timeStr);

            if (isBooked) {
                slotListContainer.getChildren().add(loadSlotRow(
                        timeStr, "Booked", "slot-status-booked", null, null, "primary-button"));
            } else if (isPast) {
                slotListContainer.getChildren().add(loadSlotRow(
                        timeStr, "Past", "slot-status-past", null, null, "primary-button"));
            } else if (isOffered) {
                final String finalTime = timeStr;
                final String finalDate = dateStr;
                final String finalClinic = clinicUsername;
                slotListContainer.getChildren().add(loadSlotRow(
                        timeStr, "Available", "slot-status-available", "Book",
                        () -> handleBook(finalClinic, finalDate, finalTime), "primary-button"));
            } else {
                slotListContainer.getChildren().add(loadSlotRow(
                        timeStr, "Not Offered", "slot-status-closed", null, null, "primary-button"));
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

    private void handleBook(String clinicUsername, String date, String time) {
        if (AppController.bookAppointment(clinicUsername, date, time)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Appointment Booked");
            alert.setContentText(
                    "Your appointment with " + clinicUsername + " has been booked for " + date + " at " + time + ".");
            alert.showAndWait();
            loadSlots();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Slot Unavailable");
            alert.setContentText(
                    "This slot is not currently offered, or it was just taken. Please choose another time.");
            alert.showAndWait();
            loadSlots();
        }
    }

    @FXML
    private void handleBack() {
        AppController.goToHomeDashboard();
    }
}