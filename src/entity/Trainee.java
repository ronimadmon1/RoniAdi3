package entity;

import java.util.Date;
import java.util.Objects;

public class Trainee {

    private String traineeId;        // PK
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String preferredContact; // "Email" / "SMS"
    private Date birthDate;

    // constructor מלא
    public Trainee(String traineeId, String firstName, String lastName,
                   String phone, String email, String preferredContact,
                   Date birthDate) {
        this.traineeId = traineeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.preferredContact = preferredContact;
        this.birthDate = birthDate;
    }

    // constructor ריק
    public Trainee() {
    }

    // getters & setters
    public String getTraineeId() {
        return traineeId;
    }

    public void setTraineeId(String traineeId) {
        this.traineeId = traineeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPreferredContact() {
        return preferredContact;
    }

    public void setPreferredContact(String preferredContact) {
        this.preferredContact = preferredContact;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    // equals & hashCode לפי PK בלבד
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trainee)) return false;
        Trainee trainee = (Trainee) o;
        return Objects.equals(traineeId, trainee.traineeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(traineeId);
    }

    // נוח ל-ComboBox / Debug
    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}
