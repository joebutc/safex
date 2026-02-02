package app.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SettingsPage implements Page {
    private Pane infoCard(String header, String body) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(14));
        box.setPrefWidth(300);
        box.setMinHeight(86);

        Label h = new Label(header);
        h.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        h.setTextFill(Color.web("#111827"));

        Label b = new Label(body);
        b.setWrapText(true);
        b.setTextFill(Color.web("#6B7280"));
        b.setFont(Font.font("System", 12));

        box.getChildren().addAll(h, b);

        return box;
    }

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
        cards.setPrefWrapLength(900);

        cards.getChildren().addAll(
                infoCard("What data is collected",
                        "Basic account info, usage patterns, device information"),
                infoCard("Why the data is used",
                        "To provide and improve our services, ensure security"),
                infoCard("Who can access it",
                        "Only authorized personnel and trusted service providers"),
                infoCard("How long data is kept",
                        "Retained as long as your account is active"),
                infoCard("User rights",
                        "Access, modify, or delete your data anytime")
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
