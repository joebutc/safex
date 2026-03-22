package app.view;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class PartnerContactRowController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;

    private Runnable onDelete;

    public void setOnDelete(Runnable deleteAction) {
        onDelete = deleteAction;
    }

    public String getNameValue() {
        return nameField.getText();
    }

    public String getEmailValue() {
        return emailField.getText();
    }

    @FXML
    private void handleDelete() {
        if (onDelete != null) {
            onDelete.run();
        }
    }
}
