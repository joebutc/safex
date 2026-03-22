package app;

import app.model.DatabaseHandler;
import app.model.State;
import app.model.StiTestEntry;
import app.model.TreatmentEntry;
import app.view.AppStyles;
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
    private static String selectedDate;

    private AppController() {
    }

    static void initForTest(String username) {
        currentUser = username;
        patientUser = null;
        currentLoginType = "USER";
        selectedDate = null;
        initialised = true;
    }

    static void resetForTest() {
        currentUser = null;
        patientUser = null;
        currentLoginType = null;
        selectedDate = null;
        primaryStage = null;
        initialised = false;
    }

    public static void init(Stage stage, State state) {
        if (initialised) {
            System.err.println("AppController already initialised");
            return;
        }
        primaryStage = stage;
        Parent root = loadRoot(state);
        Scene scene = new Scene(root);
        AppStyles.apply(scene);
        primaryStage.setScene(scene);
        primaryStage.show();
        initialised = true;
    }

    public static void updateView(State state) {
        assertInitialised();
        if (state == State.LOGIN || state == State.CLINIC_LOGIN) {
            currentUser = null;
            patientUser = null;
            currentLoginType = null;
            selectedDate = null;
        }
        Parent root = loadRoot(state);
        Scene scene = primaryStage.getScene();
        if (scene != null) {
            AppStyles.apply(scene);
            scene.setRoot(root);
        } else {
            scene = new Scene(root);
            AppStyles.apply(scene);
            primaryStage.setScene(scene);
        }
        primaryStage.show();
    }

    public static boolean attemptLogin(String username, String password, String loginType) {
        assertInitialised();
        if (DatabaseHandler.checkLogin(username, password, loginType)) {
            currentUser = username;
            currentLoginType = loginType;
            updateView(homeDashboardStateFor(loginType));
            return true;
        }
        return false;
    }

    public static boolean attemptRegister(String username, String password, String loginType) {
        assertInitialised();
        if (DatabaseHandler.registerUser(username, password, loginType)) {
            currentUser = username;
            currentLoginType = loginType;
            updateView(homeDashboardStateFor(loginType));
            return true;
        }
        return false;
    }

    public static String getCurrentUser() {
        assertInitialised();
        return currentUser;
    }

    public static String getCurrentLoginType() {
        return currentLoginType;
    }

    public static State getHomeDashboardState() {
        assertInitialised();
        return homeDashboardStateFor(currentLoginType);
    }

    public static void goToHomeDashboard() {
        updateView(getHomeDashboardState());
    }

    public static List<String> getPendingNotifications() {
        assertInitialised();
        List<String> notifications = new ArrayList<>();

        String testMsg = getReminderMessage();
        if (testMsg != null) {
            notifications.add(testMsg);
        }

        boolean[] treatPerms = DatabaseHandler.getTreatmentPermissions(currentUser);
        if (treatPerms[0]) {
            for (TreatmentEntry treatment : DatabaseHandler.getTreatments(currentUser)) {
                if (treatment.isLowStock()) {
                    int days = (int) Math.ceil(treatment.daysRemaining());
                    notifications.add("Low supply: " + treatment.getMedicationName()
                            + " - approx. " + days + " day(s) remaining. Consider requesting a refill.");
                }
            }
        }

        return notifications;
    }

    public static String getReminderMessage() {
        assertInitialised();
        if (!shouldShowReminder()) {
            return null;
        }

        String latestDate = DatabaseHandler.getLatestTestDate(currentUser);
        int interval = DatabaseHandler.getReminderIntervalMonths(currentUser);
        if (latestDate == null) {
            return "You haven't logged any STI tests yet.\n\nRegular testing is important for your sexual health. Consider getting tested and logging your results in 'Track Your Tests'.";
        }
        return "It has been more than " + interval + " month(s) since your last logged STI test (" + latestDate
                + ").\n\nConsider booking a new test and logging your results.";
    }

    public static boolean shouldShowReminder() {
        assertInitialised();
        boolean[] perms = DatabaseHandler.getTestPermissions(currentUser);
        if (!perms[0]) {
            return false;
        }

        String latestDate = DatabaseHandler.getLatestTestDate(currentUser);
        if (latestDate == null) {
            return true;
        }

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

    public static boolean[] getDataAccessPermissions() {
        assertInitialised();
        return DatabaseHandler.getDataAccessPermissions(currentUser);
    }

    public static void saveDataAccessPermissions(boolean staff, boolean analytics, boolean partners) {
        assertInitialised();
        DatabaseHandler.saveDataAccessPermissions(currentUser, staff, analytics, partners);
    }

    public static String getDataRetention() {
        assertInitialised();
        return DatabaseHandler.getDataRetention(currentUser);
    }

    public static void saveDataRetention(String option) {
        assertInitialised();
        DatabaseHandler.saveDataRetention(currentUser, option);
    }

    public static void acceptTerms() {
        assertInitialised();
        DatabaseHandler.saveTermsAccepted(currentUser);
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

    public static void setSelectedDate(String date) {
        selectedDate = date;
    }

    public static String getSelectedDate() {
        return selectedDate;
    }

    public static List<String> getClinicUsernames() {
        assertInitialised();
        return DatabaseHandler.getClinicUsernames();
    }

    public static List<String> getAvailableTimesForDate(String date) {
        assertInitialised();
        if ("CLINIC".equals(currentLoginType)) {
            return DatabaseHandler.getAvailableTimesForDate(date, currentUser);
        }
        return DatabaseHandler.getAvailableTimesForDate(date);
    }

    public static List<String> getAvailableTimesForDate(String date, String clinicUsername) {
        assertInitialised();
        return DatabaseHandler.getAvailableTimesForDate(date, clinicUsername);
    }

    public static boolean saveAvailableTimesForDate(String date, List<String> times) {
        assertInitialised();
        return DatabaseHandler.saveAvailableTimesForDate(currentUser, date, times);
    }

    public static boolean bookAppointment(String date, String time) {
        assertInitialised();
        return DatabaseHandler.bookAppointment(currentUser, date, time);
    }

    public static boolean bookAppointment(String clinicUsername, String date, String time) {
        assertInitialised();
        return DatabaseHandler.bookAppointment(currentUser, clinicUsername, date, time);
    }

    public static List<String> getBookedTimesForDate(String date) {
        assertInitialised();
        if ("CLINIC".equals(currentLoginType)) {
            return DatabaseHandler.getBookedTimesForDate(date, currentUser);
        }
        return DatabaseHandler.getBookedTimesForDate(date);
    }

    public static List<String> getBookedTimesForDate(String date, String clinicUsername) {
        assertInitialised();
        return DatabaseHandler.getBookedTimesForDate(date, clinicUsername);
    }

    public static List<String[]> getMyAppointments() {
        assertInitialised();
        return DatabaseHandler.getAppointmentsForPatient(currentUser);
    }

    public static boolean cancelAppointment(int id) {
        assertInitialised();
        return DatabaseHandler.cancelAppointment(id, currentUser);
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
        assertInitialised();
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

    private static State homeDashboardStateFor(String loginType) {
        return "CLINIC".equals(loginType) ? State.CLINIC_DASHBOARD : State.DASHBOARD;
    }

    private static void assertInitialised() {
        if (!initialised) {
            throw new RuntimeException("AppController not initialised");
        }
    }
}
