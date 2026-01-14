package entity;

import java.io.File;

public class Consts {

    private static final String DB_FILE_NAME = "Database.accdb";

    public static final String DB_PATH =
            System.getProperty("user.dir") + File.separator + DB_FILE_NAME;

    public static final String CONN_STR =
            "jdbc:ucanaccess://" + DB_PATH + ";COLUMNORDER=DISPLAY";

    public static final String GROUP_REG_TABLE = "Group_Plan_Registration";

    public static void assertDbExists() {
        File f = new File(DB_PATH);
        if (!f.exists()) {
            throw new IllegalStateException("Database file not found: " + f.getAbsolutePath());
        }
    }

    public static final String SQL_SEL_CLASS_TYPES =
            "SELECT Class_Type_ID, Class_Type_Name " +
            "FROM Class_Type " +
            "ORDER BY Class_Type_Name";

    public static final String SQL_SEL_CLASSES =
            "SELECT Class_ID, Class_Name, Class_Type, Class_Date, Start_Time, End_Time, " +
            "Max_Participants, Consultant_ID " +
            "FROM Classes " +
            "ORDER BY Class_Date, Start_Time";

    public static final String SQL_SEL_CLASS_BY_ID =
            "SELECT Class_ID, Class_Name, Class_Type, Class_Date, Start_Time, End_Time, " +
            "Max_Participants, Consultant_ID " +
            "FROM Classes WHERE Class_ID = ?";

    public static final String SQL_SEL_ACTIVE_REG_COUNT =
            "SELECT COUNT(*) " +
            "FROM Class_Registration " +
            "WHERE Class_ID = ? AND Attendance_Status <> 'CANCELLED'";

    public static final String SQL_SEL_REG_EXISTS =
            "SELECT COUNT(*) " +
            "FROM Class_Registration " +
            "WHERE Trainee_ID = ? AND Class_ID = ? AND Attendance_Status <> 'CANCELLED'";

    public static final String SQL_SEL_TRAINEE_HAS_PERSONAL =
            "SELECT COUNT(*) FROM Personal_Plan WHERE Trainee_ID = ?";

    public static final String SQL_SEL_TRAINEE_HAS_GROUP =
            "SELECT COUNT(*) FROM " + GROUP_REG_TABLE + " WHERE Trainee_ID = ?";

    public static final String SQL_SEL_ASSIGNED_CLASSES_FOR_TRAINEE =
            "SELECT DISTINCT c.Class_ID, c.Class_Name, c.Class_Type, c.Class_Date, " +
            "c.Start_Time, c.End_Time, c.Max_Participants, c.Consultant_ID " +
            "FROM (Classes AS c " +
            "INNER JOIN Plan_Class_Assignment AS pca ON c.Class_ID = pca.Class_ID) " +
            "WHERE pca.Plan_ID IN ( " +
            "   SELECT pp.Plan_ID FROM Personal_Plan pp WHERE pp.Trainee_ID = ? " +
            "   UNION " +
            "   SELECT gpr.Group_Plan_ID FROM " + GROUP_REG_TABLE + " gpr WHERE gpr.Trainee_ID = ? " +
            ") " +
            "ORDER BY c.Class_Date, c.Start_Time";

    public static final String SQL_SEL_CLASS_ASSIGNED_TO_TRAINEE =
            "SELECT COUNT(*) " +
            "FROM Plan_Class_Assignment pca " +
            "WHERE pca.Class_ID = ? " +
            "AND pca.Plan_ID IN ( " +
            "   SELECT pp.Plan_ID FROM Personal_Plan pp WHERE pp.Trainee_ID = ? " +
            "   UNION " +
            "   SELECT gpr.Group_Plan_ID FROM " + GROUP_REG_TABLE + " gpr WHERE gpr.Trainee_ID = ? " +
            ")";

    public static final String SQL_INS_CLASS =
            "INSERT INTO Classes (Class_ID, Class_Name, Class_Type, Class_Date, Start_Time, End_Time, " +
            "Max_Participants, Consultant_ID) " +
            "VALUES (?,?,?,?,?,?,?,?)";

    public static final String SQL_UPD_CLASS =
            "UPDATE Classes SET Class_Name=?, Class_Type=?, Class_Date=?, Start_Time=?, End_Time=?, " +
            "Max_Participants=?, Consultant_ID=? " +
            "WHERE Class_ID=?";

    public static final String SQL_DEL_CLASS =
            "DELETE FROM Classes WHERE Class_ID=?";

    public static final String SQL_INS_REGISTRATION =
            "INSERT INTO Class_Registration (Trainee_ID, Class_ID, Registration_Date, Attendance_Status) " +
            "VALUES (?,?,?,?)";

    public static final String SQL_UPD_REG_STATUS =
            "UPDATE Class_Registration SET Attendance_Status=? " +
            "WHERE Trainee_ID=? AND Class_ID=?";

    public static final String SQL_INS_CLASS_TYPE =
            "INSERT INTO Class_Type (Class_Type_ID, Class_Type_Name) VALUES (?,?)";

    public static final String SQL_UPD_CLASS_TYPE =
            "UPDATE Class_Type SET Class_Type_Name=? WHERE Class_Type_ID=?";

    public static final String SQL_DEL_CLASS_TYPE =
            "DELETE FROM Class_Type WHERE Class_Type_ID=?";

    public static final String SQL_CNT_CLASSES_BY_TYPE =
            "SELECT COUNT(*) FROM Classes WHERE Class_Type = ?";

    public static final String SQL_SEL_TIPS_BY_CLASS_ID =
            "SELECT tip_id, class_id, resource_link, tip_text " +
            "FROM Class_Tips " +
            "WHERE class_id = ? " +
            "ORDER BY tip_id";

    public static final String SQL_INS_TIP =
            "INSERT INTO Class_Tips (tip_id, class_id, resource_link, tip_text) " +
            "VALUES (?,?,?,?)";

    public static final String SQL_DEL_TIPS_FOR_CLASS =
            "DELETE FROM Class_Tips WHERE class_id = ?";

    // ✅ NEW: overlapping classes check (warn if overlap)
    public static final String SQL_SEL_OVERLAPPING_CLASSES_FOR_TRAINEE =
            "SELECT COUNT(*) " +
            "FROM (Class_Registration CR " +
            "INNER JOIN Classes C ON CR.Class_ID = C.Class_ID) " +
            "WHERE CR.Trainee_ID = ? " +
            "AND CR.Attendance_Status <> 'CANCELLED' " +
            "AND C.Class_Date = ? " +
            "AND (C.Start_Time < ? AND C.End_Time > ?)";
}
