package app.view;

import app.model.StiTestEntry;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class StiTestRowController {
    @FXML private Label dateLabel;
    @FXML private Label typeLabel;
    @FXML private Label resultLabel;
    @FXML private Label notesLabel;
    @FXML private Button deleteButton;

    private Runnable onDelete;

    public void configure(StiTestEntry test, Runnable onDeleteAction) {
        dateLabel.setText(test.getTestDate());
        typeLabel.setText(test.getStiType());
        resultLabel.setText(test.getResult());
        notesLabel.setText((test.getNotes() == null || test.getNotes().isBlank()) ? "No notes" : test.getNotes());

        resultLabel.getStyleClass().removeAll(
                "sti-result-positive",
                "sti-result-negative",
                "sti-result-pending",
                "sti-result-default"
        );
        resultLabel.getStyleClass().add(resultStyleClass(test.getResult()));

        onDelete = onDeleteAction;
        deleteButton.setManaged(onDeleteAction != null);
        deleteButton.setVisible(onDeleteAction != null);
    }

    @FXML
    private void handleDelete() {
        if (onDelete != null) {
            onDelete.run();
        }
    }

    private String resultStyleClass(String result) {
        return switch (result) {
            case "Positive" -> "sti-result-positive";
            case "Negative" -> "sti-result-negative";
            case "Pending" -> "sti-result-pending";
            default -> "sti-result-default";
        };
    }
}
