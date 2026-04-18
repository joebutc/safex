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
    private static final String RECOVERY_SUBJECT = "[SafeX] Password Recovery Code";
    private static final String VERIFICATION_SUBJECT = "[SafeX] Verify Your Email Address";
    private static final String BLOCKED_RECIPIENT_EMAIL = "cp5g23@soton.ac.uk";

    private EmailService() {
    }

    public static boolean sendNotification(String recipientName, String recipientEmail, String senderName) {
        return sendHtmlEmail(recipientEmail, SUBJECT, buildHtmlBody(recipientName, senderName));
    }

    public static boolean sendRecoveryCode(String recipientEmail, String recoveryCode) {
        return sendHtmlEmail(recipientEmail, RECOVERY_SUBJECT, buildRecoveryHtmlBody(recoveryCode));
    }

    public static boolean sendVerificationCode(String recipientEmail, String verificationCode) {
        return sendHtmlEmail(recipientEmail, VERIFICATION_SUBJECT, buildVerificationHtmlBody(verificationCode));
    }

    private static boolean sendHtmlEmail(String recipientEmail, String subject, String htmlBody) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return false;
        }
        if (isBlockedRecipient(recipientEmail)) {
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
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail.trim(), true));
            message.setSubject(subject);
            message.setHeader("X-Auto-Response-Suppress", "All");
            message.setHeader("Auto-Submitted", "auto-generated");
            message.setSentDate(new Date());
            message.setContent(htmlBody, "text/html; charset=UTF-8");

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
        return loadCredentials(CONFIG_FILE);
    }

    static Credentials loadCredentials(String resourceName) {
        Properties config = new Properties();
        try (InputStream inputStream = EmailService.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing " + resourceName + " resource");
            }
            config.load(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load " + resourceName + " resource", exception);
        }

        return parseCredentials(config, resourceName);
    }

    static Credentials parseCredentials(Properties config, String sourceName) {
        String emailAddress = config.getProperty(EMAIL_ADDRESS_KEY, "").trim();
        String emailPassword = config.getProperty(EMAIL_PASSWORD_KEY, "").trim();
        if (emailAddress.isBlank() || emailPassword.isBlank()) {
            throw new IllegalStateException("Missing email credentials in " + sourceName);
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

    protected static String buildRecoveryHtmlBody(String recoveryCode) {
        String safeRecoveryCode = escapeHtml(recoveryCode == null ? "" : recoveryCode.trim());

        return """
                <html>
                  <body style="font-family: Arial, sans-serif; color: #1f2933; line-height: 1.6;">
                    <p>This is your SafeX password recovery code:</p>
                    <p style="font-size: 28px; font-weight: 700; letter-spacing: 6px;">%s</p>
                    <p>The code will expire in 15 minutes.</p>
                    <p>If you did not request a password reset, you can ignore this email.</p>
                    <p>Please do not reply to this email. This inbox is not monitored.</p>
                    <p>Kind regards,<br/>SafeX Support</p>
                  </body>
                </html>
                """.formatted(safeRecoveryCode);
    }

    protected static String buildVerificationHtmlBody(String verificationCode) {
        String safeVerificationCode = escapeHtml(verificationCode == null ? "" : verificationCode.trim());

        return """
                <html>
                  <body style="font-family: Arial, sans-serif; color: #1f2933; line-height: 1.6;">
                    <p>Welcome to SafeX.</p>
                    <p>Use this 6-digit code to verify your email address and finish creating your account:</p>
                    <p style="font-size: 28px; font-weight: 700; letter-spacing: 6px;">%s</p>
                    <p>The code will expire in 15 minutes.</p>
                    <p>If you did not try to create a SafeX account, you can ignore this email.</p>
                    <p>Please do not reply to this email. This inbox is not monitored.</p>
                    <p>Kind regards,<br/>SafeX Support</p>
                  </body>
                </html>
                """.formatted(safeVerificationCode);
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

    static boolean isBlockedRecipient(String recipientEmail) {
        if (recipientEmail == null) {
            return false;
        }
        return BLOCKED_RECIPIENT_EMAIL.equalsIgnoreCase(recipientEmail.trim());
    }

    protected record Credentials(String emailAddress, String emailPassword) {
    }
}
