package app.model;

public class DataAccessLogEntry {
    private final int id;
    private final String patientUsername;
    private final String accessorUsername;
    private final String accessorType;
    private final String accessType;
    private final String accessTimestamp;
    private final String details;

    public DataAccessLogEntry(int id, String patientUsername, String accessorUsername,
                              String accessorType, String accessType, String accessTimestamp, String details) {
        this.id = id;
        this.patientUsername = patientUsername;
        this.accessorUsername = accessorUsername;
        this.accessorType = accessorType;
        this.accessType = accessType;
        this.accessTimestamp = accessTimestamp;
        this.details = details;
    }

    public int getId() { return id; }
    public String getPatientUsername() { return patientUsername; }
    public String getAccessorUsername() { return accessorUsername; }
    public String getAccessorType() { return accessorType; }
    public String getAccessType() { return accessType; }
    public String getAccessTimestamp() { return accessTimestamp; }
    public String getDetails() { return details; }
}
