package entity;

public enum PreferredContactMethod {
    EMAIL, SMS;

    public static PreferredContactMethod fromDb(String value) {
        return PreferredContactMethod.valueOf(value.trim().toUpperCase());
    }

    public String toDb() {
        return name(); // "EMAIL" או "SMS"
    }
}
