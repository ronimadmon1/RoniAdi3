package entity;

import java.util.Objects;

public class PersonalPlan {
    private final String planId;      // PK+FK -> Plan.Plan_ID
    private final String traineeId;   // FK -> Trainee.Trainee_ID
    private final String dietaryRestrictions; // טקסט (אם קיים אצלך)

    public PersonalPlan(String planId, String traineeId, String dietaryRestrictions) {
        if (planId == null || planId.isBlank()) throw new IllegalArgumentException("planId is required");
        if (traineeId == null || traineeId.isBlank()) throw new IllegalArgumentException("traineeId is required");
        this.planId = planId;
        this.traineeId = traineeId;
        this.dietaryRestrictions = dietaryRestrictions;
    }

    public String getPlanId() { return planId; }
    public String getTraineeId() { return traineeId; }
    public String getDietaryRestrictions() { return dietaryRestrictions; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonalPlan)) return false;
        PersonalPlan that = (PersonalPlan) o;
        return planId.equals(that.planId);
    }
    @Override public int hashCode() { return Objects.hash(planId); }
}
