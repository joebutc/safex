package app.model;

public enum State {
    LOGIN("/scenes/StandardLogin.fxml"),
    REGISTER("/scenes/StandardRegister.fxml"),
    CLINIC_LOGIN("/scenes/ClinicLogin.fxml"),
    CLINIC_REGISTER("/scenes/ClinicRegister.fxml"),
    DASHBOARD("/scenes/Dashboard.fxml"),
    NOTIFY("/scenes/PartnerNotify.fxml"),
    LEGAL("/scenes/LegalOnBoard.fxml"),
    SETTINGS("/scenes/Settings.fxml"),
    TREATMENT_TRACKER("/scenes/TreatmentTracker.fxml");

    private final String fxmlPath;

    State(String fxmlPath) {
        this.fxmlPath = fxmlPath;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }
}
