package app.model;

public class TreatmentEntry {
    private final int id;
    private final String username;
    private final String medicationName;
    private final String dose;
    private final String frequency;
    private final double quantityRemaining;
    private final int lowStockThreshold;
    private final String startDate;
    private final String notes;
    private final boolean active;

    public TreatmentEntry(int id, String username, String medicationName, String dose,
                          String frequency, double quantityRemaining, int lowStockThreshold,
                          String startDate, String notes, boolean active) {
        this.id = id;
        this.username = username;
        this.medicationName = medicationName;
        this.dose = dose;
        this.frequency = frequency;
        this.quantityRemaining = quantityRemaining;
        this.lowStockThreshold = lowStockThreshold;
        this.startDate = startDate;
        this.notes = notes;
        this.active = active;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getMedicationName() { return medicationName; }
    public String getDose() { return dose; }
    public String getFrequency() { return frequency; }
    public double getQuantityRemaining() { return quantityRemaining; }
    public int getLowStockThreshold() { return lowStockThreshold; }
    public String getStartDate() { return startDate; }
    public String getNotes() { return notes; }
    public boolean isActive() { return active; }

    // Returns how many doses are administered per day for this frequency
    public double dosesPerDay() {
        return switch (frequency) {
            case "Once daily" -> 1.0;
            case "Twice daily" -> 2.0;
            case "Three times daily" -> 3.0;
            case "Weekly" -> 1.0 / 7.0;
            default -> 0.0; // "As needed" — no automatic stock calculation
        };
    }

    // Returns estimated days of supply remaining; MAX_VALUE when frequency is "As needed"
    public double daysRemaining() {
        double dpd = dosesPerDay();
        if (dpd <= 0) return Double.MAX_VALUE;
        return quantityRemaining / dpd;
    }

    // Returns true when supply is at or below the low-stock threshold (in days)
    public boolean isLowStock() {
        return dosesPerDay() > 0 && daysRemaining() <= lowStockThreshold;
    }
}
