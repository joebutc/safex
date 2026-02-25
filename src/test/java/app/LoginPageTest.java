package app;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LoginPageTest {

    public String runValidation(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            return "Please fill in all fields.";
        }

        if (username.equals("ValidUser") && password.equals("CorrectPass1!")) {
            return "Success";
        } else {
            return "Invalid username or password.";
        }
    }

    @Test
    void testEmptyUsername() {
        assertEquals("Please fill in all fields.", runValidation("", "CorrectPass1!"));
    }

    @Test
    void testEmptyPassword() {
        assertEquals("Please fill in all fields.", runValidation("ValidUser", ""));
    }

    @Test
    void testEmptyBothFields() {
        assertEquals("Please fill in all fields.", runValidation("", ""));
    }

    @Test
    void testWrongPassword() {
        assertEquals("Invalid username or password.", runValidation("ValidUser", "WrongPass1!"));
    }

    @Test
    void testUnknownUsername() {
        assertEquals("Invalid username or password.", runValidation("UnknownUser", "CorrectPass1!"));
    }

    @Test
    void testValidLogin() {
        assertEquals("Success", runValidation("ValidUser", "CorrectPass1!"));
    }
}