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

    public List<ClinicAccount> findClinicsNear(String fullPostcode) {
        List<ClinicAccount> matchingClinics = new ArrayList<>();
        if (fullPostcode == null || fullPostcode.isBlank()) {
            return matchingClinics;
        }

        String formattedPostcode = fullPostcode.toUpperCase().trim();

        for (ClinicAccount clinic : allActiveClinics) {
            for (String prefix : clinic.getCoveredPostcodePrefixes()) {
                if (formattedPostcode.startsWith(prefix)) {
                    matchingClinics.add(clinic);
                    break;
                }
            }
        }
        return matchingClinics;
    }
}