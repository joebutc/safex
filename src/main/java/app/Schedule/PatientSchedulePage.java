package app.Schedule;

import app.AppController;
import app.model.AppointmentSlot;
import app.model.ScheduleDatabaseHandler;
import app.view.Page;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientSchedulePage implements Page {
    private final AppController controller;
    private final String patientUsername;
    private final String clinicUsername;
    private final LocalDate currentDate;

    private GridPane timetableGrid;
    private Button currentlySelectedButton = null;
    private LocalTime currentlySelectedTime = null;
    private final Map<LocalTime, Integer> availableSlotsMap = new HashMap<>();

    private final String STYLE_UNAVAILABLE = "-fx-background-color: #e0e0e0; -fx-text-fill: grey;";
    private final String STYLE_AVAILABLE = "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;";
    private final String STYLE_SELECTED = "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;";

    // This is the constructor that was missing or undefined
    public PatientSchedulePage(AppController controller, String patientUsername, String clinicUsername,
            LocalDate currentDate) {
        this.controller = controller;
        this.patientUsername = patientUsername;
        this.clinicUsername = clinicUsername;
        this.currentDate = currentDate;
    }

    @Override
    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F8FAFC;");
        root.setPrefSize(800, 600);

        root.setTop(buildHeader());
        root.setCenter(buildCenter());

        refreshDatabaseSlots();
        populateTimetable();

        return new Scene(root);
    }

    private HBox buildHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 20, 0, 20));
        header.setPrefHeight(60);
        header.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

        Button backBtn = new Button("Back to Calendar");
        backBtn.setStyle(
                "-fx-background-color: transparent; -fx-border-color: #cbd5e1; -fx-border-radius: 5; -fx-cursor: hand;");
        backBtn.setTextFill(Color.web("#475569"));
        backBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        backBtn.setOnAction(e -> controller.goToPatientCalendar());

        Label title = new Label("Available Appointments");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        title.setTextFill(Color.web("#0f172a"));
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        HBox.setHgrow(title, Priority.ALWAYS);

        header.getChildren().addAll(backBtn, title);
        return header;
    }

    private ScrollPane buildCenter() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #F8FAFC;");

        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.setPadding(new Insets(40, 40, 40, 40));

        Label infoLabel = new Label("Booking for: " + currentDate.toString());
        infoLabel.setTextFill(Color.web("#475569"));
        infoLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        timetableGrid = new GridPane();
        timetableGrid.setAlignment(Pos.CENTER);
        timetableGrid.setHgap(20);
        timetableGrid.setVgap(10);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHalignment(HPos.RIGHT);
        col1.setPercentWidth(30);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHalignment(HPos.LEFT);
        col2.setPercentWidth(50);

        timetableGrid.getColumnConstraints().addAll(col1, col2);

        vbox.getChildren().addAll(infoLabel, timetableGrid);
        scrollPane.setContent(vbox);

        return scrollPane;
    }

    private void refreshDatabaseSlots() {
        availableSlotsMap.clear();
        List<AppointmentSlot> slots = ScheduleDatabaseHandler.getAvailableSlotsForDate(clinicUsername,
                currentDate.toString());
        for (AppointmentSlot slot : slots) {
            LocalTime time = LocalTime.parse(slot.getTime());
            availableSlotsMap.put(time, slot.getId());
        }
    }

    private void populateTimetable() {
        timetableGrid.getChildren().clear();
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        int rowNum = 0;
        LocalTime currentTime = startTime;

        while (!currentTime.isAfter(endTime)) {
            Label timeLabel = new Label(currentTime.format(formatter));
            timeLabel.setFont(Font.font("System", 14));
            timetableGrid.add(timeLabel, 0, rowNum);

            Button slotButton = new Button();
            slotButton.setPrefWidth(200);
            updateButtonVisuals(slotButton, currentTime);

            final LocalTime slotTime = currentTime;
            if (availableSlotsMap.containsKey(slotTime)) {
                slotButton.setOnAction(e -> handleSlotSelection(slotButton, slotTime));
            } else {
                slotButton.setDisable(true);
            }

            timetableGrid.add(slotButton, 1, rowNum);
            currentTime = currentTime.plusMinutes(30);
            rowNum++;
        }

        Button bookButton = new Button("Confirm Booking");
        bookButton.setPrefWidth(300);
        bookButton.setStyle(
                "-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        bookButton.setOnAction(e -> handleBooking());

        timetableGrid.add(bookButton, 0, rowNum, 2, 1);
        GridPane.setHalignment(bookButton, HPos.CENTER);
        GridPane.setMargin(bookButton, new Insets(20, 0, 0, 0));
    }

    private void handleSlotSelection(Button clickedButton, LocalTime time) {
        if (currentlySelectedButton != null) {
            updateButtonVisuals(currentlySelectedButton, currentlySelectedTime);
        }
        currentlySelectedButton = clickedButton;
        currentlySelectedTime = time;
        currentlySelectedButton.setStyle(STYLE_SELECTED);
        currentlySelectedButton.setText("Selected");
    }

    private void handleBooking() {
        if (currentlySelectedTime == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a time slot.");
            return;
        }
        int slotId = availableSlotsMap.get(currentlySelectedTime);
        if (ScheduleDatabaseHandler.bookSlot(slotId, patientUsername)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Appointment booked!");
            controller.goToDashboard();
        }
    }

    private void updateButtonVisuals(Button button, LocalTime time) {
        if (availableSlotsMap.containsKey(time)) {
            button.setStyle(STYLE_AVAILABLE);
            button.setText("Available");
            button.setDisable(false);
        } else {
            button.setStyle(STYLE_UNAVAILABLE);
            button.setText("Unavailable");
            button.setDisable(true);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}