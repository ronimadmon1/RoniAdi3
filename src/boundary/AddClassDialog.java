package boundary;

import control.ClassController;
import entity.ClassType;
import entity.Classes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.*;
import java.util.Date;

public class AddClassDialog extends JDialog {

    private final JTextField txtName = new JTextField(22);
    private final JComboBox<ClassType> cmbType = new JComboBox<>();
    private final JTextField txtConsultantId = new JTextField(12);

    private final JSpinner spDate;
    private final JSpinner spStart;
    private final JSpinner spEnd;
    private final JSpinner spMax;

    private final JCheckBox chkTips = new JCheckBox("Add tips (optional)");
    private final DefaultListModel<String> tipsModel = new DefaultListModel<>();
    private final JList<String> lstTips = new JList<>(tipsModel);
    private final JButton btnAddTip = new JButton("Add");
    private final JButton btnRemoveTip = new JButton("Remove");
    private final JButton btnClearTips = new JButton("Clear");

    private Classes result = null;

    public AddClassDialog(Window owner) {
        super(owner, "Add Class", ModalityType.APPLICATION_MODAL);

        // Date spinner
        spDate = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
        spDate.setEditor(new JSpinner.DateEditor(spDate, "yyyy-MM-dd"));

        // Time spinners
        spStart = new JSpinner(new SpinnerDateModel());
        spStart.setEditor(new JSpinner.DateEditor(spStart, "HH:mm"));

        spEnd = new JSpinner(new SpinnerDateModel());
        spEnd.setEditor(new JSpinner.DateEditor(spEnd, "HH:mm"));

        spMax = new JSpinner(new SpinnerNumberModel(10, 1, 500, 1));

        loadTypes();
        buildUi();
        pack();
        setLocationRelativeTo(owner);
    }

    public Classes showDialog() {
        setVisible(true);
        return result;
    }

    private void loadTypes() {
        cmbType.removeAllItems();
        for (ClassType t : ClassController.getInstance().getClassTypes()) {
            cmbType.addItem(t);
        }
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new TitledBorder("Class details"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        int r = 0;

        addRow(form, c, r++, "Name:", txtName);
        addRow(form, c, r++, "Type:", cmbType);

        JPanel dateLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        dateLine.add(spDate);
        addRow(form, c, r++, "Date:", dateLine);

        JPanel timeLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        timeLine.add(new JLabel("Start"));
        timeLine.add(spStart);
        timeLine.add(Box.createHorizontalStrut(10));
        timeLine.add(new JLabel("End"));
        timeLine.add(spEnd);
        addRow(form, c, r++, "Time:", timeLine);

        addRow(form, c, r++, "Max participants:", spMax);
        addRow(form, c, r++, "Consultant ID:", txtConsultantId);

        // Tips panel
        JPanel tips = new JPanel(new BorderLayout(8, 8));
        tips.setBorder(new TitledBorder("Tips (max 5)"));
        lstTips.setVisibleRowCount(6);
        tips.add(new JScrollPane(lstTips), BorderLayout.CENTER);

        JPanel tipsTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tipsTop.add(chkTips);
        tips.add(tipsTop, BorderLayout.NORTH);

        JPanel tipsBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tipsBtns.add(btnAddTip);
        tipsBtns.add(btnRemoveTip);
        tipsBtns.add(btnClearTips);
        tips.add(tipsBtns, BorderLayout.SOUTH);

        setTipsEnabled(false);
        chkTips.addActionListener(e -> {
            boolean on = chkTips.isSelected();
            setTipsEnabled(on);
            if (!on) tipsModel.clear();
        });

        btnAddTip.addActionListener(e -> addTip());
        btnRemoveTip.addActionListener(e -> {
            int i = lstTips.getSelectedIndex();
            if (i >= 0) tipsModel.remove(i);
        });
        btnClearTips.addActionListener(e -> tipsModel.clear());

        // Buttons
        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");

        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> {
            result = null;
            dispose();
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.add(btnCancel);
        bottom.add(btnSave);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.add(form, BorderLayout.NORTH);
        center.add(tips, BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void addRow(JPanel p, GridBagConstraints c, int row, String label, JComponent comp) {
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        p.add(new JLabel(label), c);

        c.gridx = 1; c.weightx = 1;
        p.add(comp, c);
    }

    private void setTipsEnabled(boolean enabled) {
        lstTips.setEnabled(enabled);
        btnAddTip.setEnabled(enabled);
        btnRemoveTip.setEnabled(enabled);
        btnClearTips.setEnabled(enabled);
    }

    private void addTip() {
        if (tipsModel.size() >= 5) {
            JOptionPane.showMessageDialog(this, "Up to 5 tips only.");
            return;
        }
        String tip = JOptionPane.showInputDialog(this, "Enter tip (text/link):");
        if (tip == null) return;
        tip = tip.trim();
        if (!tip.isEmpty()) tipsModel.addElement(tip);
    }

    private void onSave() {
        try {
            String name = txtName.getText().trim();
            if (name.isEmpty()) throw new IllegalArgumentException("Name is required");

            ClassType ct = (ClassType) cmbType.getSelectedItem();
            if (ct == null) throw new IllegalArgumentException("Type is required");

            String consultantId = txtConsultantId.getText().trim();
            if (consultantId.isEmpty()) throw new IllegalArgumentException("Consultant ID is required");

            LocalDate date = Instant.ofEpochMilli(((Date) spDate.getValue()).getTime())
                    .atZone(ZoneId.systemDefault()).toLocalDate();

            LocalTime start = Instant.ofEpochMilli(((Date) spStart.getValue()).getTime())
                    .atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);

            LocalTime end = Instant.ofEpochMilli(((Date) spEnd.getValue()).getTime())
                    .atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);

            if (!end.isAfter(start)) throw new IllegalArgumentException("End must be after start");

            int max = (Integer) spMax.getValue();

            String tips = null;
            if (chkTips.isSelected() && tipsModel.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < tipsModel.size(); i++) {
                    if (i > 0) sb.append("\n");
                    sb.append(tipsModel.get(i));
                }
                tips = sb.toString();
            }

            // ✅ WARNING ONLY (still allow saving)
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

            result = new Classes(
                    null,
                    name,
                    ct.getClassTypeId(),
                    date,
                    start,
                    end,
                    max,
                    consultantId,
                    tips
            );

            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
