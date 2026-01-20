package app.view;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class LoginPage implements Page {
    @Override
    public Scene createScene() {
        StackPane root = new StackPane(new Label("Init Page"));
        return new Scene(root, 600, 400);
    }

    @Override
    public void onEnter() {
        System.out.println("Entered init page");
    }
}
