package app.view;


import app.AppController;
import app.model.State;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.time.YearMonth;
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
            Button dayButton = new Button(String.valueOf(day));

            dayButton.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);

            // Styling hook for day buttons can be applied here if needed.
            dayButton.setOnAction(this::openDaySchedule);


            calendarGrid.add(dayButton,currentColumn,currentRow);

            currentColumn++;
            if (currentColumn > 6) {
                currentColumn = 0;
                currentRow++;
            }

        }

    }

    public void openDaySchedule(ActionEvent e)  {
        AppController.updateView(State.CLINIC_SCHEDULE);
    }

    @FXML
    private void handleHome() {
        AppController.updateView(State.DASHBOARD);
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
