package app.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class DatabaseHandler {
    private static final String DB_URL = "jdbc:sqlite:useraccounts.db";

    private DatabaseHandler() {
    }

    private static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
        return conn;
    }

    // Creates both the users and clinics tables if they do not already exist
    public static void initialiseDatabase() {
        String usersSql = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT NOT NULL UNIQUE,"
                + "password TEXT NOT NULL"
                + ");";

        // Separate table allows for clinic-specific columns to be added later
        String clinicsSql = "CREATE TABLE IF NOT EXISTS clinics ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT NOT NULL UNIQUE,"
                + "password TEXT NOT NULL"
                + ");";

        String stiTestsSql = "CREATE TABLE IF NOT EXISTS sti_tests ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT NOT NULL,"
                + "test_date TEXT NOT NULL,"
                + "sti_type TEXT NOT NULL,"
                + "result TEXT NOT NULL,"
                + "notes TEXT"
                + ");";

        String testPermissionsSql = "CREATE TABLE IF NOT EXISTS test_permissions ("
                + "username TEXT PRIMARY KEY,"
                + "allow_reminders INTEGER NOT NULL DEFAULT 0,"
                + "allow_share_with_doctor INTEGER NOT NULL DEFAULT 0"
                + ");";

        try (Connection conn = connect()) {
            if (conn == null) {
                System.out.println("Error initialising database: connection unavailable.");
                return;
            }

        String treatmentPlansSql = "CREATE TABLE IF NOT EXISTS treatment_plans ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT NOT NULL,"
                + "medication_name TEXT NOT NULL,"
                + "dose TEXT NOT NULL,"
                + "frequency TEXT NOT NULL,"
                + "quantity_remaining REAL NOT NULL DEFAULT 0,"
                + "low_stock_threshold INTEGER NOT NULL DEFAULT 7,"
                + "start_date TEXT NOT NULL,"
                + "notes TEXT,"
                + "active INTEGER NOT NULL DEFAULT 1"
                + ");";

        String treatmentPermissionsSql = "CREATE TABLE IF NOT EXISTS treatment_permissions ("
                + "username TEXT PRIMARY KEY,"
                + "allow_treatment_reminders INTEGER NOT NULL DEFAULT 0,"
                + "allow_clinic_access INTEGER NOT NULL DEFAULT 0"
                + ");";

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(usersSql);
                stmt.execute(clinicsSql);
                stmt.execute(stiTestsSql);
                stmt.execute(testPermissionsSql);
                stmt.execute(treatmentPlansSql);
                stmt.execute(treatmentPermissionsSql);
                System.out.println("All tables are ready.");
            }

            // Migration: add reminder_interval_months to existing test_permissions rows
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE test_permissions ADD COLUMN reminder_interval_months INTEGER NOT NULL DEFAULT 3");
            } catch (SQLException ignored) {
                // Column already exists — safe to ignore
            }

            // Migration: add source column to treatment_plans (patient vs clinic)
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE treatment_plans ADD COLUMN source TEXT NOT NULL DEFAULT 'patient'");
            } catch (SQLException ignored) {}

            // Migration: add patient_note column to treatment_plans
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE treatment_plans ADD COLUMN patient_note TEXT");
            } catch (SQLException ignored) {}

        } catch (SQLException e) {
            System.out.println("Error initialising database: " + e.getMessage());
        }
    }

    // Returns the test_date string of the most recently logged test, or null if none
    public static String getLatestTestDate(String username) {
        String sql = "SELECT test_date FROM sti_tests WHERE username = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = connect()) {
            if (conn == null) return null;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) return rs.getString("test_date");
            }
        } catch (SQLException e) {
            System.out.println("getLatestTestDate error: " + e.getMessage());
        }
        return null;
    }

    // Returns the reminder interval in months for a user; defaults to 3
    public static int getReminderIntervalMonths(String username) {
        String sql = "SELECT reminder_interval_months FROM test_permissions WHERE username = ?";
        try (Connection conn = connect()) {
            if (conn == null) return 3;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) return rs.getInt("reminder_interval_months");
            }
        } catch (SQLException e) {
            System.out.println("getReminderIntervalMonths error: " + e.getMessage());
        }
        return 3;
    }

    // Inserts a new STI test entry for the given user
    public static boolean addStiTest(String username, String testDate, String stiType, String result, String notes) {
        String sql = "INSERT INTO sti_tests (username, test_date, sti_type, result, notes) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connect()) {
            if (conn == null) return false;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, testDate);
                pstmt.setString(3, stiType);
                pstmt.setString(4, result);
                pstmt.setString(5, notes);
                pstmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.out.println("addStiTest error: " + e.getMessage());
            return false;
        }
    }

    // Returns all STI test entries for a user, newest first
    public static List<StiTestEntry> getStiTests(String username) {
        String sql = "SELECT id, username, test_date, sti_type, result, notes FROM sti_tests WHERE username = ? ORDER BY id DESC";
        List<StiTestEntry> entries = new ArrayList<>();
        try (Connection conn = connect()) {
            if (conn == null) return entries;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    entries.add(new StiTestEntry(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("test_date"),
                            rs.getString("sti_type"),
                            rs.getString("result"),
                            rs.getString("notes")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("getStiTests error: " + e.getMessage());
        }
        return entries;
    }

    // Deletes a specific STI test entry; username check prevents cross-user deletion
    public static boolean deleteStiTest(int id, String username) {
        String sql = "DELETE FROM sti_tests WHERE id = ? AND username = ?";
        try (Connection conn = connect()) {
            if (conn == null) return false;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.setString(2, username);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("deleteStiTest error: " + e.getMessage());
            return false;
        }
    }

    // Returns [allowReminders, allowShareWithDoctor] for a user; defaults to false if no row exists
    public static boolean[] getTestPermissions(String username) {
        String sql = "SELECT allow_reminders, allow_share_with_doctor FROM test_permissions WHERE username = ?";
        try (Connection conn = connect()) {
            if (conn == null) return new boolean[]{false, false};
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return new boolean[]{rs.getInt("allow_reminders") == 1, rs.getInt("allow_share_with_doctor") == 1};
                }
            }
        } catch (SQLException e) {
            System.out.println("getTestPermissions error: " + e.getMessage());
        }
        return new boolean[]{false, false};
    }

    // Upserts test permissions for a user including reminder interval
    public static boolean saveTestPermissions(String username, boolean allowReminders, boolean allowShareWithDoctor, int reminderIntervalMonths) {
        String sql = "INSERT INTO test_permissions (username, allow_reminders, allow_share_with_doctor, reminder_interval_months) VALUES (?, ?, ?, ?)"
                + " ON CONFLICT(username) DO UPDATE SET allow_reminders = excluded.allow_reminders,"
                + " allow_share_with_doctor = excluded.allow_share_with_doctor,"
                + " reminder_interval_months = excluded.reminder_interval_months";
        try (Connection conn = connect()) {
            if (conn == null) return false;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setInt(2, allowReminders ? 1 : 0);
                pstmt.setInt(3, allowShareWithDoctor ? 1 : 0);
                pstmt.setInt(4, reminderIntervalMonths);
                pstmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.out.println("saveTestPermissions error: " + e.getMessage());
            return false;
        }
    }

    // Safely maps the loginType string to the correct database table
    private static String getTableName(String accountType) {
        if ("CLINIC".equals(accountType)) {
            return "clinics";
        }
        return "users";
    }

    // Registers a new user or clinic into the respective database table
    public static boolean registerUser(String username, String password, String accountType) {
        String tableName = getTableName(accountType);

        // Dynamic table name insertion
        String sql = "INSERT INTO " + tableName + "(username, password) VALUES(?, ?)";

        try (Connection conn = connect()) {
            if (conn == null) {
                System.out.println("Database Insert Error: connection unavailable.");
                return false;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.executeUpdate();
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Database Insert Error: " + e.getMessage());
            return false;
        }
    }

    // Convenience overload used by tests — defaults source to "patient"
    public static boolean addTreatment(String username, String medicationName, String dose,
                                       String frequency, double quantityRemaining,
                                       int lowStockThreshold, String startDate, String notes) {
        return addTreatment(username, medicationName, dose, frequency, quantityRemaining,
                lowStockThreshold, startDate, notes, "patient");
    }

    // Inserts a new treatment plan entry; source is "patient" or "clinic"
    public static boolean addTreatment(String username, String medicationName, String dose,
                                       String frequency, double quantityRemaining,
                                       int lowStockThreshold, String startDate, String notes,
                                       String source) {
        String sql = "INSERT INTO treatment_plans (username, medication_name, dose, frequency, "
                + "quantity_remaining, low_stock_threshold, start_date, notes, active, source) "
                + "VALUES (?,?,?,?,?,?,?,?,1,?)";
        try (Connection conn = connect()) {
            if (conn == null) return false;
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setString(1, username);
                p.setString(2, medicationName);
                p.setString(3, dose);
                p.setString(4, frequency);
                p.setDouble(5, quantityRemaining);
                p.setInt(6, lowStockThreshold);
                p.setString(7, startDate);
                p.setString(8, notes);
                p.setString(9, source != null ? source : "patient");
                p.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.out.println("addTreatment error: " + e.getMessage());
            return false;
        }
    }

    // Returns active treatment plans for a user, most recent first
    public static List<TreatmentEntry> getTreatments(String username) {
        String sql = "SELECT id, username, medication_name, dose, frequency, quantity_remaining, "
                + "low_stock_threshold, start_date, notes, active, source, patient_note "
                + "FROM treatment_plans WHERE username = ? AND active = 1 ORDER BY id DESC";
        List<TreatmentEntry> entries = new ArrayList<>();
        try (Connection conn = connect()) {
            if (conn == null) return entries;
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setString(1, username);
                ResultSet rs = p.executeQuery();
                while (rs.next()) {
                    entries.add(mapTreatmentRow(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("getTreatments error: " + e.getMessage());
        }
        return entries;
    }

    // Updates the quantity_remaining for a treatment; usernameGuard prevents cross-user edits
    public static boolean updateTreatmentQuantity(int id, String usernameGuard, double newQuantity) {
        String sql = "UPDATE treatment_plans SET quantity_remaining = ? WHERE id = ? AND username = ?";
        try (Connection conn = connect()) {
            if (conn == null) return false;
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setDouble(1, newQuantity);
                p.setInt(2, id);
                p.setString(3, usernameGuard);
                return p.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("updateTreatmentQuantity error: " + e.getMessage());
            return false;
        }
    }

    // Marks a treatment as inactive (soft delete)
    public static boolean deleteTreatment(int id, String usernameGuard) {
        String sql = "UPDATE treatment_plans SET active = 0 WHERE id = ? AND username = ?";
        try (Connection conn = connect()) {
            if (conn == null) return false;
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setInt(1, id);
                p.setString(2, usernameGuard);
                return p.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("deleteTreatment error: " + e.getMessage());
            return false;
        }
    }

    // Clinic: fetches active treatment plans for any patient (no username guard — caller must check permission)
    public static List<TreatmentEntry> getPatientTreatmentsForClinic(String patientUsername) {
        String sql = "SELECT id, username, medication_name, dose, frequency, quantity_remaining, "
                + "low_stock_threshold, start_date, notes, active, source, patient_note "
                + "FROM treatment_plans WHERE username = ? AND active = 1 ORDER BY id DESC";
        List<TreatmentEntry> entries = new ArrayList<>();
        try (Connection conn = connect()) {
            if (conn == null) return entries;
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setString(1, patientUsername);
                ResultSet rs = p.executeQuery();
                while (rs.next()) entries.add(mapTreatmentRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("getPatientTreatmentsForClinic error: " + e.getMessage());
        }
        return entries;
    }

    // Clinic: updates full treatment plan details
    public static boolean updateTreatmentPlan(int id, String patientUsername, String medicationName,
                                              String dose, String frequency,
                                              double quantityRemaining, int lowStockThreshold, String notes) {
        String sql = "UPDATE treatment_plans SET medication_name=?, dose=?, frequency=?, "
                + "quantity_remaining=?, low_stock_threshold=?, notes=? WHERE id=? AND username=?";
        try (Connection conn = connect()) {
            if (conn == null) return false;
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setString(1, medicationName);
                p.setString(2, dose);
                p.setString(3, frequency);
                p.setDouble(4, quantityRemaining);
                p.setInt(5, lowStockThreshold);
                p.setString(6, notes);
                p.setInt(7, id);
                p.setString(8, patientUsername);
                return p.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("updateTreatmentPlan error: " + e.getMessage());
            return false;
        }
    }

    // Returns [allowTreatmentReminders, allowClinicAccess] for a user
    public static boolean[] getTreatmentPermissions(String username) {
        String sql = "SELECT allow_treatment_reminders, allow_clinic_access FROM treatment_permissions WHERE username = ?";
        try (Connection conn = connect()) {
            if (conn == null) return new boolean[]{false, false};
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setString(1, username);
                ResultSet rs = p.executeQuery();
                if (rs.next()) {
                    return new boolean[]{rs.getInt("allow_treatment_reminders") == 1,
                            rs.getInt("allow_clinic_access") == 1};
                }
            }
        } catch (SQLException e) {
            System.out.println("getTreatmentPermissions error: " + e.getMessage());
        }
        return new boolean[]{false, false};
    }

    // Upserts treatment permissions for a user
    public static boolean saveTreatmentPermissions(String username, boolean allowReminders, boolean allowClinicAccess) {
        String sql = "INSERT INTO treatment_permissions (username, allow_treatment_reminders, allow_clinic_access) VALUES (?,?,?)"
                + " ON CONFLICT(username) DO UPDATE SET allow_treatment_reminders = excluded.allow_treatment_reminders,"
                + " allow_clinic_access = excluded.allow_clinic_access";
        try (Connection conn = connect()) {
            if (conn == null) return false;
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setString(1, username);
                p.setInt(2, allowReminders ? 1 : 0);
                p.setInt(3, allowClinicAccess ? 1 : 0);
                p.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.out.println("saveTreatmentPermissions error: " + e.getMessage());
            return false;
        }
    }

    // Updates the patient's note to clinic on a treatment they own
    public static boolean updatePatientNote(int id, String usernameGuard, String note) {
        String sql = "UPDATE treatment_plans SET patient_note = ? WHERE id = ? AND username = ?";
        try (Connection conn = connect()) {
            if (conn == null) return false;
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setString(1, note);
                p.setInt(2, id);
                p.setString(3, usernameGuard);
                return p.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("updatePatientNote error: " + e.getMessage());
            return false;
        }
    }

    private static TreatmentEntry mapTreatmentRow(ResultSet rs) throws SQLException {
        return new TreatmentEntry(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("medication_name"),
                rs.getString("dose"),
                rs.getString("frequency"),
                rs.getDouble("quantity_remaining"),
                rs.getInt("low_stock_threshold"),
                rs.getString("start_date"),
                rs.getString("notes"),
                rs.getInt("active") == 1,
                rs.getString("source"),
                rs.getString("patient_note")
        );
    }

    // Checks if a user or clinic exists in the respective database table
    public static boolean checkLogin(String username, String password, String accountType) {
        String tableName = getTableName(accountType);

        // Dynamic table name insertion
        String sql = "SELECT * FROM " + tableName + " WHERE username = ? AND password = ?";

        try (Connection conn = connect()) {
            if (conn == null) {
                System.out.println("Database Login Check Error: connection unavailable.");
                return false;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                ResultSet rs = pstmt.executeQuery();

                return rs.next();
            }

        } catch (SQLException e) {
            System.out.println("Database Login Check Error: " + e.getMessage());
            return false;
        }
    }
}