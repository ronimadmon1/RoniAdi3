package entity;

import java.time.LocalDate;
import java.util.Objects;

public class GroupPlanRegistration {
    private final String groupPlanId;         // FK -> Group_Plan.Plan_ID
    private final String traineeId;           // FK -> Trainee.Trainee_ID
    private final LocalDate dateOfRegistration;

    public GroupPlanRegistration(String groupPlanId, String traineeId, LocalDate dateOfRegistration) {
        if (groupPlanId == null || groupPlanId.isBlank()) throw new IllegalArgumentException("groupPlanId is required");
        if (traineeId == null || traineeId.isBlank()) throw new IllegalArgumentException("traineeId is required");
        this.groupPlanId = groupPlanId;
        this.traineeId = traineeId;
        this.dateOfRegistration = dateOfRegistration;
    }

    public String getGroupPlanId() { return groupPlanId; }
    public String getTraineeId() { return traineeId; }
    public LocalDate getDateOfRegistration() { return dateOfRegistration; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupPlanRegistration)) return false;
        GroupPlanRegistration that = (GroupPlanRegistration) o;
        return groupPlanId.equals(that.groupPlanId) && traineeId.equals(that.traineeId);
    }
    @Override public int hashCode() { return Objects.hash(groupPlanId, traineeId); }
}
