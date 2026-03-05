package app.view;

import app.App;
import app.AppController;
import app.model.State;
import app.model.StiTestEntry;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class StiTestTrackerPage {
    @FXML private VBox testListContainer;
    @FXML private Button homeButton;
    @FXML private Button addButton;

    @FXML
    private void initialize() {
        refreshTestList();
        homeButton.setOnAction(e -> {
            AppController.updateView(State.DASHBOARD);
        });
        addButton.setOnAction(e -> showAddDialog());
    }

    private void refreshTestList() {
        testListContainer.getChildren().clear();

        List<StiTestEntry> tests = AppController.getStiTests();

        if (tests.isEmpty()) {
            testListContainer.getChildren().add(
                    emptyStateLabel("No tests logged yet. Click '+ Log New Test' to add your first entry.")
            );
            return;
        }

        for (StiTestEntry test : tests) {
            testListContainer.getChildren().add(createRow(test));
        }
    }

    private Label emptyStateLabel(String text) {
        Label lbl = new Label(text);
        lbl.setWrapText(true);
        lbl.setTextFill(Color.web("#94A3B8"));
        lbl.setFont(Font.font("System", 14));
        lbl.setPadding(new Insets(24, 0, 0, 4));
        return lbl;
    }

    private HBox createRow(StiTestEntry test) {
        Label dateLabel = new Label(test.getTestDate());
        dateLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        dateLabel.setTextFill(Color.web("#0F172A"));
        dateLabel.setMinWidth(110);

        Label typeLabel = new Label(test.getStiType());
        typeLabel.setFont(Font.font("System", 13));
        typeLabel.setTextFill(Color.web("#334155"));
        typeLabel.setMinWidth(160);

        Label resultLabel = new Label(test.getResult());
        resultLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        resultLabel.setPadding(new Insets(3, 10, 3, 10));
        resultLabel.setTextFill(Color.WHITE);
        resultLabel.setMinWidth(100);
        resultLabel.setAlignment(Pos.CENTER);
        String resultColor = switch (test.getResult()) {
            case "Positive" -> "#DC2626";
            case "Negative" -> "#16A34A";
            case "Pending" -> "#D97706";
            default -> "#6B7280";
        };
        resultLabel.setStyle("-fx-background-color: " + resultColor + "; -fx-background-radius: 999;");

        String notesText = (test.getNotes() != null && !test.getNotes().isBlank()) ? test.getNotes() : "—";
        Label notesLabel = new Label(notesText);
        notesLabel.setFont(Font.font("System", 12));
        notesLabel.setTextFill(Color.web("#64748B"));
        notesLabel.setMaxWidth(Double.MAX_VALUE);
        notesLabel.setWrapText(false);
        HBox.setHgrow(notesLabel, Priority.ALWAYS);

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("secondary-button");
        deleteBtn.setMinHeight(30);
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this test entry?",
                    ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);
            confirm.setTitle("Confirm Delete");
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) {
                    AppController.deleteStiTest(test.getId());
                    refreshTestList();
                }
            });
        });

        HBox row = new HBox(16, dateLabel, typeLabel, resultLabel, notesLabel, deleteBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setStyle("-fx-background-color: #FFFFFF;"
                + " -fx-background-radius: 10;"
                + " -fx-border-color: #E5E7EB;"
                + " -fx-border-radius: 10;"
                + " -fx-border-width: 1;");
        return row;
    }

    private void showAddDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/TrackerDialog.fxml")
            );

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        refreshTestList();
    }
}
