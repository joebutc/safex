package app.view;

import app.AppController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

public class TreatmentPermissionsDialogController {
    @FXML private CheckBox remindersCheckBox;
    @FXML private CheckBox clinicCheckBox;
    @FXML private Button saveButton;

    @FXML
    private void initialize() {
        boolean[] perms = AppController.getTreatmentPermissions();
        remindersCheckBox.setSelected(perms[0]);
        clinicCheckBox.setSelected(perms[1]);
    }

    @FXML
    private void handleSave() {
        AppController.saveTreatmentPermissions(
                remindersCheckBox.isSelected(),
                clinicCheckBox.isSelected()
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
