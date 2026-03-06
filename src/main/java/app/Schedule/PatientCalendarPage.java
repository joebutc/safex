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
        root.setPrefSize(800, 600);

        root.setTop(buildHeader());
        root.setCenter(buildCenter());

        populateCalendar(displayedMonth);

        return new Scene(root);
    }

    private HBox buildHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 20, 0, 20));
        header.setPrefHeight(90);
        header.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

        Button backBtn = new Button("Back to Dashboard");
        backBtn.setStyle(
                "-fx-background-color: transparent; -fx-border-color: #cbd5e1; -fx-border-radius: 5; -fx-cursor: hand;");
        backBtn.setTextFill(Color.web("#475569"));
        backBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        backBtn.setOnAction(e -> controller.goToDashboard());

        HBox monthControls = new HBox(12);
        monthControls.setAlignment(Pos.CENTER);

        Button prevBtn = new Button("<");
        prevBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        prevBtn.setTextFill(Color.web("#64748b"));
        prevBtn.setFont(Font.font("System", FontWeight.BOLD, 18));
        prevBtn.setOnAction(e -> handlePreviousMonth());

        monthLabel = new Label("Month Year");
        monthLabel.setAlignment(Pos.CENTER);
        monthLabel.setPrefWidth(200);
        monthLabel.setTextFill(Color.web("#0f172a"));
        monthLabel.setFont(Font.font("System", FontWeight.BOLD, 20));

        Button nextBtn = new Button(">");
        nextBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        nextBtn.setTextFill(Color.web("#64748b"));
        nextBtn.setFont(Font.font("System", FontWeight.BOLD, 18));
        nextBtn.setOnAction(e -> handleNextMonth());

        selectedClinicLabel = new Label();
        selectedClinicLabel.setTextFill(Color.web("#475569"));
        selectedClinicLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));

        clinicSelector = new ComboBox<>();
        clinicSelector.getItems().addAll(clinicUsernames);
        clinicSelector.setValue(selectedClinicUsername);
        clinicSelector.setOnAction(e -> {
            selectedClinicUsername = clinicSelector.getValue();
            updateSelectedClinicLabel();
            populateCalendar(displayedMonth);
        });

        HBox clinicControls = new HBox(8, new Label("Clinic"), clinicSelector, selectedClinicLabel);
        clinicControls.setAlignment(Pos.CENTER);

        VBox centerControls = new VBox(6, monthControls, clinicControls);
        centerControls.setAlignment(Pos.CENTER);
        HBox.setHgrow(centerControls, Priority.ALWAYS);

        updateSelectedClinicLabel();
        monthControls.getChildren().addAll(prevBtn, monthLabel, nextBtn);
        header.getChildren().addAll(backBtn, centerControls);

        return header;
    }

    private VBox buildCenter() {
        VBox centerBox = new VBox(10);
        centerBox.setAlignment(Pos.TOP_CENTER);
        centerBox.setPadding(new Insets(20, 40, 20, 40));

        GridPane daysHeader = new GridPane();
        daysHeader.setAlignment(Pos.CENTER);
        daysHeader.setHgap(10);
        daysHeader.setVgap(10);

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
            dayLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
            daysHeader.add(dayLbl, i, 0);
        }
        RowConstraints row = new RowConstraints();
        row.setMinHeight(10);
        row.setPrefHeight(40);
        daysHeader.getRowConstraints().add(row);

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

        String monthName = yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        monthLabel.setText(monthName + " " + displayedMonth.getYear());

        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int dayOfWeekValue = firstOfMonth.getDayOfWeek().getValue() - 1;

        int currentRow = 0;
        int currentColumn = dayOfWeekValue;

        for (int day = 1; day <= daysInMonth; day++) {
            Button dayButton = new Button(String.valueOf(day));
            dayButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            final int selectedDay = day;
            final String clinicForSelection = selectedClinicUsername;
            if (clinicForSelection == null || clinicForSelection.isBlank()) {
                dayButton.setDisable(true);
            } else {
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
