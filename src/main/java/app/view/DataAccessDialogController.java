package app.view;

import app.AppController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

public class DataAccessDialogController {
    @FXML private CheckBox staffCheckBox;
    @FXML private CheckBox analyticsCheckBox;
    @FXML private CheckBox partnersCheckBox;
    @FXML private Button saveButton;

    @FXML
    private void initialize() {
        boolean[] current = AppController.getDataAccessPermissions();
        staffCheckBox.setSelected(current[0]);
        analyticsCheckBox.setSelected(current[1]);
        partnersCheckBox.setSelected(current[2]);
    }

    @FXML
    private void handleSave() {
        AppController.saveDataAccessPermissions(
                staffCheckBox.isSelected(),
                analyticsCheckBox.isSelected(),
                partnersCheckBox.isSelected()
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
