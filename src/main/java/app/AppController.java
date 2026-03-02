package app;

import app.model.DatabaseHandler;
import app.model.State;
import app.model.StiTestEntry;
import app.model.TreatmentEntry;
import app.view.LoginPage.LoginType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AppController {
    private final AppModel model;
    private final AppView view;

    public AppController(AppModel model, AppView view) {
        this.model = model;
        this.view = view;
    }

    public void updateView() {
        view.updateView(model.getState());
    }

    public String getCurrentUser() {
        return model.getCurrentUser();
    }

    public boolean attemptRegister(String username, String password, LoginType loginType) {
        if (DatabaseHandler.registerUser(username, password, loginType.name())) {
            model.setCurrentUser(username);
            model.setClinicSession(loginType == LoginType.CLINIC);

            if (loginType == LoginType.CLINIC) {
                model.setState(State.ClinicDashboard);
            } else {
                model.setState(State.Dashboard);
            }

            updateView();
            return true;
        }
        return false;
    }

    public void goToDashboard() {
        if (model.isClinicSession()) {
            model.setState(State.ClinicDashboard);
        } else {
            model.setState(State.Dashboard);
        }
        updateView();
    }

    public void goToRegister(LoginType loginType) {
        model.setClinicSession(loginType == LoginType.CLINIC);
        if (loginType == LoginType.CLINIC) {
            model.setState(State.ClinicRegister);
        } else {
            model.setState(State.Register);
        }
        updateView();
    }

    public void goToRegister() {
        goToRegister(LoginType.STANDARD);
    }

    public void goToLogin(LoginType loginType) {
        model.setCurrentUser(null);
        model.setClinicSession(loginType == LoginType.CLINIC);
        if (loginType == LoginType.CLINIC) {
            model.setState(State.ClinicLogin);
        } else {
            model.setState(State.Login);
        }
        updateView();
    }

    public void goToLogin() {
        goToLogin(LoginType.STANDARD);
    }

    public void goToNotify() {
        model.setState(State.Notify);
        updateView();
    }

    public void goToSettings() {
        model.setState(State.Settings);
        updateView();
    }

    public void goToLegalOnboard() {
        model.setState(State.LegalOnboard);
        updateView();
    }

    public void goToClinicSchedule() {
        model.setState(State.ClinicSchedule);
        updateView();
    }

    public void goToStiTestTracker() {
        model.setState(State.StiTestTracker);
        updateView();
    }

    public void goToStiTestBooking() {
        model.setState(State.StiTestBooking);
        updateView();
    }

    public List<StiTestEntry> getStiTests() {
        return DatabaseHandler.getStiTests(model.getCurrentUser());
    }

    public boolean addStiTest(String testDate, String stiType, String result, String notes) {
        return DatabaseHandler.addStiTest(model.getCurrentUser(), testDate, stiType, result, notes);
    }

    public boolean deleteStiTest(int id) {
        return DatabaseHandler.deleteStiTest(id, model.getCurrentUser());
    }

    public boolean[] getTestPermissions() {
        return DatabaseHandler.getTestPermissions(model.getCurrentUser());
    }

    public int getReminderIntervalMonths() {
        return DatabaseHandler.getReminderIntervalMonths(model.getCurrentUser());
    }

    public boolean saveTestPermissions(boolean allowReminders, boolean allowShareWithDoctor, int intervalMonths) {
        return DatabaseHandler.saveTestPermissions(model.getCurrentUser(), allowReminders, allowShareWithDoctor, intervalMonths);
    }

    // Returns true when the user has reminders enabled and is overdue for a test
    public boolean shouldShowReminder() {
        boolean[] perms = DatabaseHandler.getTestPermissions(model.getCurrentUser());
        if (!perms[0]) return false;
        String latestDate = DatabaseHandler.getLatestTestDate(model.getCurrentUser());
        if (latestDate == null) return true;
        try {
            LocalDate last = LocalDate.parse(latestDate, DateTimeFormatter.ofPattern("dd MMM yyyy"));
            int interval = DatabaseHandler.getReminderIntervalMonths(model.getCurrentUser());
            return LocalDate.now().isAfter(last.plusMonths(interval));
        } catch (Exception e) {
            return false;
        }
    }

    // Returns the reminder message to display, or null if no reminder should be shown
    public String getReminderMessage() {
        if (!shouldShowReminder()) return null;
        String latestDate = DatabaseHandler.getLatestTestDate(model.getCurrentUser());
        int interval = DatabaseHandler.getReminderIntervalMonths(model.getCurrentUser());
        if (latestDate == null) {
            return "You haven't logged any STI tests yet.\n\nRegular testing is important for your sexual health. Consider getting tested and logging your results in 'Track Your Tests'.";
        }
        return "It has been more than " + interval + " month(s) since your last logged STI test (" + latestDate + ").\n\nConsider booking a new test and logging your results.";
    }

    // ── Treatment tracker ────────────────────────────────────────────────────

    public void goToTreatmentTracker() {
        model.setState(State.TreatmentTracker);
        updateView();
    }

    public List<TreatmentEntry> getTreatments() {
        return DatabaseHandler.getTreatments(model.getCurrentUser());
    }

    public boolean addTreatment(String medicationName, String dose, String frequency,
                                double quantity, int lowStockThreshold, String startDate, String notes) {
        return DatabaseHandler.addTreatment(model.getCurrentUser(), medicationName, dose,
                frequency, quantity, lowStockThreshold, startDate, notes, "patient");
    }

    public boolean updatePatientNote(int id, String note) {
        return DatabaseHandler.updatePatientNote(id, model.getCurrentUser(), note);
    }

    public boolean deleteTreatment(int id) {
        return DatabaseHandler.deleteTreatment(id, model.getCurrentUser());
    }

    public boolean updateTreatmentQuantity(int id, double newQuantity) {
        return DatabaseHandler.updateTreatmentQuantity(id, model.getCurrentUser(), newQuantity);
    }

    public boolean[] getTreatmentPermissions() {
        return DatabaseHandler.getTreatmentPermissions(model.getCurrentUser());
    }

    public boolean saveTreatmentPermissions(boolean allowReminders, boolean allowClinicAccess) {
        return DatabaseHandler.saveTreatmentPermissions(model.getCurrentUser(), allowReminders, allowClinicAccess);
    }

    // ── Clinic patient treatment management ─────────────────────────────────

    public void goToClinicPatientTreatment(String patientUsername) {
        model.setClinicPatientUsername(patientUsername);
        model.setState(State.ClinicPatientTreatment);
        updateView();
    }

    public String getClinicPatientUsername() {
        return model.getClinicPatientUsername();
    }

    public boolean[] getPatientTreatmentPermissions(String patientUsername) {
        return DatabaseHandler.getTreatmentPermissions(patientUsername);
    }

    public List<TreatmentEntry> getPatientTreatmentsForClinic(String patientUsername) {
        return DatabaseHandler.getPatientTreatmentsForClinic(patientUsername);
    }

    public boolean addTreatmentForPatient(String patientUsername, String medicationName, String dose,
                                          String frequency, double quantity, int lowStockThreshold,
                                          String startDate, String notes) {
        return DatabaseHandler.addTreatment(patientUsername, medicationName, dose,
                frequency, quantity, lowStockThreshold, startDate, notes, "clinic");
    }

    public boolean updateTreatmentPlanForPatient(int id, String patientUsername, String medicationName,
                                                 String dose, String frequency,
                                                 double quantity, int lowStockThreshold, String notes) {
        return DatabaseHandler.updateTreatmentPlan(id, patientUsername, medicationName, dose,
                frequency, quantity, lowStockThreshold, notes);
    }

    public boolean deleteTreatmentForPatient(int id, String patientUsername) {
        return DatabaseHandler.deleteTreatment(id, patientUsername);
    }

    // ── Notification aggregation ─────────────────────────────────────────────

    // Returns all pending reminder messages (test overdue + low-stock treatments)
    public List<String> getPendingNotifications() {
        List<String> notifications = new ArrayList<>();

        // STI test overdue reminder
        String testMsg = getReminderMessage();
        if (testMsg != null) {
            notifications.add(testMsg);
        }

        // Treatment low-stock reminders (only if user has enabled them)
        boolean[] treatPerms = DatabaseHandler.getTreatmentPermissions(model.getCurrentUser());
        if (treatPerms[0]) {
            for (TreatmentEntry t : DatabaseHandler.getTreatments(model.getCurrentUser())) {
                if (t.isLowStock()) {
                    int days = (int) Math.ceil(t.daysRemaining());
                    notifications.add("Low supply: " + t.getMedicationName()
                            + " — approx. " + days + " day(s) remaining. Consider requesting a refill.");
                }
            }
        }

        return notifications;
    }

    public boolean attemptLogin(String username, String password, LoginType loginType) {
        if (DatabaseHandler.checkLogin(username, password, loginType.name())) {
            model.setCurrentUser(username);
            model.setClinicSession(loginType == LoginType.CLINIC);

            if (loginType == LoginType.CLINIC) {
                model.setState(State.ClinicDashboard);
            } else {
                model.setState(State.Dashboard);
            }

            updateView();
            return true;
        }
        return false;
    }
}
