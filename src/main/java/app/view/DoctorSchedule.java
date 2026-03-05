package app.view;

import app.AppController;
import app.model.State;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DoctorSchedule {
    @FXML
    private GridPane timetableGrid;

    /**
     * The initialize method is automatically called by the FXMLLoader
     * after the FXML file is loaded and the @FXML fields are injected.
     */
    @FXML
    public void initialize() {
        populateTimetable();
    }

    private void populateTimetable() {

        LocalTime startTime = LocalTime.of(9, 0);  // 09:00
        LocalTime endTime = LocalTime.of(17, 0);   // 17:00
        int intervalMinutes = 30;                 // 30-minute steps
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        int rowNum = 0;

        LocalTime currentTime = startTime;

        while (!currentTime.isAfter(endTime)){
            RowConstraints row = new RowConstraints();
            row.setMinHeight(50);
            timetableGrid.getRowConstraints().add(row);

            Label time = new Label(currentTime.format(formatter));

            timetableGrid.add(time, 0, rowNum );

            // Future enhancement: add interaction logic for selecting available time slots.
            timetableGrid.add(new Region(),1,rowNum );

            currentTime = currentTime.plusMinutes(intervalMinutes);

            rowNum++;

        }

    }

    // Logic for back Button in day app.Schedule.
    @FXML
    private void backToCalendar() {
        AppController.updateView(State.CALENDER);
    }

}
