package app.model;

public class ClinicAccount extends Account {
    private String clinicName;
    private String registrationNumber;

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
}
