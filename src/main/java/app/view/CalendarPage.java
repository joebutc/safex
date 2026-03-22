package app.view;


import app.AppController;
import app.model.State;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class CalendarPage {
    private YearMonth displayedMonth = YearMonth.now();
    @FXML
    private GridPane calendarGrid;
    @FXML
    private Label monthLabel;
    @FXML
    public void initialize() {
        populateCalendar(displayedMonth);
    }

    public void populateCalendar(YearMonth yearMonth){

        // remove existing buttons.
        calendarGrid.getChildren().clear();

        // Get the Month
        String monthName = yearMonth.getMonth().getDisplayName(TextStyle.FULL,Locale.ENGLISH);
        monthLabel.setText(monthName + " " + displayedMonth.getYear());

        //Get the first day of the month.
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();

        // Monday = column 0 on grid, Sunday is 6
        // getValue() for Monday returns 1 so we -1.
        int dayOfWeekValue = firstOfMonth.getDayOfWeek().getValue() - 1;

        int currentRow = 0;
        int currentColumn = dayOfWeekValue;

        // Loop through and add Buttons.
        for (int day = 1; day <= daysInMonth; day++){
            Node dayButton = loadDayButton(day);
            calendarGrid.add(dayButton,currentColumn,currentRow);

            currentColumn++;
            if (currentColumn > 6) {
                currentColumn = 0;
                currentRow++;
            }

        }

    }

    private Node loadDayButton(int day) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/CalendarDayButton.fxml"));
            Node dayButton = loader.load();
            CalendarDayButtonController controller = loader.getController();
            controller.configure(day, () -> openDaySchedule(day));
            return dayButton;
        } catch (IOException e) {
            throw new RuntimeException("Could not load calendar day button", e);
        }
    }

    public void openDaySchedule(int day) {
        LocalDate date = displayedMonth.atDay(day);
        AppController.setSelectedDate(date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        AppController.updateView(State.CLINIC_SCHEDULE);
    }

    @FXML
    private void handleHome() {
        AppController.goToHomeDashboard();
    }


    @FXML
    public void handlePreviousMonth(){
        displayedMonth = displayedMonth.minusMonths(1);
        updateUI();
    }

    @FXML
    public void handleNextMonth() {
        // Add 1 month to our tracker
        displayedMonth = displayedMonth.plusMonths(1);
        updateUI();
    }


    private void updateUI() {
        // Update the Label text (e.g., "March 2026")
        String monthName = displayedMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        monthLabel.setText(monthName + " " + displayedMonth.getYear());

        // Repopulate the grid using the method we wrote earlier
        populateCalendar(displayedMonth);
    }

}
