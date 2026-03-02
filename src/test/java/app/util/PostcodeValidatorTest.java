package app.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PostcodeValidatorTest {

    @Test
    void validPostcodes() {
        String[] valids = {
                "SW1A 1AA",
                "SW1A1AA",
                "GIR 0AA",
                "EC1A 1BB",
                "W1A 0AX",
                "M1 1AE",
                "B33 8TH",
                "CR2 6XH"
        };
        for (String p : valids) assertTrue(PostcodeValidator.isValid(p), "Should be valid: " + p);
    }

    @Test
    void invalidPostcodes() {
        String[] invalids = {
                "12345",
                "ABCDE",
                "",
                "SW1  "
        };
        for (String p : invalids) assertFalse(PostcodeValidator.isValid(p), "Should be invalid: " + p);
    }
}
