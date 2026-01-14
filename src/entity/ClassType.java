package entity;

import java.util.Objects;

public class ClassType {

    // Access: Number
    private int classTypeId;

    // Access: Short Text
    private String classTypeName;

    public ClassType() {}

    public ClassType(int classTypeId, String classTypeName) {
        this.classTypeId = classTypeId;
        this.classTypeName = classTypeName;
    }

    public int getClassTypeId() {
        return classTypeId;
    }

    public void setClassTypeId(int classTypeId) {
        this.classTypeId = classTypeId;
    }

    public String getClassTypeName() {
        return classTypeName;
    }

    public void setClassTypeName(String classTypeName) {
        this.classTypeName = classTypeName;
    }

    @Override
    public String toString() {
        return classTypeName; // מה שמוצג ב-ComboBox
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassType)) return false;
        ClassType classType = (ClassType) o;
        return classTypeId == classType.classTypeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(classTypeId);
    }
}
