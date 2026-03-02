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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for P&S (Privacy & Settings) permissions and the dashboard notification
 * aggregation logic in AppController.
 *
 * AppController is constructed with a null AppView because none of the methods
 * under test call updateView() or any UI method.
 */
@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock(value = "useraccounts.db", mode = ResourceAccessMode.READ_WRITE)
class NotificationAndPermissionsTests {

    private static final String TEST_USER = "__test_notif_user__";
    private static final String DB_URL = "jdbc:sqlite:useraccounts.db";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private AppController controller;

    @BeforeEach
    void setup() {
        DatabaseHandler.initialiseDatabase();
        AppModel model = new AppModel();
        model.setCurrentUser(TEST_USER);
        controller = new AppController(model, null); // null view — no UI methods invoked
        cleanAll();
    }

    @AfterEach
    void teardown() {
        cleanAll();
    }

    // ── shouldShowReminder ──────────────────────────────────────────────────

    @Test
    void shouldShowReminder_falseWhenRemindersDisabled() {
        DatabaseHandler.saveTestPermissions(TEST_USER, false, false, 3);
        assertFalse(controller.shouldShowReminder());
    }

    @Test
    void shouldShowReminder_trueWhenNoTestsAndRemindersEnabled() {
        // No STI tests at all → should always remind
        DatabaseHandler.saveTestPermissions(TEST_USER, true, false, 3);
        assertTrue(controller.shouldShowReminder());
    }

    @Test
    void shouldShowReminder_falseWhenNoPermissionRow() {
        // No row in test_permissions → defaults to allow_reminders=false
        assertFalse(controller.shouldShowReminder());
    }

    @Test
    void shouldShowReminder_falseWhenTestedRecently() {
        DatabaseHandler.saveTestPermissions(TEST_USER, true, false, 3);
        String yesterday = LocalDate.now().minusDays(1).format(FMT);
        DatabaseHandler.addStiTest(TEST_USER, yesterday, "HIV", "Negative", "");
        assertFalse(controller.shouldShowReminder());
    }

    @Test
    void shouldShowReminder_trueWhenTestOverdue() {
        DatabaseHandler.saveTestPermissions(TEST_USER, true, false, 3);
        String twoYearsAgo = LocalDate.now().minusYears(2).format(FMT);
        DatabaseHandler.addStiTest(TEST_USER, twoYearsAgo, "HIV", "Negative", "");
        assertTrue(controller.shouldShowReminder());
    }

    @Test
    void shouldShowReminder_falseAtExactIntervalBoundary() {
        // isAfter(last.plusMonths(3)) — exactly at boundary is NOT after, so false
        DatabaseHandler.saveTestPermissions(TEST_USER, true, false, 3);
        String exactlyThreeMonthsAgo = LocalDate.now().minusMonths(3).format(FMT);
        DatabaseHandler.addStiTest(TEST_USER, exactlyThreeMonthsAgo, "HIV", "Negative", "");
        assertFalse(controller.shouldShowReminder());
    }

    @Test
    void shouldShowReminder_trueOneDayPastInterval() {
        DatabaseHandler.saveTestPermissions(TEST_USER, true, false, 3);
        String justOverThreeMonthsAgo = LocalDate.now().minusMonths(3).minusDays(1).format(FMT);
        DatabaseHandler.addStiTest(TEST_USER, justOverThreeMonthsAgo, "HIV", "Negative", "");
        assertTrue(controller.shouldShowReminder());
    }

    @Test
    void shouldShowReminder_respectsCustomInterval() {
        // Interval set to 1 month; last test was 40 days ago → overdue
        DatabaseHandler.saveTestPermissions(TEST_USER, true, false, 1);
        String fortyDaysAgo = LocalDate.now().minusDays(40).format(FMT);
        DatabaseHandler.addStiTest(TEST_USER, fortyDaysAgo, "HIV", "Negative", "");
        assertTrue(controller.shouldShowReminder());
    }

    @Test
    void shouldShowReminder_falseWithinCustomInterval() {
        // Interval set to 6 months; last test was 2 months ago → not overdue
        DatabaseHandler.saveTestPermissions(TEST_USER, true, false, 6);
        String twoMonthsAgo = LocalDate.now().minusMonths(2).format(FMT);
        DatabaseHandler.addStiTest(TEST_USER, twoMonthsAgo, "HIV", "Negative", "");
        assertFalse(controller.shouldShowReminder());
    }

    // ── getReminderMessage ──────────────────────────────────────────────────

    @Test
    void getReminderMessage_nullWhenRemindersDisabled() {
        DatabaseHandler.saveTestPermissions(TEST_USER, false, false, 3);
        assertNull(controller.getReminderMessage());
    }

    @Test
    void getReminderMessage_nullWhenNoPermissionRow() {
        assertNull(controller.getReminderMessage());
    }

    @Test
    void getReminderMessage_noTestsMessage() {
        DatabaseHandler.saveTestPermissions(TEST_USER, true, false, 3);
        String msg = controller.getReminderMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("haven't logged any STI tests"));
    }

    @Test
    void getReminderMessage_overdueContainsIntervalAndDate() {
        DatabaseHandler.saveTestPermissions(TEST_USER, true, false, 3);
        String overdueDate = LocalDate.now().minusYears(1).format(FMT);
        DatabaseHandler.addStiTest(TEST_USER, overdueDate, "HIV", "Negative", "");
        String msg = controller.getReminderMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("3 month"), "Message should mention the interval");
        assertTrue(msg.contains(overdueDate), "Message should mention the last test date");
    }

    @Test
    void getReminderMessage_usesCorrectCustomInterval() {
        DatabaseHandler.saveTestPermissions(TEST_USER, true, false, 6);
        String overdueDate = LocalDate.now().minusYears(1).format(FMT);
        DatabaseHandler.addStiTest(TEST_USER, overdueDate, "HIV", "Negative", "");
        String msg = controller.getReminderMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("6 month"));
    }

    // ── P&S: test permissions via controller ────────────────────────────────

    @Test
    void getTestPermissions_defaultFalseWhenNoRow() {
        boolean[] perms = controller.getTestPermissions();
        assertFalse(perms[0]); // allow_reminders
        assertFalse(perms[1]); // allow_share_with_doctor
    }

    @Test
    void saveAndGetTestPermissions_bothEnabled() {
        assertTrue(controller.saveTestPermissions(true, true, 6));
        boolean[] perms = controller.getTestPermissions();
        assertTrue(perms[0]);
        assertTrue(perms[1]);
    }

    @Test
    void saveAndGetTestPermissions_remindersOnlyNoShare() {
        controller.saveTestPermissions(true, false, 3);
        boolean[] perms = controller.getTestPermissions();
        assertTrue(perms[0]);
        assertFalse(perms[1]);
    }

    @Test
    void saveTestPermissions_upsertOverwritesPrevious() {
        controller.saveTestPermissions(true, true, 3);
        controller.saveTestPermissions(false, false, 12);
        boolean[] perms = controller.getTestPermissions();
        assertFalse(perms[0]);
        assertFalse(perms[1]);
    }

    @Test
    void getReminderIntervalMonths_defaultThreeWhenNoRow() {
        assertEquals(3, controller.getReminderIntervalMonths());
    }

    @Test
    void getReminderIntervalMonths_returnsSavedValue() {
        controller.saveTestPermissions(true, false, 6);
        assertEquals(6, controller.getReminderIntervalMonths());
    }

    @Test
    void getReminderIntervalMonths_upsertedValueIsUsed() {
        controller.saveTestPermissions(true, false, 3);
        controller.saveTestPermissions(true, false, 12);
        assertEquals(12, controller.getReminderIntervalMonths());
    }

    // ── P&S: treatment permissions via controller ───────────────────────────

    @Test
    void getTreatmentPermissions_defaultFalseWhenNoRow() {
        boolean[] perms = controller.getTreatmentPermissions();
        assertFalse(perms[0]); // allow_treatment_reminders
        assertFalse(perms[1]); // allow_clinic_access
    }

    @Test
    void saveAndGetTreatmentPermissions_bothEnabled() {
        assertTrue(controller.saveTreatmentPermissions(true, true));
        boolean[] perms = controller.getTreatmentPermissions();
        assertTrue(perms[0]);
        assertTrue(perms[1]);
    }

    @Test
    void saveAndGetTreatmentPermissions_remindersOnlyNoClinicAccess() {
        controller.saveTreatmentPermissions(true, false);
        boolean[] perms = controller.getTreatmentPermissions();
        assertTrue(perms[0]);
        assertFalse(perms[1]);
    }

    @Test
    void saveAndGetTreatmentPermissions_clinicAccessOnlyNoReminders() {
        controller.saveTreatmentPermissions(false, true);
        boolean[] perms = controller.getTreatmentPermissions();
        assertFalse(perms[0]);
        assertTrue(perms[1]);
    }

    @Test
    void saveTreatmentPermissions_upsertOverwritesPrevious() {
        controller.saveTreatmentPermissions(true, true);
        controller.saveTreatmentPermissions(false, false);
        boolean[] perms = controller.getTreatmentPermissions();
        assertFalse(perms[0]);
        assertFalse(perms[1]);
    }

    // ── getPendingNotifications ─────────────────────────────────────────────

    @Test
    void getPendingNotifications_emptyWhenNoPermissionsSet() {
        // No permission rows at all → everything defaults to disabled
        assertTrue(controller.getPendingNotifications().isEmpty());
    }

    @Test
    void getPendingNotifications_emptyWhenAllPermissionsDisabled() {
        controller.saveTestPermissions(false, false, 3);
        controller.saveTreatmentPermissions(false, false);
        assertTrue(controller.getPendingNotifications().isEmpty());
    }

    @Test
    void getPendingNotifications_includesTestReminderWhenOverdue() {
        controller.saveTestPermissions(true, false, 3);
        String overdueDate = LocalDate.now().minusYears(1).format(FMT);
        DatabaseHandler.addStiTest(TEST_USER, overdueDate, "HIV", "Negative", "");
        List<String> notifications = controller.getPendingNotifications();
        assertEquals(1, notifications.size());
        assertTrue(notifications.get(0).contains("month"));
    }

    @Test
    void getPendingNotifications_noTestReminderWhenRecentlyTested() {
        controller.saveTestPermissions(true, false, 3);
        String recentDate = LocalDate.now().minusDays(5).format(FMT);
        DatabaseHandler.addStiTest(TEST_USER, recentDate, "HIV", "Negative", "");
        assertTrue(controller.getPendingNotifications().isEmpty());
    }

    @Test
    void getPendingNotifications_includesTreatmentLowStock() {
        controller.saveTreatmentPermissions(true, false);
        // 5 doses "Once daily" with threshold 7 → 5 days remaining ≤ 7 → low stock
        controller.addTreatment("PrEP", "1 tablet", "Once daily", 5, 7, "01 Jan 2026", "");
        List<String> notifications = controller.getPendingNotifications();
        assertEquals(1, notifications.size());
        assertTrue(notifications.get(0).contains("PrEP"));
    }

    @Test
    void getPendingNotifications_excludesTreatmentWhenRemindersOff() {
        controller.saveTreatmentPermissions(false, false);
        controller.addTreatment("PrEP", "1 tablet", "Once daily", 5, 7, "01 Jan 2026", "");
        assertTrue(controller.getPendingNotifications().isEmpty());
    }

    @Test
    void getPendingNotifications_excludesWellStockedTreatments() {
        controller.saveTreatmentPermissions(true, false);
        // 30 doses "Once daily", threshold 7 → 30 days remaining > 7 → not low stock
        controller.addTreatment("PrEP", "1 tablet", "Once daily", 30, 7, "01 Jan 2026", "");
        assertTrue(controller.getPendingNotifications().isEmpty());
    }

    @Test
    void getPendingNotifications_excludesAsNeededTreatmentsAlways() {
        controller.saveTreatmentPermissions(true, false);
        // As needed: dosesPerDay=0 → isLowStock always false
        controller.addTreatment("Ibuprofen", "400 mg", "As needed", 0, 7, "01 Jan 2026", "");
        assertTrue(controller.getPendingNotifications().isEmpty());
    }

    @Test
    void getPendingNotifications_combinesTestAndTreatmentNotifications() {
        // Both STI overdue + low-stock treatment active
        controller.saveTestPermissions(true, false, 3);
        String overdueDate = LocalDate.now().minusYears(1).format(FMT);
        DatabaseHandler.addStiTest(TEST_USER, overdueDate, "HIV", "Negative", "");

        controller.saveTreatmentPermissions(true, false);
        controller.addTreatment("PrEP", "1 tablet", "Once daily", 5, 7, "01 Jan 2026", "");

        List<String> notifications = controller.getPendingNotifications();
        assertEquals(2, notifications.size());
    }

    @Test
    void getPendingNotifications_multipleLoStockTreatments() {
        controller.saveTreatmentPermissions(true, false);
        // 3 doses "Once daily", threshold 7 → low stock
        controller.addTreatment("PrEP", "1 tablet", "Once daily", 3, 7, "01 Jan 2026", "");
        // 5 doses "Twice daily" = 2.5 days, threshold 14 → low stock
        controller.addTreatment("Doxycycline", "200 mg", "Twice daily", 5, 14, "01 Jan 2026", "");
        List<String> notifications = controller.getPendingNotifications();
        assertEquals(2, notifications.size());
    }

    @Test
    void getPendingNotifications_onlyLowStockCountedNotWellStocked() {
        controller.saveTreatmentPermissions(true, false);
        controller.addTreatment("PrEP", "1 tablet", "Once daily", 3, 7, "01 Jan 2026", "");   // low stock
        controller.addTreatment("ART", "1 tablet", "Once daily", 90, 7, "01 Jan 2026", "");   // well stocked
        List<String> notifications = controller.getPendingNotifications();
        assertEquals(1, notifications.size());
        assertTrue(notifications.get(0).contains("PrEP"));
    }

    @Test
    void getPendingNotifications_treatmentReminderContainsMedicationName() {
        controller.saveTreatmentPermissions(true, false);
        controller.addTreatment("Azithromycin", "500 mg", "Once daily", 2, 7, "01 Jan 2026", "");
        List<String> notifications = controller.getPendingNotifications();
        assertEquals(1, notifications.size());
        assertTrue(notifications.get(0).contains("Azithromycin"));
    }

    @Test
    void getPendingNotifications_treatmentReminderContainsDaysRemaining() {
        controller.saveTreatmentPermissions(true, false);
        // 2 doses "Once daily" = 2 days remaining
        controller.addTreatment("PrEP", "1 tablet", "Once daily", 2, 7, "01 Jan 2026", "");
        List<String> notifications = controller.getPendingNotifications();
        assertEquals(1, notifications.size());
        assertTrue(notifications.get(0).contains("2 day"));
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private void cleanAll() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            for (String sql : List.of(
                    "DELETE FROM sti_tests WHERE username = ?",
                    "DELETE FROM test_permissions WHERE username = ?",
                    "DELETE FROM treatment_plans WHERE username = ?",
                    "DELETE FROM treatment_permissions WHERE username = ?")) {
                try (PreparedStatement p = conn.prepareStatement(sql)) {
                    p.setString(1, TEST_USER);
                    p.executeUpdate();
                }
            }
        } catch (Exception ignored) {}
    }
}
