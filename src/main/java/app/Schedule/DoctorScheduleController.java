package app.Schedule;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class DoctorScheduleController {
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

            //TODO @ Ryan you might want to add the logic for the clicking regions/selecting times here.
            timetableGrid.add(new Region(),1,rowNum );

            currentTime = currentTime.plusMinutes(intervalMinutes);

            rowNum++;

        }



    }

    // Logic for back Button in day app.Schedule.
    @FXML
    private void backToCalendar(ActionEvent e) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxmlFiles/monthCalenderUI.fxml")));
        Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }




}
