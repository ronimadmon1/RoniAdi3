package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class Classes {

    private String classId;
    private String name;
    private int classTypeId;
    private LocalDate classDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int maxParticipants;
    private String consultantId;
    private String classTips;

    public Classes() {}

    public Classes(String classId, String name, int classTypeId,
                   LocalDate classDate, LocalTime startTime, LocalTime endTime,
                   int maxParticipants, String consultantId, String classTips) {
        this.classId = classId;
        this.name = name;
        this.classTypeId = classTypeId;
        this.classDate = classDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxParticipants = maxParticipants;
        this.consultantId = consultantId;
        this.classTips = classTips;
    }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getClassTypeId() { return classTypeId; }
    public void setClassTypeId(int classTypeId) { this.classTypeId = classTypeId; }

    public LocalDate getClassDate() { return classDate; }
    public void setClassDate(LocalDate classDate) { this.classDate = classDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }

    public String getConsultantId() { return consultantId; }
    public void setConsultantId(String consultantId) { this.consultantId = consultantId; }

    public String getClassTips() { return classTips; }
    public void setClassTips(String classTips) { this.classTips = classTips; }

    public LocalDateTime getStartDateTime() {
        if (classDate == null || startTime == null) return null;
        return LocalDateTime.of(classDate, startTime);
    }

    @Override
    public String toString() {
        return classId + " - " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Classes)) return false;
        Classes classes = (Classes) o;
        return Objects.equals(classId, classes.classId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classId);
    }
}
