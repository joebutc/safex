package app.service;

import app.model.ClinicAccount;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock(value = "useraccounts.db", mode = ResourceAccessMode.READ_WRITE)
class ClinicServiceTest {
    private static final String DB_URL = "jdbc:sqlite:useraccounts.db";

    private ClinicService clinicService;
    private String londonClinicUsername;
    private String portsmouthClinicUsername;
    private String southamptonClinicUsername;
    private String manchesterClinicUsername;

    @BeforeEach
    void setUp() {
        DatabaseHandler.initialiseDatabase();

        long suffix = System.nanoTime();
        londonClinicUsername = "__postcode_london_" + suffix + "__";
        portsmouthClinicUsername = "__postcode_portsmouth_" + suffix + "__";
        southamptonClinicUsername = "__postcode_southampton_" + suffix + "__";
        manchesterClinicUsername = "__postcode_manchester_" + suffix + "__";

        cleanup();

        registerClinic(londonClinicUsername, "London Clinic", "QA1A", "QA1");
        registerClinic(portsmouthClinicUsername, "Portsmouth Clinic", "QB");
        registerClinic(southamptonClinicUsername, "Southampton Clinic", "QC14");
        registerClinic(manchesterClinicUsername, "Manchester Clinic", "QD1");

        clinicService = new ClinicService();
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    @Test
    void testExactOutcodeMatch_returnsSouthamptonClinic() {
        List<ClinicAccount> results = clinicService.findClinicsNear("QC14 0YG");

        assertTrue(results.stream().anyMatch(clinic -> southamptonClinicUsername.equals(clinic.getUsername())),
                "Should include the exact-match clinic for QC14");
    }

    @Test
    void testBroadPrefixMatch_returnsPortsmouthClinic() {
        List<ClinicAccount> results = clinicService.findClinicsNear("QB5 3QT");

        assertTrue(results.stream().anyMatch(clinic -> portsmouthClinicUsername.equals(clinic.getUsername())),
                "Should include the broad-prefix clinic for QB");
    }

    @Test
    void testCaseInsensitiveAndWhitespace_returnsLondonClinic() {
        List<ClinicAccount> result1 = clinicService.findClinicsNear("  qa1a 1aa  ");
        List<ClinicAccount> result2 = clinicService.findClinicsNear("qa1a1aa");

        assertTrue(result1.stream().anyMatch(clinic -> londonClinicUsername.equals(clinic.getUsername())),
                "Should handle lowercase and padding");
        assertTrue(result2.stream().anyMatch(clinic -> londonClinicUsername.equals(clinic.getUsername())),
                "Should handle missing middle space");
    }

    @Test
    void testUnregisteredArea_returnsEmptyToTriggerNhsFallback() {
        List<ClinicAccount> results = clinicService.findClinicsNear("QZ1 1AA");

        assertTrue(results.isEmpty(), "Should return an empty list for unsupported areas to trigger NHS fallback");
    }

    @Test
    void testFalsePositiveOverlap_returnsEmptyToTriggerNhsFallback() {
        List<ClinicAccount> results = clinicService.findClinicsNear("QD14 5TE");

        assertTrue(results.isEmpty(),
                "QD14 should NOT falsely trigger the QD1 clinic; should return empty for NHS fallback");
    }

    @Test
    void testSubstringTrap_returnsEmptyToTriggerNhsFallback() {
        List<ClinicAccount> results = clinicService.findClinicsNear("QA17 8XZ");

        assertTrue(results.isEmpty(),
                "QA17 should NOT falsely trigger the QA1 clinic; should return empty for NHS fallback");
    }

    @Test
    void testNullOrEmptyInput_returnsEmptyToTriggerNhsFallback() {
        assertTrue(clinicService.findClinicsNear(null).isEmpty(), "Null input should return empty list");
        assertTrue(clinicService.findClinicsNear("").isEmpty(), "Empty string should return empty list");
        assertTrue(clinicService.findClinicsNear("   ").isEmpty(), "Blank string should return empty list");
    }

    private void registerClinic(String username, String displayName, String... postcodePrefixes) {
        assertTrue(DatabaseHandler.registerUser(username, "testpass", "CLINIC"),
                "Expected clinic registration to succeed for " + username);

        ClinicAccount clinic = new ClinicAccount(username, "testpass", "", displayName);
        for (String postcodePrefix : postcodePrefixes) {
            clinic.addCoveredPostcodePrefix(postcodePrefix);
        }
        DatabaseHandler.updateClinicAccount(clinic);
    }

    private void cleanup() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            deleteClinic(conn, manchesterClinicUsername);
            deleteClinic(conn, southamptonClinicUsername);
            deleteClinic(conn, portsmouthClinicUsername);
            deleteClinic(conn, londonClinicUsername);
        } catch (Exception ignored) {
        }
    }

    private void deleteClinic(Connection conn, String username) throws Exception {
        if (username == null || username.isBlank()) {
            return;
        }

        try (PreparedStatement deletePostcodes = conn.prepareStatement(
                "DELETE FROM clinic_postcodes WHERE clinic_username = ?")) {
            deletePostcodes.setString(1, username);
            deletePostcodes.executeUpdate();
        }

        try (PreparedStatement deleteClinics = conn.prepareStatement(
                "DELETE FROM clinics WHERE username = ?")) {
            deleteClinics.setString(1, username);
            deleteClinics.executeUpdate();
        }
    }
}
