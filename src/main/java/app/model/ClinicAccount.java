package app.model;

public class ClinicAccount extends Account {
    private String clinicName;
    private String registrationNumber;

    public ClinicAccount(String username, String password, String email, String clinicName) {
        super(username, password, email);
        this.clinicName = clinicName;
    }

}