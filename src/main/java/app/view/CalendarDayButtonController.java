package app.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class CalendarDayButtonController {
    @FXML private Button dayButton;

    private Runnable onSelect;

    public void configure(int day, Runnable action) {
        dayButton.setText(String.valueOf(day));
        onSelect = action;
    }

    @FXML
    private void handleSelect() {
        if (onSelect != null) {
            onSelect.run();
        }
    }
}
