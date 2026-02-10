package app.service;

import jakarta.mail.Address;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

public final class EmailService {
    private static final String CONFIG_FILE = "config.properties";
    private static final String EMAIL_ADDRESS_KEY = "email.address";
    private static final String EMAIL_PASSWORD_KEY = "email.password";
    private static final String SUBJECT = "[SafeX] Important Sexual Health Notification - Do Not Reply";

    private EmailService() {
    }

    public static boolean sendNotification(String recipientName, String recipientEmail, String senderName) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return false;
        }

        Credentials credentials;
        try {
            credentials = loadCredentials();
        } catch (IllegalStateException exception) {
            return false;
        }

        Properties smtpProperties = createSmtpProperties();
        Session session = Session.getInstance(smtpProperties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(credentials.emailAddress(), credentials.emailPassword());
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            InternetAddress fromAddress = new InternetAddress(credentials.emailAddress());
            message.setFrom(fromAddress);
            message.setReplyTo(new Address[]{fromAddress});
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail, false));
            message.setSubject(SUBJECT);
            message.setHeader("X-Auto-Response-Suppress", "All");
            message.setHeader("Auto-Submitted", "auto-generated");
            message.setSentDate(new Date());
            message.setContent(buildHtmlBody(recipientName, senderName), "text/html; charset=UTF-8");

            Transport.send(message);
            return true;
        } catch (MessagingException exception) {
            return false;
        }
    }

    private static Properties createSmtpProperties() {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        return properties;
    }

    protected static Credentials loadCredentials() {
        Properties config = new Properties();
        try (InputStream inputStream = EmailService.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing config.properties resource");
            }
            config.load(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load config.properties resource", exception);
        }

        String emailAddress = config.getProperty(EMAIL_ADDRESS_KEY, "").trim();
        String emailPassword = config.getProperty(EMAIL_PASSWORD_KEY, "").trim();
        if (emailAddress.isBlank() || emailPassword.isBlank()) {
            throw new IllegalStateException("Missing email credentials in config.properties");
        }
        return new Credentials(emailAddress, emailPassword);
    }

    protected static String buildHtmlBody(String recipientName, String senderName) {
        String safeRecipientName = formatDisplayName(recipientName, "there");
        String safeSenderName = formatDisplayName(senderName, "a SafeX user");

        return """
                <html>
                  <body style="font-family: Arial, sans-serif; color: #1f2933; line-height: 1.6;">
                    <p>Dear %s,</p>
                    <p>This is an automated message from SafeX.</p>
                    <p>%s has used SafeX to notify you of a possible sexual health exposure.</p>
                    <p>We recommend arranging a check-up with a trusted healthcare provider as soon as possible.</p>
                    <p>You can find trusted support and sexual health guidance from the NHS here:
                       <a href="https://www.nhs.uk/conditions/sexually-transmitted-infections-stis/">
                         NHS Sexual Health Services
                       </a>.
                    </p>
                    <p>Please do not reply to this email. This inbox is not monitored.</p>
                    <p>Kind regards,<br/>SafeX Automated Notification Service</p>
                  </body>
                </html>
                """.formatted(safeRecipientName, safeSenderName);
    }

    protected static String formatDisplayName(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return escapeHtml(value.trim());
    }

    protected static String escapeHtml(String input) {
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    protected record Credentials(String emailAddress, String emailPassword) {
    }
}
