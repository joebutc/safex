package app.view;

import app.AppController;
import app.model.StiTestEntry;
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

public class StiTestTrackerPage implements Page {
    private final AppController controller;
    private VBox testListContainer;

    public StiTestTrackerPage(AppController controller) {
        this.controller = controller;
    }

    @Override
    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F8FAFC;");
        root.setMinSize(900, 650);

        root.setTop(buildHeader());

        VBox content = new VBox(16);
        content.setPadding(new Insets(24));

        Label subtitle = new Label("Keep a private record of your STI testing history. All data is stored locally on this device.");
        subtitle.setFont(Font.font("System", 13));
        subtitle.setTextFill(Color.web("#475569"));
        subtitle.setWrapText(true);

        Button addButton = new Button("+ Log New Test");
        addButton.getStyleClass().add("primary-button");
        addButton.setMinHeight(40);
        addButton.setOnAction(e -> showAddTestDialog());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionsRow = new HBox(12, subtitle, spacer, addButton);
        actionsRow.setAlignment(Pos.CENTER_LEFT);

        // Column headers
        HBox columnHeaders = buildColumnHeaders();

        testListContainer = new VBox(8);
        testListContainer.setAlignment(Pos.TOP_LEFT);
        testListContainer.setPadding(new Insets(0, 0, 16, 0));

        ScrollPane scroll = new ScrollPane(testListContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        content.getChildren().addAll(actionsRow, columnHeaders, scroll);
        VBox.setVgrow(content, Priority.ALWAYS);
        root.setCenter(content);

        refreshTestList();

        return new Scene(root, AppStyles.SCENE_WIDTH, AppStyles.SCENE_HEIGHT);
    }

    private HBox buildHeader() {
        Image homeImage = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/images/Home.png")), 25, 25, true, true);
        ImageView icon = new ImageView(homeImage);
        icon.setFitWidth(28);
        icon.setFitHeight(28);
        icon.setPreserveRatio(true);

        Button homeButton = new Button();
        homeButton.setGraphic(icon);
        homeButton.getStyleClass().add("icon-button");
        homeButton.setFocusTraversable(false);
        homeButton.setOnAction(e -> {
            if (controller != null) controller.goToDashboard();
        });

        Label title = new Label("Track Your Tests");
        title.getStyleClass().add("app-header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(10, homeButton, title, spacer);
        header.getStyleClass().add("app-header");
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private HBox buildColumnHeaders() {
        HBox row = new HBox(16);
        row.setPadding(new Insets(4, 16, 4, 16));
        row.setAlignment(Pos.CENTER_LEFT);

        Label[] labels = {
                colHeader("Date", 110),
                colHeader("Test Type", 160),
                colHeader("Result", 100),
                colHeader("Notes", -1)
        };
        labels[3].setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(labels[3], Priority.ALWAYS);

        row.getChildren().addAll(labels);
        return row;
    }

    private Label colHeader(String text, double width) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        lbl.setTextFill(Color.web("#64748B"));
        if (width > 0) lbl.setMinWidth(width);
        return lbl;
    }

    private void refreshTestList() {
        testListContainer.getChildren().clear();

        if (controller == null) {
            testListContainer.getChildren().add(emptyStateLabel("Controller not available."));
            return;
        }

        List<StiTestEntry> tests = controller.getStiTests();

        if (tests.isEmpty()) {
            testListContainer.getChildren().add(
                    emptyStateLabel("No tests logged yet. Click '+ Log New Test' to add your first entry.")
            );
            return;
        }

        for (StiTestEntry test : tests) {
            testListContainer.getChildren().add(buildTestRow(test));
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

    private HBox buildTestRow(StiTestEntry test) {
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
                if (bt == ButtonType.YES && controller != null) {
                    controller.deleteStiTest(test.getId());
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

    private void showAddTestDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Log New STI Test");

        VBox form = new VBox(12);
        form.setPadding(new Insets(24));
        form.setPrefWidth(420);

        Label header = new Label("Log a New Test");
        header.setFont(Font.font("System", FontWeight.BOLD, 16));
        header.setTextFill(Color.web("#0F172A"));

        // Date
        Label dateLabel = new Label("Test Date");
        dateLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);

        // STI type
        Label typeLabel = new Label("Type of Test");
        typeLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(
                "General STI Panel",
                "HIV",
                "Chlamydia",
                "Gonorrhoea",
                "Syphilis",
                "Herpes (HSV)",
                "HPV",
                "Hepatitis B",
                "Hepatitis C",
                "Other"
        );
        typeCombo.setValue("General STI Panel");
        typeCombo.setMaxWidth(Double.MAX_VALUE);

        // Result
        Label resultLabel = new Label("Result");
        resultLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        ComboBox<String> resultCombo = new ComboBox<>();
        resultCombo.getItems().addAll("Negative", "Positive", "Pending", "Inconclusive");
        resultCombo.setValue("Negative");
        resultCombo.setMaxWidth(Double.MAX_VALUE);

        // Notes
        Label notesLabel = new Label("Notes (optional)");
        notesLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        TextArea notesField = new TextArea();
        notesField.setPromptText("Any additional notes...");
        notesField.setPrefRowCount(3);
        notesField.setWrapText(true);

        // Error
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web("#DC2626"));
        errorLabel.setFont(Font.font("System", 12));
        errorLabel.setVisible(false);

        // Buttons
        Button saveBtn = new Button("Save Test");
        saveBtn.getStyleClass().add("primary-button");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setMinHeight(40);
        saveBtn.setOnAction(e -> {
            if (datePicker.getValue() == null) {
                errorLabel.setText("Please select a date.");
                errorLabel.setVisible(true);
                return;
            }
            String date = datePicker.getValue().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
            String type = typeCombo.getValue();
            String result = resultCombo.getValue();
            String notes = notesField.getText().trim();

            if (controller != null && controller.addStiTest(date, type, result, notes)) {
                dialog.close();
                refreshTestList();
            } else {
                errorLabel.setText("Failed to save. Please try again.");
                errorLabel.setVisible(true);
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary-button");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setOnAction(e -> dialog.close());

        HBox buttons = new HBox(10, cancelBtn, saveBtn);
        HBox.setHgrow(saveBtn, Priority.ALWAYS);
        HBox.setHgrow(cancelBtn, Priority.ALWAYS);

        form.getChildren().addAll(
                header,
                dateLabel, datePicker,
                typeLabel, typeCombo,
                resultLabel, resultCombo,
                notesLabel, notesField,
                errorLabel,
                buttons
        );

        Scene dialogScene = new Scene(form);
        AppStyles.apply(dialogScene);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    @Override
    public void onEnter() {
    }
}
