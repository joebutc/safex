package app.view;

import app.AppController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

import java.awt.Desktop;
import java.net.URI;
import java.util.Objects;

public class DashboardPage implements Page {
    private final AppController controller;

    public DashboardPage(AppController controller) {
        this.controller = controller;
    }

    public DashboardPage() {
        this(null);
    }

    @Override
    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F8FAFC;");
        root.setMinSize(900, 650);

        BorderPane header = buildHeader();
        root.setTop(header);

        VBox content = new VBox(18);
        content.setPadding(new Insets(24));

        FlowPane cards = new FlowPane();
        cards.setHgap(18);
        cards.setVgap(18);
        cards.setAlignment(Pos.TOP_CENTER);
        cards.prefWrapLengthProperty().bind(root.widthProperty().subtract(48));

        cards.getChildren().addAll(
                buildNotifyCard(),
                buildSettingsCard(),
                buildLegalCard(),
                buildResourcesCard()
        );

        VBox.setVgrow(cards, Priority.ALWAYS);
        content.getChildren().add(cards);
        root.setCenter(content);

        HBox footer = buildFooter();
        root.setBottom(footer);

        return new Scene(root, AppStyles.SCENE_WIDTH, AppStyles.SCENE_HEIGHT);
    }

    private BorderPane buildHeader() {
        BorderPane header = new BorderPane();
        header.getStyleClass().add("app-header");

        Label appTitle = new Label("SafeX");
        appTitle.setFont(Font.font("System", FontWeight.BOLD, 22));
        appTitle.setTextFill(Color.web("#0F172A"));

        Label welcome = new Label(buildWelcomeText());
        welcome.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        welcome.setTextFill(Color.web("#334155"));

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("secondary-button");
        logoutButton.setOnAction(event -> {
            if (controller != null) {
                controller.goToLogin();
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
            return "Welcome back";
        }
        String user = controller.getCurrentUser();
        if (user == null || user.isBlank()) {
            return "Welcome back";
        }
        return "Welcome back, " + user;
    }

    private VBox buildNotifyCard() {
        NodeIcon icon = NodeIcon.image("/images/Bell.png", "#DBEAFE", "#1D4ED8");
        return buildActionCard(
                icon,
                "Notify Partners",
                "Send anonymous notifications to sexual health contacts.",
                "Start Notification",
                () -> navigate("notify"),
                true
        );
    }

    private VBox buildSettingsCard() {
        NodeIcon icon = NodeIcon.svg(
                "M6 10V8a6 6 0 1112 0v2h1a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2v-8a2 2 0 012-2h1V8zm2 2h8V8a4 4 0 00-8 0v2z",
                "#CCFBF1",
                "#0F766E"
        );

        return buildActionCard(
                icon,
                "Privacy & Settings",
                "Manage your data, privacy preferences, and account settings.",
                "Open Settings",
                () -> navigate("settings"),
                false
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
                "Review our privacy policy and terms of service.",
                "View T&C",
                () -> navigate("legal"),
                false
        );
    }

    private VBox buildResourcesCard() {
        NodeIcon icon = NodeIcon.svg(
                "M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41 0.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z",
                "#DCFCE7",
                "#166534"
        );

        return buildActionCard(
                icon,
                "Health Resources",
                "Find trusted sexual health services and information.",
                "View Resources",
                this::openResourcesUrl,
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
        button.setDefaultButton(primary);
        button.getStyleClass().add(primary ? "primary-button" : "secondary-button");
        button.setOnAction(event -> action.run());
        button.setMinHeight(40);
        button.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(header, spacer, button);
        return card;
    }

    private void navigate(String destination) {
        if (controller == null) {
            showAlert(Alert.AlertType.WARNING, "Navigation unavailable", "Controller is not available.");
            return;
        }

        switch (destination) {
            case "notify" -> controller.goToNotify();
            case "settings" -> controller.goToSettings();
            case "legal" -> controller.goToLegalOnboard();
            default -> {
            }
        }
    }

    private void openResourcesUrl() {
        String url = "https://www.nhs.uk/conditions/sexually-transmitted-infections-stis/";
        try {
            if (!Desktop.isDesktopSupported()) {
                showAlert(Alert.AlertType.ERROR, "Browser unavailable", "Desktop browsing is not supported on this platform.");
                return;
            }
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception exception) {
            showAlert(Alert.AlertType.ERROR, "Unable to open browser", "SafeX could not open the NHS resources page.");
        }
    }

    private HBox buildFooter() {
        Label left = new Label("SafeX - Your privacy is our priority");
        left.setTextFill(Color.web("#64748B"));
        left.setFont(Font.font("System", 12));

        Label right = new Label("v0.0.1");
        right.setTextFill(Color.web("#64748B"));
        right.setFont(Font.font("System", 12));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox footer = new HBox(10, left, spacer, right);
        footer.setPadding(new Insets(12, 24, 18, 24));
        footer.setAlignment(Pos.CENTER_LEFT);
        return footer;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private record NodeIcon(StackPane badge) {
        static NodeIcon image(String resourcePath, String background, String tint) {
            Image image = new Image(Objects.requireNonNull(DashboardPage.class.getResourceAsStream(resourcePath)));
            ImageView view = new ImageView(image);
            view.setFitWidth(22);
            view.setFitHeight(22);
            view.setPreserveRatio(true);
            return new NodeIcon(buildBadge(view, background, tint));
        }

        static NodeIcon svg(String path, String background, String color) {
            SVGPath icon = new SVGPath();
            icon.setContent(path);
            icon.setFill(Color.web(color));
            icon.setScaleX(0.9);
            icon.setScaleY(0.9);
            return new NodeIcon(buildBadge(icon, background, color));
        }

        private static StackPane buildBadge(javafx.scene.Node icon, String background, String color) {
            StackPane badge = new StackPane(icon);
            badge.setPrefSize(46, 46);
            badge.setMinSize(46, 46);
            badge.setMaxSize(46, 46);
            badge.setStyle("-fx-background-color: " + background + "; -fx-background-radius: 999;");
            return badge;
        }
    }
}
