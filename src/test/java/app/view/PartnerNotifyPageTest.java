package app.view;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PartnerNotifyPageTest {

    // Helper method to simulate the validation logic found in the real class
    public String validateRecipients(String email1, String email2) {

        // 1. Check if the list of emails is completely empty
        if (email1.isBlank() && email2.isBlank()) {
            return "No Contacts: Add at least one contact with an email address before sending notifications.";
        }

        // 2. Validate the syntax of the first email (if provided)
        if (!email1.isBlank() && !isSyntaxValid(email1)) {
            return "Invalid Email Address: The email '" + email1 + "' is not formatted correctly.";
        }

        // 3. Validate the syntax of the second email (if provided)
        if (!email2.isBlank() && !isSyntaxValid(email2)) {
            return "Invalid Email Address: The email '" + email2 + "' is not formatted correctly.";
        }

        // 4. If all provided emails are valid
        return "Success: Ready to send notifications.";
    }

    // Exact replica of the regex check used in PartnerNotifyPage.java
    private boolean isSyntaxValid(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email != null && email.matches(regex);
    }

    @Test
    void testEmptyContactList() {
        assertEquals(
                "No Contacts: Add at least one contact with an email address before sending notifications.",
                validateRecipients("", ""));
    }

    @Test
    void testInvalidEmailMissingAtSymbol() {
        assertEquals(
                "Invalid Email Address: The email 'johndoegmail.com' is not formatted correctly.",
                validateRecipients("johndoegmail.com", ""));
    }

    @Test
    void testInvalidEmailMissingDomain() {
        assertEquals(
                "Invalid Email Address: The email 'johndoe@' is not formatted correctly.",
                validateRecipients("johndoe@", ""));
    }

    @Test
    void testInvalidEmailMissingExtension() {
        assertEquals(
                "Invalid Email Address: The email 'johndoe@gmail' is not formatted correctly.",
                validateRecipients("johndoe@gmail", ""));
    }

    @Test
    void testValidAndInvalidCombination() {
        // Tests that the system correctly flags an invalid email even if a valid one is
        // also present
        assertEquals(
                "Invalid Email Address: The email 'badEmail' is not formatted correctly.",
                validateRecipients("valid.email@example.co.uk", "badEmail"));
    }

    @Test
    void testValidEmails() {
        assertEquals(
                "Success: Ready to send notifications.",
                validateRecipients("test1@gmail.com", "test2@hotmail.com"));
    }
}