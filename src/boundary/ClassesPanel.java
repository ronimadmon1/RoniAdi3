package boundary;

import control.ClassController;
import entity.ClassType;
import entity.Classes;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class ClassesPanel extends JPanel {

    private final JTable tblClasses;
    private final DefaultTableModel model;

    private final JTextField txtId = new JTextField(8);
    private final JTextField txtName = new JTextField(20);
    private final JComboBox<ClassType> cmbType = new JComboBox<>();
    private final JTextField txtDate = new JTextField(10);
    private final JTextField txtStart = new JTextField(8);
    private final JTextField txtEnd = new JTextField(8);
    private final JTextField txtMax = new JTextField(5);
    private final JTextField txtConsultantId = new JTextField(12);

    private final JCheckBox chkAddTips = new JCheckBox("Add tips (optional)");
    private final DefaultListModel<String> tipsModel = new DefaultListModel<>();
    private final JList<String> lstTips = new JList<>(tipsModel);

    private final JButton btnAddTip = new JButton("Add Tip");
    private final JButton btnRemoveTip = new JButton("Remove Selected");
    private final JButton btnClearTips = new JButton("Clear Tips");

    private final JButton btnManageTypes = new JButton("Manage Types...");

    public ClassesPanel() {

        model = new DefaultTableModel(
                new Object[]{"ID", "Name", "TypeId", "Date", "Start", "End", "Max", "ConsultantId", "Tips"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblClasses = new JTable(model);
        tblClasses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tblClasses.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedRowToInputs();
            }
        });

        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");

        btnAdd.addActionListener(e -> onAdd());
        btnUpdate.addActionListener(e -> onUpdate());
        btnDelete.addActionListener(e -> onDelete());

        btnManageTypes.addActionListener(e -> {
            new ClassTypesDialog(SwingUtilities.getWindowAncestor(this)).setVisible(true);
            loadClassTypes();
        });

        setLayout(new BorderLayout(10, 10));
        add(new JScrollPane(tblClasses), BorderLayout.CENTER);
        add(buildRightPanel(btnAdd, btnUpdate, btnDelete), BorderLayout.EAST);

        txtId.setEditable(false);
        loadClassTypes();
        refreshTable();

        setTipsEnabled(false);
        lstTips.setVisibleRowCount(5);

        chkAddTips.addActionListener(e -> {
            boolean on = chkAddTips.isSelected();
            setTipsEnabled(on);
            if (!on) tipsModel.clear();
        });

        btnAddTip.addActionListener(e -> onAddTip());
        btnRemoveTip.addActionListener(e -> onRemoveTip());
        btnClearTips.addActionListener(e -> tipsModel.clear());
    }

    private JPanel buildRightPanel(JButton btnAdd, JButton btnUpdate, JButton btnDelete) {
        JPanel p = new JPanel(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        int row = 0;

        addRow(form, c, row++, "Class ID:", txtId);
        addRow(form, c, row++, "Name:", txtName);

        JPanel typeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        typeRow.add(cmbType);
        typeRow.add(btnManageTypes);
        addRow(form, c, row++, "Type:", typeRow);

        addRow(form, c, row++, "Date (YYYY-MM-DD):", txtDate);
        addRow(form, c, row++, "Start (HH:MM):", txtStart);
        addRow(form, c, row++, "End (HH:MM):", txtEnd);
        addRow(form, c, row++, "Max Participants:", txtMax);
        addRow(form, c, row++, "Consultant ID:", txtConsultantId);

        c.gridx = 0;
        c.gridy = row;
        form.add(new JLabel("Tips:"), c);
        c.gridx = 1;
        form.add(chkAddTips, c);
        row++;

        c.gridx = 0;
        c.gridy = row;
        form.add(new JLabel("Tips (max 5):"), c);

        c.gridx = 1;
        JPanel tipsPanel = new JPanel(new BorderLayout(6, 6));
        tipsPanel.add(new JScrollPane(lstTips), BorderLayout.CENTER);

        JPanel tipsBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        tipsBtns.add(btnAddTip);
        tipsBtns.add(btnRemoveTip);
        tipsBtns.add(btnClearTips);

        tipsPanel.add(tipsBtns, BorderLayout.SOUTH);
        form.add(tipsPanel, c);

        JPanel buttons = new JPanel(new GridLayout(0, 1, 6, 6));
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);

        p.add(form, BorderLayout.CENTER);
        p.add(buttons, BorderLayout.SOUTH);
        return p;
    }

    private void addRow(JPanel form, GridBagConstraints c, int row, String label, JComponent input) {
        c.gridx = 0;
        c.gridy = row;
        form.add(new JLabel(label), c);

        c.gridx = 1;
        form.add(input, c);
    }

    private void setTipsEnabled(boolean enabled) {
        lstTips.setEnabled(enabled);
        btnAddTip.setEnabled(enabled);
        btnRemoveTip.setEnabled(enabled);
        btnClearTips.setEnabled(enabled);
    }

    private void onAddTip() {
        if (tipsModel.size() >= 5) {
            JOptionPane.showMessageDialog(this, "You can add up to 5 tips only.");
            return;
        }

        String tip = JOptionPane.showInputDialog(
                this,
                "Enter a tip (text or a link):",
                "Add Tip",
                JOptionPane.PLAIN_MESSAGE
        );

        if (tip == null) return;
        tip = tip.trim();
        if (tip.isEmpty()) return;

        tipsModel.addElement(tip);
    }

    private void onRemoveTip() {
        int idx = lstTips.getSelectedIndex();
        if (idx < 0) return;
        tipsModel.remove(idx);
    }

    private void loadClassTypes() {
        cmbType.removeAllItems();
        for (ClassType t : ClassController.getInstance().getClassTypes()) {
            cmbType.addItem(t);
        }

        cmbType.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel();
            lbl.setOpaque(true);
            if (value != null) lbl.setText(value.getClassTypeName());
            lbl.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            lbl.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            return lbl;
        });
    }

    // ✅ table shows tips correctly
    private void refreshTable() {
        model.setRowCount(0);

        for (Classes cl : ClassController.getInstance().getClasses()) {
            Classes full = ClassController.getInstance().getClassById(cl.getClassId());
            String tips = (full == null) ? null : full.getClassTips();

            model.addRow(new Object[]{
                    cl.getClassId(),
                    cl.getName(),
                    cl.getClassTypeId(),
                    cl.getClassDate(),
                    cl.getStartTime(),
                    cl.getEndTime(),
                    cl.getMaxParticipants(),
                    cl.getConsultantId(),
                    tipsSummary(tips)
            });
        }
    }

    private String tipsSummary(String tips) {
        if (tips == null || tips.isBlank()) return "No tips";

        int count = 0;
        for (String line : tips.split("\\R")) {
            if (!line.trim().isEmpty()) count++;
        }
        return count + " tips";
    }

    private void onAdd() {
        AddClassDialog dlg = new AddClassDialog(SwingUtilities.getWindowAncestor(this));
        Classes cl = dlg.showDialog();
        if (cl == null) return;

        try {
            boolean ok = ClassController.getInstance().addClass(cl);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Class added successfully!\nNew Class ID: " + cl.getClassId());
                refreshTable();
                clearInputs();
            } else {
                JOptionPane.showMessageDialog(this, "Add failed.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUpdate() {
        try {
            Classes cl = readInputsToEntity(true);
            boolean ok = ClassController.getInstance().updateClass(cl);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Class updated successfully!");
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Update failed.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        try {
            int row = tblClasses.getSelectedRow();
            if (row < 0) throw new IllegalStateException("Select a class in the table first.");

            String classId = model.getValueAt(row, 0).toString();

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Delete class " + classId + "?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            boolean ok = ClassController.getInstance().deleteClass(classId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Class deleted.");
                refreshTable();
                clearInputs();
            } else {
                JOptionPane.showMessageDialog(this, "Delete failed.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearInputs() {
        txtId.setText("");
        txtName.setText("");
        txtDate.setText("");
        txtStart.setText("");
        txtEnd.setText("");
        txtMax.setText("");
        txtConsultantId.setText("");

        chkAddTips.setSelected(false);
        tipsModel.clear();
        setTipsEnabled(false);

        if (cmbType.getItemCount() > 0) cmbType.setSelectedIndex(0);
        tblClasses.clearSelection();
    }

    private void loadSelectedRowToInputs() {
        int row = tblClasses.getSelectedRow();
        if (row < 0) return;

        String classId = model.getValueAt(row, 0).toString();

        txtId.setText(classId);
        txtName.setText(model.getValueAt(row, 1).toString());

        int typeId = Integer.parseInt(model.getValueAt(row, 2).toString());
        selectClassTypeById(typeId);

        txtDate.setText(model.getValueAt(row, 3).toString());
        txtStart.setText(model.getValueAt(row, 4).toString());
        txtEnd.setText(model.getValueAt(row, 5).toString());
        txtMax.setText(model.getValueAt(row, 6).toString());
        txtConsultantId.setText(model.getValueAt(row, 7).toString());

        tipsModel.clear();
        Classes full = ClassController.getInstance().getClassById(classId);
        String tips = (full == null || full.getClassTips() == null) ? "" : full.getClassTips();

        if (tips.isBlank()) {
            chkAddTips.setSelected(false);
            setTipsEnabled(false);
        } else {
            chkAddTips.setSelected(true);
            setTipsEnabled(true);

            String[] lines = tips.split("\\R");
            for (String line : lines) {
                String t = line.trim();
                if (!t.isEmpty()) tipsModel.addElement(t);
            }
        }
    }

    private void selectClassTypeById(int typeId) {
        for (int i = 0; i < cmbType.getItemCount(); i++) {
            ClassType ct = cmbType.getItemAt(i);
            if (ct != null && ct.getClassTypeId() == typeId) {
                cmbType.setSelectedIndex(i);
                return;
            }
        }
    }

    private Classes readInputsToEntity(boolean includeId) {

        String name = txtName.getText().trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Class name is required");

        ClassType selected = (ClassType) cmbType.getSelectedItem();
        if (selected == null) throw new IllegalArgumentException("Class type is required");

        LocalDate date = parseDate(txtDate.getText().trim());
        LocalTime start = parseTime(txtStart.getText().trim(), "Start time");
        LocalTime end = parseTime(txtEnd.getText().trim(), "End time");

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        int max;
        try {
            max = Integer.parseInt(txtMax.getText().trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Max participants must be a number");
        }
        if (max <= 0) throw new IllegalArgumentException("Max participants must be > 0");

        String consultantId = txtConsultantId.getText().trim();
        if (consultantId.isEmpty()) throw new IllegalArgumentException("Consultant ID is required");

        String id = null;
        if (includeId) {
            id = txtId.getText().trim();
            if (id.isEmpty()) throw new IllegalArgumentException("Class ID is required");
        }

        String tips = null;
        if (chkAddTips.isSelected() && tipsModel.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tipsModel.size(); i++) {
                if (i > 0) sb.append("\n");
                sb.append(tipsModel.get(i));
            }
            tips = sb.toString();
        }

        // ✅ WARNING ONLY (still allow update)
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);

        boolean pastDate = date.isBefore(today);
        boolean startedAlreadyToday = date.isEqual(today) && !start.isAfter(now); // start <= now

        if (pastDate || startedAlreadyToday) {
            JOptionPane.showMessageDialog(
                    this,
                    "Warning: The details shown may be invalid. Please check the date and time.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE
            );
        }

        return new Classes(
                id,
                name,
                selected.getClassTypeId(),
                date,
                start,
                end,
                max,
                consultantId,
                tips
        );
    }

    private LocalDate parseDate(String s) {
        try {
            return LocalDate.parse(s);
        } catch (Exception ignored) {}

        try {
            String[] p = s.split("[-/]");
            if (p.length != 3) throw new RuntimeException();
            int day = Integer.parseInt(p[0]);
            int month = Integer.parseInt(p[1]);
            int year = Integer.parseInt(p[2]);
            return LocalDate.of(year, month, day);
        } catch (Exception ignored) {}

        throw new IllegalArgumentException("Date must be YYYY-MM-DD or DD-MM-YYYY");
    }

    private LocalTime parseTime(String s, String fieldName) {
        try {
            return LocalTime.parse(s);
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldName + " must be in format HH:MM");
        }
    }
}
