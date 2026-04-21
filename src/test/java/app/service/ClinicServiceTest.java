package app.service;

import app.model.ClinicAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClinicServiceTest {

    private ClinicService clinicService;

    @BeforeEach
    void setUp() {
        // 1. Ensure the database tables exist
        app.model.DatabaseHandler.initialiseDatabase();

        // 2. Register the dummy clinics into the database
        app.model.DatabaseHandler.registerUser("london_central", "testpass", "CLINIC");
        app.model.DatabaseHandler.registerUser("pompey_clinic", "testpass", "CLINIC");

        // 3. Give them their postcodes using the method we wrote earlier
        ClinicAccount london = new ClinicAccount("london_central", "testpass", "", "London Clinic");
        london.addCoveredPostcodePrefix("SW1A");
        london.addCoveredPostcodePrefix("SW1");
        app.model.DatabaseHandler.updateClinicAccount(london);

        ClinicAccount pompey = new ClinicAccount("pompey_clinic", "testpass", "", "Portsmouth Clinic");
        pompey.addCoveredPostcodePrefix("PO");
        app.model.DatabaseHandler.updateClinicAccount(pompey);

        // 4. Initialize the service now that the DB is prepped
        clinicService = new ClinicService();
    }

    // ─── 1. Successful Matches (Partner Clinics Found) ───────────────────────

    @Test
    void testExactOutcodeMatch_returnsSouthamptonClinic() {
        List<ClinicAccount> results = clinicService.findClinicsNear("SO14 0YG");

        assertEquals(1, results.size(), "Should find exactly one clinic for SO14");
        assertEquals("ryansclinic", results.get(0).getUsername(), "Should be the Southampton clinic");
    }

    @Test
    void testBroadPrefixMatch_returnsPortsmouthClinic() {
        // Portsmouth supports just "PO", so "PO5" should match
        List<ClinicAccount> results = clinicService.findClinicsNear("PO5 3QT");

        assertEquals(1, results.size(), "Should find exactly one clinic for PO5");
        assertEquals("pompey_clinic", results.get(0).getUsername(), "Should be the Portsmouth clinic");
    }

    @Test
    void testCaseInsensitiveAndWhitespace_returnsLondonClinic() {
        // Testing messy input to ensure the formatting logic works
        List<ClinicAccount> result1 = clinicService.findClinicsNear("  sw1a 1aa  ");
        List<ClinicAccount> result2 = clinicService.findClinicsNear("sw1a1aa");

        assertEquals(1, result1.size(), "Should handle lowercase and padding");
        assertEquals("london_central", result1.get(0).getUsername());

        assertEquals(1, result2.size(), "Should handle missing middle space");
        assertEquals("london_central", result2.get(0).getUsername());
    }

    // ─── 2. Graceful Degradation (Triggering the NHS Fallback) ───────────────

    @Test
    void testUnregisteredArea_returnsEmptyToTriggerNhsFallback() {
        // Birmingham is not in our mock database.
        // Returning an empty list is the correct behaviour so the UI displays the NHS
        // link.
        List<ClinicAccount> results = clinicService.findClinicsNear("B1 1AA");

        assertTrue(results.isEmpty(), "Should return an empty list for unsupported areas to trigger NHS fallback");
    }

    @Test
    void testFalsePositiveOverlap_returnsEmptyToTriggerNhsFallback() {
        // "M14" starts with "M1", but they are entirely different districts.
        // It is better to show the NHS fallback than to incorrectly route them to M1.
        List<ClinicAccount> results = clinicService.findClinicsNear("M14 5TE");

        assertTrue(results.isEmpty(),
                "M14 should NOT falsely trigger the M1 clinic; should return empty for NHS fallback");
    }

    @Test
    void testSubstringTrap_returnsEmptyToTriggerNhsFallback() {
        // "SW17" starts with "SW1", but London Central only covers SW1/SW1A.
        List<ClinicAccount> results = clinicService.findClinicsNear("SW17 8XZ");

        assertTrue(results.isEmpty(),
                "SW17 should NOT falsely trigger the SW1 clinic; should return empty for NHS fallback");
    }

    @Test
    void testNullOrEmptyInput_returnsEmptyToTriggerNhsFallback() {
        assertTrue(clinicService.findClinicsNear(null).isEmpty(), "Null input should return empty list");
        assertTrue(clinicService.findClinicsNear("").isEmpty(), "Empty string should return empty list");
        assertTrue(clinicService.findClinicsNear("   ").isEmpty(), "Blank string should return empty list");
    }
}