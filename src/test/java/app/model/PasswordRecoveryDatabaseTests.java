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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock(value = "useraccounts.db", mode = ResourceAccessMode.READ_WRITE)
class PasswordRecoveryDatabaseTests {
    private static final String DB_URL = "jdbc:sqlite:useraccounts.db";
    private static final DateTimeFormatter RECOVERY_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private String testUsername;
    private String testEmail;

    @BeforeEach
    void setup() {
        DatabaseHandler.initialiseDatabase();
        testUsername = "__recovery_user_" + System.nanoTime() + "__";
        testEmail = "recovery" + System.nanoTime() + "@example.com";
        cleanup();
    }

    @AfterEach
    void teardown() {
        cleanup();
    }

    @Test
    void storeRecoveryCode_andVerifyRecoveryCode_acceptValidOtp() {
        assertTrue(DatabaseHandler.storeRecoveryCode(testEmail, "123456"));
        assertTrue(DatabaseHandler.verifyRecoveryCode(testEmail, "123456"));
        assertFalse(DatabaseHandler.verifyRecoveryCode(testEmail, "654321"));
    }

    @Test
    void verifyRecoveryCode_rejectsExpiredOtpAndClearsIt() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement p = conn.prepareStatement(
                     "INSERT INTO password_recovery_codes (email, code, expires_at) VALUES (?, ?, ?)")) {
            p.setString(1, testEmail.toLowerCase());
            p.setString(2, "999999");
            p.setString(3, LocalDateTime.now().minusMinutes(20).format(RECOVERY_FMT));
            p.executeUpdate();
        }

        assertFalse(DatabaseHandler.verifyRecoveryCode(testEmail, "999999"));
        assertFalse(DatabaseHandler.verifyRecoveryCode(testEmail, "999999"));
    }

    @Test
    void updatePasswordByEmail_updatesStoredPasswordAndClearsRecoveryCode() {
        assertTrue(DatabaseHandler.registerUser(
                testUsername,
                "OldPass1!",
                testEmail,
                "1 Test Street",
                "STANDARD"));
        assertTrue(DatabaseHandler.storeRecoveryCode(testEmail, "123456"));

        assertTrue(DatabaseHandler.updatePasswordByEmail(testEmail, "NewPass1!"));
        assertTrue(DatabaseHandler.checkLogin(testUsername, "NewPass1!", "STANDARD"));
        assertFalse(DatabaseHandler.checkLogin(testUsername, "OldPass1!", "STANDARD"));
        assertFalse(DatabaseHandler.verifyRecoveryCode(testEmail, "123456"));
    }

    private void cleanup() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (PreparedStatement deleteRecovery = conn.prepareStatement(
                    "DELETE FROM password_recovery_codes WHERE email = ?")) {
                deleteRecovery.setString(1, testEmail == null ? "" : testEmail.toLowerCase());
                deleteRecovery.executeUpdate();
            }

            try (PreparedStatement deleteUser = conn.prepareStatement(
                    "DELETE FROM users WHERE username = ? OR LOWER(email) = ?")) {
                deleteUser.setString(1, testUsername == null ? "" : testUsername);
                deleteUser.setString(2, testEmail == null ? "" : testEmail.toLowerCase());
                deleteUser.executeUpdate();
            }
        } catch (Exception ignored) {
        }
    }
}
