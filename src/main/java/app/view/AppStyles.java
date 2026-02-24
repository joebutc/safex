package app.view;

import javafx.scene.Scene;

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

        var stylesheetResource = AppStyles.class.getResource(STYLESHEET_PATH);
        if (stylesheetResource == null) {
            System.err.println("Missing stylesheet resource: " + STYLESHEET_PATH);
            return;
        }

        String stylesheetUrl = stylesheetResource.toExternalForm();

        if (!scene.getStylesheets().contains(stylesheetUrl)) {
            scene.getStylesheets().add(stylesheetUrl);
        }
    }
}
