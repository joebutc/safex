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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock(value = "useraccounts.db", mode = ResourceAccessMode.READ_WRITE)
class AppointmentSchedulingTests {

    private static final String TEST_USER = "__test_appointment_user__";
    private static final String OTHER_USER = "__test_other_appointment_user__";
    private static final String CLINIC_ONE = "__clinic_one__";
    private static final String CLINIC_TWO = "__clinic_two__";
    private static final String DB_URL = "jdbc:sqlite:useraccounts.db";
    private static final String DATE_ONE = "01 Jan 2099";
    private static final String DATE_TWO = "02 Jan 2099";

    @BeforeEach
    void setup() {
        DatabaseHandler.initialiseDatabase();
        cleanAppointments();
    }

    @AfterEach
    void teardown() {
        cleanAppointments();
    }

    @Test
    void saveAvailableTimesForDate_persistsRoundTrip() {
        assertTrue(DatabaseHandler.saveAvailableTimesForDate(DATE_ONE, List.of("09:00", "09:30", "10:00")));
        assertEquals(List.of("09:00", "09:30", "10:00"), DatabaseHandler.getAvailableTimesForDate(DATE_ONE));
    }

    @Test
    void saveAvailableTimesForDate_overwritesPreviousSelection() {
        DatabaseHandler.saveAvailableTimesForDate(DATE_ONE, List.of("09:00", "09:30"));
        DatabaseHandler.saveAvailableTimesForDate(DATE_ONE, List.of("10:00"));
        assertEquals(List.of("10:00"), DatabaseHandler.getAvailableTimesForDate(DATE_ONE));
    }

    @Test
    void bookAppointment_returnsFalseWhenSlotNotOffered() {
        assertFalse(DatabaseHandler.bookAppointment(TEST_USER, DATE_ONE, "09:00"));
    }

    @Test
    void bookAppointment_returnsTrueWhenSlotOffered() {
        DatabaseHandler.saveAvailableTimesForDate(DATE_ONE, List.of("09:00"));
        assertTrue(DatabaseHandler.bookAppointment(TEST_USER, DATE_ONE, "09:00"));
        assertEquals(List.of("09:00"), DatabaseHandler.getBookedTimesForDate(DATE_ONE));
    }

    @Test
    void bookAppointment_preventsDoubleBookingSameSlot() {
        DatabaseHandler.saveAvailableTimesForDate(DATE_ONE, List.of("09:00"));
        assertTrue(DatabaseHandler.bookAppointment(TEST_USER, DATE_ONE, "09:00"));
        assertFalse(DatabaseHandler.bookAppointment("__other_user__", DATE_ONE, "09:00"));
    }

    @Test
    void bookAppointment_supportsClinicSpecificSlotsAtSameTime() {
        DatabaseHandler.saveAvailableTimesForDate(CLINIC_ONE, DATE_ONE, List.of("09:00"));
        DatabaseHandler.saveAvailableTimesForDate(CLINIC_TWO, DATE_ONE, List.of("09:00"));

        assertTrue(DatabaseHandler.bookAppointment(TEST_USER, CLINIC_ONE, DATE_ONE, "09:00"));
        assertTrue(DatabaseHandler.bookAppointment(OTHER_USER, CLINIC_TWO, DATE_ONE, "09:00"));

        assertEquals(List.of("09:00"), DatabaseHandler.getBookedTimesForDate(DATE_ONE, CLINIC_ONE));
        assertEquals(List.of("09:00"), DatabaseHandler.getBookedTimesForDate(DATE_ONE, CLINIC_TWO));
    }

    @Test
    void bookAppointment_rejectsWrongClinicSelection() {
        DatabaseHandler.saveAvailableTimesForDate(CLINIC_ONE, DATE_ONE, List.of("10:00"));

        assertFalse(DatabaseHandler.bookAppointment(TEST_USER, CLINIC_TWO, DATE_ONE, "10:00"));
        assertTrue(DatabaseHandler.getBookedTimesForDate(DATE_ONE, CLINIC_ONE).isEmpty());
    }

    @Test
    void cancelAppointment_marksStatusCancelled() {
        DatabaseHandler.saveAvailableTimesForDate(DATE_TWO, List.of("10:30"));
        assertTrue(DatabaseHandler.bookAppointment(TEST_USER, DATE_TWO, "10:30"));
        int appointmentId = Integer.parseInt(DatabaseHandler.getAppointmentsForPatient(TEST_USER).get(0)[0]);

        assertTrue(DatabaseHandler.cancelAppointment(appointmentId, TEST_USER));
        assertTrue(DatabaseHandler.getBookedTimesForDate(DATE_TWO).isEmpty());
        assertEquals("cancelled", DatabaseHandler.getAppointmentsForPatient(TEST_USER).get(0)[3]);
    }

    private void cleanAppointments() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (PreparedStatement p = conn.prepareStatement(
                    "DELETE FROM appointments WHERE patient_username IN (?, ?) OR appointment_date IN (?, ?) OR clinic_username IN (?, ?)")) {
                p.setString(1, TEST_USER);
                p.setString(2, OTHER_USER);
                p.setString(3, DATE_ONE);
                p.setString(4, DATE_TWO);
                p.setString(5, CLINIC_ONE);
                p.setString(6, CLINIC_TWO);
                p.executeUpdate();
            }

            try (PreparedStatement p = conn.prepareStatement(
                    "DELETE FROM clinic_availability WHERE appointment_date IN (?, ?) OR clinic_username IN (?, ?)")) {
                p.setString(1, DATE_ONE);
                p.setString(2, DATE_TWO);
                p.setString(3, CLINIC_ONE);
                p.setString(4, CLINIC_TWO);
                p.executeUpdate();
            }
        } catch (Exception ignored) {
        }
    }
}
