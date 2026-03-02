package app.view;

import app.AppController;
import app.model.State;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ClinicDashboardPage {
    @FXML private Label welcomeLabel;
    @FXML private FlowPane cardContainer;

    @FXML
    private void initialize() {
        welcomeLabel.setText(buildWelcomeText());
        buildCards();
    }

    private String buildWelcomeText() {
        String user = AppController.getCurrentUser();
        if (user == null || user.isBlank()) {
            return "Clinic dashboard";
        }
        return "Clinic dashboard, " + user;
    }

    @FXML
    private void handleLogout() {
        AppController.updateView(State.CLINIC_LOGIN);
    }

    private void buildCards() {
        cardContainer.getChildren().clear();
        cardContainer.getChildren().addAll(
                buildScheduleCard(),
                buildPatientTreatmentCard(),
                buildLegalCard()
        );
    }

    private VBox buildScheduleCard() {
        return buildActionCard(
                createIcon(
                        "M19 3h-1V1h-2v2H8V1H6v2H5a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2V5a2 2 0 00-2-2zm0 16H5V8h14v11zm0-13H5V5h14v1z",
                        "#DBEAFE",
                        "#1D4ED8"
                ),
                "Schedule GUI",
                "Open the clinic scheduling interface.",
                "Open Schedule",
                () -> AppController.updateView(State.CLINIC_SCHEDULE),
                true
        );
    }

    private VBox buildPatientTreatmentCard() {
        return buildActionCard(
                createIcon(
                        "M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z",
                        "#FCE7F3",
                        "#9D174D"
                ),
                "Patient Treatments",
                "Look up a patient by username and manage their treatment plan and prescriptions.",
                "Manage Treatments",
                this::showPatientLookupDialog,
                false
        );
    }

    private VBox buildLegalCard() {
        return buildActionCard(
                createIcon(
                        "M6 2a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8l-6-6H6zm8 1.5L19.5 9H14V3.5z",
                        "#E0E7FF",
                        "#4338CA"
                ),
                "Terms & Conditions",
                "Review privacy and terms documents.",
                "View T&C",
                () -> AppController.updateView(State.LEGAL),
                false
        );
    }

    private VBox buildActionCard(
            StackPane icon,
            String titleText,
            String descriptionText,
            String buttonText,
            Runnable action,
            boolean primary) {

        VBox card = getVBox(primary);

        Label title = new Label(titleText);
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#0F172A"));

        Label description = new Label(descriptionText);
        description.setWrapText(true);
        description.setFont(Font.font("System", 13));
        description.setTextFill(Color.web("#475569"));

        VBox text = new VBox(6, title, description);
        text.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox(12, icon, text);
        header.setAlignment(Pos.TOP_LEFT);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button button = new Button(buttonText);
        button.getStyleClass().add(primary ? "primary-button" : "secondary-button");
        button.setOnAction(e -> action.run());
        button.setMinHeight(40);
        button.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(header, spacer, button);
        return card;
    }

    private static VBox getVBox(boolean primary) {
        String baseStyle =
                "-fx-background-color: #ffffff;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-radius: 18;" +
                        "-fx-border-color: #E5E7EB;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.10), 20, 0.2, 0, 6);";

        String primaryAccent = "-fx-border-color: #2563EB; -fx-border-width: 2;";

        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setPrefWidth(400);
        card.setMinWidth(360);
        card.setMaxWidth(520);
        card.setStyle(primary ? baseStyle + primaryAccent : baseStyle);
        return card;
    }

    private StackPane createIcon(String svgPath, String background, String color) {
        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);
        icon.setFill(Color.web(color));
        icon.setScaleX(0.9);
        icon.setScaleY(0.9);

        StackPane badge = new StackPane(icon);
        badge.setPrefSize(46, 46);
        badge.setMinSize(46, 46);
        badge.setMaxSize(46, 46);
        badge.setStyle("-fx-background-color: " + background + "; -fx-background-radius: 999;");
        return badge;
    }

    private void showPatientLookupDialog() {
        TextInputDialog input = new TextInputDialog();
        input.setTitle("Patient Lookup");
        input.setHeaderText("Enter patient username");
        input.setContentText("Username:");

        input.showAndWait().ifPresent(username -> {
            if (!username.isBlank())
                AppController.getPatientTreatment(username.trim());
        });
    }
}
