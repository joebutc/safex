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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Window; // Added for the window size fix

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

    private final String STYLE_UNAVAILABLE = "-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #94a3b8; -fx-font-weight: bold;";
    private final String STYLE_AVAILABLE = "-fx-background-color: white; -fx-border-color: #10b981; -fx-border-width: 2; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #10b981; -fx-font-weight: bold; -fx-cursor: hand;";
    private final String STYLE_AVAILABLE_HOVER = "-fx-background-color: #ecfdf5; -fx-border-color: #10b981; -fx-border-width: 2; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #047857; -fx-font-weight: bold; -fx-cursor: hand;";
    private final String STYLE_SELECTED = "-fx-background-color: #3b82f6; -fx-border-color: #2563eb; -fx-border-width: 2; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: white; -fx-font-weight: bold; -fx-effect: dropshadow(three-pass-box, rgba(59,130,246,0.3), 5, 0, 0, 2);";

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
        root.setPrefSize(800, 600); // Fallback size

        root.setTop(buildHeader());
        root.setCenter(buildCenter());

        refreshDatabaseSlots();
        populateTimetable();

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

        Button backBtn = new Button("Back to Calendar");
        backBtn.setStyle(
                "-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 16 8 16;");
        backBtn.setTextFill(Color.web("#475569"));
        backBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(
                "-fx-background-color: #e2e8f0; -fx-border-color: #94a3b8; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 16 8 16;"));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(
                "-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 16 8 16;"));
        backBtn.setOnAction(e -> controller.goToPatientCalendar());

        Label title = new Label("Available Appointments");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        title.setTextFill(Color.web("#0f172a"));
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        HBox.setHgrow(title, Priority.ALWAYS);

        Label placeholder = new Label("Back to Calendar");
        placeholder.setVisible(false);
        placeholder.setManaged(true);
        placeholder.setPadding(new Insets(8, 16, 8, 16));

        header.getChildren().addAll(backBtn, title, placeholder);
        return header;
    }

    private ScrollPane buildCenter() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
                "-fx-background-color: transparent; -fx-background: #F8FAFC; -fx-padding: 0; -fx-background-insets: 0;");

        VBox vbox = new VBox(25);
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.setPadding(new Insets(40, 40, 60, 40));

        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy");
        Label infoLabel = new Label("Booking for: " + currentDate.format(displayFormatter));
        infoLabel.setTextFill(Color.web("#334155"));
        infoLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        timetableGrid = new GridPane();
        timetableGrid.setAlignment(Pos.CENTER);
        timetableGrid.setHgap(30);
        timetableGrid.setVgap(15);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHalignment(HPos.RIGHT);
        col1.setPercentWidth(40);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHalignment(HPos.LEFT);
        col2.setPercentWidth(60);

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

        currentlySelectedButton = null;
        currentlySelectedTime = null;

        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        int rowNum = 0;
        LocalTime currentTime = startTime;

        while (!currentTime.isAfter(endTime)) {
            Label timeLabel = new Label(currentTime.format(formatter));
            timeLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 15));
            timeLabel.setTextFill(Color.web("#475569"));
            timetableGrid.add(timeLabel, 0, rowNum);

            Button slotButton = new Button();
            slotButton.setPrefWidth(220);
            slotButton.setPrefHeight(40);

            final LocalTime slotTime = currentTime;
            updateButtonVisuals(slotButton, slotTime);

            if (availableSlotsMap.containsKey(slotTime)) {
                slotButton.setOnMouseEntered(e -> {
                    if (currentlySelectedButton != slotButton) {
                        slotButton.setStyle(STYLE_AVAILABLE_HOVER);
                    }
                });
                slotButton.setOnMouseExited(e -> {
                    if (currentlySelectedButton != slotButton) {
                        slotButton.setStyle(STYLE_AVAILABLE);
                    }
                });

                slotButton.setOnAction(e -> handleSlotSelection(slotButton, slotTime));
            }

            timetableGrid.add(slotButton, 1, rowNum);
            currentTime = currentTime.plusMinutes(30);
            rowNum++;
        }

        Button bookButton = new Button("Confirm Booking");
        bookButton.setPrefWidth(250);
        bookButton.setPrefHeight(45);
        String bookBaseStyle = "-fx-background-color: #f97316; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(249,115,22,0.3), 8, 0, 0, 3);";
        String bookHoverStyle = "-fx-background-color: #ea580c; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(234,88,12,0.4), 10, 0, 0, 4);";

        bookButton.setStyle(bookBaseStyle);
        bookButton.setOnMouseEntered(e -> bookButton.setStyle(bookHoverStyle));
        bookButton.setOnMouseExited(e -> bookButton.setStyle(bookBaseStyle));
        bookButton.setOnAction(e -> handleBooking());

        timetableGrid.add(bookButton, 0, rowNum, 2, 1);
        GridPane.setHalignment(bookButton, HPos.CENTER);
        GridPane.setMargin(bookButton, new Insets(30, 0, 0, 0));
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
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a time slot before confirming.");
            return;
        }
        int slotId = availableSlotsMap.get(currentlySelectedTime);
        if (ScheduleDatabaseHandler.bookSlot(slotId, patientUsername)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Appointment booked successfully!");
            controller.goToDashboard();
        } else {
            showAlert(Alert.AlertType.ERROR, "Booking Failed",
                    "This slot is no longer available. The page will now refresh so you can choose another slot.");
            refreshDatabaseSlots();
            populateTimetable();
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