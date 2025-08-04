package admin;

import common.Conn;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class AdminMarksPanel extends JPanel {
    private JComboBox<String> deptCombo, semCombo, courseCombo;
    private JTable marksTable;
    private DefaultTableModel tableModel;

    private final Color primaryColor = new Color(72, 145, 220);
    private final Color secondaryColor = new Color(230, 240, 250);
    private final Color accentColor = new Color(45, 110, 180);
    private final Font headerFont = new Font("Segoe UI", Font.BOLD, 14);
    private final Font tableFont = new Font("Segoe UI", Font.PLAIN, 14);

    public AdminMarksPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(secondaryColor);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Panel for filters
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setBackground(secondaryColor);

        deptCombo = new JComboBox<>(getDepartments());
        decorateComboBox(deptCombo);
        semCombo = new JComboBox<>();
        decorateComboBox(semCombo);
        courseCombo = new JComboBox<>();
        decorateComboBox(courseCombo);

        JButton loadMarksBtn = new JButton("Load Marksheet");
        decorateButton(loadMarksBtn);

        topPanel.add(new JLabel("Department:"));
        topPanel.add(deptCombo);
        topPanel.add(new JLabel("Semester:"));
        topPanel.add(semCombo);
        topPanel.add(new JLabel("Course:"));
        topPanel.add(courseCombo);
        topPanel.add(loadMarksBtn);

        add(topPanel, BorderLayout.NORTH);

        // Table setup
        String[] columns = {"Roll Number", "Student Name", "Theory Marks", "Practical Marks"};
        tableModel = new DefaultTableModel(null, columns) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex >= 2) return Integer.class;
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only for admin view
            }
        };

        marksTable = new JTable(tableModel);
        marksTable.setFont(tableFont);
        marksTable.getTableHeader().setFont(headerFont);
        marksTable.setRowHeight(28);
        marksTable.setSelectionBackground(primaryColor);
        marksTable.setSelectionForeground(Color.WHITE);
        marksTable.getTableHeader().setBackground(accentColor);
        marksTable.getTableHeader().setForeground(Color.WHITE);
        marksTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(marksTable);
        scrollPane.setBorder(new CompoundBorder(
                new LineBorder(accentColor, 2, true),
                new EmptyBorder(10, 10, 10, 10)
        ));
        add(scrollPane, BorderLayout.CENTER);

        // Listeners
        deptCombo.addActionListener(e -> loadSemesters());
        semCombo.addActionListener(e -> loadCourses());
        loadMarksBtn.addActionListener(e -> loadMarks());

        // Initial load
        loadSemesters();
    }

    private String[] getDepartments() {
        try {
            Conn c = new Conn();
            Statement st = c.c.createStatement();
            ResultSet rs = st.executeQuery("SELECT DISTINCT Departmentcode FROM students");
            Vector<String> depts = new Vector<>();
            while (rs.next()) {
                depts.add(rs.getString("Departmentcode"));
            }
            if (depts.isEmpty()) depts.add("NOT ASSIGNED");
            return depts.toArray(new String[0]);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new String[]{"NOT ASSIGNED"};
        }
    }

    private void loadSemesters() {
        semCombo.removeAllItems();
        String dept = (String) deptCombo.getSelectedItem();
        if (dept == null) return;
        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement(
                    "SELECT DISTINCT semoryear FROM students WHERE Departmentcode = ?");
            ps.setString(1, dept);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                semCombo.addItem(String.valueOf(rs.getInt("semoryear")));
            }
            if (semCombo.getItemCount() > 0)
                semCombo.setSelectedIndex(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        loadCourses();
    }

    private void loadCourses() {
        courseCombo.removeAllItems();
        String dept = (String) deptCombo.getSelectedItem();
        String sem = (String) semCombo.getSelectedItem();
        if (dept == null || sem == null) return;
        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement(
                    "SELECT DISTINCT coursecode FROM marks WHERE Departmentcode = ? AND semoryear = ?");
            ps.setString(1, dept);
            ps.setInt(2, Integer.parseInt(sem));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String courseCode = rs.getString("coursecode");
                // Remove suffixes "-T" or "-P" to show course once
                courseCode = courseCode.replace("-T", "").replace("-P", "");
                if (((DefaultComboBoxModel<String>) courseCombo.getModel()).getIndexOf(courseCode) == -1) {
                    courseCombo.addItem(courseCode);
                }
            }
            if (courseCombo.getItemCount() > 0)
                courseCombo.setSelectedIndex(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadMarks() {
        String dept = (String) deptCombo.getSelectedItem();
        String sem = (String) semCombo.getSelectedItem();
        String course = (String) courseCombo.getSelectedItem();

        if (dept == null || sem == null || course == null) {
            JOptionPane.showMessageDialog(this, "Please select Department, Semester and Course");
            return;
        }

        tableModel.setRowCount(0);

        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement(
                    "SELECT s.rollnumber, s.firstname, s.lastname, " +
                            "(SELECT marks FROM marks WHERE rollnumber = s.rollnumber AND coursecode = CONCAT(?, '-T')) AS theoryMarks, " +
                            "(SELECT marks FROM marks WHERE rollnumber = s.rollnumber AND coursecode = CONCAT(?, '-P')) AS practicalMarks " +
                            "FROM students s WHERE Departmentcode = ? AND semoryear = ? ORDER BY s.rollnumber"
            );
            ps.setString(1, course);
            ps.setString(2, course);
            ps.setString(3, dept);
            ps.setInt(4, Integer.parseInt(sem));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String roll = rs.getString("rollnumber");
                String name = rs.getString("firstname") + " " + rs.getString("lastname");
                int theory = rs.getInt("theoryMarks");
                int practical = rs.getInt("practicalMarks");
                tableModel.addRow(new Object[]{roll, name, theory, practical});
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading marksheet: " + ex.getMessage());
        }
    }

    private void decorateComboBox(JComboBox<String> combo) {
        combo.setBackground(Color.WHITE);
        combo.setForeground(Color.DARK_GRAY);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBorder(new LineBorder(accentColor, 1, true));
    }

    private void decorateButton(JButton btn) {
        btn.setBackground(primaryColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
