package control;

import entity.AttendanceStatus;
import entity.Consts;
import entity.Classes;

import javax.swing.JOptionPane;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClassRegistrationController {

    private static ClassRegistrationController instance;

    // warning on late cancel (<= 2 hours)
    private static final long LATE_CANCEL_WARNING_MINUTES = 2L * 60L;

    private ClassRegistrationController() {}

    public static ClassRegistrationController getInstance() {
        if (instance == null) instance = new ClassRegistrationController();
        return instance;
    }

    // ---------- DTO for UI ----------
    public static class ClassRow {
        private final Classes c;
        private final int registered;
        private final int capacity;
        private final String audience; // Personal / Group / Both / Unassigned
        private final String status;
        private final boolean canRegister;
        private final boolean canCancel;

        public ClassRow(Classes c, int registered, int capacity, String audience, String status,
                        boolean canRegister, boolean canCancel) {
            this.c = c;
            this.registered = registered;
            this.capacity = capacity;
            this.audience = audience;
            this.status = status;
            this.canRegister = canRegister;
            this.canCancel = canCancel;
        }

        public Classes getC() { return c; }
        public int getRegistered() { return registered; }
        public int getCapacity() { return capacity; }
        public String getAudience() { return audience; }
        public String getStatus() { return status; }
        public boolean canRegister() { return canRegister; }
        public boolean canCancel() { return canCancel; }
    }

    // ---------- enforce trainee must have at least one plan ----------
    public boolean traineeHasAnyPlan(String traineeId) {
        traineeId = requireId(traineeId, "Trainee ID");

        try {
            Consts.assertDbExists();
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");

            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR)) {

                int personal = 0;
                try (PreparedStatement st = conn.prepareStatement(Consts.SQL_SEL_TRAINEE_HAS_PERSONAL)) {
                    st.setString(1, traineeId);
                    try (ResultSet rs = st.executeQuery()) { rs.next(); personal = rs.getInt(1); }
                }

                int group = 0;
                try (PreparedStatement st = conn.prepareStatement(Consts.SQL_SEL_TRAINEE_HAS_GROUP)) {
                    st.setString(1, traineeId);
                    try (ResultSet rs = st.executeQuery()) { rs.next(); group = rs.getInt(1); }
                }

                return personal > 0 || group > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String audienceLabel(boolean isPersonal, boolean isGroup) {
        if (isPersonal && isGroup) return "Both";
        if (isPersonal) return "Personal";
        if (isGroup) return "Group";
        return "Unassigned";
    }

    // ---------- Assigned + Eligible by trainee plan types ----------
    public List<ClassRow> getAssignedEligibleClassesWithStatus(String traineeId) {
        traineeId = requireId(traineeId, "Trainee ID");

        if (!traineeHasAnyPlan(traineeId)) {
            throw new IllegalStateException("This trainee has no Personal/Group plan.");
        }

        List<Classes> assigned = getAssignedClassesForTrainee(traineeId);
        List<ClassRow> out = new ArrayList<>();

        try {
            Consts.assertDbExists();
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");

            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR)) {

                boolean hasPersonal = hasPersonalPlan(conn, traineeId);
                boolean hasGroup = hasGroupPlan(conn, traineeId);

                for (Classes c : assigned) {
                    String classId = c.getClassId();

                    boolean isPersonalForTrainee = isClassAssignedToTraineePersonal(conn, traineeId, classId);
                    boolean isGroupForTrainee = isClassAssignedToTraineeGroup(conn, traineeId, classId);

                    // filter exactly as requested
                    if (hasPersonal && !hasGroup) {
                        if (!isPersonalForTrainee) continue;
                    } else if (!hasPersonal && hasGroup) {
                        if (!isGroupForTrainee) continue;
                    } else {
                        if (!isPersonalForTrainee && !isGroupForTrainee) continue;
                    }

                    String audience = audienceLabel(isPersonalForTrainee, isGroupForTrainee);

                    boolean already = registrationExistsActive(traineeId, classId);

                    int reg = countRegistered(classId);
                    int cap = c.getMaxParticipants();
                    boolean full = reg >= cap;

                    LocalDateTime start = LocalDateTime.of(c.getClassDate(), c.getStartTime());
                    long minutesToStart = Duration.between(LocalDateTime.now(), start).toMinutes();

                    String status;
                    boolean canRegister;
                    boolean canCancel;

                    if (already) {
                        canRegister = false;
                        canCancel = true; // ✅ always allow cancel (with warning if late)

                        if (minutesToStart <= 0) {
                            status = "Registered (started/ended)";
                        } else if (minutesToStart <= LATE_CANCEL_WARNING_MINUTES) {
                            status = "Registered (late cancel warning)";
                        } else {
                            status = "Registered (can cancel)";
                        }

                    } else if (minutesToStart <= 0) {
                        status = "Already started";
                        canRegister = false;
                        canCancel = false;

                    } else if (minutesToStart < 24L * 60L) {
                        status = "Less than 24 hours";
                        canRegister = false;
                        canCancel = false;

                    } else if (full) {
                        status = "Class Full";
                        canRegister = false;
                        canCancel = false;

                    } else {
                        status = "Available";
                        canRegister = true;
                        canCancel = false;
                    }

                    out.add(new ClassRow(c, reg, cap, audience, status, canRegister, canCancel));
                }
            }

        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Failed to load eligible classes.");
        }

        return out;
    }

    // ---------- Registration Actions ----------
    public boolean registerToClass(String traineeId, String classId) {
        traineeId = requireId(traineeId, "Trainee ID");
        classId = requireId(classId, "Class ID");

        if (!traineeHasAnyPlan(traineeId)) {
            throw new IllegalStateException("Cannot register: trainee has no Personal/Group plan.");
        }

        if (!isClassAssignedToTraineePlans(traineeId, classId)) {
            throw new IllegalStateException("Cannot register: class is not assigned to trainee's plans.");
        }

        ClassInfo info = fetchClassInfo(classId);
        if (info == null) throw new IllegalArgumentException("Class not found: " + classId);

        LocalDateTime classEnd = fetchClassEnd(classId);
        if (classEnd == null) throw new IllegalStateException("Failed to read class end time.");

        return registerToClass(traineeId, classId, info.classStart, classEnd, info.maxParticipants);
    }

    private boolean registerToClass(String traineeId, String classId,
                                    LocalDateTime classStart, LocalDateTime classEnd,
                                    int maxParticipants) {

        long minutes = Duration.between(LocalDateTime.now(), classStart).toMinutes();

        if (minutes <= 0) throw new IllegalStateException("Cannot register: class already started/ended.");
        if (minutes < 24L * 60L) throw new IllegalStateException("Cannot register less than 24 hours before class start.");

        if (registrationExistsActive(traineeId, classId)) {
            throw new IllegalStateException("Trainee already has an active registration for this class.");
        }

        // ✅ warn on overlap (not block)
        if (hasOverlappingClass(traineeId, classStart, classEnd)) {
            int confirm = JOptionPane.showConfirmDialog(
                    null,
                    "Warning: You are already registered to another class that overlaps this time.\n" +
                            "Do you want to continue anyway?",
                    "Overlapping Classes",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) return false;
        }

        int activeCount = countRegistered(classId);
        if (activeCount >= maxParticipants) throw new IllegalStateException("Class is full.");

        try {
            Consts.assertDbExists();
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");

            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(Consts.SQL_INS_REGISTRATION)) {

                int i = 1;
                stmt.setString(i++, traineeId);
                stmt.setString(i++, classId);
                stmt.setTimestamp(i++, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(i, AttendanceStatus.REGISTERED.name());

                stmt.executeUpdate();
                return true;
            }
        } catch (SQLIntegrityConstraintViolationException dup) {
            throw new IllegalStateException("Trainee is already registered to this class.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Registration failed: " + e.getMessage());
        }
    }

    /**
     * ✅ Cancel always allowed (if active registration exists).
     * If <= 2 hours before start -> warning.
     */
    public boolean cancelRegistration(String traineeId, String classId) {
        traineeId = requireId(traineeId, "Trainee ID");
        classId = requireId(classId, "Class ID");

        if (!registrationExistsActive(traineeId, classId)) {
            throw new IllegalStateException("No active registration to cancel.");
        }

        ClassInfo info = fetchClassInfo(classId);
        if (info == null) throw new IllegalArgumentException("Class not found: " + classId);

        long minutes = Duration.between(LocalDateTime.now(), info.classStart).toMinutes();

        if (minutes > 0 && minutes <= LATE_CANCEL_WARNING_MINUTES) {
            int confirm = JOptionPane.showConfirmDialog(
                    null,
                    "Warning: The class starts in " + minutes + " minutes.\n" +
                            "Are you sure you want to cancel?",
                    "Late Cancellation Warning",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return false;
            }
        }

        return updateStatus(traineeId, classId, AttendanceStatus.CANCELLED);
    }

    // ---------- overlap helpers ----------
    private boolean hasOverlappingClass(String traineeId, LocalDateTime newStart, LocalDateTime newEnd) {
        try {
            Consts.assertDbExists();
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");

            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SEL_OVERLAPPING_CLASSES_FOR_TRAINEE)) {

                stmt.setString(1, traineeId);
                stmt.setDate(2, Date.valueOf(newStart.toLocalDate()));
                stmt.setTime(3, Time.valueOf(newEnd.toLocalTime()));   // newEnd
                stmt.setTime(4, Time.valueOf(newStart.toLocalTime())); // newStart

                try (ResultSet rs = stmt.executeQuery()) {
                    rs.next();
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private LocalDateTime fetchClassEnd(String classId) {
        final String sql = "SELECT Class_Date, End_Time FROM Classes WHERE Class_ID=?";
        try {
            Consts.assertDbExists();
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");

            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, classId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) return null;

                    return LocalDateTime.of(
                            rs.getDate(1).toLocalDate(),
                            rs.getTime(2).toLocalTime()
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ---------- Helpers (DB) ----------
    private List<Classes> getAssignedClassesForTrainee(String traineeId) {
        List<Classes> out = new ArrayList<>();

        try {
            Consts.assertDbExists();
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");

            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SEL_ASSIGNED_CLASSES_FOR_TRAINEE)) {

                stmt.setString(1, traineeId);
                stmt.setString(2, traineeId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Classes c = new Classes(
                                rs.getString("Class_ID"),
                                rs.getString("Class_Name"),
                                rs.getInt("Class_Type"),
                                rs.getDate("Class_Date").toLocalDate(),
                                rs.getTime("Start_Time").toLocalTime(),
                                rs.getTime("End_Time").toLocalTime(),
                                rs.getInt("Max_Participants"),
                                rs.getString("Consultant_ID"),
                                null // ✅ FIX: no Class_Tips column here
                        );
                        out.add(c);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out;
    }

    private boolean isClassAssignedToTraineePlans(String traineeId, String classId) {
        final String sql =
                "SELECT COUNT(*) " +
                "FROM Plan_Class_Assignment pca " +
                "WHERE pca.Class_ID = ? " +
                "AND pca.Plan_ID IN ( " +
                "   SELECT pp.Plan_ID FROM Personal_Plan pp WHERE pp.Trainee_ID = ? " +
                "   UNION " +
                "   SELECT gpr.Group_Plan_ID FROM " + Consts.GROUP_REG_TABLE + " gpr WHERE gpr.Trainee_ID = ? " +
                ")";

        try {
            Consts.assertDbExists();
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");

            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, classId);
                stmt.setString(2, traineeId);
                stmt.setString(3, traineeId);

                try (ResultSet rs = stmt.executeQuery()) {
                    rs.next();
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private int countRegistered(String classId) {
        try {
            Consts.assertDbExists();
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");

            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SEL_ACTIVE_REG_COUNT)) {

                stmt.setString(1, classId);
                try (ResultSet rs = stmt.executeQuery()) {
                    rs.next();
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private boolean registrationExistsActive(String traineeId, String classId) {
        try {
            Consts.assertDbExists();
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");

            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(Consts.SQL_SEL_REG_EXISTS)) {

                stmt.setString(1, traineeId);
                stmt.setString(2, classId);

                try (ResultSet rs = stmt.executeQuery()) {
                    rs.next();
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean updateStatus(String traineeId, String classId, AttendanceStatus status) {
        try {
            Consts.assertDbExists();
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");

            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(Consts.SQL_UPD_REG_STATUS)) {

                stmt.setString(1, status.name());
                stmt.setString(2, traineeId);
                stmt.setString(3, classId);

                int updated = stmt.executeUpdate();
                return updated > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private ClassInfo fetchClassInfo(String classId) {
        final String sql = "SELECT Class_Date, Start_Time, Max_Participants FROM Classes WHERE Class_ID=?";
        try {
            Consts.assertDbExists();
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");

            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, classId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) return null;

                    LocalDateTime start = LocalDateTime.of(
                            rs.getDate(1).toLocalDate(),
                            rs.getTime(2).toLocalTime()
                    );
                    int max = rs.getInt(3);

                    return new ClassInfo(start, max);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class ClassInfo {
        final LocalDateTime classStart;
        final int maxParticipants;

        ClassInfo(LocalDateTime classStart, int maxParticipants) {
            this.classStart = classStart;
            this.maxParticipants = maxParticipants;
        }
    }

    private String requireId(String s, String fieldName) {
        Objects.requireNonNull(s, fieldName + " is required");
        s = s.trim();
        if (s.isEmpty()) throw new IllegalArgumentException(fieldName + " is required");
        return s;
    }

    // ---------- helpers for audience-by-trainee ----------
    private boolean hasPersonalPlan(Connection conn, String traineeId) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement(Consts.SQL_SEL_TRAINEE_HAS_PERSONAL)) {
            st.setString(1, traineeId);
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private boolean hasGroupPlan(Connection conn, String traineeId) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement(Consts.SQL_SEL_TRAINEE_HAS_GROUP)) {
            st.setString(1, traineeId);
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private boolean isClassAssignedToTraineePersonal(Connection conn, String traineeId, String classId) throws SQLException {
        final String sql =
                "SELECT COUNT(*) " +
                "FROM (Plan_Class_Assignment PCA " +
                "INNER JOIN Personal_Plan PP ON PCA.Plan_ID = PP.Plan_ID) " +
                "WHERE PCA.Class_ID = ? AND PP.Trainee_ID = ?";

        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, classId);
            st.setString(2, traineeId);
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private boolean isClassAssignedToTraineeGroup(Connection conn, String traineeId, String classId) throws SQLException {
        final String sql =
                "SELECT COUNT(*) " +
                "FROM (Plan_Class_Assignment PCA " +
                "INNER JOIN " + Consts.GROUP_REG_TABLE + " GPR ON PCA.Plan_ID = GPR.Group_Plan_ID) " +
                "WHERE PCA.Class_ID = ? AND GPR.Trainee_ID = ?";

        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, classId);
            st.setString(2, traineeId);
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}
