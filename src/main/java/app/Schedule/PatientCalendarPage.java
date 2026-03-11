package app.Schedule;

import app.AppController;
import app.view.Page;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Window; // Added for the window size fix

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PatientCalendarPage implements Page {
    private final AppController controller;
    private final String patientUsername;
    private final List<String> clinicUsernames;
    private String selectedClinicUsername;

    private YearMonth displayedMonth = YearMonth.now();
    private GridPane calendarGrid;
    private Label monthLabel;
    private Label selectedClinicLabel;
    private ComboBox<String> clinicSelector;

    public PatientCalendarPage(AppController controller, String patientUsername, List<String> clinicUsernames) {
        this.controller = controller;
        this.patientUsername = patientUsername;
        this.clinicUsernames = clinicUsernames == null ? new ArrayList<>() : new ArrayList<>(clinicUsernames);
        if (!this.clinicUsernames.isEmpty()) {
            this.selectedClinicUsername = this.clinicUsernames.get(0);
        }
    }

    @Override
    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F8FAFC;");
        root.setPrefSize(800, 600); // Fallback size

        root.setTop(buildHeader());
        root.setCenter(buildCenter());

        populateCalendar(displayedMonth);

        // Anti-shrink workaround: Dynamically read the current window dimensions
        double width = 800;
        double height = 600;
        for (Window window : Window.getWindows()) {
            if (window.isShowing() && window.getScene() != null) {
                width = window.getScene().getWidth();
                height = window.getScene().getHeight();
                break;
            }
        }

        return new Scene(root, width, height);
    }

    private HBox buildHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setPrefHeight(90);
        header.setStyle(
                "-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1px 0; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        Button backBtn = new Button("Back to Dashboard");
        backBtn.setStyle(
                "-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 16 8 16;");
        backBtn.setTextFill(Color.web("#475569"));
        backBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(
                "-fx-background-color: #e2e8f0; -fx-border-color: #94a3b8; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 16 8 16;"));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(
                "-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 16 8 16;"));
        backBtn.setOnAction(e -> controller.goToDashboard());

        HBox monthControls = new HBox(15);
        monthControls.setAlignment(Pos.CENTER);

        Button prevBtn = new Button("◄");
        prevBtn.setStyle(
                "-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 5 10 5 10; -fx-background-radius: 4;");
        prevBtn.setTextFill(Color.web("#64748b"));
        prevBtn.setFont(Font.font("System", FontWeight.BOLD, 16));
        prevBtn.setOnMouseEntered(e -> prevBtn.setStyle(
                "-fx-background-color: #f1f5f9; -fx-cursor: hand; -fx-padding: 5 10 5 10; -fx-background-radius: 4;"));
        prevBtn.setOnMouseExited(e -> prevBtn.setStyle(
                "-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 5 10 5 10; -fx-background-radius: 4;"));
        prevBtn.setOnAction(e -> handlePreviousMonth());

        monthLabel = new Label("Month Year");
        monthLabel.setAlignment(Pos.CENTER);
        monthLabel.setPrefWidth(220);
        monthLabel.setTextFill(Color.web("#0f172a"));
        monthLabel.setFont(Font.font("System", FontWeight.BOLD, 22));

        Button nextBtn = new Button("►");
        nextBtn.setStyle(
                "-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 5 10 5 10; -fx-background-radius: 4;");
        nextBtn.setTextFill(Color.web("#64748b"));
        nextBtn.setFont(Font.font("System", FontWeight.BOLD, 16));
        nextBtn.setOnMouseEntered(e -> nextBtn.setStyle(
                "-fx-background-color: #f1f5f9; -fx-cursor: hand; -fx-padding: 5 10 5 10; -fx-background-radius: 4;"));
        nextBtn.setOnMouseExited(e -> nextBtn.setStyle(
                "-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 5 10 5 10; -fx-background-radius: 4;"));
        nextBtn.setOnAction(e -> handleNextMonth());

        selectedClinicLabel = new Label();
        selectedClinicLabel.setTextFill(Color.web("#64748b"));
        selectedClinicLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));

        clinicSelector = new ComboBox<>();
        clinicSelector.getItems().addAll(clinicUsernames);
        clinicSelector.setValue(selectedClinicUsername);
        clinicSelector.setStyle(
                "-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 2;");
        clinicSelector.setOnAction(e -> {
            selectedClinicUsername = clinicSelector.getValue();
            updateSelectedClinicLabel();
            populateCalendar(displayedMonth);
        });

        Label clinicPrefixLbl = new Label("Clinic:");
        clinicPrefixLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
        clinicPrefixLbl.setTextFill(Color.web("#475569"));

        HBox clinicControls = new HBox(10, clinicPrefixLbl, clinicSelector, selectedClinicLabel);
        clinicControls.setAlignment(Pos.CENTER);

        VBox centerControls = new VBox(10, monthControls, clinicControls);
        centerControls.setAlignment(Pos.CENTER);
        HBox.setHgrow(centerControls, Priority.ALWAYS);

        updateSelectedClinicLabel();
        monthControls.getChildren().addAll(prevBtn, monthLabel, nextBtn);
        header.getChildren().addAll(backBtn, centerControls);

        return header;
    }

    private VBox buildCenter() {
        VBox centerBox = new VBox(15);
        centerBox.setAlignment(Pos.TOP_CENTER);
        centerBox.setPadding(new Insets(25, 40, 30, 40));

        GridPane daysHeader = new GridPane();
        daysHeader.setAlignment(Pos.CENTER);
        daysHeader.setHgap(10);

        String[] days = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setMinWidth(10);
            col.setPrefWidth(100);
            col.setHgrow(Priority.SOMETIMES);
            daysHeader.getColumnConstraints().add(col);

            Label dayLbl = new Label(days[i]);
            dayLbl.setMaxWidth(Double.MAX_VALUE);
            dayLbl.setAlignment(Pos.CENTER);
            dayLbl.setTextFill(Color.web("#64748b"));
            dayLbl.setFont(Font.font("System", FontWeight.BOLD, 15));
            daysHeader.add(dayLbl, i, 0);
        }

        calendarGrid = new GridPane();
        calendarGrid.setHgap(10);
        calendarGrid.setVgap(10);
        VBox.setVgrow(calendarGrid, Priority.ALWAYS);

        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setMinWidth(10);
            col.setPrefWidth(100);
            col.setHgrow(Priority.SOMETIMES);
            calendarGrid.getColumnConstraints().add(col);
        }

        centerBox.getChildren().addAll(daysHeader, calendarGrid);
        return centerBox;
    }

    private void populateCalendar(YearMonth yearMonth) {
        calendarGrid.getChildren().clear();
        calendarGrid.getRowConstraints().clear();

        String monthName = yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        monthLabel.setText(monthName + " " + displayedMonth.getYear());

        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int dayOfWeekValue = firstOfMonth.getDayOfWeek().getValue() - 1;

        int numRows = (int) Math.ceil((daysInMonth + dayOfWeekValue) / 7.0);
        for (int i = 0; i < numRows; i++) {
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.ALWAYS);
            calendarGrid.getRowConstraints().add(row);
        }

        int currentRow = 0;
        int currentColumn = dayOfWeekValue;

        for (int day = 1; day <= daysInMonth; day++) {
            Button dayButton = new Button(String.valueOf(day));
            dayButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            dayButton.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));

            final int selectedDay = day;
            final String clinicForSelection = selectedClinicUsername;

            if (clinicForSelection == null || clinicForSelection.isBlank()) {
                dayButton.setDisable(true);
                dayButton.setStyle(
                        "-fx-background-color: #e2e8f0; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #94a3b8;");
            } else {
                String baseStyle = "-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-text-fill: #334155; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.02), 5, 0, 0, 1);";
                String hoverStyle = "-fx-background-color: #f8fafc; -fx-border-color: #94a3b8; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-text-fill: #0f172a; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);";

                dayButton.setStyle(baseStyle);
                dayButton.setOnMouseEntered(e -> dayButton.setStyle(hoverStyle));
                dayButton.setOnMouseExited(e -> dayButton.setStyle(baseStyle));

                dayButton.setOnAction(e -> controller.goToPatientDaySchedule(
                        patientUsername,
                        clinicForSelection,
                        yearMonth.atDay(selectedDay)));
            }

            calendarGrid.add(dayButton, currentColumn, currentRow);

            currentColumn++;
            if (currentColumn > 6) {
                currentColumn = 0;
                currentRow++;
            }
        }
    }

    private void handlePreviousMonth() {
        displayedMonth = displayedMonth.minusMonths(1);
        populateCalendar(displayedMonth);
    }

    private void handleNextMonth() {
        displayedMonth = displayedMonth.plusMonths(1);
        populateCalendar(displayedMonth);
    }

    private void updateSelectedClinicLabel() {
        if (selectedClinicLabel == null) {
            return;
        }
        if (selectedClinicUsername == null || selectedClinicUsername.isBlank()) {
            selectedClinicLabel.setText("(no clinic selected)");
            return;
        }
        selectedClinicLabel.setText("Booking with " + selectedClinicUsername);
    }
}