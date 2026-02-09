package app;

import javafx.application.Application;

public class App {
    public static void main(String[] args) {
        if (System.getProperty("prism.order") == null) {
            System.setProperty("prism.order", "sw");
        }

        Application.launch(FxApp.class, args);
    }
}
