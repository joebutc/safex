package app;

import app.model.Account;
import app.model.ClinicAccount;
import app.model.DataAccessLogEntry;
import app.model.DatabaseHandler;
import app.model.State;
import app.model.StiTestEntry;
import app.model.TreatmentEntry;
import app.model.UserDataExport;
import app.service.EmailService;
import app.service.RecoveryService;
import app.view.AppStyles;
import app.view.BookAppointmentPage;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class AppController {
    private static boolean initialised = false;
    private static Stage primaryStage;
    private static String currentUser;
    private static String patientUser;
    private static String currentLoginType;
    private static String selectedDate;
    private static String recoveryEmail;
    private static String recoveryAccountType;
    private static State recoveryReturnState = State.LOGIN;
    private static State registrationReturnState = State.REGISTER;
    private static PendingRegistration pendingRegistration;
    private static String registrationErrorMessage;

    private static final String CONFIG_FILE = "clinic_hours.txt";

    private AppController() {
    }

    static void initForTest(String username) {
        currentUser = username;
        patientUser = null;
        currentLoginType = "USER";
        selectedDate = null;
        recoveryEmail = null;
        recoveryAccountType = null;
        recoveryReturnState = State.LOGIN;
        registrationReturnState = State.REGISTER;
        pendingRegistration = null;
        registrationErrorMessage = null;
        initialised = true;
    }

    static void resetForTest() {
        currentUser = null;
        patientUser = null;
        currentLoginType = null;
        selectedDate = null;
        recoveryEmail = null;
        recoveryAccountType = null;
        recoveryReturnState = State.LOGIN;
        registrationReturnState = State.REGISTER;
        pendingRegistration = null;
        registrationErrorMessage = null;
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
        if (state == State.REGISTER || state == State.CLINIC_REGISTER) {
            registrationReturnState = state;
        }
        if (state == State.LOGIN || state == State.CLINIC_LOGIN) {
            currentUser = null;
            patientUser = null;
            currentLoginType = null;
            selectedDate = null;
            recoveryEmail = null;
            recoveryAccountType = null;
            pendingRegistration = null;
            registrationErrorMessage = null;
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
            enforceRetentionIfNeeded();
            if (!"CLINIC".equals(loginType) && DatabaseHandler.getTermsAcceptedDate(username) == null) {
                updateView(State.PRIVACY_ONBOARDING);
            } else {
                updateView(homeDashboardStateFor(loginType));
            }
            return true;
        }
        return false;
    }

    public static boolean attemptRegister(String username, String password, String loginType) {
        return attemptRegister(username, password, null, null, loginType);
    }

    public static boolean attemptRegister(String username, String password, String email, String loginType) {
        return attemptRegister(username, password, email, null, loginType);
    }

    public static boolean attemptRegister(String username, String password, String email, String address,
            String loginType) {
        assertInitialised();
        registrationErrorMessage = null;

        if (!DatabaseHandler.isRegistrationAvailable(username, email, loginType)) {
            registrationErrorMessage = "Username or email already exists.";
            return false;
        }

        String verificationCode = RecoveryService.generateRecoveryCode();
        if (!DatabaseHandler.storeRecoveryCode(email, verificationCode)) {
            registrationErrorMessage = "We couldn't generate a verification code. Please try again.";
            return false;
        }

        if (!EmailService.sendVerificationCode(email, verificationCode)) {
            DatabaseHandler.clearRecoveryCode(email);
            registrationErrorMessage = "We couldn't send the verification email right now. Please try again.";
            return false;
        }

        registrationReturnState = "CLINIC".equals(loginType) ? State.CLINIC_REGISTER : State.REGISTER;
        pendingRegistration = new PendingRegistration(username, password, email, address, loginType);
        updateView(State.REGISTER_VERIFICATION);
        return true;
    }

    public static String getCurrentUser() {
        assertInitialised();
        return currentUser;
    }

    public static String getRegistrationErrorMessage() {
        assertInitialised();
        return registrationErrorMessage;
    }

    public static boolean hasPendingRegistration() {
        assertInitialised();
        return pendingRegistration != null;
    }

    public static String getPendingRegistrationEmail() {
        assertInitialised();
        return pendingRegistration == null ? null : pendingRegistration.email();
    }

    public static String getPendingRegistrationAddress() {
        assertInitialised();
        return pendingRegistration == null ? null : pendingRegistration.address();
    }

    public static State getPendingRegistrationReturnState() {
        assertInitialised();
        if (pendingRegistration == null) {
            return registrationReturnState == null ? State.REGISTER : registrationReturnState;
        }
        return "CLINIC".equals(pendingRegistration.loginType()) ? State.CLINIC_REGISTER : State.REGISTER;
    }

    public static boolean resendPendingRegistrationCode() {
        assertInitialised();
        if (pendingRegistration == null) {
            return false;
        }

        String verificationCode = RecoveryService.generateRecoveryCode();
        if (!DatabaseHandler.storeRecoveryCode(pendingRegistration.email(), verificationCode)) {
            return false;
        }

        if (!EmailService.sendVerificationCode(pendingRegistration.email(), verificationCode)) {
            DatabaseHandler.clearRecoveryCode(pendingRegistration.email());
            return false;
        }
        return true;
    }

    public static boolean completePendingRegistration(String verificationCode) {
        assertInitialised();
        if (pendingRegistration == null) {
            return false;
        }

        if (!DatabaseHandler.verifyRecoveryCode(pendingRegistration.email(), verificationCode)) {
            return false;
        }

        if (!DatabaseHandler.registerUser(
                pendingRegistration.username(),
                pendingRegistration.password(),
                pendingRegistration.email(),
                pendingRegistration.address(),
                pendingRegistration.loginType())) {
            return false;
        }

        currentUser = pendingRegistration.username();
        currentLoginType = pendingRegistration.loginType();
        DatabaseHandler.clearRecoveryCode(pendingRegistration.email());
        pendingRegistration = null;
        registrationReturnState = State.REGISTER;
        registrationErrorMessage = null;

        if (!"CLINIC".equals(currentLoginType)) {
            updateView(State.PRIVACY_ONBOARDING);
        } else {
            updateView(homeDashboardStateFor(currentLoginType));
        }
        return true;
    }

    public static void cancelPendingRegistration() {
        assertInitialised();
        if (pendingRegistration != null) {
            DatabaseHandler.clearRecoveryCode(pendingRegistration.email());
        }
        pendingRegistration = null;
        registrationReturnState = State.REGISTER;
        registrationErrorMessage = null;
    }

    public static void startRecoveryFlow(State returnState) {
        assertInitialised();
        recoveryEmail = null;
        recoveryAccountType = null;
        recoveryReturnState = returnState == null ? State.LOGIN : returnState;
        updateView(State.ACCOUNT_RECOVERY);
    }

    public static void setRecoveryContext(String email, String accountType) {
        assertInitialised();
        recoveryEmail = email;
        recoveryAccountType = accountType;
    }

    public static String getRecoveryEmail() {
        assertInitialised();
        return recoveryEmail;
    }

    public static String getRecoveryAccountType() {
        assertInitialised();
        return recoveryAccountType;
    }

    public static State getRecoveryReturnState() {
        assertInitialised();
        if ("CLINIC".equals(recoveryAccountType)) {
            return State.CLINIC_LOGIN;
        }
        if ("STANDARD".equals(recoveryAccountType)) {
            return State.LOGIN;
        }
        return recoveryReturnState == null ? State.LOGIN : recoveryReturnState;
    }

    public static void goToResetPassword() {
        assertInitialised();
        updateView(State.RESET_PASSWORD);
    }

    public static void clearRecoveryContext() {
        recoveryEmail = null;
        recoveryAccountType = null;
        recoveryReturnState = State.LOGIN;
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

    public static Account getCurrentAccount() {
        assertInitialised();
        if ("CLINIC".equals(currentLoginType)) {
            return DatabaseHandler.getClinicAccount(currentUser);
        }
        return null;
    }

    public static void updateClinicAccount(ClinicAccount clinic) {
        assertInitialised();
        DatabaseHandler.updateClinicAccount(clinic);
    }

    public static void goToClinicDashboard() {
        updateView(State.CLINIC_DASHBOARD);
    }

    public static void goToBookAppointment(String preselectedClinicUsername) {
        assertInitialised();
        try {
            FXMLLoader loader = new FXMLLoader(AppController.class.getResource(State.BOOK_APPOINTMENT.getFxmlPath()));
            Parent root = loader.load();

            BookAppointmentPage controller = loader.getController();
            if (preselectedClinicUsername != null && !preselectedClinicUsername.isBlank()) {
                controller.setPreselectedClinic(preselectedClinicUsername);
            } else {
                Platform.runLater(controller::promptForLocationAndSort);
            }

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
        } catch (IOException e) {
            throw new RuntimeException("Could not load FXML for Book Appointment", e);
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
        enforceRetentionIfNeeded();
    }

    public static void acceptTerms() {
        assertInitialised();
        DatabaseHandler.saveTermsAccepted(currentUser);
    }

    public static int getReminderIntervalMonths() {
        assertInitialised();
        return DatabaseHandler.getReminderIntervalMonths(currentUser);
    }

    public static boolean saveTestPermissions(boolean allowReminders, boolean allowShareWithDoctor,
            int intervalMonths) {
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

    public static String getTermsAcceptedDate() {
        assertInitialised();
        return DatabaseHandler.getTermsAcceptedDate(currentUser);
    }

    public static void logClinicAccess(String patientUsername, String accessType, String details) {
        assertInitialised();
        DatabaseHandler.logDataAccess(patientUsername, currentUser, "CLINIC", accessType, details);
    }

    public static List<DataAccessLogEntry> getDataAccessLog() {
        assertInitialised();
        return DatabaseHandler.getDataAccessLog(currentUser);
    }

    public static UserDataExport getUserDataExport() {
        assertInitialised();
        return DatabaseHandler.getUserDataExport(currentUser);
    }

    public static boolean exportUserDataToCsv(File file) {
        assertInitialised();
        UserDataExport export = DatabaseHandler.getUserDataExport(currentUser);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(export.toCsv());
            return true;
        } catch (IOException e) {
            System.out.println("Error exporting user data: " + e.getMessage());
            return false;
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // ── NEW: Methods for Clinic Location and Booking Integrations ─────────────
    // ──────────────────────────────────────────────────────────────────────────

    public static String getClinicDefaultStartTime() {
        assertInitialised();
        Properties props = loadProperties();
        String clinicUsername = getCurrentUser();
        return props.getProperty(clinicUsername + "_start", "09:00");
    }

    public static String getClinicDefaultEndTime() {
        assertInitialised();
        Properties props = loadProperties();
        String clinicUsername = getCurrentUser();
        return props.getProperty(clinicUsername + "_end", "17:00");
    }

    public static void saveClinicDefaultWorkingHours(String startTime, String endTime) {
        assertInitialised();
        Properties props = loadProperties();
        String clinicUsername = getCurrentUser();

        props.setProperty(clinicUsername + "_start", startTime);
        props.setProperty(clinicUsername + "_end", endTime);

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Clinic Working Hours Configuration");
            System.out.println("Working hours saved to text file successfully.");
        } catch (IOException e) {
            System.out.println("Error saving working hours to file: " + e.getMessage());
        }
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        File file = new File(CONFIG_FILE);

        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                props.load(in);
            } catch (IOException e) {
                System.out.println("Error reading working hours file: " + e.getMessage());
            }
        }
        return props;
    }

    private record PendingRegistration(String username, String password, String email, String address,
            String loginType) {
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

    private static void enforceRetentionIfNeeded() {
        if (!"CLINIC".equals(currentLoginType) && currentUser != null && !currentUser.isBlank()) {
            DatabaseHandler.enforceDataRetention(currentUser);
        }
    }

    private static void assertInitialised() {
        if (!initialised) {
            throw new RuntimeException("AppController not initialised");
        }
    }

    public static String getClinicWorkingStartTime(String clinicUsername) {
        assertInitialised();
        Properties props = loadProperties();
        return props.getProperty(clinicUsername + "_start", "09:00");
    }

    public static String getClinicWorkingEndTime(String clinicUsername) {
        assertInitialised();
        Properties props = loadProperties();
        return props.getProperty(clinicUsername + "_end", "17:00");
    }
}