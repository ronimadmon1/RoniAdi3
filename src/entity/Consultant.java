package entity;

import java.util.Objects;

public class Consultant {
    private String consultantId;   // Consultant_ID (PK)
    private String firstName;      // First_Name
    private String lastName;       // Last_Name
    private String phone;          // Phone
    private String email;          // Email
    private boolean isManager;     // Is_Manager (Yes/No)

    public Consultant(String consultantId, String firstName, String lastName,
                      String phone, String email, boolean isManager) {
        this.consultantId = consultantId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.isManager = isManager;
    }

    public String getConsultantId() { return consultantId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public boolean isManager() { return isManager; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Consultant)) return false;
        Consultant that = (Consultant) o;
        return Objects.equals(consultantId, that.consultantId);
    }
    @Override public int hashCode() { return Objects.hash(consultantId); }
}
