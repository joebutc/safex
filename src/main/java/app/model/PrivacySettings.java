package app.model;

import java.util.HashMap;

public class PrivacySettings {
    private final String tsAndCs;
    private final HashMap<String, String> storedInfo = new HashMap<>();
    private DataRetentionType dataRetention;

    public String getTsAndCs() {
        return tsAndCs;
    }

    public String getStoredInfo(String key) {
        return storedInfo.get(key);
    }

    public DataRetentionType getDataRetention() {
        return dataRetention;
    }

    public void setStoredInfo(String key, String value) {
        storedInfo.put(key, value);
    }

    public void setDataRetention(DataRetentionType dataRetention) {
        this.dataRetention = dataRetention;
    }

    public PrivacySettings(DataRetentionType dataRetention) {
        this.dataRetention = dataRetention;
        tsAndCs = "Test";
    }
}
