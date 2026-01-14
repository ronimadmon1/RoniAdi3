// src/control/ReportController.java
package control;

import entity.Consts;

import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

import javax.swing.*;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ReportController {

    // ✅ הקובץ נמצא תחת src/boundary לכן הנתיב הוא /boundary/...
    private static final String REPORT_RESOURCE_PATH = "/boundary/RptUnregisteredClasses.jasper";

    public static void showUnregisteredClassesReport(int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        showUnregisteredClassesReport(Date.valueOf(start), Date.valueOf(end));
    }

    public static void showUnregisteredClassesReport(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("StartDate/EndDate cannot be null");
        }

        if (startDate.after(endDate)) {
            JOptionPane.showMessageDialog(
                    null,
                    "Start date must be before or equal to end date.",
                    "Invalid Dates",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            Consts.assertDbExists();
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");

            try (Connection conn = DriverManager.getConnection(Consts.CONN_STR);
                 InputStream reportStream = ReportController.class.getResourceAsStream(REPORT_RESOURCE_PATH)) {

                // בדיקה שהקובץ באמת נמצא על ה-classpath
                if (reportStream == null) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Report file not found:\n" + REPORT_RESOURCE_PATH +
                                    "\n\nבדקי שהקובץ נמצא ב-src/boundary ושיש לך RptUnregisteredClasses.jasper",
                            "Report Not Found",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                Map<String, Object> params = new HashMap<>();
                params.put("StartDate", startDate);
                params.put("EndDate", endDate);

                JasperPrint print = JasperFillManager.fillReport(reportStream, params, conn);

                JasperViewer viewer = new JasperViewer(print, false);
                viewer.setTitle("Unregistered Classes Report");
                viewer.setVisible(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "Failed to generate report:\n" + e.getMessage(),
                    "Report Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
