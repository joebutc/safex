package app.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

    // Creates the users table if it does not already exist
    public static void initialiseDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS users ("
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
                stmt.execute(sql);
                System.out.println("Table 'users' is ready.");
            }
        } catch (SQLException e) {
            System.out.println("Error initialising database: " + e.getMessage());
        }
    }

    // Registers a new user into the database
    public static boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";

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

    // Checks if a user exists (login)
    public static boolean checkLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

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
