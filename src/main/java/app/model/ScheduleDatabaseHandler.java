package app.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDatabaseHandler {
    private static final String DB_URL = "jdbc:sqlite:useraccounts.db";

    private static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
        return conn;
    }

    public static void initialiseScheduleDatabase() {
        String appointmentsSql = "CREATE TABLE IF NOT EXISTS appointments ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "clinic_username TEXT NOT NULL,"
                + "appointment_date TEXT NOT NULL,"
                + "appointment_time TEXT NOT NULL,"
                + "is_booked INTEGER NOT NULL DEFAULT 0,"
                + "patient_username TEXT"
                + ");";

        try (Connection conn = connect()) {
            if (conn == null) {
                System.out.println("Error initialising schedule database: connection unavailable.");
                return;
            }
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(appointmentsSql);
                System.out.println("Appointments table is ready.");
            }
        } catch (SQLException e) {
            System.out.println("Error initialising schedule database: " + e.getMessage());
        }
    }

    public static boolean addAvailableSlot(String clinicUsername, String date, String time) {
        String sql = "INSERT INTO appointments (clinic_username, appointment_date, appointment_time, is_booked) VALUES (?, ?, ?, 0)";
        try (Connection conn = connect()) {
            if (conn == null)
                return false;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, clinicUsername);
                pstmt.setString(2, date);
                pstmt.setString(3, time);
                pstmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.out.println("addAvailableSlot error: " + e.getMessage());
            return false;
        }
    }

    public static List<AppointmentSlot> getAvailableSlotsForDate(String clinicUsername, String date) {
        String sql = "SELECT * FROM appointments WHERE clinic_username = ? AND appointment_date = ? AND is_booked = 0 ORDER BY appointment_time ASC";
        List<AppointmentSlot> slots = new ArrayList<>();
        try (Connection conn = connect()) {
            if (conn == null)
                return slots;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, clinicUsername);
                pstmt.setString(2, date);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    slots.add(new AppointmentSlot(
                            rs.getInt("id"),
                            rs.getString("clinic_username"),
                            rs.getString("appointment_date"),
                            rs.getString("appointment_time"),
                            rs.getInt("is_booked") == 1,
                            rs.getString("patient_username")));
                }
            }
        } catch (SQLException e) {
            System.out.println("getAvailableSlotsForDate error: " + e.getMessage());
        }
        return slots;
    }

    public static boolean bookSlot(int slotId, String patientUsername) {
        String sql = "UPDATE appointments SET is_booked = 1, patient_username = ? WHERE id = ? AND is_booked = 0";
        try (Connection conn = connect()) {
            if (conn == null)
                return false;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, patientUsername);
                pstmt.setInt(2, slotId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("bookSlot error: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteSlot(int slotId, String clinicUsername) {
        String sql = "DELETE FROM appointments WHERE id = ? AND clinic_username = ?";
        try (Connection conn = connect()) {
            if (conn == null)
                return false;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, slotId);
                pstmt.setString(2, clinicUsername);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("deleteSlot error: " + e.getMessage());
            return false;
        }
    }
}