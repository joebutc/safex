package app.view;

import app.AppController;
import app.model.DataAccessLogEntry;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

public class AccessLogController {
    @FXML private VBox logContainer;
    @FXML private Button closeButton;

    @FXML
    private void initialize() {
        List<DataAccessLogEntry> entries = AppController.getDataAccessLog();

        if (entries.isEmpty()) {
            Label empty = new Label("No access log entries yet.");
            empty.setTextFill(Color.web("#64748B"));
            empty.setFont(Font.font("System", 14));
            logContainer.getChildren().add(empty);
            return;
        }

        for (DataAccessLogEntry entry : entries) {
            logContainer.getChildren().add(buildLogCard(entry));
        }
    }

    private VBox buildLogCard(DataAccessLogEntry entry) {
        Label accessor = new Label(entry.getAccessorUsername() + " (" + entry.getAccessorType() + ")");
        accessor.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        accessor.setTextFill(Color.web("#0F172A"));

        String actionText = formatAccessType(entry.getAccessType());
        if (entry.getDetails() != null && !entry.getDetails().isBlank()) {
            actionText += ": " + entry.getDetails();
        }
        Label action = new Label(actionText);
        action.setFont(Font.font("System", 13));
        action.setTextFill(Color.web("#334155"));
        action.setWrapText(true);

        Label timestamp = new Label(entry.getAccessTimestamp());
        timestamp.setFont(Font.font("System", 12));
        timestamp.setTextFill(Color.web("#94A3B8"));

        VBox card = new VBox(4, accessor, action, timestamp);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8;");
        return card;
    }

    private String formatAccessType(String accessType) {
        return switch (accessType) {
            case "VIEW_TREATMENTS" -> "Viewed treatment plan";
            case "ADD_TREATMENT" -> "Prescribed new treatment";
            case "UPDATE_TREATMENT" -> "Updated treatment";
            case "DISCONTINUE_TREATMENT" -> "Discontinued treatment";
            default -> accessType;
        };
    }

    @FXML
    private void handleClose() {
        ((Stage) closeButton.getScene().getWindow()).close();
    }
}
