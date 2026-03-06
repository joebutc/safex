package app.util;

import java.util.regex.Pattern;

public final class PostcodeValidator {
    private PostcodeValidator() {}

    // UK postcode regex 
    private static final Pattern POSTCODE = Pattern.compile(
            "^(GIR 0AA|[A-PR-UWYZ]([0-9]{1,2}|[A-HK-Y][0-9]{1,2}|[0-9][A-HJKPSTUW]|[A-HK-Y][0-9]?[ABEHMNPRV-Y])\\s?[0-9][ABD-HJLNP-UW-Z]{2})$",
            Pattern.CASE_INSENSITIVE);

    public static boolean isValid(String input) {
        if (input == null) return false;
        String trimmed = input.trim();
        if (trimmed.isEmpty()) return false;
        return POSTCODE.matcher(trimmed).matches();
    }
}
