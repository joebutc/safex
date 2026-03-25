package app.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock(value = "useraccounts.db", mode = ResourceAccessMode.READ_WRITE)
class DataPrivacyTests {

    private static final String TEST_USER = "__test_privacy_user__";
    private static final String TEST_CLINIC = "__test_clinic__";
    private static final String DB_URL = "jdbc:sqlite:useraccounts.db";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @BeforeEach
    void setup() {
        DatabaseHandler.initialiseDatabase();
        cleanTestData();
    }

    @AfterEach
    void teardown() {
        cleanTestData();
    }

    @Test
    void saveDataRetention_persistsToDatabase() {
        DatabaseHandler.saveDataRetention(TEST_USER, "30 days");
        assertEquals("30 days", DatabaseHandler.getDataRetention(TEST_USER));
    }

    @Test
    void saveDataRetention_updatesExistingValue() {
        DatabaseHandler.saveDataRetention(TEST_USER, "30 days");
        DatabaseHandler.saveDataRetention(TEST_USER, "90 days");
        assertEquals("90 days", DatabaseHandler.getDataRetention(TEST_USER));
    }

    @Test
    void saveDataRetention_allOptionsWork() {
        for (String option : new String[]{"30 days", "90 days", "180 days", "While account is active"}) {
            DatabaseHandler.saveDataRetention(TEST_USER, option);
            assertEquals(option, DatabaseHandler.getDataRetention(TEST_USER));
        }
    }

    @Test
    void getDataRetention_defaultsToWhileActive() {
        String retention = DatabaseHandler.getDataRetention("__nonexistent_user__");
        assertEquals("While account is active", retention);
    }

    @Test
    void enforceDataRetention_demonstrateActualDeletion() {
        String veryOldDate = LocalDate.now().minusDays(100).format(DATE_FMT);
        String oldDate = LocalDate.now().minusDays(40).format(DATE_FMT);
        String recentDate = LocalDate.now().minusDays(10).format(DATE_FMT);

        DatabaseHandler.addStiTest(TEST_USER, veryOldDate, "HIV", "Negative", "100 days old");
        DatabaseHandler.addStiTest(TEST_USER, oldDate, "Chlamydia", "Negative", "40 days old");
        DatabaseHandler.addStiTest(TEST_USER, recentDate, "Gonorrhea", "Negative", "10 days old");

        addTestTreatment(TEST_USER, "VeryOldInactive", veryOldDate, false);
        addTestTreatment(TEST_USER, "OldInactive", oldDate, false);
        addTestTreatment(TEST_USER, "RecentInactive", recentDate, false);
        addTestTreatment(TEST_USER, "VeryOldActive", veryOldDate, true);

        assertEquals(3, DatabaseHandler.getStiTests(TEST_USER).size(), "Should have 3 STI tests before enforcement");
        assertEquals(4, DatabaseHandler.getAllTreatments(TEST_USER).size(), "Should have 4 treatments before enforcement");

        DatabaseHandler.saveDataRetention(TEST_USER, "30 days");
        DatabaseHandler.enforceDataRetention(TEST_USER);

        List<StiTestEntry> remainingTests = DatabaseHandler.getStiTests(TEST_USER);
        assertEquals(1, remainingTests.size(), "Should have 1 STI test after 30-day enforcement");
        assertEquals("Gonorrhea", remainingTests.get(0).getStiType(), "Only the recent test should remain");

        List<TreatmentEntry> remainingTreatments = DatabaseHandler.getAllTreatments(TEST_USER);
        assertEquals(2, remainingTreatments.size(), "Should have 2 treatments after enforcement");
        assertTrue(remainingTreatments.stream().anyMatch(t -> t.getMedicationName().equals("RecentInactive")),
                "Recent inactive treatment should remain");
        assertTrue(remainingTreatments.stream().anyMatch(t -> t.getMedicationName().equals("VeryOldActive")),
                "Old but active treatment should remain");
        assertFalse(remainingTreatments.stream().anyMatch(t -> t.getMedicationName().equals("VeryOldInactive")),
                "Very old inactive treatment should be deleted");
        assertFalse(remainingTreatments.stream().anyMatch(t -> t.getMedicationName().equals("OldInactive")),
                "Old inactive treatment should be deleted");
    }

    @Test
    void enforceDataRetention_verifyPhysicalDatabaseDeletion() {
        String oldDate = LocalDate.now().minusDays(100).format(DATE_FMT);
        String recentDate = LocalDate.now().minusDays(10).format(DATE_FMT);

        for (int i = 0; i < 5; i++) {
            DatabaseHandler.addStiTest(TEST_USER, oldDate, "HIV-" + i, "Negative", "Old");
        }
        for (int i = 0; i < 2; i++) {
            DatabaseHandler.addStiTest(TEST_USER, recentDate, "New-" + i, "Negative", "Recent");
        }

        int beforeCount = countStiTestsDirectSql(TEST_USER);
        assertEquals(7, beforeCount, "Should have 7 rows in database before enforcement");

        DatabaseHandler.saveDataRetention(TEST_USER, "30 days");
        DatabaseHandler.enforceDataRetention(TEST_USER);

        int afterCount = countStiTestsDirectSql(TEST_USER);
        assertEquals(2, afterCount, "Should have 2 rows in database after enforcement - physical deletion confirmed");
    }

    @Test
    void enforceDataRetention_deletesOldStiTests_30days() {
        String oldDate = LocalDate.now().minusDays(40).format(DATE_FMT);
        DatabaseHandler.addStiTest(TEST_USER, oldDate, "HIV", "Negative", "Old test");

        String recentDate = LocalDate.now().minusDays(10).format(DATE_FMT);
        DatabaseHandler.addStiTest(TEST_USER, recentDate, "Chlamydia", "Negative", "Recent test");

        DatabaseHandler.saveDataRetention(TEST_USER, "30 days");
        DatabaseHandler.enforceDataRetention(TEST_USER);

        List<StiTestEntry> remaining = DatabaseHandler.getStiTests(TEST_USER);
        assertEquals(1, remaining.size());
        assertEquals("Chlamydia", remaining.get(0).getStiType());
    }

    @Test
    void enforceDataRetention_deletesOldStiTests_90days() {
        String oldDate = LocalDate.now().minusDays(100).format(DATE_FMT);
        DatabaseHandler.addStiTest(TEST_USER, oldDate, "HIV", "Negative", "Old");

        String midDate = LocalDate.now().minusDays(50).format(DATE_FMT);
        DatabaseHandler.addStiTest(TEST_USER, midDate, "Gonorrhea", "Negative", "Mid");

        DatabaseHandler.saveDataRetention(TEST_USER, "90 days");
        DatabaseHandler.enforceDataRetention(TEST_USER);

        List<StiTestEntry> remaining = DatabaseHandler.getStiTests(TEST_USER);
        assertEquals(1, remaining.size());
        assertEquals("Gonorrhea", remaining.get(0).getStiType());
    }

    @Test
    void enforceDataRetention_deletesOldStiTests_180days() {
        String oldDate = LocalDate.now().minusDays(200).format(DATE_FMT);
        DatabaseHandler.addStiTest(TEST_USER, oldDate, "HIV", "Negative", "Very old");

        String midDate = LocalDate.now().minusDays(100).format(DATE_FMT);
        DatabaseHandler.addStiTest(TEST_USER, midDate, "Syphilis", "Negative", "Mid");

        DatabaseHandler.saveDataRetention(TEST_USER, "180 days");
        DatabaseHandler.enforceDataRetention(TEST_USER);

        List<StiTestEntry> remaining = DatabaseHandler.getStiTests(TEST_USER);
        assertEquals(1, remaining.size());
        assertEquals("Syphilis", remaining.get(0).getStiType());
    }

    @Test
    void enforceDataRetention_whileActiveKeepsAllData() {
        String oldDate = LocalDate.now().minusDays(365).format(DATE_FMT);
        DatabaseHandler.addStiTest(TEST_USER, oldDate, "HIV", "Negative", "Year old");

        DatabaseHandler.saveDataRetention(TEST_USER, "While account is active");
        DatabaseHandler.enforceDataRetention(TEST_USER);

        List<StiTestEntry> remaining = DatabaseHandler.getStiTests(TEST_USER);
        assertEquals(1, remaining.size());
    }

    @Test
    void enforceDataRetention_deletesOldInactiveTreatments() {
        String oldDate = LocalDate.now().minusDays(100).format(DATE_FMT);
        addTestTreatment(TEST_USER, "Old Med", oldDate, false);

        String recentDate = LocalDate.now().minusDays(10).format(DATE_FMT);
        addTestTreatment(TEST_USER, "Recent Med", recentDate, false);

        addTestTreatment(TEST_USER, "Active Med", oldDate, true);

        DatabaseHandler.saveDataRetention(TEST_USER, "30 days");
        DatabaseHandler.enforceDataRetention(TEST_USER);

        List<TreatmentEntry> remaining = DatabaseHandler.getAllTreatments(TEST_USER);
        assertEquals(2, remaining.size());
        assertTrue(remaining.stream().noneMatch(t -> t.getMedicationName().equals("Old Med")));
    }

    @Test
    void enforceDataRetention_keepsActiveTreatmentsRegardlessOfAge() {
        String oldDate = LocalDate.now().minusDays(200).format(DATE_FMT);
        addTestTreatment(TEST_USER, "Active Old Med", oldDate, true);

        DatabaseHandler.saveDataRetention(TEST_USER, "30 days");
        DatabaseHandler.enforceDataRetention(TEST_USER);

        List<TreatmentEntry> remaining = DatabaseHandler.getAllTreatments(TEST_USER);
        assertEquals(1, remaining.size());
        assertEquals("Active Old Med", remaining.get(0).getMedicationName());
    }

    @Test
    void enforceDataRetention_isolatedPerUser() {
        String otherUser = "__test_privacy_other__";
        try {
            String oldDate = LocalDate.now().minusDays(100).format(DATE_FMT);
            DatabaseHandler.addStiTest(TEST_USER, oldDate, "HIV", "Negative", "");
            DatabaseHandler.addStiTest(otherUser, oldDate, "HIV", "Negative", "");

            DatabaseHandler.saveDataRetention(TEST_USER, "30 days");
            DatabaseHandler.saveDataRetention(otherUser, "While account is active");

            DatabaseHandler.enforceDataRetention(TEST_USER);

            assertEquals(0, DatabaseHandler.getStiTests(TEST_USER).size());
            assertEquals(1, DatabaseHandler.getStiTests(otherUser).size());
        } finally {
            cleanUserData(otherUser);
        }
    }

    @Test
    void logDataAccess_recordsClinicAuditTrailEntries() {
        String testClinic = "__test_clinic_user__";

        DatabaseHandler.registerUser(TEST_USER, "password123", "USER");
        DatabaseHandler.registerUser(testClinic, "password123", "CLINIC");

        try {
            DatabaseHandler.logDataAccess(TEST_USER, testClinic, "CLINIC", "VIEW_TREATMENTS", null);
            DatabaseHandler.logDataAccess(TEST_USER, testClinic, "CLINIC", "ADD_TREATMENT", "Medication X - 10mg");
            DatabaseHandler.logDataAccess(TEST_USER, testClinic, "CLINIC", "DISCONTINUE_TREATMENT", "Medication Y");

            List<DataAccessLogEntry> logs = DatabaseHandler.getDataAccessLog(TEST_USER);
            assertEquals(3, logs.size(), "Should have 3 access log entries");

            assertEquals("DISCONTINUE_TREATMENT", logs.get(0).getAccessType());
            assertEquals("ADD_TREATMENT", logs.get(1).getAccessType());
            assertEquals("VIEW_TREATMENTS", logs.get(2).getAccessType());

            assertEquals(testClinic, logs.get(0).getAccessorUsername());
            assertEquals("CLINIC", logs.get(0).getAccessorType());
            assertEquals("Medication Y", logs.get(0).getDetails());

            assertEquals("Medication X - 10mg", logs.get(1).getDetails());
            assertNull(logs.get(2).getDetails());

            for (DataAccessLogEntry log : logs) {
                assertNotNull(log.getAccessTimestamp(), "Timestamp should be recorded");
            }
        } finally {
            cleanUserData(testClinic);
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                try (PreparedStatement p = conn.prepareStatement("DELETE FROM users WHERE username IN (?, ?)")) {
                    p.setString(1, TEST_USER);
                    p.setString(2, testClinic);
                    p.executeUpdate();
                } catch (Exception ignored) {}
                try (PreparedStatement p = conn.prepareStatement("DELETE FROM clinics WHERE username = ?")) {
                    p.setString(1, testClinic);
                    p.executeUpdate();
                } catch (Exception ignored) {}
            } catch (Exception ignored) {}
        }
    }

    @Test
    void logDataAccess_recordsEntry() {
        DatabaseHandler.logDataAccess(TEST_USER, TEST_CLINIC, "CLINIC", "VIEW_TREATMENTS", "Test details");

        List<DataAccessLogEntry> log = DatabaseHandler.getDataAccessLog(TEST_USER);
        assertEquals(1, log.size());
        assertEquals(TEST_USER, log.get(0).getPatientUsername());
        assertEquals(TEST_CLINIC, log.get(0).getAccessorUsername());
        assertEquals("CLINIC", log.get(0).getAccessorType());
        assertEquals("VIEW_TREATMENTS", log.get(0).getAccessType());
        assertEquals("Test details", log.get(0).getDetails());
        assertNotNull(log.get(0).getAccessTimestamp());
    }

    @Test
    void logDataAccess_multipleEntriesOrderedByMostRecent() {
        DatabaseHandler.logDataAccess(TEST_USER, "clinic1", "CLINIC", "VIEW", "First");
        DatabaseHandler.logDataAccess(TEST_USER, "clinic2", "CLINIC", "EDIT", "Second");

        List<DataAccessLogEntry> log = DatabaseHandler.getDataAccessLog(TEST_USER);
        assertEquals(2, log.size());
        assertEquals("clinic2", log.get(0).getAccessorUsername());
        assertEquals("clinic1", log.get(1).getAccessorUsername());
    }

    @Test
    void logDataAccess_isolatedPerPatient() {
        String otherUser = "__test_privacy_other__";
        try {
            DatabaseHandler.logDataAccess(TEST_USER, TEST_CLINIC, "CLINIC", "VIEW", "For TEST_USER");
            DatabaseHandler.logDataAccess(otherUser, TEST_CLINIC, "CLINIC", "VIEW", "For other");

            List<DataAccessLogEntry> log = DatabaseHandler.getDataAccessLog(TEST_USER);
            assertEquals(1, log.size());
            assertEquals("For TEST_USER", log.get(0).getDetails());
        } finally {
            cleanUserData(otherUser);
        }
    }

    @Test
    void getDataAccessLog_emptyForUnknownUser() {
        List<DataAccessLogEntry> log = DatabaseHandler.getDataAccessLog("__no_such_user__");
        assertTrue(log.isEmpty());
    }

    @Test
    void logDataAccess_nullDetailsAllowed() {
        DatabaseHandler.logDataAccess(TEST_USER, TEST_CLINIC, "CLINIC", "VIEW", null);

        List<DataAccessLogEntry> log = DatabaseHandler.getDataAccessLog(TEST_USER);
        assertEquals(1, log.size());
        assertNull(log.get(0).getDetails());
    }

    @Test
    void getUserDataExport_includesDataRetention() {
        DatabaseHandler.saveDataRetention(TEST_USER, "90 days");

        UserDataExport export = DatabaseHandler.getUserDataExport(TEST_USER);
        assertEquals("90 days", export.getDataRetention());
    }

    @Test
    void getUserDataExport_includesStiTests() {
        String testDate = LocalDate.now().format(DATE_FMT);
        DatabaseHandler.addStiTest(TEST_USER, testDate, "HIV", "Negative", "Note");

        UserDataExport export = DatabaseHandler.getUserDataExport(TEST_USER);
        assertEquals(1, export.getStiTests().size());
        assertEquals("HIV", export.getStiTests().get(0).getStiType());
    }

    @Test
    void getUserDataExport_includesToCsv() {
        DatabaseHandler.saveDataRetention(TEST_USER, "30 days");
        String testDate = LocalDate.now().format(DATE_FMT);
        DatabaseHandler.addStiTest(TEST_USER, testDate, "HIV", "Negative", "");

        UserDataExport export = DatabaseHandler.getUserDataExport(TEST_USER);
        String csv = export.toCsv();

        assertTrue(csv.contains(TEST_USER));
        assertTrue(csv.contains("30 days"));
        assertTrue(csv.contains("HIV"));
    }

    @Test
    void saveDataAccessPermissions_persistsToDatabase() {
        DatabaseHandler.saveDataAccessPermissions(TEST_USER, true, false, true);
        boolean[] perms = DatabaseHandler.getDataAccessPermissions(TEST_USER);
        assertTrue(perms[0]);
        assertFalse(perms[1]);
        assertTrue(perms[2]);
    }

    @Test
    void saveDataAccessPermissions_updatesExistingValues() {
        DatabaseHandler.saveDataAccessPermissions(TEST_USER, true, true, true);
        DatabaseHandler.saveDataAccessPermissions(TEST_USER, false, false, false);

        boolean[] perms = DatabaseHandler.getDataAccessPermissions(TEST_USER);
        assertFalse(perms[0]);
        assertFalse(perms[1]);
        assertFalse(perms[2]);
    }

    @Test
    void getDataAccessPermissions_defaultsToFalse() {
        boolean[] perms = DatabaseHandler.getDataAccessPermissions("__nonexistent_user__");
        assertFalse(perms[0]);
        assertFalse(perms[1]);
        assertFalse(perms[2]);
    }

    private void cleanTestData() {
        cleanUserData(TEST_USER);
        cleanUserData("__test_privacy_other__");
        cleanAllTestAccessLogs();
    }

    private void cleanUserData(String username) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (PreparedStatement p = conn.prepareStatement("DELETE FROM sti_tests WHERE username = ?")) {
                p.setString(1, username);
                p.executeUpdate();
            } catch (Exception ignored) {}

            try (PreparedStatement p = conn.prepareStatement("DELETE FROM treatment_plans WHERE username = ?")) {
                p.setString(1, username);
                p.executeUpdate();
            } catch (Exception ignored) {}

            try (PreparedStatement p = conn.prepareStatement("DELETE FROM privacy_settings WHERE username = ?")) {
                p.setString(1, username);
                p.executeUpdate();
            } catch (Exception ignored) {}

            try (PreparedStatement p = conn.prepareStatement("DELETE FROM test_permissions WHERE username = ?")) {
                p.setString(1, username);
                p.executeUpdate();
            } catch (Exception ignored) {}

            try (PreparedStatement p = conn.prepareStatement("DELETE FROM treatment_permissions WHERE username = ?")) {
                p.setString(1, username);
                p.executeUpdate();
            } catch (Exception ignored) {}
        } catch (Exception ignored) {}
    }

    private void cleanAllTestAccessLogs() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (PreparedStatement p = conn.prepareStatement(
                    "DELETE FROM data_access_log WHERE patient_username LIKE '\\_\\_test\\_privacy%' ESCAPE '\\'")) {
                p.executeUpdate();
            } catch (Exception ignored) {}

            try (PreparedStatement p = conn.prepareStatement(
                    "DELETE FROM data_access_log WHERE accessor_username LIKE '\\_\\_test\\_%' ESCAPE '\\'")) {
                p.executeUpdate();
            } catch (Exception ignored) {}
        } catch (Exception ignored) {}
    }

    private void addTestTreatment(String username, String medName, String startDate, boolean active) {
        String sql = "INSERT INTO treatment_plans (username, medication_name, dose, frequency, quantity_remaining, low_stock_threshold, start_date, notes, active) "
                + "VALUES (?, ?, '10mg', 'Daily', 30, 7, ?, '', ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, username);
            p.setString(2, medName);
            p.setString(3, startDate);
            p.setInt(4, active ? 1 : 0);
            p.executeUpdate();
        } catch (Exception e) {
            fail("Failed to add test treatment: " + e.getMessage());
        }
    }

    private int countStiTestsDirectSql(String username) {
        String sql = "SELECT COUNT(*) as count FROM sti_tests WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, username);
            var rs = p.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (Exception e) {
            fail("Failed to count STI tests: " + e.getMessage());
        }
        return 0;
    }
}
