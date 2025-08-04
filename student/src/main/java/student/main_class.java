package student;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import common.Conn;
import common.navigation.ILoginLauncher;
import common.navigation.NavigationHelper;

public class main_class extends JFrame {
    private String registration;
    private JPanel contentPanel;

    private final ILoginLauncher loginLauncher;
    // ✅ Called explicitly from login module AFTER login is successful
  /*  public static void launchDashboard(String registration) {
        SwingUtilities.invokeLater(() -> new main_class(registration));
    }*/


    public main_class(String registration, ILoginLauncher loginLauncher) {
        this.registration = registration;

        this.loginLauncher = loginLauncher;
        setTitle("Student Dashboard");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        contentPanel = new JPanel(new CardLayout());
        contentPanel.add(buildHomePanel(), "Home");
        contentPanel.add(new StudentAssignedCoursesPanel (registration), "My Courses");
        contentPanel.add(new StudentAttendancePanel (registration), "Attendance");
       // contentPanel.add(new MarksheetPanel(registration), "Marksheet");
        contentPanel.add(new StudentResultPanel(registration), "Result");
        contentPanel.add(new StudentProfilePanel(registration), "Profile");
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            dispose();
            loginLauncher.launchLogin();  // clean logout
        });
        add(contentPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel createSidebar() {
        String[] options = {"Home", "My Courses", "Attendance",  "Result", "Profile", "Logout"};
        JPanel panel = new JPanel();
        panel.setBackground(new Color(30, 30, 60));
        panel.setLayout(new GridLayout(options.length, 1, 0, 10));
        panel.setPreferredSize(new Dimension(200, getHeight()));

        for (String option : options) {
            JButton btn = new JButton(option);
            btn.setFocusPainted(false);
            btn.setForeground(Color.WHITE);
            btn.setBackground(new Color(50, 50, 90));
            btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btn.addActionListener(e -> {
                if (option.equals("Logout")) {
                    try {
                        loginLauncher.launchLogin();  // ✅ Use interface
                        dispose();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    dispose();
                } else {
                    CardLayout cl = (CardLayout) contentPanel.getLayout();
                    cl.show(contentPanel, option);
                }
            });

            panel.add(btn);
        }

        return panel;
    }

    private JPanel buildHomePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 2, 20, 20));
        panel.setBackground(new Color(240, 240, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        String dept = "N/A", semester = "N/A", courses = "N/A";
        try {
            Conn c = new Conn();

            // fetch department and semester
            ResultSet rs = c.s.executeQuery("SELECT * FROM students WHERE registration='" + registration + "'");
            if (rs.next()) {
                dept = rs.getString("Departmentcode");
                semester = rs.getString("semoryear");
            }

            // fetch assigned course codes from student_course_map
            ResultSet rc = c.s.executeQuery("SELECT coursecode FROM student_course_map WHERE registration = '" + registration + "'");
            StringBuilder sb = new StringBuilder();
            while (rc.next()) {
                sb.append(rc.getString("coursecode")).append(", ");
            }
            if (sb.length() > 0) sb.setLength(sb.length() - 2); // remove last comma
            courses = sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }


        panel.add(createCard("Department", dept));
        panel.add(createCard("Semester", semester));
        panel.add(createCard("Courses Enrolled", courses));
        panel.add(createCard("Welcome", registration));

        return panel;
    }

    private JPanel createCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(70, 130, 180));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        valueLabel.setForeground(Color.WHITE);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // Panels
    static class MyCoursesPanel extends JPanel {
        public MyCoursesPanel(String reg) {
            setLayout(new BorderLayout());
            JLabel label = new JLabel("My Courses Panel for: " + reg, SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 22));
            add(label, BorderLayout.CENTER);
        }
    }

    static class AttendancePanel extends JPanel {
        public AttendancePanel(String reg) {
            setLayout(new BorderLayout());
            JLabel label = new JLabel("Attendance Panel for: " + reg, SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 22));
            add(label, BorderLayout.CENTER);
        }
    }

    static class MarksheetPanel extends JPanel {
        public MarksheetPanel(String reg) {
            setLayout(new BorderLayout());
            JLabel label = new JLabel("Marksheet Panel for: " + reg, SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 22));
            add(label, BorderLayout.CENTER);
        }
    }

    static class ResultPanel extends JPanel {
        public ResultPanel(String reg) {
            setLayout(new BorderLayout());
            JLabel label = new JLabel("Result Panel for: " + reg, SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 22));
            add(label, BorderLayout.CENTER);
        }
    }

    static class ProfilePanel extends JPanel {
        public ProfilePanel(String reg) {
            setLayout(new BorderLayout());
            JLabel label = new JLabel("Profile Panel for: " + reg, SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 22));
            add(label, BorderLayout.CENTER);
        }
    }
}
