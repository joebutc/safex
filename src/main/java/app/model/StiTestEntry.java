package app.model;

public class StiTestEntry {
    private final int id;
    private final String username;
    private final String testDate;
    private final String stiType;
    private final String result;
    private final String notes;

    public StiTestEntry(int id, String username, String testDate, String stiType, String result, String notes) {
        this.id = id;
        this.username = username;
        this.testDate = testDate;
        this.stiType = stiType;
        this.result = result;
        this.notes = notes;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getTestDate() { return testDate; }
    public String getStiType() { return stiType; }
    public String getResult() { return result; }
    public String getNotes() { return notes; }
}
