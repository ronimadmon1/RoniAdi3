package boundary;

import control.ClassController;
import entity.ClassType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class ClassTypesDialog extends JDialog {

    private final DefaultTableModel model;
    private final JTable tbl;

    private final JTextField txtId = new JTextField(6);   // read-only
    private final JTextField txtName = new JTextField(18);

    public ClassTypesDialog(Window owner) {
        super(owner, "Manage Class Types", ModalityType.APPLICATION_MODAL);

        model = new DefaultTableModel(new Object[]{"ID", "Name"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl = new JTable(model);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        txtId.setEditable(false);

        JButton btnLoad = new JButton("Load");
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");
        JButton btnClose = new JButton("Close");

        btnLoad.addActionListener(e -> refresh());
        btnAdd.addActionListener(e -> onAdd());
        btnUpdate.addActionListener(e -> onUpdate());
        btnDelete.addActionListener(e -> onDelete());
        btnClear.addActionListener(e -> clearInputs());
        btnClose.addActionListener(e -> dispose());

        tbl.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelected();
        });

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.anchor = GridBagConstraints.WEST;

        c.gridx=0; c.gridy=0; form.add(new JLabel("Type ID:"), c);
        c.gridx=1; form.add(txtId, c);

        c.gridx=0; c.gridy=1; form.add(new JLabel("Name:"), c);
        c.gridx=1; form.add(txtName, c);

        JPanel buttons = new JPanel(new GridLayout(0,1,6,6));
        buttons.add(btnLoad);
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnClear);
        buttons.add(btnClose);

        setLayout(new BorderLayout(10,10));
        add(new JScrollPane(tbl), BorderLayout.CENTER);
        add(form, BorderLayout.NORTH);
        add(buttons, BorderLayout.EAST);

        setSize(650, 350);
        setLocationRelativeTo(owner);

        refresh();
    }

    private void refresh() {
        model.setRowCount(0);
        ArrayList<ClassType> types = ClassController.getInstance().getClassTypes();
        for (ClassType t : types) {
            model.addRow(new Object[]{t.getClassTypeId(), t.getClassTypeName()});
        }
    }

    private void loadSelected() {
        int row = tbl.getSelectedRow();
        if (row < 0) return;
        txtId.setText(model.getValueAt(row, 0).toString());
        txtName.setText(model.getValueAt(row, 1).toString());
    }

    private void clearInputs() {
        txtId.setText("");
        txtName.setText("");
        tbl.clearSelection();
    }

    private void onAdd() {
        try {
            String name = txtName.getText().trim();
            boolean ok = ClassController.getInstance().addClassType(name);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Type added.");
                refresh();
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
            if (txtId.getText().trim().isEmpty())
                throw new IllegalStateException("Select a type first.");

            int id = Integer.parseInt(txtId.getText().trim());
            String newName = txtName.getText().trim();

            boolean ok = ClassController.getInstance().updateClassType(id, newName);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Type updated.");
                refresh();
            } else {
                JOptionPane.showMessageDialog(this, "Update failed.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        try {
            if (txtId.getText().trim().isEmpty())
                throw new IllegalStateException("Select a type first.");

            int id = Integer.parseInt(txtId.getText().trim());

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Delete type " + id + "?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            boolean ok = ClassController.getInstance().deleteClassType(id);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Type deleted.");
                refresh();
                clearInputs();
            } else {
                JOptionPane.showMessageDialog(this, "Delete failed.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
