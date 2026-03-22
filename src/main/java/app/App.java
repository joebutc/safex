package app;

import app.model.DatabaseHandler;
import app.model.State;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        DatabaseHandler.initialiseDatabase();

        primaryStage.setTitle("SafeX");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(650);

        AppController.init(primaryStage, State.LOGIN);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
