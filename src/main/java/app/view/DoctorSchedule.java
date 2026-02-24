package app.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.util.Objects;

public class DoctorSchedule implements Page {


    @Override
    public Scene createScene() {

        try {
            // The loader sees the 'fx:controller' attribute,
            // creates the DoctorScheduleController object, and calls initialize().
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxmlFiles/monthCalenderUI.fxml")));

            return new Scene(root);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


}
