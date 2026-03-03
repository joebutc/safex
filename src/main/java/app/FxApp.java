package app;

import app.model.DatabaseHandler;
import app.model.ScheduleDatabaseHandler;
import javafx.application.Application;
import javafx.stage.Stage;

public class FxApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        DatabaseHandler.initialiseDatabase();
        ScheduleDatabaseHandler.initialiseScheduleDatabase();
        primaryStage.setTitle("SafeX");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(650);

        AppModel model = new AppModel();
        AppView view = new AppView(primaryStage);
        AppController controller = new AppController(model, view);
        controller.setPrimaryStage(primaryStage);
        view.setController(controller);
        controller.updateView();
    }
}
