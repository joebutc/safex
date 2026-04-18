package app.model;

public enum State {
    LOGIN("/scenes/StandardLogin.fxml"),
    REGISTER("/scenes/StandardRegister.fxml"),
    CLINIC_LOGIN("/scenes/ClinicLogin.fxml"),
    CLINIC_REGISTER("/scenes/ClinicRegister.fxml"),
    DASHBOARD("/scenes/Dashboard.fxml"),
    CLINIC_DASHBOARD("/scenes/ClinicDashboard.fxml"),
    NOTIFY("/scenes/PartnerNotify.fxml"),
    LEGAL("/scenes/LegalOnBoard.fxml"),
    PRIVACY_POLICY("/scenes/PrivacyPolicyPage.fxml"),
    TERMS_CONDITIONS("/scenes/TermsConditionsPage.fxml"),
    SETTINGS("/scenes/Settings.fxml"),
    PRIVACY_ONBOARDING("/scenes/PrivacyOnboarding.fxml"),
    PERSONA_SELECTION("/scenes/PersonaSelection.fxml"),
    TREATMENT_TRACKER("/scenes/TreatmentTracker.fxml"),
    STI_BOOKING("/scenes/StiBooking.fxml"),
    STI_TRACKER("/scenes/StiTracker.fxml"),
    CLINIC_SCHEDULE("/scenes/ClinicSchedule.fxml"),
    CALENDER("/scenes/Calender.fxml"),
    PATIENT_TREATMENT("/scenes/PatientTreatment.fxml"),
    BOOK_APPOINTMENT("/scenes/BookAppointment.fxml"),
    SETTINGS_MENU("/scenes/SettingsPage.fxml"),
    CLINIC_SETTING_MENU("/scenes/SettingsPageClinic.fxml");

    private final String fxmlPath;

    State(String fxmlPath) {
        this.fxmlPath = fxmlPath;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }
}
