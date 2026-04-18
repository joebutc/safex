package app.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class DatabaseHandler {
    private static final String DB_URL = "jdbc:sqlite:useraccounts.db";
    private static final String DEFAULT_CLINIC_USERNAME = "__default_clinic__";
    private static final int RECOVERY_CODE_EXPIRY_MINUTES = 15;

    private DatabaseHandler() {
    }

    private static Connection connect() {
        try {
            return DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println("Connection error: " + e.getMessage());
            return null;
        }
    }

    // Creates both the users and clinics tables if they do not already exist
    public static void initialiseDatabase() {
        String usersSql = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT NOT NULL UNIQUE,"
                + "password TEXT NOT NULL,"
                + "email TEXT,"
                + "address TEXT,"
                + "email_verified INTEGER NOT NULL DEFAULT 1"
                + ");";

        // Separate table allows for clinic-specific columns to be added later
        String clinicsSql = "CREATE TABLE IF NOT EXISTS clinics ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT NOT NULL UNIQUE,"
                + "password TEXT NOT NULL,"
                + "email TEXT,"
                + "address TEXT,"
                + "email_verified INTEGER NOT NULL DEFAULT 1"
                + ");";

        String recoveryCodesSql = "CREATE TABLE IF NOT EXISTS password_recovery_codes ("
                + "email TEXT PRIMARY KEY,"
                + "code TEXT NOT NULL,"
                + "expires_at TEXT NOT NULL"
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

        // New table for storing multiple postcodes per clinic
        String clinicPostcodesSql = "CREATE TABLE IF NOT EXISTS clinic_postcodes ("
                + "clinic_username TEXT NOT NULL,"
                + "postcode_prefix TEXT NOT NULL,"
                + "PRIMARY KEY (clinic_username, postcode_prefix)"
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
                stmt.execute(recoveryCodesSql);
                stmt.execute(stiTestsSql);
                stmt.execute(testPermissionsSql);
                stmt.execute(treatmentPlansSql);
                stmt.execute(treatmentPermissionsSql);
                stmt.execute(clinicPostcodesSql);
                System.out.println("All tables are ready.");
            }

            // Migration: add reminder_interval_months to existing test_permissions rows
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(
                        "ALTER TABLE test_permissions ADD COLUMN reminder_interval_months INTEGER NOT NULL DEFAULT 3");
            } catch (SQLException ignored) {
                // Column already exists — safe to ignore
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE users ADD COLUMN email TEXT");
            } catch (SQLException ignored) {
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE users ADD COLUMN address TEXT");
            } catch (SQLException ignored) {
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE users ADD COLUMN email_verified INTEGER NOT NULL DEFAULT 1");
            } catch (SQLException ignored) {
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE clinics ADD COLUMN email TEXT");
            } catch (SQLException ignored) {
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE clinics ADD COLUMN address TEXT");
            } catch (SQLException ignored) {
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE clinics ADD COLUMN email_verified INTEGER NOT NULL DEFAULT 1");
            } catch (SQLException ignored) {
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(
                        "CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email_unique "
                                + "ON users(email COLLATE NOCASE) WHERE email IS NOT NULL");
                stmt.execute(
                        "CREATE UNIQUE INDEX IF NOT EXISTS idx_clinics_email_unique "
                                + "ON clinics(email COLLATE NOCASE) WHERE email IS NOT NULL");
            } catch (SQLException ignored) {
            }

            // Migration: add source column to treatment_plans (patient vs clinic)
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE treatment_plans ADD COLUMN source TEXT NOT NULL DEFAULT 'patient'");
            } catch (SQLException ignored) {
            }

            // Migration: add patient_note column to treatment_plans
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE treatment_plans ADD COLUMN patient_note TEXT");
            } catch (SQLException ignored) {
            }

            // Privacy settings table
            String privacySettingsSql = "CREATE TABLE IF NOT EXISTS privacy_settings ("
                    + "username TEXT PRIMARY KEY,"
                    + "allow_internal_staff INTEGER NOT NULL DEFAULT 0,"
                    + "allow_analytics INTEGER NOT NULL DEFAULT 0,"
                    + "allow_trusted_partners INTEGER NOT NULL DEFAULT 0,"
                    + "data_retention TEXT NOT NULL DEFAULT 'While account is active',"
                    + "terms_accepted_date TEXT"
                    + ");";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(privacySettingsSql);
            } catch (SQLException ignored) {
            }

            // Appointments table
            String appointmentsSql = "CREATE TABLE IF NOT EXISTS appointments ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "clinic_username TEXT NOT NULL DEFAULT '" + DEFAULT_CLINIC_USERNAME + "',"
                    + "appointment_date TEXT NOT NULL,"
                    + "appointment_time TEXT NOT NULL,"
                    + "is_booked INTEGER NOT NULL DEFAULT 1,"
                    + "patient_username TEXT,"
                    + "status TEXT NOT NULL DEFAULT 'booked'"
                    + ");";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(appointmentsSql);
            } catch (SQLException ignored) {
            }

            String clinicAvailabilitySql = "CREATE TABLE IF NOT EXISTS clinic_availability ("
                    + "clinic_username TEXT NOT NULL DEFAULT '" + DEFAULT_CLINIC_USERNAME + "',"
                    + "appointment_date TEXT NOT NULL,"
                    + "appointment_time TEXT NOT NULL,"
                    + "PRIMARY KEY (clinic_username, appointment_date, appointment_time)"
                    + ");";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(clinicAvailabilitySql);
            } catch (SQLException ignored) {
            }

            // Migration: keep the appointments schema compatible with older DBs.
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE appointments ADD COLUMN clinic_username TEXT NOT NULL DEFAULT '"
                        + DEFAULT_CLINIC_USERNAME + "'");
            } catch (SQLException ignored) {
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE appointments ADD COLUMN is_booked INTEGER NOT NULL DEFAULT 1");
            } catch (SQLException ignored) {
            }

            // Migration: add status column to legacy appointments tables.
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE appointments ADD COLUMN status TEXT NOT NULL DEFAULT 'booked'");
            } catch (SQLException ignored) {
            }

            // Migration: keep clinic availability scoped to a clinic account.
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE clinic_availability ADD COLUMN clinic_username TEXT NOT NULL DEFAULT '"
                        + DEFAULT_CLINIC_USERNAME + "'");
            } catch (SQLException ignored) {
            }

            migrateClinicAvailabilitySchema(conn);

            // Data access log table for P2 audit trail
            String dataAccessLogSql = "CREATE TABLE IF NOT EXISTS data_access_log ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "patient_username TEXT NOT NULL,"
                    + "accessor_username TEXT NOT NULL,"
                    + "accessor_type TEXT NOT NULL,"
                    + "access_type TEXT NOT NULL,"
                    + "access_timestamp TEXT NOT NULL,"
                    + "details TEXT"
                    + ");";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(dataAccessLogSql);
            } catch (SQLException ignored) {}

            // Migration: convert old retention values to new 30/90/180 day options
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("UPDATE privacy_settings SET data_retention = '180 days' WHERE data_retention = '6 months'");
                stmt.execute("UPDATE privacy_settings SET data_retention = 'While account is active' WHERE data_retention = '1 year'");
                stmt.execute("UPDATE privacy_settings SET data_retention = 'While account is active' WHERE data_retention = 'Delete immediately'");
            } catch (SQLException ignored) {}

        } catch (SQLException e) {
            System.out.println("Error initialising database: " + e.getMessage());
        }
    }

    // ── Privacy Settings ─────────────────────────────────────────────────────

    /**
     * Returns [allowInternalStaff, allowAnalytics, allowTrustedPartners]. Defaults
     * all false.
     */
    public static boolean[] getDataAccessPermissions(String username) {
        String sql = "SELECT allow_internal_staff, allow_analytics, allow_trusted_partners "
                + "FROM privacy_settings WHERE username = ?";
        try (Connection conn = connect()) {
            if (conn == null)
                return new boolean[] { false, false, false };
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setString(1, username);
                ResultSet rs = p.executeQuery();
                if (rs.next()) {
                    return new boolean[] {
                            rs.getInt("allow_internal_staff") == 1,
                            rs.getInt("allow_analytics") == 1,
                            rs.getInt("allow_trusted_partners") == 1
                    };
                }
            }
        } catch (SQLException e) {
            System.out.println("getDataAccessPermissions error: " + e.getMessage());
        }
        return new boolean[] { false, false, false };
    }

    public static void saveDataAccessPermissions(String username,
            boolean staff, boolean analytics, boolean partners) {
        String sql = "INSERT INTO privacy_settings (username, allow_internal_staff, allow_analytics, allow_trusted_partners) "
                + "VALUES (?, ?, ?, ?) "
                + "ON CONFLICT(username) DO UPDATE SET "
                + "allow_internal_staff = excluded.allow_internal_staff, "
                + "allow_analytics = excluded.allow_analytics, "
                + "allow_trusted_partners = excluded.allow_trusted_partners";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, username);
            p.setInt(2, staff ? 1 : 0);
            p.setInt(3, analytics ? 1 : 0);
            p.setInt(4, partners ? 1 : 0);
            p.executeUpdate();
        } catch (SQLException e) {
            System.out.println("saveDataAccessPermissions error: " + e.getMessage());
        }
    }

    /** Returns the saved retention option, or the default if none set. */
    public static String getDataRetention(String username) {
        String sql = "SELECT data_retention FROM privacy_settings WHERE username = ?";
        try (Connection conn = connect()) {
            if (conn == null)
                return "While account is active";
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setString(1, username);
                ResultSet rs = p.executeQuery();
                if (rs.next())
                    return rs.getString("data_retention");
            }
        } catch (SQLException e) {
            System.out.println("getDataRetention error: " + e.getMessage());
        }
        return "While account is active";
    }

    public static void saveDataRetention(String username, String option) {
        String sql = "INSERT INTO privacy_settings (username, data_retention) VALUES (?, ?) "
                + "ON CONFLICT(username) DO UPDATE SET data_retention = excluded.data_retention";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, username);
            p.setString(2, option);
            p.executeUpdate();
        } catch (SQLException e) {
            System.out.println("saveDataRetention error: " + e.getMessage());
        }
    }

    /** Records the current date as the terms acceptance date for the user. */
    public static void saveTermsAccepted(String username) {
        String date = LocalDate.now().format(DATE_FMT);
        String sql = "INSERT INTO privacy_settings (username, terms_accepted_date) VALUES (?, ?) "
                + "ON CONFLICT(username) DO UPDATE SET terms_accepted_date = excluded.terms_accepted_date";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, username);
            p.setString(2, date);
            p.executeUpdate();
        } catch (SQLException e) {
            System.out.println("saveTermsAccepted error: " + e.getMessage());
        }
    }

    // Returns the test_date string of the most recently logged test, or null if
    // none
    public static String getLatestTestDate(String username) {
        String sql = "SELECT test_date FROM sti_tests WHERE username = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = connect()) {
            if (conn == null)
                return null;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next())
                    return rs.getString("test_date");
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
            if (conn == null)
                return 3;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next())
                    return rs.getInt("reminder_interval_months");
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
            if (conn == null)
                return false;
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
            if (conn == null)
                return entries;
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
                            rs.getString("notes")));
                }
            }
        } catch (SQLException e) {
            System.out.println("getStiTests error: " + e.getMessage());
        }
        return entries;
    }

    // Deletes a specific STI test entry; username check prevents cross-user
    // deletion
    public static boolean deleteStiTest(int id, String username) {
        String sql = "DELETE FROM sti_tests WHERE id = ? AND username = ?";
        try (Connection conn = connect()) {
            if (conn == null)
                return false;
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

    // Returns [allowReminders, allowShareWithDoctor] for a user; defaults to false
    // if no row exists
    public static boolean[] getTestPermissions(String username) {
        String sql = "SELECT allow_reminders, allow_share_with_doctor FROM test_permissions WHERE username = ?";
        try (Connection conn = connect()) {
            if (conn == null)
                return new boolean[] { false, false };
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return new boolean[] { rs.getInt("allow_reminders") == 1,
                            rs.getInt("allow_share_with_doctor") == 1 };
                }
            }
        } catch (SQLException e) {
            System.out.println("getTestPermissions error: " + e.getMessage());
        }
        return new boolean[] { false, false };
    }

    // Upserts test permissions for a user including reminder interval
    public static boolean saveTestPermissions(String username, boolean allowReminders, boolean allowShareWithDoctor,
            int reminderIntervalMonths) {
        String sql = "INSERT INTO test_permissions (username, allow_reminders, allow_share_with_doctor, reminder_interval_months) VALUES (?, ?, ?, ?)"
                + " ON CONFLICT(username) DO UPDATE SET allow_reminders = excluded.allow_reminders,"
                + " allow_share_with_doctor = excluded.allow_share_with_doctor,"
                + " reminder_interval_months = excluded.reminder_interval_months";
        try (Connection conn = connect()) {
            if (conn == null)
                return false;
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
        return "CLINIC".equals(accountType) ? "clinics" : "users";
    }

    private static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }

        String normalized = email.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizeAddress(String address) {
        if (address == null) {
            return null;
        }

        String normalized = address.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static boolean usernameExists(Connection conn, String tableName, String username) throws SQLException {
        String sql = "SELECT 1 FROM " + tableName + " WHERE LOWER(username) = ? LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username == null ? "" : username.trim().toLowerCase(Locale.ROOT));
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    private static boolean accountExistsByEmail(Connection conn, String tableName, String email) throws SQLException {
        String sql = "SELECT 1 FROM " + tableName + " WHERE LOWER(email) = ? LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    private static boolean emailExistsInAnyAccount(Connection conn, String email) throws SQLException {
        return accountExistsByEmail(conn, "users", email) || accountExistsByEmail(conn, "clinics", email);
    }

    public static boolean isRegistrationAvailable(String username, String email, String accountType) {
        String tableName = getTableName(accountType);
        String normalizedEmail = normalizeEmail(email);
        if (username == null || username.isBlank() || normalizedEmail == null) {
            return false;
        }

        try (Connection conn = connect()) {
            if (conn == null) {
                return false;
            }
            return !usernameExists(conn, tableName, username) && !emailExistsInAnyAccount(conn, normalizedEmail);
        } catch (SQLException e) {
            System.out.println("isRegistrationAvailable error: " + e.getMessage());
            return false;
        }
    }

    // Returns all clinic usernames to support patient appointment booking.
    public static List<String> getClinicUsernames() {
        String sql = "SELECT username FROM clinics ORDER BY username COLLATE NOCASE ASC";
        List<String> clinics = new ArrayList<>();
        try (Connection conn = connect()) {
            if (conn == null)
                return clinics;
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    clinics.add(rs.getString("username"));
                }
            }
        } catch (SQLException e) {
            System.out.println("getClinicUsernames error: " + e.getMessage());
        }
        return clinics;
    }

    // Registers a new user or clinic into the respective database table
    public static boolean registerUser(String username, String password, String accountType) {
        return registerUser(username, password, null, null, accountType);
    }

    public static boolean registerUser(String username, String password, String email, String accountType) {
        return registerUser(username, password, email, null, accountType);
    }

    public static boolean registerUser(String username, String password, String email, String address,
            String accountType) {
        String tableName = getTableName(accountType);
        String normalizedEmail = normalizeEmail(email);
        String normalizedAddress = normalizeAddress(address);

        // Dynamic table name insertion
        String sql = "INSERT INTO " + tableName + "(username, password, email, address, email_verified) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = connect()) {
            if (conn == null) {
                System.out.println("Database Insert Error: connection unavailable.");
                return false;
            }

            if (usernameExists(conn, tableName, username)) {
                System.out.println("Database Insert Error: username already exists.");
                return false;
            }

            if (normalizedEmail != null && emailExistsInAnyAccount(conn, normalizedEmail)) {
                System.out.println("Database Insert Error: email already exists.");
                return false;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                if (normalizedEmail == null) {
                    pstmt.setNull(3, Types.VARCHAR);
                } else {
                    pstmt.setString(3, normalizedEmail);
                }
                if (normalizedAddress == null) {
                    pstmt.setNull(4, Types.VARCHAR);
                } else {
                    pstmt.setString(4, normalizedAddress);
                }
                pstmt.setInt(5, 1);
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
            if (conn == null)
                return false;
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
            if (conn == null)
                return entries;
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

    // Updates the quantity_remaining for a treatment; usernameGuard prevents
    // cross-user edits
    public static boolean updateTreatmentQuantity(int id, String usernameGuard, double newQuantity) {
        String sql = "UPDATE treatment_plans SET quantity_remaining = ? WHERE id = ? AND username = ?";
        try (Connection conn = connect()) {
            if (conn == null)
                return false;
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
            if (conn == null)
                return false;
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

    // Clinic: fetches active treatment plans for any patient (no username guard —
    // caller must check permission)
    public static List<TreatmentEntry> getPatientTreatmentsForClinic(String patientUsername) {
        String sql = "SELECT id, username, medication_name, dose, frequency, quantity_remaining, "
                + "low_stock_threshold, start_date, notes, active, source, patient_note "
                + "FROM treatment_plans WHERE username = ? AND active = 1 ORDER BY id DESC";
        List<TreatmentEntry> entries = new ArrayList<>();
        try (Connection conn = connect()) {
            if (conn == null)
                return entries;
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setString(1, patientUsername);
                ResultSet rs = p.executeQuery();
                while (rs.next())
                    entries.add(mapTreatmentRow(rs));
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
            if (conn == null)
                return false;
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
            if (conn == null)
                return new boolean[] { false, false };
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setString(1, username);
                ResultSet rs = p.executeQuery();
                if (rs.next()) {
                    return new boolean[] { rs.getInt("allow_treatment_reminders") == 1,
                            rs.getInt("allow_clinic_access") == 1 };
                }
            }
        } catch (SQLException e) {
            System.out.println("getTreatmentPermissions error: " + e.getMessage());
        }
        return new boolean[] { false, false };
    }

    // Upserts treatment permissions for a user
    public static boolean saveTreatmentPermissions(String username, boolean allowReminders, boolean allowClinicAccess) {
        String sql = "INSERT INTO treatment_permissions (username, allow_treatment_reminders, allow_clinic_access) VALUES (?,?,?)"
                + " ON CONFLICT(username) DO UPDATE SET allow_treatment_reminders = excluded.allow_treatment_reminders,"
                + " allow_clinic_access = excluded.allow_clinic_access";
        try (Connection conn = connect()) {
            if (conn == null)
                return false;
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
            if (conn == null)
                return false;
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
                rs.getString("patient_note"));
    }

    // ── Appointments ──────────────────────────────────────────────────────────

    /**
     * Returns the offered appointment times (HH:mm) for a given date across all
     * clinics.
     */
    public static List<String> getAvailableTimesForDate(String date) {
        String sql = "SELECT DISTINCT appointment_time FROM clinic_availability "
                + "WHERE appointment_date = ? ORDER BY appointment_time";
        List<String> times = new ArrayList<>();
        try (Connection conn = connect()) {
            if (conn == null)
                return times;
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setString(1, date);
                ResultSet rs = p.executeQuery();
                while (rs.next())
                    times.add(rs.getString("appointment_time"));
            }
        } catch (SQLException e) {
            System.out.println("getAvailableTimesForDate error: " + e.getMessage());
        }
        return times;
    }

    /**
     * Returns the offered appointment times (HH:mm) for a specific clinic on a
     * given date.
     */
    public static List<String> getAvailableTimesForDate(String date, String clinicUsername) {
        String sql = "SELECT appointment_time FROM clinic_availability "
                + "WHERE appointment_date = ? AND clinic_username = ? ORDER BY appointment_time";
        List<String> times = new ArrayList<>();
        try (Connection conn = connect()) {
            if (conn == null)
                return times;
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setString(1, date);
                p.setString(2, normalizeClinicUsername(clinicUsername));
                ResultSet rs = p.executeQuery();
                while (rs.next())
                    times.add(rs.getString("appointment_time"));
            }
        } catch (SQLException e) {
            System.out.println("getAvailableTimesForDate error: " + e.getMessage());
        }
        return times;
    }

    /** Replaces the offered appointment times for a date with the provided set. */
    public static boolean saveAvailableTimesForDate(String date, List<String> times) {
        return saveAvailableTimesForDate(DEFAULT_CLINIC_USERNAME, date, times);
    }

    /**
     * Replaces the offered appointment times for a clinic on a date with the
     * provided set.
     */
    public static boolean saveAvailableTimesForDate(String clinicUsername, String date, List<String> times) {
        String deleteSql = "DELETE FROM clinic_availability WHERE clinic_username = ? AND appointment_date = ?";
        String insertSql = "INSERT INTO clinic_availability (clinic_username, appointment_date, appointment_time) "
                + "VALUES (?, ?, ?)";
        try (Connection conn = connect()) {
            if (conn == null)
                return false;
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setString(1, normalizeClinicUsername(clinicUsername));
                    deleteStmt.setString(2, date);
                    deleteStmt.executeUpdate();
                }
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    for (String time : times) {
                        insertStmt.setString(1, normalizeClinicUsername(clinicUsername));
                        insertStmt.setString(2, date);
                        insertStmt.setString(3, time);
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("saveAvailableTimesForDate error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Books a time slot for a patient. Returns false if already booked or not
     * offered.
     */
    public static boolean bookAppointment(String username, String date, String time) {
        String availabilitySql = "SELECT clinic_username FROM clinic_availability "
                + "WHERE appointment_date = ? AND appointment_time = ? "
                + "ORDER BY clinic_username LIMIT 1";
        try (Connection conn = connect()) {
            if (conn == null)
                return false;
            String clinicUsername;
            try (PreparedStatement p = conn.prepareStatement(availabilitySql)) {
                p.setString(1, date);
                p.setString(2, time);
                ResultSet rs = p.executeQuery();
                if (!rs.next())
                    return false;
                clinicUsername = rs.getString("clinic_username");
            }
            return bookAppointment(username, clinicUsername, date, time);
        } catch (SQLException e) {
            System.out.println("bookAppointment error: " + e.getMessage());
            return false;
        }
    }

    /** Books a clinic-specific time slot for a patient. */
    public static boolean bookAppointment(String username, String clinicUsername, String date, String time) {
        String normalizedClinic = normalizeClinicUsername(clinicUsername);
        String availabilitySql = "SELECT 1 FROM clinic_availability "
                + "WHERE clinic_username = ? AND appointment_date = ? AND appointment_time = ?";
        String checkSql = "SELECT id FROM appointments "
                + "WHERE clinic_username = ? AND appointment_date = ? AND appointment_time = ? "
                + "AND status = 'booked' AND is_booked = 1";
        String insertSql = "INSERT INTO appointments "
                + "(clinic_username, appointment_date, appointment_time, is_booked, patient_username, status) "
                + "VALUES (?, ?, ?, 1, ?, 'booked')";
        try (Connection conn = connect()) {
            if (conn == null)
                return false;
            try (PreparedStatement p = conn.prepareStatement(availabilitySql)) {
                p.setString(1, normalizedClinic);
                p.setString(2, date);
                p.setString(3, time);
                ResultSet rs = p.executeQuery();
                if (!rs.next())
                    return false;
            }
            try (PreparedStatement p = conn.prepareStatement(checkSql)) {
                p.setString(1, normalizedClinic);
                p.setString(2, date);
                p.setString(3, time);
                ResultSet rs = p.executeQuery();
                if (rs.next())
                    return false;
            }
            try (PreparedStatement p = conn.prepareStatement(insertSql)) {
                p.setString(1, normalizedClinic);
                p.setString(2, date);
                p.setString(3, time);
                p.setString(4, username);
                p.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.out.println("bookAppointment error: " + e.getMessage());
            return false;
        }
    }

    /** Returns the list of booked times (HH:mm) for a given date. */
    public static List<String> getBookedTimesForDate(String date) {
        String sql = "SELECT appointment_time FROM appointments "
                + "WHERE appointment_date = ? AND status = 'booked' AND is_booked = 1";
        List<String> times = new ArrayList<>();
        try (Connection conn = connect()) {
            if (conn == null)
                return times;
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setString(1, date);
                ResultSet rs = p.executeQuery();
                while (rs.next())
                    times.add(rs.getString("appointment_time"));
            }
        } catch (SQLException e) {
            System.out.println("getBookedTimesForDate error: " + e.getMessage());
        }
        return times;
    }

    /**
     * Returns the list of booked times (HH:mm) for a specific clinic on a given
     * date.
     */
    public static List<String> getBookedTimesForDate(String date, String clinicUsername) {
        String sql = "SELECT appointment_time FROM appointments "
                + "WHERE clinic_username = ? AND appointment_date = ? AND status = 'booked' AND is_booked = 1";
        List<String> times = new ArrayList<>();
        try (Connection conn = connect()) {
            if (conn == null)
                return times;
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setString(1, normalizeClinicUsername(clinicUsername));
                p.setString(2, date);
                ResultSet rs = p.executeQuery();
                while (rs.next())
                    times.add(rs.getString("appointment_time"));
            }
        } catch (SQLException e) {
            System.out.println("getBookedTimesForDate error: " + e.getMessage());
        }
        return times;
    }

    /**
     * Returns appointments for a patient as [id, date, time, status] arrays, newest
     * first.
     */
    public static List<String[]> getAppointmentsForPatient(String username) {
        String sql = "SELECT id, appointment_date, appointment_time, status FROM appointments WHERE patient_username = ? ORDER BY id DESC";
        List<String[]> results = new ArrayList<>();
        try (Connection conn = connect()) {
            if (conn == null)
                return results;
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setString(1, username);
                ResultSet rs = p.executeQuery();
                while (rs.next()) {
                    results.add(new String[] {
                            String.valueOf(rs.getInt("id")),
                            rs.getString("appointment_date"),
                            rs.getString("appointment_time"),
                            rs.getString("status")
                    });
                }
            }
        } catch (SQLException e) {
            System.out.println("getAppointmentsForPatient error: " + e.getMessage());
        }
        return results;
    }

    /** Cancels an appointment by id, guarded by patient_username. */
    public static boolean cancelAppointment(int id, String username) {
        String sql = "UPDATE appointments SET status = 'cancelled', is_booked = 0 "
                + "WHERE id = ? AND patient_username = ?";
        try (Connection conn = connect()) {
            if (conn == null)
                return false;
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setInt(1, id);
                p.setString(2, username);
                return p.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("cancelAppointment error: " + e.getMessage());
            return false;
        }
    }

    private static String normalizeClinicUsername(String clinicUsername) {
        return (clinicUsername == null || clinicUsername.isBlank()) ? DEFAULT_CLINIC_USERNAME : clinicUsername;
    }

    private static void migrateClinicAvailabilitySchema(Connection conn) throws SQLException {
        List<String> primaryKeyColumns = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("PRAGMA table_info('clinic_availability')")) {
            while (rs.next()) {
                if (rs.getInt("pk") > 0) {
                    primaryKeyColumns.add(rs.getString("name"));
                }
            }
        }

        List<String> expectedPrimaryKey = List.of("clinic_username", "appointment_date", "appointment_time");
        if (expectedPrimaryKey.equals(primaryKeyColumns)) {
            return;
        }

        boolean originalAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE clinic_availability RENAME TO clinic_availability_legacy");
            stmt.execute("CREATE TABLE clinic_availability ("
                    + "clinic_username TEXT NOT NULL DEFAULT '" + DEFAULT_CLINIC_USERNAME + "',"
                    + "appointment_date TEXT NOT NULL,"
                    + "appointment_time TEXT NOT NULL,"
                    + "PRIMARY KEY (clinic_username, appointment_date, appointment_time)"
                    + ")");
            stmt.execute(
                    "INSERT OR IGNORE INTO clinic_availability (clinic_username, appointment_date, appointment_time) "
                            + "SELECT COALESCE(clinic_username, '" + DEFAULT_CLINIC_USERNAME
                            + "'), appointment_date, appointment_time "
                            + "FROM clinic_availability_legacy");
            stmt.execute("DROP TABLE clinic_availability_legacy");
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
    private static final DateTimeFormatter RECOVERY_TIMESTAMP_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static String getTermsAcceptedDate(String username) {
        String sql = "SELECT terms_accepted_date FROM privacy_settings WHERE username = ?";
        try (Connection conn = connect()) {
            if (conn == null) return null;
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setString(1, username);
                ResultSet rs = p.executeQuery();
                if (rs.next()) return rs.getString("terms_accepted_date");
            }
        } catch (SQLException e) {
            System.out.println("getTermsAcceptedDate error: " + e.getMessage());
        }
        return null;
    }

    public static void logDataAccess(String patientUsername, String accessorUsername,
                                     String accessorType, String accessType, String details) {
        String timestamp = java.time.LocalDateTime.now().format(TIMESTAMP_FMT);
        String sql = "INSERT INTO data_access_log (patient_username, accessor_username, accessor_type, access_type, access_timestamp, details) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, patientUsername);
            p.setString(2, accessorUsername);
            p.setString(3, accessorType);
            p.setString(4, accessType);
            p.setString(5, timestamp);
            p.setString(6, details);
            p.executeUpdate();
        } catch (SQLException e) {
            System.out.println("logDataAccess error: " + e.getMessage());
        }
    }

    public static List<DataAccessLogEntry> getDataAccessLog(String patientUsername) {
        String sql = "SELECT id, patient_username, accessor_username, accessor_type, access_type, access_timestamp, details "
                + "FROM data_access_log WHERE patient_username = ? ORDER BY id DESC LIMIT 50";
        List<DataAccessLogEntry> entries = new ArrayList<>();
        try (Connection conn = connect()) {
            if (conn == null) return entries;
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setString(1, patientUsername);
                ResultSet rs = p.executeQuery();
                while (rs.next()) {
                    entries.add(new DataAccessLogEntry(
                            rs.getInt("id"),
                            rs.getString("patient_username"),
                            rs.getString("accessor_username"),
                            rs.getString("accessor_type"),
                            rs.getString("access_type"),
                            rs.getString("access_timestamp"),
                            rs.getString("details")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("getDataAccessLog error: " + e.getMessage());
        }
        return entries;
    }


    public static void enforceDataRetention(String username) {
        String retention = getDataRetention(username);
        if ("While account is active".equals(retention)) return;

        int days = switch (retention) {
            case "30 days" -> 30;
            case "90 days" -> 90;
            case "180 days" -> 180;
            default -> -1;
        };

        if (days <= 0) return;

        LocalDate cutoff = LocalDate.now().minusDays(days);

        String selectTestsSql = "SELECT id, test_date FROM sti_tests WHERE username = ?";
        String selectTreatmentsSql = "SELECT id, start_date FROM treatment_plans WHERE username = ? AND active = 0";
        String deleteTestsSql = "DELETE FROM sti_tests WHERE id = ? AND username = ?";
        String deleteTreatmentsSql = "DELETE FROM treatment_plans WHERE id = ? AND username = ?";

        try (Connection conn = connect()) {
            if (conn == null) return;

            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                List<Integer> testIdsToDelete = new ArrayList<>();
                try (PreparedStatement p = conn.prepareStatement(selectTestsSql)) {
                    p.setString(1, username);
                    ResultSet rs = p.executeQuery();
                    while (rs.next()) {
                        try {
                            LocalDate testDate = LocalDate.parse(rs.getString("test_date"), DATE_FMT);
                            if (testDate.isBefore(cutoff)) {
                                testIdsToDelete.add(rs.getInt("id"));
                            }
                        } catch (DateTimeParseException ignored) {}
                    }
                }

                List<Integer> treatmentIdsToDelete = new ArrayList<>();
                try (PreparedStatement p = conn.prepareStatement(selectTreatmentsSql)) {
                    p.setString(1, username);
                    ResultSet rs = p.executeQuery();
                    while (rs.next()) {
                        try {
                            LocalDate startDate = LocalDate.parse(rs.getString("start_date"), DATE_FMT);
                            if (startDate.isBefore(cutoff)) {
                                treatmentIdsToDelete.add(rs.getInt("id"));
                            }
                        } catch (DateTimeParseException ignored) {}
                    }
                }

                if (!testIdsToDelete.isEmpty()) {
                    try (PreparedStatement p = conn.prepareStatement(deleteTestsSql)) {
                        for (int id : testIdsToDelete) {
                            p.setInt(1, id);
                            p.setString(2, username);
                            p.addBatch();
                        }
                        p.executeBatch();
                    }
                    System.out.println("Data retention: deleted " + testIdsToDelete.size() + " old STI tests for " + username);
                }

                if (!treatmentIdsToDelete.isEmpty()) {
                    try (PreparedStatement p = conn.prepareStatement(deleteTreatmentsSql)) {
                        for (int id : treatmentIdsToDelete) {
                            p.setInt(1, id);
                            p.setString(2, username);
                            p.addBatch();
                        }
                        p.executeBatch();
                    }
                    System.out.println("Data retention: deleted " + treatmentIdsToDelete.size() + " old inactive treatments for " + username);
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            System.err.println("enforceDataRetention error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static UserDataExport getUserDataExport(String username) {
        UserDataExport export = new UserDataExport(username);

        PrivacySettingsSnapshot privacySettings = getPrivacySettingsSnapshot(username);
        export.setTermsAcceptedDate(privacySettings.termsAcceptedDate);
        export.setDataAccessPermissions(privacySettings.dataAccessPermissions);
        export.setDataRetention(privacySettings.dataRetention);

        boolean[] testPerms = getTestPermissions(username);
        export.setTestPermissions(testPerms);
        export.setReminderInterval(getReminderIntervalMonths(username));

        export.setTreatmentPermissions(getTreatmentPermissions(username));

        export.setStiTests(getStiTests(username));

        export.setTreatments(getAllTreatments(username));

        return export;
    }

    private static class PrivacySettingsSnapshot {
        String termsAcceptedDate;
        boolean[] dataAccessPermissions;
        String dataRetention;

        PrivacySettingsSnapshot(String termsAcceptedDate, boolean[] dataAccessPermissions, String dataRetention) {
            this.termsAcceptedDate = termsAcceptedDate;
            this.dataAccessPermissions = dataAccessPermissions;
            this.dataRetention = dataRetention;
        }
    }

    private static PrivacySettingsSnapshot getPrivacySettingsSnapshot(String username) {
        String sql = "SELECT terms_accepted_date, data_retention, " +
                     "allow_internal_staff, allow_analytics, allow_trusted_partners " +
                     "FROM privacy_settings WHERE username = ?";
        try (Connection conn = connect()) {
            if (conn == null) {
                return new PrivacySettingsSnapshot(null, new boolean[]{false, false, false}, "While account is active");
            }
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setString(1, username);
                ResultSet rs = p.executeQuery();
                if (rs.next()) {
                    String termsDate = rs.getString("terms_accepted_date");
                    String retention = rs.getString("data_retention");
                    if (retention == null) retention = "While account is active";
                    boolean[] permissions = new boolean[]{
                            rs.getInt("allow_internal_staff") == 1,
                            rs.getInt("allow_analytics") == 1,
                            rs.getInt("allow_trusted_partners") == 1
                    };
                    return new PrivacySettingsSnapshot(termsDate, permissions, retention);
                }
            }
        } catch (SQLException e) {
            System.err.println("getPrivacySettingsSnapshot error: " + e.getMessage());
        }
        // Defaults if no row found
        return new PrivacySettingsSnapshot(null, new boolean[]{false, false, false}, "While account is active");
    }

    public static List<TreatmentEntry> getAllTreatments(String username) {
        String sql = "SELECT id, username, medication_name, dose, frequency, quantity_remaining, "
                + "low_stock_threshold, start_date, notes, active, source, patient_note "
                + "FROM treatment_plans WHERE username = ? ORDER BY id DESC";
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
            System.out.println("getAllTreatments error: " + e.getMessage());
        }
        return entries;
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

    public static String getAccountTypeByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return null;
        }

        try (Connection conn = connect()) {
            if (conn == null) {
                return null;
            }

            boolean userExists = accountExistsByEmail(conn, "users", normalizedEmail);
            boolean clinicExists = accountExistsByEmail(conn, "clinics", normalizedEmail);

            if (userExists == clinicExists) {
                return null;
            }

            return clinicExists ? "CLINIC" : "STANDARD";
        } catch (SQLException e) {
            System.out.println("getAccountTypeByEmail error: " + e.getMessage());
            return null;
        }
    }

    public static boolean storeRecoveryCode(String email, String code) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null || code == null || code.isBlank()) {
            return false;
        }

        String sql = "INSERT INTO password_recovery_codes (email, code, expires_at) VALUES (?, ?, ?) "
                + "ON CONFLICT(email) DO UPDATE SET code = excluded.code, expires_at = excluded.expires_at";
        String expiry = LocalDateTime.now()
                .plusMinutes(RECOVERY_CODE_EXPIRY_MINUTES)
                .format(RECOVERY_TIMESTAMP_FMT);

        try (Connection conn = connect()) {
            if (conn == null) {
                return false;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, normalizedEmail);
                pstmt.setString(2, code.trim());
                pstmt.setString(3, expiry);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("storeRecoveryCode error: " + e.getMessage());
            return false;
        }
    }

    public static boolean verifyRecoveryCode(String email, String code) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null || code == null || code.isBlank()) {
            return false;
        }

        String sql = "SELECT code, expires_at FROM password_recovery_codes WHERE email = ?";
        try (Connection conn = connect()) {
            if (conn == null) {
                return false;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, normalizedEmail);
                ResultSet rs = pstmt.executeQuery();
                if (!rs.next()) {
                    return false;
                }

                String storedCode = rs.getString("code");
                LocalDateTime expiresAt = LocalDateTime.parse(rs.getString("expires_at"), RECOVERY_TIMESTAMP_FMT);
                LocalDateTime now = LocalDateTime.now();
                boolean expired = expiresAt.isBefore(now);
                boolean matches = code.trim().equals(storedCode);

                if (!matches || expired) {
                    if (expired) {
                        clearRecoveryCode(conn, normalizedEmail);
                    }
                    return false;
                }
                return true;
            }
        } catch (SQLException | RuntimeException e) {
            System.out.println("verifyRecoveryCode error: " + e.getMessage());
            return false;
        }
    }

    public static void clearRecoveryCode(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return;
        }

        String sql = "DELETE FROM password_recovery_codes WHERE email = ?";
        try (Connection conn = connect()) {
            if (conn == null) {
                return;
            }
            clearRecoveryCode(conn, normalizedEmail);
        } catch (SQLException e) {
            System.out.println("clearRecoveryCode error: " + e.getMessage());
        }
    }

    private static void clearRecoveryCode(Connection conn, String normalizedEmail) throws SQLException {
        String sql = "DELETE FROM password_recovery_codes WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, normalizedEmail);
            pstmt.executeUpdate();
        }
    }

    public static boolean updatePasswordByEmail(String email, String newPassword) {
        String normalizedEmail = normalizeEmail(email);
        String accountType = getAccountTypeByEmail(normalizedEmail);
        if (normalizedEmail == null || accountType == null || newPassword == null || newPassword.isBlank()) {
            return false;
        }

        String sql = "UPDATE " + getTableName(accountType) + " SET password = ? WHERE LOWER(email) = ?";
        try (Connection conn = connect()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newPassword);
                pstmt.setString(2, normalizedEmail);
                boolean updated = pstmt.executeUpdate() > 0;
                if (updated) {
                    clearRecoveryCode(conn, normalizedEmail);
                }
                return updated;
            }
        } catch (SQLException e) {
            System.out.println("updatePasswordByEmail error: " + e.getMessage());
            return false;
        }
    }

    // ── Clinic Settings & Postcodes ──────────────────────────────────────────

    /**
     * Retrieves a ClinicAccount and populates its supported postcode prefixes.
     */
    public static app.model.ClinicAccount getClinicAccount(String username) {
        String clinicEmail = "";
        String clinicAddress = "";
        String emailSql = "SELECT email, address FROM clinics WHERE username = ?";
        try (Connection conn = connect()) {
            if (conn != null) {
                try (PreparedStatement p = conn.prepareStatement(emailSql)) {
                    p.setString(1, username);
                    ResultSet rs = p.executeQuery();
                    if (rs.next()) {
                        if (rs.getString("email") != null) {
                            clinicEmail = rs.getString("email");
                        }
                        if (rs.getString("address") != null) {
                            clinicAddress = rs.getString("address");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("getClinicAccount email lookup error: " + e.getMessage());
        }

        app.model.ClinicAccount account = new app.model.ClinicAccount(username, "", clinicEmail, clinicAddress, username);

        String sql = "SELECT postcode_prefix FROM clinic_postcodes WHERE clinic_username = ?";
        try (Connection conn = connect()) {
            if (conn == null)
                return account;
            try (PreparedStatement p = conn.prepareStatement(sql)) {
                p.setString(1, username);
                ResultSet rs = p.executeQuery();
                while (rs.next()) {
                    account.addCoveredPostcodePrefix(rs.getString("postcode_prefix"));
                }
            }
        } catch (SQLException e) {
            System.out.println("getClinicAccount error: " + e.getMessage());
        }
        return account;
    }

    /**
     * Overwrites a clinic's supported postcodes in the database.
     */
    public static void updateClinicAccount(app.model.ClinicAccount clinic) {
        String deleteSql = "DELETE FROM clinic_postcodes WHERE clinic_username = ?";
        String insertSql = "INSERT INTO clinic_postcodes (clinic_username, postcode_prefix) VALUES (?, ?)";

        try (Connection conn = connect()) {
            if (conn == null)
                return;

            // Turn off auto-commit so we can safely delete old data and insert new data in
            // one transaction
            conn.setAutoCommit(false);

            try {
                // 1. Wipe the old postcodes for this clinic
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setString(1, clinic.getUsername());
                    deleteStmt.executeUpdate();
                }

                // 2. Insert the fresh list of postcodes
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    for (String prefix : clinic.getCoveredPostcodePrefixes()) {
                        insertStmt.setString(1, clinic.getUsername());
                        insertStmt.setString(2, prefix);
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }

                // 3. Commit the transaction
                conn.commit();
            } catch (SQLException e) {
                // If anything fails, roll back to the original state to prevent data loss
                conn.rollback();
                throw e;
            } finally {
                // Restore default behaviour
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("updateClinicAccount error: " + e.getMessage());
        }
    }
}
