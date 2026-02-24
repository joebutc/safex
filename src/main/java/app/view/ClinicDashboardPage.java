package app.view;

import app.AppController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ClinicDashboardPage implements Page {
    private final AppController controller;

    public ClinicDashboardPage(AppController controller) {
        this.controller = controller;
    }

    @Override
    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F8FAFC;");
        root.setMinSize(900, 650);

        root.setTop(buildHeader());

        VBox content = new VBox(18);
        content.setPadding(new Insets(24));

        FlowPane cards = new FlowPane();
        cards.setHgap(18);
        cards.setVgap(18);
        cards.setAlignment(Pos.TOP_CENTER);
        cards.prefWrapLengthProperty().bind(root.widthProperty().subtract(48));
        cards.getChildren().addAll(
                buildScheduleCard(),
                buildLegalCard()
        );

        VBox.setVgrow(cards, Priority.ALWAYS);
        content.getChildren().add(cards);
        root.setCenter(content);

        HBox footer = new HBox(10);
        footer.setPadding(new Insets(12, 24, 18, 24));
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getChildren().add(new Label("SafeX Clinic Portal"));
        root.setBottom(footer);

        return new Scene(root, AppStyles.SCENE_WIDTH, AppStyles.SCENE_HEIGHT);
    }

    private BorderPane buildHeader() {
        BorderPane header = new BorderPane();
        header.getStyleClass().add("app-header");

        Label appTitle = new Label("SafeX Clinic");
        appTitle.setFont(Font.font("System", FontWeight.BOLD, 22));
        appTitle.setTextFill(Color.web("#0F172A"));

        Label welcome = new Label(buildWelcomeText());
        welcome.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        welcome.setTextFill(Color.web("#334155"));

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("secondary-button");
        logoutButton.setOnAction(event -> {
            if (controller != null) {
                controller.goToLogin(LoginPage.LoginType.CLINIC);
                return;
            }
            showAlert(Alert.AlertType.WARNING, "Logout unavailable", "Controller is not available.");
        });

        header.setLeft(appTitle);
        header.setCenter(welcome);
        header.setRight(logoutButton);
        BorderPane.setAlignment(appTitle, Pos.CENTER_LEFT);
        BorderPane.setAlignment(welcome, Pos.CENTER);
        BorderPane.setAlignment(logoutButton, Pos.CENTER_RIGHT);
        return header;
    }

    private String buildWelcomeText() {
        if (controller == null) {
            return "Clinic dashboard";
        }
        String user = controller.getCurrentUser();
        if (user == null || user.isBlank()) {
            return "Clinic dashboard";
        }
        return "Clinic dashboard, " + user;
    }

    private VBox buildScheduleCard() {
        NodeIcon icon = NodeIcon.svg(
                "M19 3h-1V1h-2v2H8V1H6v2H5a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2V5a2 2 0 00-2-2zm0 16H5V8h14v11zm0-13H5V5h14v1z",
                "#DBEAFE",
                "#1D4ED8"
        );
        return buildActionCard(
                icon,
                "Schedule GUI",
                "Open the clinic scheduling interface.",
                "Open Schedule",
                () -> {
                    if (controller != null) {
                        controller.goToClinicSchedule();
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Navigation unavailable", "Controller is not available.");
                    }
                },
                true
        );
    }

    private VBox buildLegalCard() {
        NodeIcon icon = NodeIcon.svg(
                "M6 2a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8l-6-6H6zm8 1.5L19.5 9H14V3.5z",
                "#E0E7FF",
                "#4338CA"
        );
        return buildActionCard(
                icon,
                "Terms & Conditions",
                "Review privacy and terms documents.",
                "View T&C",
                () -> {
                    if (controller != null) {
                        controller.goToLegalOnboard();
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Navigation unavailable", "Controller is not available.");
                    }
                },
                false
        );
    }

    private VBox buildActionCard(
            NodeIcon icon,
            String titleText,
            String descriptionText,
            String buttonText,
            Runnable action,
            boolean primary) {

        String baseStyle = "-fx-background-color: #ffffff;"
                + " -fx-background-radius: 18;"
                + " -fx-border-radius: 18;"
                + " -fx-border-color: #E5E7EB;"
                + " -fx-border-width: 1;"
                + " -fx-effect: dropshadow(gaussian, rgba(15, 23, 42, 0.10), 20, 0.2, 0, 6);";
        String primaryAccent = "-fx-border-color: #2563EB; -fx-border-width: 2;";

        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setPrefWidth(400);
        card.setMinWidth(360);
        card.setMaxWidth(520);
        card.setStyle(primary ? (baseStyle + primaryAccent) : baseStyle);

        Label title = new Label(titleText);
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#0F172A"));

        Label description = new Label(descriptionText);
        description.setWrapText(true);
        description.setFont(Font.font("System", 13));
        description.setTextFill(Color.web("#475569"));

        VBox text = new VBox(6, title, description);
        text.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox(12, icon.badge(), text);
        header.setAlignment(Pos.TOP_LEFT);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button button = new Button(buttonText);
        button.getStyleClass().add(primary ? "primary-button" : "secondary-button");
        button.setOnAction(event -> action.run());
        button.setMinHeight(40);
        button.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(header, spacer, button);
        return card;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private record NodeIcon(StackPane badge) {
        static NodeIcon svg(String path, String background, String color) {
            SVGPath icon = new SVGPath();
            icon.setContent(path);
            icon.setFill(Color.web(color));
            icon.setScaleX(0.9);
            icon.setScaleY(0.9);
            return new NodeIcon(buildBadge(icon, background));
        }

        private static StackPane buildBadge(javafx.scene.Node icon, String background) {
            StackPane badge = new StackPane(icon);
            badge.setPrefSize(46, 46);
            badge.setMinSize(46, 46);
            badge.setMaxSize(46, 46);
            badge.setStyle("-fx-background-color: " + background + "; -fx-background-radius: 999;");
            return badge;
        }
    }
}
