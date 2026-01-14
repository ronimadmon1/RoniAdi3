package entity;

public enum PlanStatus {
    ACTIVE, PAUSED, COMPLETED, CANCELLED;

    public static PlanStatus fromDb(String value) {
        return PlanStatus.valueOf(value.trim().toUpperCase());
    }

    public String toDb() {
        return name(); // מחזיר "ACTIVE" וכו'
    }
}
