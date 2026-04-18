package app;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RegisterPageTest {

    public String runValidation(String username, String email, String address, String password, String confirm) {
        if (username.isEmpty() || email.isEmpty() || address.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            return "Please fill in all fields.";
        }

        if (!username.matches("^[a-zA-Z]+$")) {
            return "Username must contain only letters and no spaces.";
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return "Please enter a valid email address.";
        }

        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        if (!password.matches(passwordPattern)) {
            return "Password must be at least 8 characters and include uppercase, lowercase, a number, and a special character.";
        }

        if (!password.equals(confirm)) {
            return "Passwords do not match.";
        }

        return "Success";
    }

    @Test
    void testEmptyFields() {
        assertEquals("Please fill in all fields.", runValidation("", "", "", "", ""));
    }

    @Test
    void testInvalidUsername() {
        assertEquals("Username must contain only letters and no spaces.",
                runValidation("User 123", "user@example.com", "1 Test Road", "Pass123!", "Pass123!"));
    }

    @Test
    void testInvalidEmail() {
        assertEquals("Please enter a valid email address.",
                runValidation("ValidUser", "invalid-email", "1 Test Road", "Pass123!", "Pass123!"));
    }

    @Test
    void testWeakPassword() {
        assertEquals(
                "Password must be at least 8 characters and include uppercase, lowercase, a number, and a special character.",
                runValidation("ValidUser", "user@example.com", "1 Test Road", "123", "123"));
    }

    @Test
    void testPasswordMismatch() {
        assertEquals("Passwords do not match.",
                runValidation("ValidUser", "user@example.com", "1 Test Road", "SecurePass1!", "WrongPass1!"));
    }

    @Test
    void testValidRegistration() {
        assertEquals("Success",
                runValidation("ValidUser", "user@example.com", "1 Test Road", "SecurePass1!", "SecurePass1!"));
    }
}
