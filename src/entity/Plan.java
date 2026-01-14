package entity;

import java.time.LocalDate;
import java.util.Objects;

public class Plan {
    private String planId;                 // Plan_ID (PK)
    private LocalDate planStartDate;        // Plan_Start_Date
    private int planDurationWeeks;          // Plan_Duration (Weeks)
    private PlanStatus planStatus;          // Plan_Status

    public enum PlanStatus { ACTIVE, PAUSED, COMPLETED, CANCELLED }

    public Plan(String planId, LocalDate planStartDate, int planDurationWeeks, PlanStatus planStatus) {
        this.planId = planId;
        this.planStartDate = planStartDate;
        this.planDurationWeeks = planDurationWeeks;
        this.planStatus = planStatus;
    }

    public String getPlanId() { return planId; }
    public LocalDate getPlanStartDate() { return planStartDate; }
    public int getPlanDurationWeeks() { return planDurationWeeks; }
    public PlanStatus getPlanStatus() { return planStatus; }

    public void setPlanStartDate(LocalDate planStartDate) { this.planStartDate = planStartDate; }
    public void setPlanDurationWeeks(int planDurationWeeks) { this.planDurationWeeks = planDurationWeeks; }
    public void setPlanStatus(PlanStatus planStatus) { this.planStatus = planStatus; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Plan)) return false;
        Plan plan = (Plan) o;
        return Objects.equals(planId, plan.planId);
    }
    @Override public int hashCode() { return Objects.hash(planId); }
}
