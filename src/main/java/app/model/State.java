package app.model;

public enum State {
    LOGIN("/scenes/Login.fxml"),
    REGISTER("/scenes/Register.fxml"),
    DASHBOARD("/scenes/Dashboard.fxml");

    private final String fxmlPath;

    State(String fxmlPath) {
        this.fxmlPath = fxmlPath;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }
}
