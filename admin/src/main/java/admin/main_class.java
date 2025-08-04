package admin;

import common.Conn;
import common.navigation.ILoginLauncher;
import common.navigation.LoginLauncherImpl;

import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.Statement;

public class main_class extends JFrame {

    JPanel sidebar, topbar, contentPanel;
    CardLayout cardLayout;
    JLabel adminNameLabel, logoLabel;
    private AddStudentPanel addStudentPanel;

    private RollGeneratorPanel rollPanel;

    private ILoginLauncher loginLauncher;

    public main_class(ILoginLauncher loginLauncher) {
        this.loginLauncher = loginLauncher;
initUi();
        setTitle("Admin Dashboard - University Management System");
        setSize(1600, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        sidebar = new JPanel(); // ✅ initialize it here
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(44, 62, 80));
        sidebar.setPreferredSize(new Dimension(250, getHeight()));

        initTopbar();
        initContentPanel();
        add(sidebar, BorderLayout.WEST);


        add(topbar, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        initSidebar();

        loadAdminInfo();
    }

    private void initUi() {
    }

    private void initSidebar() {

        JButton admissionBtn = new JButton("📝 Admission Requests");
        admissionBtn.setFocusPainted(false);
        admissionBtn.setBackground(new Color(52, 73, 94));
        admissionBtn.setForeground(Color.WHITE);
        admissionBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        admissionBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        admissionBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        admissionBtn.addActionListener(e -> new AdmissionApprovalPanel()); // ✅ launch panel

        sidebar.add(admissionBtn); // ✅ Keep existing sidebar

// OPTIONAL: adjust layout or style without reinitializing
        sidebar.setLayout(new GridLayout(0, 1, 0, 2));
        sidebar.setPreferredSize(new Dimension(200, getHeight()));
        sidebar.setBackground(new Color(33, 33, 33));

        String[] menuItems = {
                "Home", "Departments", "Courses", "Students",
                "Teachers","Assign Course", "Attendance", "Marksheet", "Result",  "Logout"
        };

        for (String item : menuItems) {
            JButton btn = new JButton(item);
            btn.setFocusPainted(false);
            btn.setBackground(new Color(48, 48, 48));
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Arial", Font.PLAIN, 14));

            btn.addActionListener(e -> {
                if (item.equals("Logout")) {
                    dispose();
                    loginLauncher.launchLogin();
                }else if (item.equals("Roll Generator")) {
                    rollPanel = new RollGeneratorPanel(addStudentPanel); // ✅ pass listener
                    JDialog dialog = new JDialog(this, "Roll Number Generator", true);
                    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    dialog.getContentPane().add(rollPanel);
                    dialog.pack();
                    dialog.setLocationRelativeTo(this);
                    dialog.setVisible(true);
                }


                else {
                    CardLayout cl = (CardLayout) contentPanel.getLayout();
                    cl.show(contentPanel, item);
                }
            });


            sidebar.add(btn);
        }
    }

    private void showRollGeneratorDialog() {
        JDialog dialog = new JDialog(this, "Roll Generator", true);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(this);

      //  RollGeneratorPanel rollPanel = new RollGeneratorPanel(this);
        //dialog.add(rollPanel);

        dialog.setVisible(true);
    }

    private void initTopbar() {
        topbar = new JPanel(new BorderLayout());
        topbar.setPreferredSize(new Dimension(getWidth(), 60));
        topbar.setBackground(new Color(102, 0, 153));

        logoLabel = new JLabel("   🏫 UMS Admin");
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));

        adminNameLabel = new JLabel("Welcome, Admin", SwingConstants.RIGHT);
        adminNameLabel.setForeground(Color.WHITE);
        adminNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        adminNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

        topbar.add(logoLabel, BorderLayout.WEST);
        topbar.add(adminNameLabel, BorderLayout.EAST);
    }

    private void initContentPanel() {
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
addStudentPanel = new AddStudentPanel();
        // Dashboard panel
        JPanel dashboardPanel = createDashboardPanel();
        contentPanel.add(dashboardPanel, "Home");

        // Other placeholders (you can replace later)
        contentPanel.add(new AddDepartmentPanel(), "Departments");
        contentPanel.add(new AddCoursePanel(), "Courses");
        contentPanel.add(addStudentPanel, "Students");
        contentPanel.add(new AddTeacherPanel(), "Teachers");
        contentPanel.add(new AssignCoursePanel(),"Assign Course");
        contentPanel.add(new AttendanceViewerPanel(), "Attendance");
        contentPanel.add(new AdminMarksPanel(), "Marksheet");
        contentPanel.add(new AdminResultPanel(), "Result");
      //  contentPanel.add(new JLabel("Certificate Panel"), "Certificate");

        // ✅ Add RollGeneratorPanel
        //rollGeneratorPanel = new RollGeneratorPanel(); // constructor without param
        //contentPanel.add(rollGeneratorPanel, "Roll Generator");
    }
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 2, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        panel.setBackground(Color.WHITE);

        int totalStudents = getTotalStudents();
        int totalTeachers = getTotalTeachers();
        int totalCourses = getTotalCourses();
        int totalDepartments = getTotalDepartments();

        panel.add(createCard("Students: " + totalStudents, "📚", new Color(0, 123, 255)));
        panel.add(createCard("Teachers: " + totalTeachers, "👨‍🏫", new Color(40, 167, 69)));
        panel.add(createCard("Courses: " + totalCourses, "📖", new Color(255, 193, 7)));
        panel.add(createCard("Departments: " + totalDepartments, "🏢", new Color(220, 53, 69)));

        return panel;
    }


    private JPanel createCard(String title, String emoji, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel icon = new JLabel(emoji, SwingConstants.LEFT);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        JLabel label = new JLabel(title, SwingConstants.LEFT);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));

        card.add(icon, BorderLayout.NORTH);
        card.add(label, BorderLayout.SOUTH);
        return card;
    }

    private int getTotalStudents() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.c.createStatement().executeQuery("SELECT COUNT(*) FROM students");
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getTotalTeachers() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.c.createStatement().executeQuery("SELECT COUNT(*) FROM teachers");
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getTotalCourses() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.c.createStatement().executeQuery("SELECT COUNT(*) FROM courses");
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getTotalDepartments() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.c.createStatement().executeQuery("SELECT COUNT(*) FROM departments");
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void loadAdminInfo() {
        try {
            Conn c = new Conn();

            if (c.c == null) {
                System.out.println("Database connection failed.");
                return;
            }

            String query = "SELECT * FROM admin WHERE userid = '2025'";
            Statement stmt = c.c.createStatement();

            if (stmt == null) {
                System.out.println("Statement creation failed.");
                return;
            }

            ResultSet rs = stmt.executeQuery(query);

            if (rs == null) {
                System.out.println("ResultSet is null. Query failed.");
                return;
            }

            if (rs.next()) {
                String collegeName = rs.getString("collagename");
                String email = rs.getString("emailid");
                System.out.println("College: " + collegeName + ", Email: " + email);
            } else {
                System.out.println("No admin found with userid = '2025'");
            }

            rs.close();
            stmt.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ILoginLauncher launcher = new LoginLauncherImpl();
            main_class m = new main_class(launcher);
            m.setVisible(true);
        });
    }

    public void setGeneratedRoll(long lastRoll) {
    }
}
