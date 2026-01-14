package entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class ClassRegistration {
    private String traineeId;          // אצלך זה Short Text
    private String classId;            // אצלך זה Short Text ב-Class_Registration
    private LocalDateTime registrationDate;
    private AttendanceStatus attendanceStatus;

    public ClassRegistration() {}

    public ClassRegistration(String traineeId, String classId,
                             LocalDateTime registrationDate,
                             AttendanceStatus attendanceStatus) {
        this.traineeId = traineeId;
        this.classId = classId;
        this.registrationDate = registrationDate;
        this.attendanceStatus = attendanceStatus;
    }

    public String getTraineeId() { return traineeId; }
    public void setTraineeId(String traineeId) { this.traineeId = traineeId; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }

    public AttendanceStatus getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(AttendanceStatus attendanceStatus) { this.attendanceStatus = attendanceStatus; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassRegistration)) return false;
        ClassRegistration that = (ClassRegistration) o;
        return Objects.equals(traineeId, that.traineeId) &&
               Objects.equals(classId, that.classId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(traineeId, classId);
    }
}
