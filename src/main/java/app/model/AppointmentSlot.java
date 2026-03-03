package app.model;

public class AppointmentSlot {
    private final int id;
    private final String clinicUsername;
    private final String date;
    private final String time;
    private final boolean isBooked;
    private final String patientUsername;

    public AppointmentSlot(int id, String clinicUsername, String date, String time, boolean isBooked,
            String patientUsername) {
        this.id = id;
        this.clinicUsername = clinicUsername;
        this.date = date;
        this.time = time;
        this.isBooked = isBooked;
        this.patientUsername = patientUsername;
    }

    public int getId() {
        return id;
    }

    public String getClinicUsername() {
        return clinicUsername;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public String getPatientUsername() {
        return patientUsername;
    }
}