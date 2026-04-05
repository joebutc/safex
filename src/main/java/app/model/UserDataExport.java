package app.model;

import java.util.List;

public class UserDataExport {
    private final String username;
    private String termsAcceptedDate;
    private boolean[] dataAccessPermissions;
    private String dataRetention;
    private boolean[] testPermissions;
    private int reminderInterval;
    private boolean[] treatmentPermissions;
    private List<StiTestEntry> stiTests;
    private List<TreatmentEntry> treatments;

    public UserDataExport(String username) {
        this.username = username;
    }

    public String getUsername() { return username; }

    public String getTermsAcceptedDate() { return termsAcceptedDate; }
    public void setTermsAcceptedDate(String termsAcceptedDate) { this.termsAcceptedDate = termsAcceptedDate; }

    public boolean[] getDataAccessPermissions() { return dataAccessPermissions; }
    public void setDataAccessPermissions(boolean[] dataAccessPermissions) { this.dataAccessPermissions = dataAccessPermissions; }

    public String getDataRetention() { return dataRetention; }
    public void setDataRetention(String dataRetention) { this.dataRetention = dataRetention; }

    public boolean[] getTestPermissions() { return testPermissions; }
    public void setTestPermissions(boolean[] testPermissions) { this.testPermissions = testPermissions; }

    public int getReminderInterval() { return reminderInterval; }
    public void setReminderInterval(int reminderInterval) { this.reminderInterval = reminderInterval; }

    public boolean[] getTreatmentPermissions() { return treatmentPermissions; }
    public void setTreatmentPermissions(boolean[] treatmentPermissions) { this.treatmentPermissions = treatmentPermissions; }

    public List<StiTestEntry> getStiTests() { return stiTests; }
    public void setStiTests(List<StiTestEntry> stiTests) { this.stiTests = stiTests; }

    public List<TreatmentEntry> getTreatments() { return treatments; }
    public void setTreatments(List<TreatmentEntry> treatments) { this.treatments = treatments; }

    public String toCsv() {
        StringBuilder sb = new StringBuilder();

        sb.append("Account details\n");
        sb.append("Username,").append(escapeCSV(username)).append("\n");
        sb.append("Terms Accepted,").append(escapeCSV(termsAcceptedDate != null ? termsAcceptedDate : "Not accepted")).append("\n");
        sb.append("\n");

        sb.append("Privacy settings\n");
        sb.append("Data Retention,").append(escapeCSV(dataRetention)).append("\n");
        if (dataAccessPermissions != null) {
            sb.append("Allow Internal Staff,").append(dataAccessPermissions[0] ? "Yes" : "No").append("\n");
            sb.append("Allow Analytics,").append(dataAccessPermissions[1] ? "Yes" : "No").append("\n");
            sb.append("Allow Trusted Partners,").append(dataAccessPermissions[2] ? "Yes" : "No").append("\n");
        }
        sb.append("\n");


        sb.append("Test permissions\n");
        if (testPermissions != null) {
            sb.append("Allow Reminders,").append(testPermissions[0] ? "Yes" : "No").append("\n");
            sb.append("Allow Share with Doctor,").append(testPermissions[1] ? "Yes" : "No").append("\n");
            sb.append("Reminder Interval (months),").append(reminderInterval).append("\n");
        }
        sb.append("\n");

        sb.append("Treatment permissions\n");
        if (treatmentPermissions != null) {
            sb.append("Allow Treatment Reminders,").append(treatmentPermissions[0] ? "Yes" : "No").append("\n");
            sb.append("Allow Clinic Access,").append(treatmentPermissions[1] ? "Yes" : "No").append("\n");
        }
        sb.append("\n");

        sb.append("STI test history\n");
        sb.append("Date,Type,Result,Notes\n");
        if (stiTests != null) {
            for (StiTestEntry test : stiTests) {
                sb.append(escapeCSV(test.getTestDate())).append(",");
                sb.append(escapeCSV(test.getStiType())).append(",");
                sb.append(escapeCSV(test.getResult())).append(",");
                sb.append(escapeCSV(test.getNotes())).append("\n");
            }
        }
        sb.append("\n");

        sb.append("Treatment history\n");
        sb.append("Medication,Dose,Frequency,Start Date,Active,Source,Notes\n");
        if (treatments != null) {
            for (TreatmentEntry t : treatments) {
                sb.append(escapeCSV(t.getMedicationName())).append(",");
                sb.append(escapeCSV(t.getDose())).append(",");
                sb.append(escapeCSV(t.getFrequency())).append(",");
                sb.append(escapeCSV(t.getStartDate())).append(",");
                sb.append(t.isActive() ? "Yes" : "No").append(",");
                sb.append(t.isClinicIssued() ? "Clinic" : "Self").append(",");
                sb.append(escapeCSV(t.getNotes())).append("\n");
            }
        }

        return sb.toString();
    }

    private static String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
