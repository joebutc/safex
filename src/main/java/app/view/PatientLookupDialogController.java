package app.view;

import app.AppController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PatientLookupDialogController {
    @FXML private TextField usernameField;
    @FXML private Label errorLabel;
    @FXML private Button searchButton;

    @FXML
    private void handleSearch() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        if (username.isBlank()) {
            errorLabel.setText("Please enter a patient username.");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            return;
        }

        close();
        AppController.getPatientTreatment(username);
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        ((Stage) searchButton.getScene().getWindow()).close();
    }
}
