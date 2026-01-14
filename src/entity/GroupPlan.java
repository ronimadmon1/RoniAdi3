package entity;

import java.util.Objects;

public class GroupPlan {
    private final String planId;              // PK+FK -> Plan.Plan_ID
    private final int traineeMinAge;
    private final int traineeMaxAge;
    private final String generalGuidelines;

    public GroupPlan(String planId, int traineeMinAge, int traineeMaxAge, String generalGuidelines) {
        if (planId == null || planId.isBlank()) throw new IllegalArgumentException("planId is required");
        if (traineeMinAge < 0 || traineeMaxAge < 0 || traineeMinAge > traineeMaxAge) {
            throw new IllegalArgumentException("Invalid age range");
        }
        this.planId = planId;
        this.traineeMinAge = traineeMinAge;
        this.traineeMaxAge = traineeMaxAge;
        this.generalGuidelines = generalGuidelines;
    }

    public String getPlanId() { return planId; }
    public int getTraineeMinAge() { return traineeMinAge; }
    public int getTraineeMaxAge() { return traineeMaxAge; }
    public String getGeneralGuidelines() { return generalGuidelines; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupPlan)) return false;
        GroupPlan that = (GroupPlan) o;
        return planId.equals(that.planId);
    }
    @Override public int hashCode() { return Objects.hash(planId); }
}

