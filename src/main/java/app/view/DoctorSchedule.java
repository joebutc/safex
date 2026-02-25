package app.view;

import app.AppController;
import app.Schedule.CalendarController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.util.Objects;

public class DoctorSchedule implements Page {
    private final AppController controller;

    public DoctorSchedule(AppController controller) {
        this.controller = controller;
    }

    @Override
    public Scene createScene() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/fxmlFiles/monthCalenderUI.fxml")));
            Parent root = loader.load();
            CalendarController calendarController = loader.getController();
            calendarController.setAppController(controller);

            return new Scene(root);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


}
