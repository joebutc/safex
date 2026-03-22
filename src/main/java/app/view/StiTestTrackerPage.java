package app.view;

import app.AppController;
import app.model.StiTestEntry;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class StiTestTrackerPage {
    @FXML private VBox testListContainer;
    @FXML private Label emptyStateLabel;
    @FXML private Button homeButton;
    @FXML private Button addButton;

    @FXML
    private void initialize() {
        refreshTestList();
        homeButton.setOnAction(e -> AppController.goToHomeDashboard());
        addButton.setOnAction(e -> showAddDialog());
    }

    private void refreshTestList() {
        testListContainer.getChildren().clear();

        List<StiTestEntry> tests = AppController.getStiTests();
        boolean isEmpty = tests.isEmpty();
        emptyStateLabel.setManaged(isEmpty);
        emptyStateLabel.setVisible(isEmpty);

        for (StiTestEntry test : tests) {
            testListContainer.getChildren().add(loadTestRow(test));
        }
    }

    private Parent loadTestRow(StiTestEntry test) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/StiTestRow.fxml"));
            Parent row = loader.load();
            StiTestRowController controller = loader.getController();
            controller.configure(test, () -> confirmDelete(test));
            return row;
        } catch (IOException e) {
            throw new RuntimeException("Could not load STI tracker row", e);
        }
    }

    private void confirmDelete(StiTestEntry test) {
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
    }

    private void showAddDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/TrackerDialog.fxml"));
            Parent root = loader.load();
            TrackerDialog dialog = loader.getController();
            dialog.setParent(this);

            Scene dialogScene = new Scene(root);
            AppStyles.apply(dialogScene);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(dialogScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        refreshTestList();
    }
}
