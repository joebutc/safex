package app.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SettingsPage implements Page {
    private void showDataCollectedDialog() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText("What data is collected");
        a.setContentText("""
            • Account details (email, username)
            • Usage activity (features used, time spent)
            • Device info (OS, app version, IP range)
        """);
        a.showAndWait();
    }

    private void showWhyUsedDialog() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText("Why your data is used");
        a.setContentText("""
            We use your data to:
            • Improve app performance
            • Personalize your experience
            • Ensure platform security
            • Provide customer support
        """);
        a.showAndWait();
    }

    private void showAccessControlDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Who can access your data");

        VBox box = new VBox(12);
        box.setPadding(new Insets(16));

        CheckBox staff = new CheckBox("Internal Support Staff");
        CheckBox analytics = new CheckBox("Analytics Team");
        CheckBox partners = new CheckBox("Trusted Partners");

        Button save = new Button("Save");
        save.setOnAction(e -> dialog.close());

        box.getChildren().addAll(
                new Label("Select who can access your data:"),
                staff, analytics, partners,
                save
        );

        dialog.setScene(new Scene(box, 300, 240));
        dialog.showAndWait();
    }

    private void showRetentionDialog() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("While account is active",
                "While account is active", "1 year", "6 months", "Delete immediately");
        dialog.setTitle("Data Retention");
        dialog.setHeaderText("How long should we keep your data?");
        dialog.showAndWait();
    }

    private void showRightsDialog() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText("Your Rights");
        a.setContentText("""
            You can:
            • Request a copy of your data
            • Correct inaccurate info
            • Delete your account anytime
        """);
        a.showAndWait();
    }

    private Button infoCard(String title, String subtitle, Runnable action) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(14));

        Label h = new Label(title);
        h.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));

        Label b = new Label(subtitle);
        b.setWrapText(true);
        b.setTextFill(Color.web("#6B7280"));
        b.setFont(Font.font("System", 12));

        box.getChildren().addAll(h, b);

        Button btn = new Button();
        btn.setGraphic(box);
        btn.setAlignment(Pos.TOP_LEFT);
        btn.setOnAction(e -> action.run());

        btn.setPrefWidth(300);
        btn.setPrefHeight(100);
        btn.setMinHeight(100);
        btn.setMaxHeight(100);
        btn.setPadding(Insets.EMPTY);

        return btn;
    }

//    private Pane infoCard(String header, String body) {
//        VBox box = new VBox(6);
//        box.setPadding(new Insets(14));
//        box.setPrefWidth(300);
//        box.setMinHeight(86);
//
//        Label h = new Label(header);
//        h.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
//        h.setTextFill(Color.web("#111827"));
//
//        Label b = new Label(body);
//        b.setWrapText(true);
//        b.setTextFill(Color.web("#6B7280"));
//        b.setFont(Font.font("System", 12));
//
//        box.getChildren().addAll(h, b);
//
//        return box;
//    }

    @Override
    public Scene createScene() {
        // Root
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: #ffffff;");

        // Top bar
        Label title = new Label("Settings Page");
        title.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));
        title.setTextFill(Color.web("#1F2937"));

        Button closeBtn = new Button("X");
        closeBtn.setFocusTraversable(false);
        // closeBtn.setOnAction(e -> {}); todo: link button to go back a page

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(10, title, spacer, closeBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 10, 0));

        root.setTop(topBar);

        // Middle content
        VBox content = new VBox(16);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(10, 20, 10, 20));

        // Placeholder for doc icon on figma
        StackPane docIcon = new StackPane();
        docIcon.setPrefSize(70, 70);

        Rectangle iconBg = new Rectangle(52, 52);
        iconBg.setArcWidth(14);
        iconBg.setArcHeight(14);
        iconBg.setFill(Color.web("#F3F4F6"));

        Label iconGlyph = new Label("📄");
        iconGlyph.setFont(Font.font(22));

        docIcon.getChildren().addAll(iconBg, iconGlyph);

        // Cards for settings
        FlowPane cards =  new FlowPane();
        cards.setHgap(14);
        cards.setVgap(14);
        cards.setAlignment(Pos.TOP_CENTER);
        cards.setPrefWrapLength(940);

        cards.getChildren().addAll(
                infoCard("What data is collected", "Basic account info, usage patterns, device information",
                        this::showDataCollectedDialog),

                infoCard("Why the data is used", "To provide and improve our services, ensure security",
                        this::showWhyUsedDialog),

                infoCard("Who can access it", "Authorized staff and service providers",
                        this::showAccessControlDialog),

                infoCard("How long data is kept", "Retained while your account is active",
                        this::showRetentionDialog),

                infoCard("User rights", "Access, modify, or delete your data",
                        this::showRightsDialog)
        );

        // T&Cs button
        Button readFullBtn = new Button("Read full Terms & Conditions");
        readFullBtn.setOnAction(e -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText("Full Terms & Conditions");
            a.setContentText("Put stuff here");
            a.showAndWait();
        });

        content.getChildren().addAll(docIcon, cards, readFullBtn);
        root.setCenter(content);

        // Bottom row
        CheckBox agree = new CheckBox("I agree to the Terms & Conditions");
        agree.setTextFill(Color.web("#374151"));

        Button acceptBtn = new Button("Accept & Continue");
        acceptBtn.setPrefWidth(160);
        acceptBtn.disableProperty().bind(agree.selectedProperty().not());

        acceptBtn.setOnAction(e -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setHeaderText("Accepted");
            a.setContentText("You accepted the Terms & Conditions");
            a.showAndWait();
        });

        Region bottomSpacer = new Region();
        HBox.setHgrow(bottomSpacer, Priority.ALWAYS);

        HBox bottom = new HBox(12, agree, bottomSpacer, acceptBtn);
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.setPadding(new Insets(12, 6, 0, 6));

        root.setBottom(bottom);

        Scene scene = new Scene(root, 980, 560);
        scene.setFill(Color.WHITE);
        return scene;
    }

    @Override
    public void onEnter() {
        System.out.println("Entered settings page");
    }
}
