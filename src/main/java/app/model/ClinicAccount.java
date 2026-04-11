package app.model;

import java.util.HashSet;
import java.util.Set;

public class ClinicAccount extends Account {
    private String clinicName;
    private String registrationNumber;
    // Store prefixes (e.g., "SW1A") for area matching
    private Set<String> coveredPostcodePrefixes;

    public ClinicAccount(String username, String password, String email, String clinicName) {
        this(username, password, email, clinicName, "");
    }

    public ClinicAccount(
            String username,
            String password,
            String email,
            String clinicName,
            String registrationNumber) {
        super(username, password, email);
        this.clinicName = clinicName;
        this.registrationNumber = registrationNumber;
        this.coveredPostcodePrefixes = new HashSet<>();
    }

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public Set<String> getCoveredPostcodePrefixes() {
        return coveredPostcodePrefixes;
    }

    public void setCoveredPostcodePrefixes(Set<String> coveredPostcodePrefixes) {
        this.coveredPostcodePrefixes = coveredPostcodePrefixes;
    }

    public void addCoveredPostcodePrefix(String prefix) {
        if (prefix != null && !prefix.isBlank()) {
            this.coveredPostcodePrefixes.add(prefix.toUpperCase().trim());
        }
    }
}