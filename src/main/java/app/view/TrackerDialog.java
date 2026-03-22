package app.view;

import app.AppController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class TrackerDialog {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<String> resultCombo;
    @FXML private TextArea notesField;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private StiTestTrackerPage parent;

    public void setParent(StiTestTrackerPage parent) {
        this.parent = parent;
    }

    @FXML
    private void initialize() {

        typeCombo.getItems().addAll(
                "General STI Panel","HIV","Chlamydia","Gonorrhoea",
                "Syphilis","Herpes (HSV)","HPV","Hepatitis B","Hepatitis C","Other"
        );

        resultCombo.getItems().addAll(
                "Negative","Positive","Pending","Inconclusive"
        );

        typeCombo.setValue("General STI Panel");
        resultCombo.setValue("Negative");

        cancelButton.setOnAction(e ->
                ((Stage) cancelButton.getScene().getWindow()).close()
        );

        saveButton.setOnAction(e -> save());
    }

    private void save() {

        if (datePicker.getValue() == null) {
            errorLabel.setText("Please select a date.");
            errorLabel.setVisible(true);
            return;
        }

        String date = datePicker.getValue()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy"));

        boolean success = AppController.addStiTest(
                date,
                typeCombo.getValue(),
                resultCombo.getValue(),
                notesField.getText()
        );

        if (success) {

            parent.reload();
            ((Stage) saveButton.getScene().getWindow()).close();

        } else {

            errorLabel.setText("Failed to save.");
            errorLabel.setVisible(true);

        }
    }
}