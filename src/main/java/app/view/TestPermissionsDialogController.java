package app.view;

import app.AppController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

public class TestPermissionsDialogController {
    private static final String[] INTERVAL_OPTIONS = {
            "Every month", "Every 3 months", "Every 6 months", "Every 12 months"
    };
    private static final int[] INTERVAL_VALUES = {1, 3, 6, 12};

    @FXML private CheckBox remindersCheckBox;
    @FXML private ComboBox<String> intervalCombo;
    @FXML private CheckBox shareCheckBox;
    @FXML private Button saveButton;

    @FXML
    private void initialize() {
        intervalCombo.getItems().addAll(INTERVAL_OPTIONS);

        boolean[] perms = AppController.getTestPermissions();
        int currentInterval = AppController.getReminderIntervalMonths();

        remindersCheckBox.setSelected(perms[0]);
        shareCheckBox.setSelected(perms[1]);

        String selectedInterval = INTERVAL_OPTIONS[1];
        for (int i = 0; i < INTERVAL_VALUES.length; i++) {
            if (INTERVAL_VALUES[i] == currentInterval) {
                selectedInterval = INTERVAL_OPTIONS[i];
                break;
            }
        }
        intervalCombo.setValue(selectedInterval);
        intervalCombo.disableProperty().bind(remindersCheckBox.selectedProperty().not());
    }

    @FXML
    private void handleSave() {
        int chosenInterval = 3;
        for (int i = 0; i < INTERVAL_OPTIONS.length; i++) {
            if (INTERVAL_OPTIONS[i].equals(intervalCombo.getValue())) {
                chosenInterval = INTERVAL_VALUES[i];
                break;
            }
        }

        AppController.saveTestPermissions(
                remindersCheckBox.isSelected(),
                shareCheckBox.isSelected(),
                chosenInterval
        );
        close();
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        ((Stage) saveButton.getScene().getWindow()).close();
    }
}
