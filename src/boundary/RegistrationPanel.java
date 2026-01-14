package boundary;

import control.ClassRegistrationController;
import control.ClassRegistrationController.ClassRow;
import entity.Classes;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.List;

public class RegistrationPanel extends JPanel {

    private final DefaultTableModel model;
    private final JTable tbl;
    private final JTextField txtTraineeId = new JTextField(12);

    private final JButton btnBrowseEligible = new JButton("Show Eligible Classes");
    private final JButton btnRegister = new JButton("Register");
    private final JButton btnCancel = new JButton("Cancel Registration");

    // model indexes (model, not view)
    private static final int COL_CLASS_ID = 0;
    private static final int COL_STATUS = 8;
    private static final int COL_CAN_REGISTER = 9;
    private static final int COL_CAN_CANCEL = 10;

    public RegistrationPanel() {

        model = new DefaultTableModel(
                new Object[]{
                        "Class ID", "Name", "Date", "Start", "End",
                        "Audience", "Capacity", "Registered", "Status",
                        "canRegister", "canCancel"
                },
                0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tbl = new JTable(model);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // hide helper columns
        hideColumn(COL_CAN_REGISTER);
        hideColumn(COL_CAN_CANCEL);

        // actions
        btnBrowseEligible.addActionListener(e -> loadEligibleAssigned());
        btnRegister.addActionListener(e -> onRegister());
        btnCancel.addActionListener(e -> onCancel());

        // Enter loads eligible classes
        txtTraineeId.addActionListener(e -> loadEligibleAssigned());

        // enable/disable buttons by selection
        tbl.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateButtons();
        });

        // top
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Trainee ID:"));
        top.add(txtTraineeId);
        top.add(btnBrowseEligible);

        // bottom
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnRegister);
        bottom.add(btnCancel);

        setLayout(new BorderLayout(8, 8));
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        updateButtons();
    }

    // ---------- UI helpers ----------

    private void hideColumn(int modelIndex) {
        int viewIndex = tbl.convertColumnIndexToView(modelIndex);
        if (viewIndex < 0) return;
        TableColumn col = tbl.getColumnModel().getColumn(viewIndex);
        tbl.removeColumn(col);
    }

    private int selectedRowModel() {
        int viewRow = tbl.getSelectedRow();
        if (viewRow < 0) return -1;
        return tbl.convertRowIndexToModel(viewRow);
    }

    private String selectedClassId(int modelRow) {
        return model.getValueAt(modelRow, COL_CLASS_ID).toString();
    }

    private String rowStatus(int modelRow) {
        return model.getValueAt(modelRow, COL_STATUS).toString();
    }

    private boolean rowCanRegister(int modelRow) {
        return Boolean.parseBoolean(model.getValueAt(modelRow, COL_CAN_REGISTER).toString());
    }

    private boolean rowCanCancel(int modelRow) {
        return Boolean.parseBoolean(model.getValueAt(modelRow, COL_CAN_CANCEL).toString());
    }

    private void updateButtons() {
        int r = selectedRowModel();
        if (r < 0) {
            btnRegister.setEnabled(false);
            btnCancel.setEnabled(false);
            return;
        }
        btnRegister.setEnabled(rowCanRegister(r));
        btnCancel.setEnabled(rowCanCancel(r));
    }

    // ---------- Load Eligible (only what fits trainee plan types) ----------

    private void loadEligibleAssigned() {
        model.setRowCount(0);

        String traineeId = txtTraineeId.getText().trim();
        if (traineeId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Trainee ID first.");
            return;
        }

        try {
            List<ClassRow> rows = ClassRegistrationController.getInstance()
                    .getAssignedEligibleClassesWithStatus(traineeId);

            for (ClassRow row : rows) {
                Classes c = row.getC();
                model.addRow(new Object[]{
                        c.getClassId(),
                        c.getName(),
                        c.getClassDate(),
                        c.getStartTime(),
                        c.getEndTime(),
                        row.getAudience(),      // Personal / Group / Both
                        row.getCapacity(),
                        row.getRegistered(),
                        row.getStatus(),
                        row.canRegister(),
                        row.canCancel()
                });
            }

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No eligible classes found for this trainee.", "Info",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        tbl.clearSelection();
        updateButtons();
    }

    // ---------- Actions ----------

    private void onRegister() {
        String traineeId = txtTraineeId.getText().trim();
        if (traineeId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Trainee ID.");
            return;
        }

        int r = selectedRowModel();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Please select a class in the table.");
            return;
        }

        if (!rowCanRegister(r)) {
            JOptionPane.showMessageDialog(this, "Cannot register: " + rowStatus(r),
                    "Registration rule", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String classId = selectedClassId(r);

        try {
            boolean ok = ClassRegistrationController.getInstance().registerToClass(traineeId, classId);
            JOptionPane.showMessageDialog(this, ok ? "Registered successfully." : "Registration failed.");
            loadEligibleAssigned();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCancel() {
        String traineeId = txtTraineeId.getText().trim();
        if (traineeId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Trainee ID.");
            return;
        }

        int r = selectedRowModel();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Please select a class in the table.");
            return;
        }

        if (!rowCanCancel(r)) {
            JOptionPane.showMessageDialog(this, "Cannot cancel: " + rowStatus(r),
                    "Cancel rule", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String classId = selectedClassId(r);

        try {
            boolean ok = ClassRegistrationController.getInstance().cancelRegistration(traineeId, classId);
            JOptionPane.showMessageDialog(this, ok ? "Cancelled successfully." : "Cancel failed.");
            loadEligibleAssigned();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cancel error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
