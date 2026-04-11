package app.view;

import app.AppController;
import app.model.ClinicAccount;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;

public class ClinicLocationDialogController {

    @FXML
    private TextField newPostcodeField;
    @FXML
    private ListView<String> postcodeListView;
    @FXML
    private Button addButton;
    @FXML
    private Button removeButton;
    @FXML
    private Button closeButton;

    private ClinicAccount currentClinic;
    private ObservableList<String> coveredPostcodes;

    @FXML
    public void initialize() {
        // Retrieve the currently logged-in clinic
        if (AppController.getCurrentAccount() instanceof ClinicAccount) {
            currentClinic = (ClinicAccount) AppController.getCurrentAccount();
        } else {
            return; // Failsafe, though this dialog should only be opened by clinics
        }

        // Load existing postcodes into the UI
        coveredPostcodes = FXCollections.observableArrayList();
        if (currentClinic.getCoveredPostcodePrefixes() != null) {
            coveredPostcodes.addAll(currentClinic.getCoveredPostcodePrefixes());
        }
        postcodeListView.setItems(coveredPostcodes);

        // Disable remove button if nothing is selected
        removeButton.setDisable(true);
        postcodeListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> removeButton.setDisable(newSelection == null));
    }

    @FXML
    private void handleAddPostcode() {
        String input = newPostcodeField.getText().trim().toUpperCase();

        if (input.isBlank()) {
            return;
        }

        // Validate for UK Outward Codes (e.g., SO14, M1, SW1A)
        if (!input.matches("^[A-Z]{1,2}[0-9][A-Z0-9]?$")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Format");
            alert.setHeaderText(null);
            alert.setContentText("Please enter a valid outward code (the first half of a postcode, e.g., SO14, SW1A).");
            alert.showAndWait();
            return;
        }

        if (!coveredPostcodes.contains(input)) {
            coveredPostcodes.add(input);
            updateClinicData();
            newPostcodeField.clear();
        }
    }

    @FXML
    private void handleRemovePostcode() {
        String selected = postcodeListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            coveredPostcodes.remove(selected);
            updateClinicData();
        }
    }

    private void updateClinicData() {
        // Save the updated list back to the clinic account
        Set<String> updatedPrefixes = new HashSet<>(coveredPostcodes);
        currentClinic.setCoveredPostcodePrefixes(updatedPrefixes);

        // Tell the main controller to save this change to your system
        AppController.updateClinicAccount(currentClinic);
    }

    @FXML
    private void handleClose() {
        // Close the modal dialog window
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}