package app.view;

import javafx.scene.Scene;

import java.util.Objects;

public final class AppStyles {
    public static final double SCENE_WIDTH = 900;
    public static final double SCENE_HEIGHT = 650;

    private static final String STYLESHEET_PATH = "/styles/app.css";

    private AppStyles() {
    }

    public static void apply(Scene scene) {
        if (scene == null) {
            return;
        }

        String stylesheetUrl = Objects.requireNonNull(
                AppStyles.class.getResource(STYLESHEET_PATH),
                "Missing stylesheet resource: " + STYLESHEET_PATH
        ).toExternalForm();

        if (!scene.getStylesheets().contains(stylesheetUrl)) {
            scene.getStylesheets().add(stylesheetUrl);
        }
    }
}

