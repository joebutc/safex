package app.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailServiceErrorTests {

    @Test void loadCredentialsMissingFile(){

        assertThrows(IllegalStateException.class, EmailService::loadCredentials);

    }

    @Test void emailIsBlank(){
        IllegalStateException exception = assertThrows(IllegalStateException.class, EmailService::loadCredentials);

        String expectedMessage = "Missing email credentials in config.properties";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);

    }

    @Test void passwordIsBlank(){

        assertThrows(IllegalStateException.class, EmailService::loadCredentials);

    }

}