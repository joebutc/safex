package app.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailServiceTest {
    private static final String VALID_NOTIFICATION_EMAIL = "safexdonotreply@gmail.com";
    private static final String INVALID_NOTIFICATION_EMAIL =
            VALID_NOTIFICATION_EMAIL.replace("@", "");
    private static final String INVALID_NOTIFICATION_EMAIL_WITH_SPACES =
            VALID_NOTIFICATION_EMAIL.replace("@", " @ ").replace(".", " . ");
    private static final String BLOCKED_NOTIFICATION_EMAIL = "cp5g23@soton.ac.uk";

    @Test
    void noRecipientEmail() {
        var result = EmailService.sendNotification(
                "John",
                "",
                "Harvey");
        assertFalse(result);
    }

    @Test
    void invalidEmail() {
        var result = EmailService.sendNotification(
                "recipient",
                INVALID_NOTIFICATION_EMAIL,
                "sender");
        assertFalse(result);
    }

    @Test
    void validEmailWithInvalidSpaces() {
        var result = EmailService.sendNotification(
                "recipient",
                INVALID_NOTIFICATION_EMAIL_WITH_SPACES,
                "sender");
        assertFalse(result);
    }

    @Test
    void blockedRecipientEmailIsRejected() {
        assertTrue(EmailService.isBlockedRecipient(BLOCKED_NOTIFICATION_EMAIL));
        assertTrue(EmailService.isBlockedRecipient("  CP5G23@SOTON.AC.UK  "));
        assertFalse(EmailService.sendNotification("recipient", BLOCKED_NOTIFICATION_EMAIL, "sender"));
    }

    @Test
    void buildHtmlBodyUsesEscapedNames() {
        String body = EmailService.buildHtmlBody("Cam & Co", "SafeX <Admin>");
        assertTrue(body.contains("Dear Cam &amp; Co,"));
        assertTrue(body.contains("SafeX &lt;Admin&gt; has used SafeX to notify you"));
    }

    @Test
    void buildHtmlBodyFallsBackWhenNamesBlank() {
        String body = EmailService.buildHtmlBody("   ", "");
        assertTrue(body.contains("Dear there,"));
        assertTrue(body.contains("a SafeX user has used SafeX to notify you"));
    }

    @Test
    void buildRecoveryHtmlBodyIncludesOtpAndExpiry() {
        String body = EmailService.buildRecoveryHtmlBody("123456");
        assertTrue(body.contains("123456"));
        assertTrue(body.contains("expire in 15 minutes"));
    }

    @Test
    void buildVerificationHtmlBodyIncludesOtpAndWelcomeCopy() {
        String body = EmailService.buildVerificationHtmlBody("654321");
        assertTrue(body.contains("654321"));
        assertTrue(body.contains("Welcome to SafeX."));
        assertTrue(body.contains("finish creating your account"));
    }

    @Test
    void sendRecoveryCodeRejectsBlockedRecipient() {
        assertFalse(EmailService.sendRecoveryCode(BLOCKED_NOTIFICATION_EMAIL, "123456"));
    }

    @Test
    void sendVerificationCodeRejectsBlankRecipient() {
        assertFalse(EmailService.sendVerificationCode("   ", "123456"));
    }

    @Test
    void escapeHtmlTests() {
        String result = EmailService.escapeHtml("& , < , > , \" , '");
        assertEquals("&amp; , &lt; , &gt; , &quot; , &#39;", result);
    }

    @Test
    void formatDisplayNameTests() {
        String result1 = EmailService.formatDisplayName("name", "fallback");
        String result2 = EmailService.formatDisplayName("", "fallback");
        String result3 = EmailService.formatDisplayName("    ", "fallback");

        assertEquals("name", result1);
        assertEquals("fallback", result2);
        assertEquals("fallback", result3);
    }
}
