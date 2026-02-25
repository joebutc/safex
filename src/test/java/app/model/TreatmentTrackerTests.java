package app.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TreatmentTrackerTests {

    private static final String TEST_USER = "__test_treatment_user__";
    private static final String DB_URL = "jdbc:sqlite:useraccounts.db";

    @BeforeEach
    void setup() {
        DatabaseHandler.initialiseDatabase();
        cleanTreatmentData();
        cleanTreatmentPermissions();
    }

    @AfterEach
    void teardown() {
        cleanTreatmentData();
        cleanTreatmentPermissions();
    }

    // ── TreatmentEntry model: dosesPerDay ───────────────────────────────────

    @Test
    void dosesPerDay_onceDaily() {
        assertEquals(1.0, make("Once daily", 30, 7).dosesPerDay());
    }

    @Test
    void dosesPerDay_twiceDaily() {
        assertEquals(2.0, make("Twice daily", 30, 7).dosesPerDay());
    }

    @Test
    void dosesPerDay_threeTimesDaily() {
        assertEquals(3.0, make("Three times daily", 30, 7).dosesPerDay());
    }

    @Test
    void dosesPerDay_weekly() {
        assertEquals(1.0 / 7.0, make("Weekly", 30, 7).dosesPerDay(), 1e-9);
    }

    @Test
    void dosesPerDay_asNeededIsZero() {
        assertEquals(0.0, make("As needed", 30, 7).dosesPerDay());
    }

    // ── TreatmentEntry model: daysRemaining ─────────────────────────────────

    @Test
    void daysRemaining_onceDailyCalculation() {
        // 30 doses ÷ 1 per day = 30 days
        assertEquals(30.0, make("Once daily", 30, 7).daysRemaining(), 0.01);
    }

    @Test
    void daysRemaining_twiceDailyCalculation() {
        // 30 doses ÷ 2 per day = 15 days
        assertEquals(15.0, make("Twice daily", 30, 7).daysRemaining(), 0.01);
    }

    @Test
    void daysRemaining_threeTimesDailyCalculation() {
        // 30 doses ÷ 3 per day = 10 days
        assertEquals(10.0, make("Three times daily", 30, 7).daysRemaining(), 0.01);
    }

    @Test
    void daysRemaining_weeklyCalculation() {
        // 4 doses × 7 days/dose = 28 days
        assertEquals(28.0, make("Weekly", 4, 7).daysRemaining(), 0.01);
    }

    @Test
    void daysRemaining_asNeededIsMaxValue() {
        assertEquals(Double.MAX_VALUE, make("As needed", 10, 7).daysRemaining());
    }

    @Test
    void daysRemaining_zeroDosesIsZero() {
        assertEquals(0.0, make("Once daily", 0, 7).daysRemaining(), 0.01);
    }

    // ── TreatmentEntry model: isLowStock ────────────────────────────────────

    @Test
    void isLowStock_trueWhenBelowThreshold() {
        // 5 doses at once daily = 5 days; threshold = 7 → low stock
        assertTrue(make("Once daily", 5, 7).isLowStock());
    }

    @Test
    void isLowStock_trueAtExactThreshold() {
        // 7 doses at once daily = 7 days; threshold = 7 → still low stock (<=)
        assertTrue(make("Once daily", 7, 7).isLowStock());
    }

    @Test
    void isLowStock_falseWhenAboveThreshold() {
        // 30 doses at once daily = 30 days; threshold = 7 → not low stock
        assertFalse(make("Once daily", 30, 7).isLowStock());
    }

    @Test
    void isLowStock_neverForAsNeeded() {
        // As needed never triggers low stock regardless of quantity
        assertFalse(make("As needed", 0, 7).isLowStock());
    }

    @Test
    void isLowStock_asNeededWithEmptySupplyStillFalse() {
        assertFalse(make("As needed", 0, 100).isLowStock());
    }

    @Test
    void isLowStock_weeklyWithLargeSupply() {
        // 100 doses weekly = 700 days; threshold = 7 → not low stock
        assertFalse(make("Weekly", 100, 7).isLowStock());
    }

    @Test
    void isLowStock_weeklyBelowThreshold() {
        // 0.5 doses weekly = 3.5 days; threshold = 7 → low stock
        assertTrue(make("Weekly", 0.5, 7).isLowStock());
    }

    // ── addTreatment ────────────────────────────────────────────────────────

    @Test
    void addTreatment_returnsTrue() {
        assertTrue(DatabaseHandler.addTreatment(
                TEST_USER, "PrEP", "1 tablet", "Once daily", 30, 7, "01 Jan 2026", ""));
    }

    @Test
    void addTreatment_withNullNotes() {
        assertTrue(DatabaseHandler.addTreatment(
                TEST_USER, "PrEP", "1 tablet", "Once daily", 30, 7, "01 Jan 2026", null));
    }

    // ── getTreatments ───────────────────────────────────────────────────────

    @Test
    void getTreatments_emptyForUnknownUser() {
        assertTrue(DatabaseHandler.getTreatments("__no_such_user__").isEmpty());
    }

    @Test
    void getTreatments_returnsAddedEntry() {
        DatabaseHandler.addTreatment(TEST_USER, "PrEP", "1 tablet", "Once daily", 30, 7, "01 Jan 2026", "Note");
        List<TreatmentEntry> entries = DatabaseHandler.getTreatments(TEST_USER);
        assertEquals(1, entries.size());
        TreatmentEntry e = entries.get(0);
        assertEquals(TEST_USER, e.getUsername());
        assertEquals("PrEP", e.getMedicationName());
        assertEquals("1 tablet", e.getDose());
        assertEquals("Once daily", e.getFrequency());
        assertEquals(30.0, e.getQuantityRemaining(), 0.01);
        assertEquals(7, e.getLowStockThreshold());
        assertEquals("01 Jan 2026", e.getStartDate());
        assertEquals("Note", e.getNotes());
        assertTrue(e.isActive());
    }

    @Test
    void getTreatments_returnsNewestFirst() {
        DatabaseHandler.addTreatment(TEST_USER, "PrEP", "1 tablet", "Once daily", 30, 7, "01 Jan 2026", "");
        DatabaseHandler.addTreatment(TEST_USER, "Doxycycline", "200 mg", "Twice daily", 14, 3, "01 Feb 2026", "");
        List<TreatmentEntry> entries = DatabaseHandler.getTreatments(TEST_USER);
        assertEquals(2, entries.size());
        assertEquals("Doxycycline", entries.get(0).getMedicationName());
        assertEquals("PrEP", entries.get(1).getMedicationName());
    }

    @Test
    void getTreatments_isolatedByUsername() {
        DatabaseHandler.addTreatment(TEST_USER, "PrEP", "1 tablet", "Once daily", 30, 7, "01 Jan 2026", "");
        assertTrue(DatabaseHandler.getTreatments("different_user").isEmpty());
    }

    // ── updateTreatmentQuantity ──────────────────────────────────────────────

    @Test
    void updateTreatmentQuantity_updatesSuccessfully() {
        DatabaseHandler.addTreatment(TEST_USER, "PrEP", "1 tablet", "Once daily", 30, 7, "01 Jan 2026", "");
        int id = DatabaseHandler.getTreatments(TEST_USER).get(0).getId();
        assertTrue(DatabaseHandler.updateTreatmentQuantity(id, TEST_USER, 25.0));
        assertEquals(25.0, DatabaseHandler.getTreatments(TEST_USER).get(0).getQuantityRemaining(), 0.01);
    }

    @Test
    void updateTreatmentQuantity_toZero() {
        DatabaseHandler.addTreatment(TEST_USER, "PrEP", "1 tablet", "Once daily", 30, 7, "01 Jan 2026", "");
        int id = DatabaseHandler.getTreatments(TEST_USER).get(0).getId();
        DatabaseHandler.updateTreatmentQuantity(id, TEST_USER, 0.0);
        assertEquals(0.0, DatabaseHandler.getTreatments(TEST_USER).get(0).getQuantityRemaining(), 0.01);
    }

    @Test
    void updateTreatmentQuantity_wrongUserReturnsFalse() {
        DatabaseHandler.addTreatment(TEST_USER, "PrEP", "1 tablet", "Once daily", 30, 7, "01 Jan 2026", "");
        int id = DatabaseHandler.getTreatments(TEST_USER).get(0).getId();
        assertFalse(DatabaseHandler.updateTreatmentQuantity(id, "other_user", 5.0));
        // Quantity unchanged for the correct user
        assertEquals(30.0, DatabaseHandler.getTreatments(TEST_USER).get(0).getQuantityRemaining(), 0.01);
    }

    // ── deleteTreatment ──────────────────────────────────────────────────────

    @Test
    void deleteTreatment_softDeletesEntry() {
        DatabaseHandler.addTreatment(TEST_USER, "PrEP", "1 tablet", "Once daily", 30, 7, "01 Jan 2026", "");
        int id = DatabaseHandler.getTreatments(TEST_USER).get(0).getId();
        assertTrue(DatabaseHandler.deleteTreatment(id, TEST_USER));
        // getTreatments only returns active entries
        assertTrue(DatabaseHandler.getTreatments(TEST_USER).isEmpty());
    }

    @Test
    void deleteTreatment_wrongUserReturnsFalse() {
        DatabaseHandler.addTreatment(TEST_USER, "PrEP", "1 tablet", "Once daily", 30, 7, "01 Jan 2026", "");
        int id = DatabaseHandler.getTreatments(TEST_USER).get(0).getId();
        assertFalse(DatabaseHandler.deleteTreatment(id, "other_user"));
        assertEquals(1, DatabaseHandler.getTreatments(TEST_USER).size());
    }

    @Test
    void deleteTreatment_nonExistentIdReturnsFalse() {
        assertFalse(DatabaseHandler.deleteTreatment(Integer.MAX_VALUE, TEST_USER));
    }

    // ── updateTreatmentPlan ──────────────────────────────────────────────────

    @Test
    void updateTreatmentPlan_updatesAllFields() {
        DatabaseHandler.addTreatment(TEST_USER, "PrEP", "1 tablet", "Once daily", 30, 7, "01 Jan 2026", "");
        int id = DatabaseHandler.getTreatments(TEST_USER).get(0).getId();
        assertTrue(DatabaseHandler.updateTreatmentPlan(
                id, TEST_USER, "Doxycycline", "200 mg", "Twice daily", 14, 3, "Updated note"));
        TreatmentEntry e = DatabaseHandler.getTreatments(TEST_USER).get(0);
        assertEquals("Doxycycline", e.getMedicationName());
        assertEquals("200 mg", e.getDose());
        assertEquals("Twice daily", e.getFrequency());
        assertEquals(14.0, e.getQuantityRemaining(), 0.01);
        assertEquals(3, e.getLowStockThreshold());
        assertEquals("Updated note", e.getNotes());
    }

    @Test
    void updateTreatmentPlan_wrongUserReturnsFalse() {
        DatabaseHandler.addTreatment(TEST_USER, "PrEP", "1 tablet", "Once daily", 30, 7, "01 Jan 2026", "");
        int id = DatabaseHandler.getTreatments(TEST_USER).get(0).getId();
        assertFalse(DatabaseHandler.updateTreatmentPlan(
                id, "other_user", "X", "Y", "Once daily", 10, 5, ""));
        // Original medication unchanged
        assertEquals("PrEP", DatabaseHandler.getTreatments(TEST_USER).get(0).getMedicationName());
    }

    // ── getPatientTreatmentsForClinic ────────────────────────────────────────

    @Test
    void getPatientTreatmentsForClinic_returnsActiveTreatments() {
        DatabaseHandler.addTreatment(TEST_USER, "PrEP", "1 tablet", "Once daily", 30, 7, "01 Jan 2026", "");
        List<TreatmentEntry> entries = DatabaseHandler.getPatientTreatmentsForClinic(TEST_USER);
        assertEquals(1, entries.size());
        assertEquals("PrEP", entries.get(0).getMedicationName());
    }

    @Test
    void getPatientTreatmentsForClinic_excludesSoftDeleted() {
        DatabaseHandler.addTreatment(TEST_USER, "PrEP", "1 tablet", "Once daily", 30, 7, "01 Jan 2026", "");
        int id = DatabaseHandler.getTreatments(TEST_USER).get(0).getId();
        DatabaseHandler.deleteTreatment(id, TEST_USER);
        assertTrue(DatabaseHandler.getPatientTreatmentsForClinic(TEST_USER).isEmpty());
    }

    @Test
    void getPatientTreatmentsForClinic_emptyForUnknownUser() {
        assertTrue(DatabaseHandler.getPatientTreatmentsForClinic("__no_such_user__").isEmpty());
    }

    // ── getTreatmentPermissions ──────────────────────────────────────────────

    @Test
    void getTreatmentPermissions_defaultsToFalseWhenNoRow() {
        boolean[] perms = DatabaseHandler.getTreatmentPermissions(TEST_USER);
        assertFalse(perms[0]); // allow_treatment_reminders
        assertFalse(perms[1]); // allow_clinic_access
    }

    // ── saveTreatmentPermissions ─────────────────────────────────────────────

    @Test
    void saveTreatmentPermissions_returnsTrue() {
        assertTrue(DatabaseHandler.saveTreatmentPermissions(TEST_USER, true, true));
    }

    @Test
    void saveTreatmentPermissions_persistsAndReadsBack() {
        DatabaseHandler.saveTreatmentPermissions(TEST_USER, true, true);
        boolean[] perms = DatabaseHandler.getTreatmentPermissions(TEST_USER);
        assertTrue(perms[0]);
        assertTrue(perms[1]);
    }

    @Test
    void saveTreatmentPermissions_upsertOverwritesPrevious() {
        DatabaseHandler.saveTreatmentPermissions(TEST_USER, true, true);
        DatabaseHandler.saveTreatmentPermissions(TEST_USER, false, false);
        boolean[] perms = DatabaseHandler.getTreatmentPermissions(TEST_USER);
        assertFalse(perms[0]);
        assertFalse(perms[1]);
    }

    @Test
    void saveTreatmentPermissions_remindersOnlyNoClinicAccess() {
        DatabaseHandler.saveTreatmentPermissions(TEST_USER, true, false);
        boolean[] perms = DatabaseHandler.getTreatmentPermissions(TEST_USER);
        assertTrue(perms[0]);
        assertFalse(perms[1]);
    }

    @Test
    void saveTreatmentPermissions_clinicAccessOnlyNoReminders() {
        DatabaseHandler.saveTreatmentPermissions(TEST_USER, false, true);
        boolean[] perms = DatabaseHandler.getTreatmentPermissions(TEST_USER);
        assertFalse(perms[0]);
        assertTrue(perms[1]);
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private TreatmentEntry make(String frequency, double quantity, int threshold) {
        return new TreatmentEntry(1, TEST_USER, "TestMed", "1 tablet",
                frequency, quantity, threshold, "01 Jan 2026", "", true);
    }

    private void cleanTreatmentData() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement p = conn.prepareStatement(
                     "DELETE FROM treatment_plans WHERE username = ?")) {
            p.setString(1, TEST_USER);
            p.executeUpdate();
        } catch (Exception ignored) {}
    }

    private void cleanTreatmentPermissions() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement p = conn.prepareStatement(
                     "DELETE FROM treatment_permissions WHERE username = ?")) {
            p.setString(1, TEST_USER);
            p.executeUpdate();
        } catch (Exception ignored) {}
    }
}
