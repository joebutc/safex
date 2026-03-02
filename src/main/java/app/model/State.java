package app.model;

public enum State {
    LOGIN("/scenes/ClinicLogin.fxml"),
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
