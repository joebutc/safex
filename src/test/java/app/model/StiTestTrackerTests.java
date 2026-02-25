package app.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StiTestTrackerTests {

    private static final String TEST_USER = "__test_sti_user__";
    private static final String DB_URL = "jdbc:sqlite:useraccounts.db";

    @BeforeEach
    void setup() {
        DatabaseHandler.initialiseDatabase();
        cleanStiTestData();
        cleanTestPermissions();
    }

    @AfterEach
    void teardown() {
        cleanStiTestData();
        cleanTestPermissions();
    }

    // ── StiTestEntry model ──────────────────────────────────────────────────

    @Test
    void stiTestEntry_gettersReturnCorrectValues() {
        StiTestEntry entry = new StiTestEntry(42, "user1", "15 Jan 2026", "HIV", "Negative", "Routine test");
        assertEquals(42, entry.getId());
        assertEquals("user1", entry.getUsername());
        assertEquals("15 Jan 2026", entry.getTestDate());
        assertEquals("HIV", entry.getStiType());
        assertEquals("Negative", entry.getResult());
        assertEquals("Routine test", entry.getNotes());
    }

    @Test
    void stiTestEntry_nullNotesStoredAsNull() {
        StiTestEntry entry = new StiTestEntry(1, "u", "01 Jan 2026", "HIV", "Negative", null);
        assertNull(entry.getNotes());
    }

    // ── addStiTest ──────────────────────────────────────────────────────────

    @Test
    void addStiTest_returnsTrue() {
        boolean result = DatabaseHandler.addStiTest(TEST_USER, "01 Feb 2026", "HIV", "Negative", "");
        assertTrue(result);
    }

    @Test
    void addStiTest_withNullNotes() {
        boolean result = DatabaseHandler.addStiTest(TEST_USER, "01 Feb 2026", "Chlamydia", "Positive", null);
        assertTrue(result);
    }

    // ── getStiTests ─────────────────────────────────────────────────────────

    @Test
    void getStiTests_emptyForUnknownUser() {
        List<StiTestEntry> results = DatabaseHandler.getStiTests("__no_such_user__");
        assertTrue(results.isEmpty());
    }

    @Test
    void getStiTests_returnsAddedEntry() {
        DatabaseHandler.addStiTest(TEST_USER, "15 Jan 2026", "HIV", "Negative", "Note");
        List<StiTestEntry> entries = DatabaseHandler.getStiTests(TEST_USER);
        assertEquals(1, entries.size());
        StiTestEntry e = entries.get(0);
        assertEquals(TEST_USER, e.getUsername());
        assertEquals("15 Jan 2026", e.getTestDate());
        assertEquals("HIV", e.getStiType());
        assertEquals("Negative", e.getResult());
        assertEquals("Note", e.getNotes());
    }

    @Test
    void getStiTests_returnsMultipleEntriesNewestFirst() {
        DatabaseHandler.addStiTest(TEST_USER, "01 Jan 2026", "HIV", "Negative", "");
        DatabaseHandler.addStiTest(TEST_USER, "01 Feb 2026", "Chlamydia", "Positive", "");
        List<StiTestEntry> entries = DatabaseHandler.getStiTests(TEST_USER);
        assertEquals(2, entries.size());
        // ORDER BY id DESC — second inserted has higher id
        assertEquals("Chlamydia", entries.get(0).getStiType());
        assertEquals("HIV", entries.get(1).getStiType());
    }

    @Test
    void getStiTests_isolatedByUsername() {
        DatabaseHandler.addStiTest(TEST_USER, "01 Jan 2026", "HIV", "Negative", "");
        assertTrue(DatabaseHandler.getStiTests("different_user").isEmpty());
    }

    // ── deleteStiTest ───────────────────────────────────────────────────────

    @Test
    void deleteStiTest_removesEntry() {
        DatabaseHandler.addStiTest(TEST_USER, "01 Feb 2026", "HIV", "Negative", "");
        List<StiTestEntry> before = DatabaseHandler.getStiTests(TEST_USER);
        assertEquals(1, before.size());
        boolean deleted = DatabaseHandler.deleteStiTest(before.get(0).getId(), TEST_USER);
        assertTrue(deleted);
        assertTrue(DatabaseHandler.getStiTests(TEST_USER).isEmpty());
    }

    @Test
    void deleteStiTest_wrongUserReturnsFalse() {
        DatabaseHandler.addStiTest(TEST_USER, "01 Feb 2026", "HIV", "Negative", "");
        int id = DatabaseHandler.getStiTests(TEST_USER).get(0).getId();
        boolean deleted = DatabaseHandler.deleteStiTest(id, "other_user");
        assertFalse(deleted);
        assertEquals(1, DatabaseHandler.getStiTests(TEST_USER).size());
    }

    @Test
    void deleteStiTest_nonExistentIdReturnsFalse() {
        boolean deleted = DatabaseHandler.deleteStiTest(Integer.MAX_VALUE, TEST_USER);
        assertFalse(deleted);
    }

    // ── getLatestTestDate ───────────────────────────────────────────────────

    @Test
    void getLatestTestDate_nullWhenNoTests() {
        assertNull(DatabaseHandler.getLatestTestDate(TEST_USER));
    }

    @Test
    void getLatestTestDate_returnsLastInserted() {
        DatabaseHandler.addStiTest(TEST_USER, "01 Jan 2026", "HIV", "Negative", "");
        DatabaseHandler.addStiTest(TEST_USER, "01 Mar 2026", "HIV", "Negative", "");
        // ORDER BY id DESC LIMIT 1 — last inserted
        assertEquals("01 Mar 2026", DatabaseHandler.getLatestTestDate(TEST_USER));
    }

    // ── getTestPermissions ──────────────────────────────────────────────────

    @Test
    void getTestPermissions_defaultsToFalseWhenNoRow() {
        boolean[] perms = DatabaseHandler.getTestPermissions(TEST_USER);
        assertFalse(perms[0]); // allow_reminders
        assertFalse(perms[1]); // allow_share_with_doctor
    }

    // ── saveTestPermissions ─────────────────────────────────────────────────

    @Test
    void saveTestPermissions_returnsTrue() {
        assertTrue(DatabaseHandler.saveTestPermissions(TEST_USER, true, true, 6));
    }

    @Test
    void saveTestPermissions_persistsAndReadsBack() {
        DatabaseHandler.saveTestPermissions(TEST_USER, true, true, 6);
        boolean[] perms = DatabaseHandler.getTestPermissions(TEST_USER);
        assertTrue(perms[0]);
        assertTrue(perms[1]);
    }

    @Test
    void saveTestPermissions_upsertOverwritesPrevious() {
        DatabaseHandler.saveTestPermissions(TEST_USER, true, true, 3);
        DatabaseHandler.saveTestPermissions(TEST_USER, false, false, 12);
        boolean[] perms = DatabaseHandler.getTestPermissions(TEST_USER);
        assertFalse(perms[0]);
        assertFalse(perms[1]);
    }

    @Test
    void saveTestPermissions_partialFlags() {
        DatabaseHandler.saveTestPermissions(TEST_USER, true, false, 3);
        boolean[] perms = DatabaseHandler.getTestPermissions(TEST_USER);
        assertTrue(perms[0]);
        assertFalse(perms[1]);
    }

    // ── getReminderIntervalMonths ───────────────────────────────────────────

    @Test
    void getReminderIntervalMonths_defaultsToThreeWhenNoRow() {
        assertEquals(3, DatabaseHandler.getReminderIntervalMonths(TEST_USER));
    }

    @Test
    void getReminderIntervalMonths_returnsSavedValue() {
        DatabaseHandler.saveTestPermissions(TEST_USER, true, false, 6);
        assertEquals(6, DatabaseHandler.getReminderIntervalMonths(TEST_USER));
    }

    @Test
    void getReminderIntervalMonths_upsertedValueIsReturned() {
        DatabaseHandler.saveTestPermissions(TEST_USER, true, false, 3);
        DatabaseHandler.saveTestPermissions(TEST_USER, true, false, 12);
        assertEquals(12, DatabaseHandler.getReminderIntervalMonths(TEST_USER));
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private void cleanStiTestData() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement p = conn.prepareStatement("DELETE FROM sti_tests WHERE username = ?")) {
            p.setString(1, TEST_USER);
            p.executeUpdate();
        } catch (Exception ignored) {}
    }

    private void cleanTestPermissions() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement p = conn.prepareStatement("DELETE FROM test_permissions WHERE username = ?")) {
            p.setString(1, TEST_USER);
            p.executeUpdate();
        } catch (Exception ignored) {}
    }
}
