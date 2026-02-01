package app.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.Objects;

public class PartnerNotifyPage implements Page{
    @Override
    public Scene createScene() {


        VBox root = new VBox();
        root.setSpacing(20); // Adds vertical space between rows
        root.setPadding(new Insets(20)); // Keeps the whole UI away from the window edges


        // CONSTANTS.
        // Makes pill shaped buttons and labels.
        String PILL_STYLE = "-fx-background-radius: 50; " +   // Makes the background round
                            "-fx-border-radius: 50; " +         // Makes the border round
                            "-fx-border-color: #cccccc; ";     // Light gray border
                            //"-fx-background-color: #ffffff; ";  // White background

        double FONT_SIZE_1 = 36.0;
        double FONT_SIZE_2 = 20.0;
        double FONT_SIZE_3 = 18.0;

        // Image file paths.
        String PLUS_IMAGE_PATH = "/images/+.png";
        String BELL_IMAGE_PATH = "/images/Bell.png";
        String BIN_IMAGE_PATH = "/images/deleteIcon.png";

        // Contact lists min Height.
        double ROW_MIN_HEIGHT = 100;


        // First row.
        HBox firstRow = new HBox();
        firstRow.setAlignment(Pos.CENTER);

        // firstRow Children.
        // Back button.
        Button backButton = new Button("Back");
        backButton.setAlignment(Pos.CENTER);
        backButton.setStyle(PILL_STYLE);
        backButton.setFont(new Font(FONT_SIZE_2));
        backButton.setPrefWidth(150);
        backButton.setPrefHeight(10);


        // Region.
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        // Title.
        Label title = new Label("Notify Partners");
        title.setAlignment(Pos.CENTER_RIGHT);
        title.setFont(new Font("System Bold",FONT_SIZE_1));

        firstRow.getChildren().addAll(backButton,spacer1,title);


        // Second row.
        HBox secondRow = new HBox();
        secondRow.setAlignment(Pos.TOP_LEFT);
        secondRow.setMinHeight(ROW_MIN_HEIGHT);

        // Second row children.
        // Prompt message.
        Label prompt = new Label("Sends an anonymous Email to chosen partners.");
        prompt.setStyle(PILL_STYLE);
        prompt.setFont(new Font("System Bold",FONT_SIZE_2));
        prompt.setPadding(new Insets(10,0,10, 10));
        prompt.setMaxWidth(Double.MAX_VALUE);
        prompt.setWrapText(true);

        // Region.
        Region spacer2 = new Region();
        spacer2.setPrefWidth(100);
        HBox.setHgrow(prompt, Priority.ALWAYS);

        // Plus button.
        Image plus_img = new Image(Objects.requireNonNull(getClass().getResourceAsStream(PLUS_IMAGE_PATH)),
                30,30,true,true);

        Button plus_button = new Button();
        plus_button.setGraphic(new ImageView(plus_img));
        plus_button.setAlignment(Pos.CENTER);
        plus_button.setStyle(PILL_STYLE);
        plus_button.setPadding(new Insets(15));

        secondRow.getChildren().addAll(prompt,spacer2,plus_button);


        // Contact list.
        ScrollPane contactsList = new ScrollPane();
        contactsList.setMinViewportHeight(ROW_MIN_HEIGHT);
        VBox.setVgrow(contactsList,Priority.ALWAYS);

        VBox container = new VBox();
        contactsList.setContent(container);
        contactsList.setFitToWidth(true);

        // Logic for Plus button. (This is just front end i.e. adding new contacts entries.)
        plus_button.setOnAction(_ -> {

            //HBox settings.
            HBox contact = new HBox();
            contact.setPadding(new Insets(20));

            // Text fields.
            TextField name = new TextField();
            name.setPromptText("Enter contacts name (Optional).");
            HBox.setHgrow(name,Priority.ALWAYS);
            name.setMaxHeight(Double.MAX_VALUE);

            TextField email = new TextField();
            email.setPromptText("Enter email address.");
            HBox.setHgrow(email,Priority.ALWAYS);
            email.setMaxHeight(Double.MAX_VALUE);

            // Delete button.
            Image binImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(BIN_IMAGE_PATH)),
                    60, 60, true, true);
            Button deleteButton = new Button();
            deleteButton.setGraphic(new ImageView(binImage));
            deleteButton.setMaxHeight(Double.MAX_VALUE);
            // delete button logic.
            deleteButton.setOnAction(_ -> container.getChildren().remove(contact));

            contact.getChildren().addAll(name,email,deleteButton);

            container.getChildren().add(contact);
        });


        // last row.
        HBox lastRow = new HBox();
        lastRow.setStyle("-fx-border-color: #000000;");
        lastRow.setAlignment(Pos.CENTER);
        lastRow.setPadding(new Insets(20));
        lastRow.setMinHeight(ROW_MIN_HEIGHT);

        // Last row children.
        // Disclaimer label.
        Label disclaimerLabel = new Label("Note: The following message will be sent " +
                "anonymously to all contacts in the list above.");
        disclaimerLabel.setWrapText(true);
        disclaimerLabel.setPadding(new Insets(0,0,0,10));
        disclaimerLabel.setFont(new Font(FONT_SIZE_3));

        // Makes the disclaimer to take up all available horizontal space.
        HBox.setHgrow(disclaimerLabel, Priority.ALWAYS);
        disclaimerLabel.setMaxWidth(Double.MAX_VALUE);


        // Notify button.
        Button notifyButton = new Button("Notify");
        notifyButton.setFont(new Font("System Bold",FONT_SIZE_2));
        notifyButton.setMinWidth(120);

        // Image settings.
        Image bellImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream(BELL_IMAGE_PATH)),
                25, 25, true, true);
        notifyButton.setGraphic(new ImageView(bellImg));
        notifyButton.setAlignment(Pos.CENTER);
        notifyButton.setStyle(PILL_STYLE);

        lastRow.getChildren().addAll(disclaimerLabel,notifyButton);


        root.getChildren().addAll(firstRow,secondRow,contactsList,lastRow);

        return new Scene(root);
    }

    @Override
    public void onEnter() {
        Page.super.onEnter();
    }
}
