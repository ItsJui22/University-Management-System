package faculty;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;

import common.Conn;
import common.navigation.ILoginLauncher;

public class main_class extends JFrame {
    private String teacherId;
    private ILoginLauncher loginLauncher;
    private JPanel contentPanel;

    public main_class(String teacherId, ILoginLauncher loginLauncher) {
        this.teacherId = teacherId;
        this.loginLauncher = loginLauncher;

        setTitle("Teacher Dashboard");
        setSize(1600, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        GradientSidebar sidebar = new GradientSidebar(new String[]{
                "🏠 Home", "📚 My Courses", "📝 Attendance", "📊 Marksheet", "🙍 Profile", "🚪 Logout"
        });
        add(sidebar, BorderLayout.WEST);

        contentPanel = new JPanel(new CardLayout());
        contentPanel.add(buildHomePanel(), "Home");
        contentPanel.add(new MyCoursesPanel(teacherId), "My Courses");
        contentPanel.add(new FacultyAttendancePanel(teacherId), "Attendance");
        contentPanel.add(new FacultyMarksPanel(teacherId), "Marksheet");
        //contentPanel.add(new ResultPanel(teacherId), "Result");
        contentPanel.add(new ProfilePanel(teacherId), "Profile");
        add(contentPanel, BorderLayout.CENTER);

        sidebar.setNavigationListener(option -> {
            if (option.equals("Logout") && loginLauncher != null) {
                loginLauncher.launchLogin();
                dispose();
            } else {
                CardLayout cl = (CardLayout) contentPanel.getLayout();
                cl.show(contentPanel, option);
            }
        });

        setVisible(true);
    }

    private JPanel buildHomePanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 20));
        panel.setBackground(new Color(245, 245, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        String department = "N/A";
        java.util.Set<String> courseSet = new java.util.LinkedHashSet<>();
        java.util.Set<String> semSet = new java.util.LinkedHashSet<>();

        try (Conn c = new Conn()) {
            ResultSet rs = c.s.executeQuery("SELECT DISTINCT department_code, coursecode, semoryear FROM teacher_course_map WHERE teacher_id = '" + teacherId + "'");
            while (rs.next()) {
                department = rs.getString("department_code");
                courseSet.add(rs.getString("coursecode"));
                semSet.add(rs.getString("semoryear"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String courseSummary = String.join(", ", courseSet);
        String semSummary = String.join(", ", semSet);

        int courseCount = courseSet.size();
        int semCount = semSet.size();

        panel.add(createCard("Department", department, new Color(102, 153, 255), null, null));
        panel.add(createCard("My Courses (" + courseCount + ")", shortText(courseSummary), new Color(153, 204, 255), courseSummary, "Courses"));
        panel.add(createCard("Semester/Year (" + semCount + ")", shortText(semSummary), new Color(204, 229, 255), semSummary, "Semesters"));
        panel.add(createCard("Welcome", teacherId, new Color(153, 204, 255), null, null));

        return panel;
    }

    private String shortText(String full) {
        return full.length() > 60 ? full.substring(0, 60) + "..." : full;
    }


    private JPanel createCard(String title, String value, Color bg, String tooltipText, String type) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bg);
        card.setBorder(new RoundedBorder(10));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JTextArea valueLabel = new JTextArea(value);
        valueLabel.setWrapStyleWord(true);
        valueLabel.setLineWrap(true);
        valueLabel.setOpaque(false);
        valueLabel.setEditable(false);
        valueLabel.setFocusable(false);
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        valueLabel.setToolTipText(tooltipText);  // Set tooltip for full list

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        // View All button if applicable
        if (type != null) {
            JButton viewAll = new JButton("View All");
            viewAll.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            viewAll.setFocusPainted(false);
            viewAll.setBackground(new Color(255, 255, 255, 200));
            viewAll.addActionListener(e -> showScrollableDialog(title, tooltipText));
            card.add(viewAll, BorderLayout.SOUTH);
        }

        return card;
    }

    private void showScrollableDialog(String title, String content) {
        JTextArea area = new JTextArea(content);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(400, 200));

        JOptionPane.showMessageDialog(this, scroll, title + " - Full List", JOptionPane.INFORMATION_MESSAGE);
    }


    private JPanel createCard(String title, String value, Color bg) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bg);
        card.setBorder(new RoundedBorder(10));
        JLabel l1 = new JLabel(title);
        l1.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JLabel l2 = new JLabel(value);
        l2.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        card.add(l1, BorderLayout.NORTH);
        card.add(l2, BorderLayout.CENTER);
        return card;
    }

    // -- Placeholder panels --
   // static class MyCoursesPanel extends JPanel { MyCoursesPanel(String t) { add(new JLabel("MyCourses for "+t)); } }
   // static class FacultyAttendancePanel extends JPanel { public FacultyAttendancePanel(String t) { add(new JLabel("Attendance UI here")); }}
   // static class MarksheetPanel extends JPanel { MarksheetPanel(String t) { add(new JLabel("Marksheet for "+t)); } }
    //static class ResultPanel extends JPanel { ResultPanel(String t) { add(new JLabel("Result for "+t)); } }
   // static class ProfilePanel extends JPanel { ProfilePanel(String t) { add(new JLabel("Profile for "+t)); } }

    public interface SidebarListener { void nav(String option); }

    class GradientSidebar extends JPanel {
        private SidebarListener listener;
        public GradientSidebar(String[] options) {
            setPreferredSize(new Dimension(220, getHeight()));
            setLayout(new GridLayout(options.length,1, 5,5));
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            int h = getHeight();
            GradientPaint gp = new GradientPaint(0,0,new Color(0,51,102),0,h,new Color(51,102,153));
            g2.setPaint(gp);
            g2.fillRect(0,0,getWidth(), h);
        }

        public void setNavigationListener(SidebarListener l) {
            this.listener = l;
            removeAll();
            String[] opts = {"🏠 Home","📚 My Courses","📝 Attendance","📊 Marksheet","🙍 Profile","🚪 Logout"};
            for (String o: opts) {
                JButton btn = new JButton(o);
                btn.setForeground(Color.WHITE);
                btn.setFocusPainted(false);
                btn.setBorder(BorderFactory.createEmptyBorder(8,15,8,8));
                btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
                btn.setContentAreaFilled(false);
                btn.setHorizontalAlignment(SwingConstants.LEFT);
                btn.addActionListener(e -> {
                    if (listener != null) listener.nav(o.split(" ")[1].equals("Home")?"Home":o.replaceFirst("\\p{So} ", ""));
                });
                btn.addMouseListener(new MouseAdapter(){
                    public void mouseEntered(MouseEvent e) { btn.setForeground(new Color(255,255,150)); }
                    public void mouseExited(MouseEvent e) { btn.setForeground(Color.WHITE); }
                });
                add(btn);
            }
        }
    }

    static class RoundedBorder implements Border {
        private int radius;
        public RoundedBorder(int radius) { this.radius = radius; }
        public Insets getBorderInsets(Component c) { return new Insets(radius,radius,radius,radius); }
        public boolean isBorderOpaque() { return false; }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            g.setColor(new Color(150,150,150));
            g.drawRoundRect(x, y, w-1, h-1, radius, radius);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new main_class("101",() -> {
            JOptionPane.showMessageDialog(null, "Back to login");
        }));
    }
}
