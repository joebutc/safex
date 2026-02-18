package app;

import app.model.DatabaseHandler;
import app.model.State;
import javafx.application.Application;
import javafx.stage.Stage;

public class FxApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        DatabaseHandler.initialiseDatabase();

        primaryStage.setTitle("SafeX");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(650);

        AppController.init(State.LOGIN);
    }
}
