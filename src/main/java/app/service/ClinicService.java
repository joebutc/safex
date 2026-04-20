package app.service;

import app.model.ClinicAccount;
import app.model.DatabaseHandler;

import java.util.ArrayList;
import java.util.List;

public class ClinicService {

    private final List<ClinicAccount> allActiveClinics;

    public ClinicService() {
        this.allActiveClinics = new ArrayList<>();

        // Load all clinic usernames from the database
        List<String> usernames = DatabaseHandler.getClinicUsernames();

        // For each clinic, fetch their full account including saved postcodes
        for (String username : usernames) {
            ClinicAccount clinic = DatabaseHandler.getClinicAccount(username);
            if (clinic != null) {
                allActiveClinics.add(clinic);
            }
        }
    }

    /**
     * Looks up clinics that service the provided user postcode.
     */
    public List<ClinicAccount> findClinicsNear(String fullPostcode) {
        List<ClinicAccount> matchingClinics = new ArrayList<>();
        if (fullPostcode == null || fullPostcode.isBlank()) {
            return matchingClinics;
        }

        String formattedPostcode = fullPostcode.toUpperCase().trim();
        String outwardCode = formattedPostcode; // Default fallback

        // 1. Separate Outward Code (First Half) from Inward Code (Second Half)
        if (formattedPostcode.contains(" ")) {
            // Standard format (e.g., "SO14 0YG" -> "SO14")
            outwardCode = formattedPostcode.split(" ")[0];
        } else if (formattedPostcode.length() > 3) {
            // Missing space format (e.g., "SW1A1AA" -> "SW1A")
            // UK Inward codes are ALWAYS exactly 3 characters at the end.
            outwardCode = formattedPostcode.substring(0, formattedPostcode.length() - 3);
        }

        // 2. Match against clinics
        for (ClinicAccount clinic : allActiveClinics) {
            for (String prefix : clinic.getCoveredPostcodePrefixes()) {

                // Scenario A: Exact Match (e.g., Clinic supports "SO14", User is "SO14")
                if (outwardCode.equals(prefix)) {
                    matchingClinics.add(clinic);
                    break; // Move to the next clinic to avoid duplicates
                }
                // Scenario B: Broad Match (e.g., Clinic supports "PO", User is "PO5")
                // Only allow this 'startsWith' check if the clinic's prefix is purely letters.
                else if (prefix.matches("^[A-Z]+$") && outwardCode.startsWith(prefix)) {
                    matchingClinics.add(clinic);
                    break;
                }
            }
        }
        return matchingClinics;
    }
}