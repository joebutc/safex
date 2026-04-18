package app.service;

import java.security.SecureRandom;

public final class RecoveryService {
    public static final int OTP_LENGTH = 6;
    public static final int OTP_EXPIRY_MINUTES = 15;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private RecoveryService() {
    }

    public static String generateRecoveryCode() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        return String.format("%0" + OTP_LENGTH + "d", SECURE_RANDOM.nextInt(bound));
    }
}
