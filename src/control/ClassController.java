package control;

import entity.ClassType;
import entity.Classes;
import entity.Consts;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class ClassController {

    private static ClassController instance;

    private ClassController() {}

    public static ClassController getInstance() {
        if (instance == null) instance = new ClassController();
        return instance;
    }

    public ArrayList<ClassType> getClassTypes() {
        ArrayList<ClassType> results = new ArrayList<>();
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SEL_CLASS_TYPES);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    results.add(new ClassType(
                            rs.getInt("Class_Type_ID"),
                            rs.getString("Class_Type_Name")
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Get class types failed: " + e.getMessage(), e);
        }
        return results;
    }

    public boolean addClassType(String typeName) {
        if (typeName == null || typeName.isBlank())
            throw new IllegalArgumentException("Type name is required");

        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR)) {

                int nextId = generateNextClassTypeId(conn);

                try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_INS_CLASS_TYPE)) {
                    stmt.setInt(1, nextId);
                    stmt.setString(2, typeName.trim());
                    stmt.executeUpdate();
                    return true;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Add class type failed: " + e.getMessage(), e);
        }
    }

    public boolean updateClassType(int typeId, String newName) {
        if (typeId <= 0) throw new IllegalArgumentException("Type ID is required");
        if (newName == null || newName.isBlank())
            throw new IllegalArgumentException("Type name is required");

        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(Consts.SQL_UPD_CLASS_TYPE)) {

                stmt.setString(1, newName.trim());
                stmt.setInt(2, typeId);
                stmt.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException("Update class type failed: " + e.getMessage(), e);
        }
    }

    public boolean deleteClassType(int typeId) {
        if (typeId <= 0) throw new IllegalArgumentException("Type ID is required");

        if (isClassTypeInUse(typeId)) {
            throw new IllegalStateException("Cannot delete: this type is used by existing classes.");
        }

        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(Consts.SQL_DEL_CLASS_TYPE)) {

                stmt.setInt(1, typeId);
                stmt.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException("Delete class type failed: " + e.getMessage(), e);
        }
    }

    // ✅ FIXED: return false when not in use
    private boolean isClassTypeInUse(int typeId) {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(Consts.SQL_CNT_CLASSES_BY_TYPE)) {

                stmt.setInt(1, typeId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) return rs.getInt(1) > 0;
                }
            }
            return false; // ✅ not in use
        } catch (Exception e) {
            // safer default: don't delete if DB error
            return true;
        }
    }

    private int generateNextClassTypeId(Connection conn) throws SQLException {
        final String sql = "SELECT MAX(Class_Type_ID) FROM Class_Type";
        int maxId = 0;

        try (PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            if (rs.next()) maxId = rs.getInt(1);
        }
        return maxId + 1;
    }

    public ArrayList<Classes> getClasses() {
        ArrayList<Classes> results = new ArrayList<>();
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SEL_CLASSES);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    results.add(mapClasses(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Get classes failed: " + e.getMessage(), e);
        }
        return results;
    }

    public Classes getClassById(String classId) {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SEL_CLASS_BY_ID)) {

                stmt.setString(1, classId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) return mapClasses(rs);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Get class by ID failed: " + e.getMessage(), e);
        }
        return null;
    }

    public boolean addClass(Classes c) {
        validateClasses(c);

        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR)) {

                if (c.getClassId() == null || c.getClassId().isBlank()) {
                    c.setClassId(generateNextClassId(conn));
                }

                try (PreparedStatement stmt = conn.prepareStatement(Consts.SQL_INS_CLASS)) {

                    int i = 1;
                    stmt.setString(i++, c.getClassId());
                    stmt.setString(i++, c.getName());
                    stmt.setInt(i++, c.getClassTypeId());
                    stmt.setDate(i++, Date.valueOf(c.getClassDate()));
                    stmt.setTime(i++, Time.valueOf(c.getStartTime()));
                    stmt.setTime(i++, Time.valueOf(c.getEndTime()));
                    stmt.setInt(i++, c.getMaxParticipants());
                    stmt.setString(i++, c.getConsultantId());

                    stmt.executeUpdate();
                    return true;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Add class failed: " + e.getMessage(), e);
        }
    }

    public boolean updateClass(Classes c) {
        if (c.getClassId() == null || c.getClassId().isBlank())
            throw new IllegalArgumentException("Class_ID is required for update");

        validateClasses(c);

        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(Consts.SQL_UPD_CLASS)) {

                int i = 1;
                stmt.setString(i++, c.getName());
                stmt.setInt(i++, c.getClassTypeId());
                stmt.setDate(i++, Date.valueOf(c.getClassDate()));
                stmt.setTime(i++, Time.valueOf(c.getStartTime()));
                stmt.setTime(i++, Time.valueOf(c.getEndTime()));
                stmt.setInt(i++, c.getMaxParticipants());
                stmt.setString(i++, c.getConsultantId());
                stmt.setString(i++, c.getClassId());

                stmt.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException("Update class failed: " + e.getMessage(), e);
        }
    }

    public boolean deleteClass(String classId) {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(Consts.SQL_DEL_CLASS)) {

                stmt.setString(1, classId);
                stmt.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException("Delete class failed: " + e.getMessage(), e);
        }
    }

    private void validateClasses(Classes c) {
        if (c == null) throw new IllegalArgumentException("Classes is null");

        if (c.getName() == null || c.getName().isBlank())
            throw new IllegalArgumentException("Class_Name is required");

        if (c.getClassDate() == null)
            throw new IllegalArgumentException("Class_Date is required");

        if (c.getStartTime() == null || c.getEndTime() == null)
            throw new IllegalArgumentException("Start/End time required");

        if (!c.getEndTime().isAfter(c.getStartTime()))
            throw new IllegalArgumentException("End_Time must be after Start_Time");

        if (c.getMaxParticipants() <= 0)
            throw new IllegalArgumentException("Max_Participants must be > 0");

        if (c.getClassTypeId() <= 0)
            throw new IllegalArgumentException("Class_Type is required");

        if (c.getConsultantId() == null || c.getConsultantId().isBlank())
            throw new IllegalArgumentException("Consultant_ID is required");
    }

    private String generateNextClassId(Connection conn) throws SQLException {

        final String sql =
                "SELECT MAX(VAL(MID(Class_ID, 3))) " +
                "FROM Classes " +
                "WHERE Class_ID LIKE 'CL*'";

        int maxNum = 0;

        try (PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            if (rs.next()) {
                maxNum = rs.getInt(1);
            }
        }

        int nextNum = maxNum + 1;
        return String.format("CL%03d", nextNum);
    }

    private Classes mapClasses(ResultSet rs) throws SQLException {
        String classId = rs.getString("Class_ID");
        String name = rs.getString("Class_Name");
        int classTypeId = rs.getInt("Class_Type");

        LocalDate classDate = rs.getDate("Class_Date").toLocalDate();
        LocalTime startTime = rs.getTime("Start_Time").toLocalTime();
        LocalTime endTime = rs.getTime("End_Time").toLocalTime();

        int maxParticipants = rs.getInt("Max_Participants");
        String consultantId = rs.getString("Consultant_ID");

        return new Classes(
                classId,
                name,
                classTypeId,
                classDate,
                startTime,
                endTime,
                maxParticipants,
                consultantId,
                null
        );
    }
}
