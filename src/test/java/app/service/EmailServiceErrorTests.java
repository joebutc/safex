package app.service;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class EmailServiceErrorTests {
    private static final String MISSING_RESOURCE = "email/missing.properties";
    private static final String BLANK_EMAIL_SOURCE = "blank-email.properties";
    private static final String BLANK_PASSWORD_SOURCE = "blank-password.properties";

    @Test void loadCredentialsMissingFile(){
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> EmailService.loadCredentials(MISSING_RESOURCE));
        assertEquals("Missing " + MISSING_RESOURCE + " resource", exception.getMessage());
    }

    @Test void emailIsBlank(){
        Properties config = new Properties();
        config.setProperty("email.address", "");
        config.setProperty("email.password", "test-password");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> EmailService.parseCredentials(config, BLANK_EMAIL_SOURCE));

        String expectedMessage = "Missing email credentials in " + BLANK_EMAIL_SOURCE;
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test void passwordIsBlank(){
        Properties config = new Properties();
        config.setProperty("email.address", "test@example.com");
        config.setProperty("email.password", "");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> EmailService.parseCredentials(config, BLANK_PASSWORD_SOURCE));
        assertEquals("Missing email credentials in " + BLANK_PASSWORD_SOURCE, exception.getMessage());
    }

}
