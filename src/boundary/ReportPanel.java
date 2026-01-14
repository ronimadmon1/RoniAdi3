// src/boundary/ReportPanel.java
package boundary;

import control.ReportController;

import javax.swing.*;
import java.awt.*;
import java.time.Year;

public class ReportPanel extends JPanel {

    private final JSpinner spYear;

    public ReportPanel() {
        spYear = new JSpinner(new SpinnerNumberModel(Year.now().getValue(), 2000, 2100, 1));

        JButton btnGenerate = new JButton("Generate Unregistered Trainees Report");
        btnGenerate.addActionListener(e -> generate());

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0;
        p.add(new JLabel("Year:"), c);

        c.gridx = 1;
        p.add(spYear, c);

        c.gridx = 0; c.gridy = 1; c.gridwidth = 2; c.anchor = GridBagConstraints.CENTER;
        p.add(btnGenerate, c);

        setLayout(new BorderLayout());
        add(p, BorderLayout.NORTH);
    }

    private void generate() {
        int year = (int) spYear.getValue();
        ReportController.showUnregisteredClassesReport(year);
    }
}
