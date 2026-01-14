package entity;

import java.util.Objects;

public class ClassTips {
    private final String classId;   // FK -> Classes.Class_ID
    private final int tipOrder;     // 1..5
    private final String tipText;   // טקסט/קישור

    public ClassTips(String classId, int tipOrder, String tipText) {
        if (classId == null || classId.isBlank()) throw new IllegalArgumentException("classId is required");
        if (tipOrder < 1 || tipOrder > 5) throw new IllegalArgumentException("tipOrder must be between 1 and 5");
        if (tipText == null || tipText.isBlank()) throw new IllegalArgumentException("tipText is required");
        this.classId = classId;
        this.tipOrder = tipOrder;
        this.tipText = tipText;
    }

    public String getClassId() { return classId; }
    public int getTipOrder() { return tipOrder; }
    public String getTipText() { return tipText; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassTips)) return false;
        ClassTips that = (ClassTips) o;
        return tipOrder == that.tipOrder && classId.equals(that.classId);
    }
    @Override public int hashCode() { return Objects.hash(classId, tipOrder); }
}
