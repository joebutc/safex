package app;

import app.model.DatabaseHandler;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock(value = "useraccounts.db", mode = ResourceAccessMode.READ_WRITE)
class DataRetentionIntegrationTest {

    private static final String TEST_USER = "__test_retention_controller__";
    private static final String DB_URL = "jdbc:sqlite:useraccounts.db";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @BeforeEach
    void setup() {
        DatabaseHandler.initialiseDatabase();
        AppController.initForTest(TEST_USER);
        cleanAll();
    }

    @AfterEach
    void teardown() {
        cleanAll();
        AppController.resetForTest();
    }

    @Test
    void saveDataRetention_appliesDeletionToCurrentUserData() {
        String oldDate = LocalDate.now().minusDays(40).format(FMT);
        String recentDate = LocalDate.now().minusDays(10).format(FMT);

        DatabaseHandler.addStiTest(TEST_USER, oldDate, "HIV", "Negative", "Old");
        DatabaseHandler.addStiTest(TEST_USER, recentDate, "Chlamydia", "Negative", "Recent");

        assertEquals(2, AppController.getStiTests().size());

        AppController.saveDataRetention("30 days");

        assertEquals("30 days", AppController.getDataRetention());
        assertEquals(1, AppController.getStiTests().size());
        assertEquals("Chlamydia", AppController.getStiTests().get(0).getStiType());
    }

    private void cleanAll() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            for (String sql : new String[]{
                    "DELETE FROM sti_tests WHERE username = ?",
                    "DELETE FROM treatment_plans WHERE username = ?",
                    "DELETE FROM test_permissions WHERE username = ?",
                    "DELETE FROM treatment_permissions WHERE username = ?",
                    "DELETE FROM privacy_settings WHERE username = ?"
            }) {
                try (PreparedStatement p = conn.prepareStatement(sql)) {
                    p.setString(1, TEST_USER);
                    p.executeUpdate();
                }
            }
        } catch (Exception ignored) {
        }
    }
}
