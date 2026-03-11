package app.Schedule;

import app.AppController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;

public class CalendarController {
    private YearMonth displayedMonth = YearMonth.now();
    private AppController appController;

    @FXML
    private GridPane calendarGrid;
    @FXML
    private Label monthLabel;

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    @FXML
    public void initialize() {
        if (monthLabel != null && calendarGrid != null) {
            populateCalendar(displayedMonth);
        }
    }

    public void populateCalendar(YearMonth yearMonth) {
        calendarGrid.getChildren().clear();
        calendarGrid.getRowConstraints().clear(); // Bug fix: Clear old row constraints

        String monthName = yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        monthLabel.setText(monthName + " " + displayedMonth.getYear());

        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int dayOfWeekValue = firstOfMonth.getDayOfWeek().getValue() - 1;

        // Bug fix: Dynamically calculate and add row constraints so buttons stretch
        // vertically
        int numRows = (int) Math.ceil((daysInMonth + dayOfWeekValue) / 7.0);
        for (int i = 0; i < numRows; i++) {
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.ALWAYS);
            calendarGrid.getRowConstraints().add(row);
        }

        int currentRow = 0;
        int currentColumn = dayOfWeekValue;
        LocalDate today = LocalDate.now();

        for (int day = 1; day <= daysInMonth; day++) {
            Button dayButton = new Button(String.valueOf(day));
            dayButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            dayButton.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            final int selectedDay = day;
            LocalDate thisDate = yearMonth.atDay(selectedDay);

            String baseStyle;
            String hoverStyle;

            if (thisDate.equals(today)) {
                // Highlight the current real-world day (e.g., today's date)
                baseStyle = "-fx-background-color: #eff6ff; -fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-text-fill: #1d4ed8; -fx-effect: dropshadow(three-pass-box, rgba(59,130,246,0.2), 5, 0, 0, 1);";
                hoverStyle = "-fx-background-color: #dbeafe; -fx-border-color: #2563eb; -fx-border-width: 2; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-text-fill: #1e3a8a; -fx-effect: dropshadow(three-pass-box, rgba(37,99,235,0.3), 8, 0, 0, 2);";
            } else {
                baseStyle = "-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-text-fill: #334155; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.02), 5, 0, 0, 1);";
                hoverStyle = "-fx-background-color: #f8fafc; -fx-border-color: #94a3b8; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-text-fill: #0f172a; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);";
            }

            dayButton.setStyle(baseStyle);
            dayButton.setOnMouseEntered(e -> dayButton.setStyle(hoverStyle));
            dayButton.setOnMouseExited(e -> dayButton.setStyle(baseStyle));

            dayButton.setOnAction(e -> openDaySchedule(e, thisDate));

            calendarGrid.add(dayButton, currentColumn, currentRow);

            currentColumn++;
            if (currentColumn > 6) {
                currentColumn = 0;
                currentRow++;
            }
        }
    }

    public void openDaySchedule(ActionEvent e, LocalDate selectedDate) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/fxmlFiles/dayCalender.fxml")));
            Parent root = loader.load();
            DoctorScheduleController dayController = loader.getController();
            dayController.setAppController(appController);

            String clinicUsername = null;
            if (appController != null) {
                clinicUsername = appController.getCurrentUser();
            }
            if (clinicUsername == null || clinicUsername.isBlank()) {
                System.out.println("Cannot open clinic schedule: logged-in clinic username is unavailable.");
                return;
            }

            dayController.setupSchedule(clinicUsername, selectedDate);

            // THE FIX: Swap the root node instead of creating a brand new Scene
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @FXML
    private void handleHome() {
        if (appController != null) {
            appController.goToDashboard();
        }
    }

    @FXML
    public void handlePreviousMonth() {
        displayedMonth = displayedMonth.minusMonths(1);
        updateUI();
    }

    @FXML
    public void handleNextMonth() {
        displayedMonth = displayedMonth.plusMonths(1);
        updateUI();
    }

    private void updateUI() {
        populateCalendar(displayedMonth);
    }
}