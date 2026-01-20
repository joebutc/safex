package app;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        AppModel model = new AppModel();
        AppView view = new AppView(primaryStage);
        AppController controller = new AppController(model, view);
        controller.updateView();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
