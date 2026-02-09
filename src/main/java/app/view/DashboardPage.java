package app.view;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

public class DashboardPage implements Page {
    @Override
    public Scene createScene() {
        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);

        Label title = new Label("Dashboard");
        Button notifyPartnersButton = new Button("Notify Partners");
        notifyPartnersButton.setOnAction(event -> {
            Scene notifyScene = new PartnerNotifyPage().createScene();
            notifyPartnersButton.getScene().setRoot(notifyScene.getRoot());
        });

        root.getChildren().addAll(title, notifyPartnersButton);
        return new Scene(root, 600, 400);
    }
}
