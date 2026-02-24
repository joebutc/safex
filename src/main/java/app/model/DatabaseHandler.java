package app.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHandler {
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

        try (Connection conn = connect()) {
            if (conn == null) {
                System.out.println("Error initialising database: connection unavailable.");
                return;
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(usersSql);
                stmt.execute(clinicsSql);
                System.out.println("Tables 'users' and 'clinics' are ready.");
            }
        } catch (SQLException e) {
            System.out.println("Error initialising database: " + e.getMessage());
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