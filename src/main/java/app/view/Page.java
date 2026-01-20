package app.view;

import javafx.scene.Scene;

public interface Page {
    Scene createScene();
    default void onEnter() {
    }
}
