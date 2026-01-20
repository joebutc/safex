package app;

public class AppController {
    private AppModel model;
    private AppView view;

    public void updateView() {
        view.updateView(model.getState());
    }

    public void updateModel() {

    }

    public AppController(AppModel model, AppView view) {
        this.model = model;
        this.view = view;
    }
}
