package app.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailServiceTest {

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
                "cp5g23soton.com",
                "sender");
        assertFalse(result);
    }

    @Test
    void validEmailWithInvalidSpaces() {
        var result = EmailService.sendNotification(
                "recipient",
                "cp5g23 @ soton . ac .uk",
                "sender");
        assertFalse(result);
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
