package app.view;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class SettingsPage implements Page {
    @Override
    public Scene createScene() {
        StackPane root = new StackPane(new Label("Settings"));
        return new Scene(root, 600, 400);
    }
}
