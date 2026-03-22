
package app.service;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;


class EmailServiceTest {

    // successful email send.
    @Test
    void successfulSend() {
        var result = EmailService.sendNotification(
                "Cam",
                "cp5g23@soton.ac.uk",
                "SAFEX.");
        assertTrue(result);
    }

    @Test
    void noRecipientName(){
        var result = EmailService.sendNotification(
                "",
                "cp5g23@soton.ac.uk",
                "Harvey");
        assertTrue(result);
    }

    @Test
    void noRecipientEmail(){
        var result = EmailService.sendNotification(
                "John",
                "",
                "Harvey");
        assertFalse(result);
    }

    @Test
    void noSenderName(){
        var result = EmailService.sendNotification(
                "Jake Sully.",
                "cp5g23@soton.ac.uk",
                "");
        assertTrue(result);
    }

    @Test
    void spaceInNames(){
        var result = EmailService.sendNotification(
                "John Doe",
                "cp5g23@soton.ac.uk",
                "Patrick Bateman");
        assertTrue(result);
    }

    @Test
    void invalidEmail(){
        var result = EmailService.sendNotification(
                "recipient",
                "cp5g23soton.com",
                "sender");
        assertFalse(result);
    }

    @Test
    void validEmailWithInvalidSpaces(){
        var result = EmailService.sendNotification(
                "recipient",
                "cp5g23 @ soton . ac .uk",
                "sender");
        assertFalse(result);
    }

    @Test
    void escapeHtmlTests(){
        String result = EmailService.escapeHtml("& , < , > , \" , '");
        assertEquals("&amp; , &lt; , &gt; , &quot; , &#39;",result);
    }

    @Test
    void formatDisplayNameTests(){
        String result1 = EmailService.formatDisplayName("name","fallback");
        String result2 = EmailService.formatDisplayName("","fallback");
        String result3 = EmailService.formatDisplayName("    ","fallback");

        assertEquals("name",result1);
        assertEquals("fallback",result2);
        assertEquals("fallback",result3);
    }

}





