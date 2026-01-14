package entity;

import java.util.Objects;

public class PlanClassAssignment {
    private final String planId;   // FK -> Plan.Plan_ID
    private final String classId;  // FK -> Classes.Class_ID

    public PlanClassAssignment(String planId, String classId) {
        if (planId == null || planId.isBlank()) throw new IllegalArgumentException("planId is required");
        if (classId == null || classId.isBlank()) throw new IllegalArgumentException("classId is required");
        this.planId = planId;
        this.classId = classId;
    }

    public String getPlanId() { return planId; }
    public String getClassId() { return classId; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanClassAssignment)) return false;
        PlanClassAssignment that = (PlanClassAssignment) o;
        return planId.equals(that.planId) && classId.equals(that.classId);
    }
    @Override public int hashCode() { return Objects.hash(planId, classId); }
}
