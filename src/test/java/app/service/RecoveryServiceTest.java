package app.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecoveryServiceTest {

    @Test
    void generateRecoveryCode_returnsSixDigitNumericOtp() {
        String code = RecoveryService.generateRecoveryCode();

        assertEquals(RecoveryService.OTP_LENGTH, code.length());
        assertTrue(code.matches("\\d{6}"));
    }

    @Test
    void generateRecoveryCode_canBeCalledRepeatedly() {
        for (int i = 0; i < 50; i++) {
            String code = RecoveryService.generateRecoveryCode();
            assertTrue(code.matches("\\d{6}"), "Expected numeric OTP but got: " + code);
        }
    }
}
