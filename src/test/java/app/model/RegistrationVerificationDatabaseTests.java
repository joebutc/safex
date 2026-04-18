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
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock(value = "useraccounts.db", mode = ResourceAccessMode.READ_WRITE)
class RegistrationVerificationDatabaseTests {
    private static final String DB_URL = "jdbc:sqlite:useraccounts.db";

    private String userUsername;
    private String userEmail;
    private String clinicUsername;
    private String clinicEmail;

    @BeforeEach
    void setup() {
        DatabaseHandler.initialiseDatabase();
        long suffix = System.nanoTime();
        userUsername = "__verify_user_" + suffix + "__";
        userEmail = "verify.user." + suffix + "@example.com";
        clinicUsername = "__verify_clinic_" + suffix + "__";
        clinicEmail = "verify.clinic." + suffix + "@example.com";
        cleanup();
    }

    @AfterEach
    void teardown() {
        cleanup();
    }

    @Test
    void isRegistrationAvailable_changesAfterVerifiedUserRegistration() {
        assertTrue(DatabaseHandler.isRegistrationAvailable(userUsername, userEmail, "STANDARD"));
        assertTrue(DatabaseHandler.registerUser(
                userUsername,
                "SecurePass1!",
                userEmail,
                "10 Example Road",
                "STANDARD"));

        assertFalse(DatabaseHandler.isRegistrationAvailable(userUsername, userEmail, "STANDARD"));
    }

    @Test
    void registerUser_storesEmailAddressAndVerifiedFlagForStandardUser() throws Exception {
        assertTrue(DatabaseHandler.registerUser(
                userUsername,
                "SecurePass1!",
                userEmail,
                "22 Privacy Lane",
                "STANDARD"));

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement p = conn.prepareStatement(
                     "SELECT email, address, email_verified FROM users WHERE username = ?")) {
            p.setString(1, userUsername);
            ResultSet rs = p.executeQuery();

            assertTrue(rs.next());
            assertEquals(userEmail.toLowerCase(), rs.getString("email"));
            assertEquals("22 Privacy Lane", rs.getString("address"));
            assertEquals(1, rs.getInt("email_verified"));
        }
    }

    @Test
    void registerUser_storesEmailAddressAndVerifiedFlagForClinic() throws Exception {
        assertTrue(DatabaseHandler.registerUser(
                clinicUsername,
                "ClinicPass1!",
                clinicEmail,
                "50 Clinic Square",
                "CLINIC"));

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement p = conn.prepareStatement(
                     "SELECT email, address, email_verified FROM clinics WHERE username = ?")) {
            p.setString(1, clinicUsername);
            ResultSet rs = p.executeQuery();

            assertTrue(rs.next());
            assertEquals(clinicEmail.toLowerCase(), rs.getString("email"));
            assertEquals("50 Clinic Square", rs.getString("address"));
            assertEquals(1, rs.getInt("email_verified"));
        }
    }

    private void cleanup() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (PreparedStatement deleteRecovery = conn.prepareStatement(
                    "DELETE FROM password_recovery_codes WHERE email IN (?, ?)")) {
                deleteRecovery.setString(1, userEmail == null ? "" : userEmail.toLowerCase());
                deleteRecovery.setString(2, clinicEmail == null ? "" : clinicEmail.toLowerCase());
                deleteRecovery.executeUpdate();
            }

            try (PreparedStatement deleteUsers = conn.prepareStatement(
                    "DELETE FROM users WHERE username = ? OR LOWER(email) = ?")) {
                deleteUsers.setString(1, userUsername == null ? "" : userUsername);
                deleteUsers.setString(2, userEmail == null ? "" : userEmail.toLowerCase());
                deleteUsers.executeUpdate();
            }

            try (PreparedStatement deleteClinics = conn.prepareStatement(
                    "DELETE FROM clinics WHERE username = ? OR LOWER(email) = ?")) {
                deleteClinics.setString(1, clinicUsername == null ? "" : clinicUsername);
                deleteClinics.setString(2, clinicEmail == null ? "" : clinicEmail.toLowerCase());
                deleteClinics.executeUpdate();
            }
        } catch (Exception ignored) {
        }
    }
}
