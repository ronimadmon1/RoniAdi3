package boundary;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }

    public MainFrame() {
        setTitle("FitWell");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        content.add(new RegistrationPanel(), "REGISTRATION");
        content.add(new ClassesPanel(), "CLASSES");
        content.add(new ReportPanel(), "REPORT");

        setJMenuBar(buildMenuBar());
        add(content, BorderLayout.CENTER);

        cards.show(content, "REGISTRATION");
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu menu = new JMenu("Menu");
        JMenuItem miRegistration = new JMenuItem("Registration");
        JMenuItem miClasses = new JMenuItem("Classes");
        JMenuItem miReport = new JMenuItem("Report");
        JMenuItem miExit = new JMenuItem("Exit");

        miRegistration.addActionListener(e -> cards.show(content, "REGISTRATION"));
        miClasses.addActionListener(e -> cards.show(content, "CLASSES"));
        miReport.addActionListener(e -> cards.show(content, "REPORT"));
        miExit.addActionListener(e -> dispose());

        menu.add(miRegistration);
        menu.add(miClasses);
        menu.add(miReport);
        menu.addSeparator();
        menu.add(miExit);

        bar.add(menu);
        return bar;
    }
}
