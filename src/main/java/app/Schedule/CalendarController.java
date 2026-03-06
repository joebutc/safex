package app.Schedule;

import app.AppController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;

public class CalendarController {
    private YearMonth displayedMonth = YearMonth.now();
    private AppController appController;

    @FXML
    private GridPane calendarGrid;
    @FXML
    private Label monthLabel;

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    @FXML
    public void initialize() {
        populateCalendar(displayedMonth);
    }

    public void populateCalendar(YearMonth yearMonth) {
        calendarGrid.getChildren().clear();

        String monthName = yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        monthLabel.setText(monthName + " " + displayedMonth.getYear());

        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();

        int dayOfWeekValue = firstOfMonth.getDayOfWeek().getValue() - 1;

        int currentRow = 0;
        int currentColumn = dayOfWeekValue;

        for (int day = 1; day <= daysInMonth; day++) {
            Button dayButton = new Button(String.valueOf(day));
            dayButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            // We need to capture the specific day for the event handler
            final int selectedDay = day;
            dayButton.setOnAction(e -> openDaySchedule(e, yearMonth.atDay(selectedDay)));

            calendarGrid.add(dayButton, currentColumn, currentRow);

            currentColumn++;
            if (currentColumn > 6) {
                currentColumn = 0;
                currentRow++;
            }
        }
    }

    public void openDaySchedule(ActionEvent e, LocalDate selectedDate) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/fxmlFiles/dayCalender.fxml")));
            Parent root = loader.load();
            DoctorScheduleController dayController = loader.getController();
            dayController.setAppController(appController);

            String clinicUsername = null;
            if (appController != null) {
                clinicUsername = appController.getCurrentUser();
            }
            if (clinicUsername == null || clinicUsername.isBlank()) {
                System.out.println("Cannot open clinic schedule: logged-in clinic username is unavailable.");
                return;
            }

            dayController.setupSchedule(clinicUsername, selectedDate);

            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @FXML
    private void handleHome() {
        if (appController != null) {
            appController.goToDashboard();
        }
    }

    @FXML
    public void handlePreviousMonth() {
        displayedMonth = displayedMonth.minusMonths(1);
        updateUI();
    }

    @FXML
    public void handleNextMonth() {
        displayedMonth = displayedMonth.plusMonths(1);
        updateUI();
    }

    private void updateUI() {
        String monthName = displayedMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        monthLabel.setText(monthName + " " + displayedMonth.getYear());
        populateCalendar(displayedMonth);
    }
}
