package app;

import app.model.DatabaseHandler;
import app.model.State;
import app.model.StiTestEntry;
import app.model.TreatmentEntry;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class AppController {
    private static boolean initialised = false;
    private static Stage primaryStage;
    private static String currentUser;
    private static String patientUser;
    private static String currentLoginType;

    private AppController() {
    }

    public static void init(Stage stage, State state) {
        if (initialised) {
            System.err.println("AppController already initialised");
            return;
        }
        primaryStage = stage;
        Parent root = loadRoot(state);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        initialised = true;
    }

    public static void updateView(State state) {
        assertInitialised();
        if (state == State.LOGIN) {
            currentUser = null;
        }
        Parent root = loadRoot(state);
        primaryStage.getScene().setRoot(root);
        primaryStage.show();
    }

    // Called by Log In Page
    public static boolean attemptLogin(String username, String password, String loginType) {
        assertInitialised();
        if (DatabaseHandler.checkLogin(username, password, loginType)) {
            currentUser = username;
            currentLoginType = loginType;
            updateView(State.DASHBOARD);
            return true;
        }
        return false;
    }

    public static boolean attemptRegister(String username, String password, String loginType) {
        assertInitialised();
        if (DatabaseHandler.registerUser(username, password, loginType)) {
            currentUser = username;
            currentLoginType = loginType;
            updateView(State.DASHBOARD);
            return true;
        }
        return false;
    }

    public static String getCurrentUser() {
        assertInitialised();
        return currentUser;
    }

    public static List<String> getPendingNotifications() {
        assertInitialised();
        List<String> notifications = new ArrayList<>();

        // STI test overdue reminder
        String testMsg = getReminderMessage();
        if (testMsg != null) {
            notifications.add(testMsg);
        }

        // Treatment low-stock reminders (only if user has enabled them)
        boolean[] treatPerms = DatabaseHandler.getTreatmentPermissions(currentUser);
        if (treatPerms[0]) {
            for (TreatmentEntry t : DatabaseHandler.getTreatments(currentUser)) {
                if (t.isLowStock()) {
                    int days = (int) Math.ceil(t.daysRemaining());
                    notifications.add("Low supply: " + t.getMedicationName()
                            + " — approx. " + days + " day(s) remaining. Consider requesting a refill.");
                }
            }
        }

        return notifications;
    }

    public static String getReminderMessage() {
        assertInitialised();
        if (!shouldShowReminder()) return null;
        String latestDate = DatabaseHandler.getLatestTestDate(currentUser);
        int interval = DatabaseHandler.getReminderIntervalMonths(currentUser);
        if (latestDate == null) {
            return "You haven't logged any STI tests yet.\n\nRegular testing is important for your sexual health. Consider getting tested and logging your results in 'Track Your Tests'.";
        }
        return "It has been more than " + interval + " month(s) since your last logged STI test (" + latestDate + ").\n\nConsider booking a new test and logging your results.";
    }

    public static boolean shouldShowReminder() {
        assertInitialised();
        boolean[] perms = DatabaseHandler.getTestPermissions(currentUser);
        if (!perms[0]) return false;
        String latestDate = DatabaseHandler.getLatestTestDate(currentUser);
        if (latestDate == null) return true;
        try {
            LocalDate last = LocalDate.parse(latestDate, DateTimeFormatter.ofPattern("dd MMM yyyy"));
            int interval = DatabaseHandler.getReminderIntervalMonths(currentUser);
            return LocalDate.now().isAfter(last.plusMonths(interval));
        } catch (Exception e) {
            return false;
        }
    }

    public static void getPatientTreatment(String patientUsername) {
        assertInitialised();
        patientUser = patientUsername;
        updateView(State.PATIENT_TREATMENT);
    }

    public static boolean saveTreatmentPermissions(boolean allowReminders, boolean allowClinicAccess) {
        assertInitialised();
        return DatabaseHandler.saveTreatmentPermissions(currentUser, allowReminders, allowClinicAccess);
    }

    public static boolean[] getTestPermissions() {
        assertInitialised();
        return DatabaseHandler.getTestPermissions(currentUser);
    }

    public static int getReminderIntervalMonths() {
        assertInitialised();
        return DatabaseHandler.getReminderIntervalMonths(currentUser);
    }

    public static boolean saveTestPermissions(boolean allowReminders, boolean allowShareWithDoctor, int intervalMonths) {
        assertInitialised();
        return DatabaseHandler.saveTestPermissions(currentUser, allowReminders, allowShareWithDoctor, intervalMonths);
    }

    public static boolean[] getTreatmentPermissions() {
        assertInitialised();
        return DatabaseHandler.getTreatmentPermissions(currentUser);
    }

    public static List<TreatmentEntry> getTreatments() {
        assertInitialised();
        return DatabaseHandler.getTreatments(currentUser);
    }

    public static boolean updateTreatmentQuantity(int id, double newQuantity) {
        assertInitialised();
        return DatabaseHandler.updateTreatmentQuantity(id, currentUser, newQuantity);
    }

    public static boolean deleteTreatmentForPatient(int id, String patientUsername) {
        assertInitialised();
        return DatabaseHandler.deleteTreatment(id, patientUsername);
    }

    public static boolean deleteTreatment(int id) {
        assertInitialised();
        return DatabaseHandler.deleteTreatment(id, currentUser);
    }

    public static boolean updatePatientNote(int id, String note) {
        assertInitialised();
        return DatabaseHandler.updatePatientNote(id, currentUser, note);
    }

    public static boolean addTreatment(String medicationName, String dose, String frequency,
                                double quantity, int lowStockThreshold, String startDate, String notes) {
        assertInitialised();
        return DatabaseHandler.addTreatment(currentUser, medicationName, dose,
                frequency, quantity, lowStockThreshold, startDate, notes, "patient");
    }

    public static boolean addTreatmentForPatient(String patientUsername, String medicationName, String dose,
                                          String frequency, double quantity, int lowStockThreshold,
                                          String startDate, String notes) {
        assertInitialised();
        return DatabaseHandler.addTreatment(patientUsername, medicationName, dose,
                frequency, quantity, lowStockThreshold, startDate, notes, "clinic");
    }

    public static List<StiTestEntry> getStiTests() {
        assertInitialised();
        return DatabaseHandler.getStiTests(currentUser);
    }

    public static boolean deleteStiTest(int id) {
        assertInitialised();
        return DatabaseHandler.deleteStiTest(id, currentUser);
    }

    public static boolean addStiTest(String testDate, String stiType, String result, String notes) {
        return DatabaseHandler.addStiTest(currentUser, testDate, stiType, result, notes);
    }

    public static String getClinicPatientUsername() {
        assertInitialised();
        return patientUser;
    }

    public static boolean[] getPatientTreatmentPermissions(String patientUsername) {
        assertInitialised();
        return DatabaseHandler.getTreatmentPermissions(patientUsername);
    }

    public static List<TreatmentEntry> getPatientTreatmentsForClinic(String patientUsername) {
        assertInitialised();
        return DatabaseHandler.getPatientTreatmentsForClinic(patientUsername);
    }

    public static boolean updateTreatmentPlanForPatient(int id, String patientUsername, String medicationName,
                                                 String dose, String frequency,
                                                 double quantity, int lowStockThreshold, String notes) {
        assertInitialised();
        return DatabaseHandler.updateTreatmentPlan(id, patientUsername, medicationName, dose,
                frequency, quantity, lowStockThreshold, notes);
    }

    private static Parent loadRoot(State state) {
        try {
            FXMLLoader loader = new FXMLLoader(AppController.class.getResource(state.getFxmlPath()));
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Could not load FXML for " + state, e);
        }
    }

    private static void assertInitialised() {
        if (!initialised) {
            throw new RuntimeException("AppController not initialised");
        }
    }

//    public void updateView() {
//        view.updateView(model.getState());
//    }
//
//    public String getCurrentUser() {
//        return model.getCurrentUser();
//    }
//
//    public boolean attemptRegister(String username, String password) {
//        if (DatabaseHandler.registerUser(username, password)) {
//            model.setCurrentUser(username);
//            model.setState(State.Dashboard);
//            updateView();
//            return true;
//        }
//        return false;
//    }
//
//    public void goToDashboard() {
//        model.setState(State.Dashboard);
//        updateView();
//    }
//
//    public void goToRegister() {
//        model.setState(State.Register);
//        updateView();
//    }
//
//    public void goToLogin() {
//        model.setCurrentUser(null);
//        model.setState(State.Login);
//        updateView();
//    }
//
//    public void goToNotify() {
//        model.setState(State.Notify);
//        updateView();
//    }
//
//    public void goToSettings() {
//        model.setState(State.Settings);
//        updateView();
//    }
//
//    public void goToLegalOnboard() {
//        model.setState(State.LegalOnboard);
//        updateView();
//    }
//
//    // Called by Log In Page
//    public boolean attemptLogin(String username, String password) {
//        if (DatabaseHandler.checkLogin(username, password)) {
//            model.setCurrentUser(username);
//            model.setState(State.Dashboard);
//            updateView();
//            return true;
//        }
//        return false;
//    }
    
}
