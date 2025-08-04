package admin;

import common.Conn;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminResultPanel extends JPanel {
    private JComboBox<String> deptCombo, semCombo, courseCombo;
    private JButton loadBtn;
    private DefaultTableModel tableModel;
    private JTable resultTable;

    private final Color primaryColor = new Color(72, 145, 220);
    private final Color secondaryColor = new Color(230, 240, 250);
    private final Color accentColor = new Color(45, 110, 180);

    public AdminResultPanel() {
        setLayout(new BorderLayout(10,10));
        setBackground(secondaryColor);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(secondaryColor);

        deptCombo = new JComboBox<>();
        semCombo = new JComboBox<>();
        courseCombo = new JComboBox<>();

        decorateComboBox(deptCombo);
        decorateComboBox(semCombo);
        decorateComboBox(courseCombo);

        loadDepartments();
        loadSemestersManually();  // 1 to 8

        deptCombo.addActionListener(e -> loadCourses());
        semCombo.addActionListener(e -> loadCourses());

        loadBtn = new JButton("Load Results");
        decorateButton(loadBtn);

        topPanel.add(new JLabel("Department:"));
        topPanel.add(deptCombo);
        topPanel.add(new JLabel("Semester:"));
        topPanel.add(semCombo);
        topPanel.add(new JLabel("Course:"));
        topPanel.add(courseCombo);
        topPanel.add(loadBtn);

        add(topPanel, BorderLayout.NORTH);

        String[] cols = {"Registration", "Student Name", "Marks", "GPA"};
        tableModel = new DefaultTableModel(null, cols) {
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 2 -> Integer.class;
                    case 3 -> Double.class;
                    default -> String.class;
                };
            }
            public boolean isCellEditable(int row, int col) { return false; }
        };

        resultTable = new JTable(tableModel);
        resultTable.setFillsViewportHeight(true);
        add(new JScrollPane(resultTable), BorderLayout.CENTER);

        loadBtn.addActionListener(e -> loadResults());
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

    private void loadDepartments() {
        deptCombo.removeAllItems();
        try (Conn c = new Conn();
             PreparedStatement ps = c.c.prepareStatement("SELECT Departmentcode, DepartmentName FROM departments ORDER BY DepartmentName");
             ResultSet rs = ps.executeQuery()) {
            while(rs.next()) {
                deptCombo.addItem(rs.getString("Departmentcode") + " - " + rs.getString("DepartmentName"));
            }
            if(deptCombo.getItemCount() > 0) deptCombo.setSelectedIndex(0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load departments: " + ex.getMessage());
        }
    }

    private void loadSemestersManually() {
        semCombo.removeAllItems();
        for (int i = 1; i <= 8; i++) semCombo.addItem(String.valueOf(i));
        semCombo.setSelectedIndex(0);
    }

    private void loadCourses() {
        courseCombo.removeAllItems();
        if (deptCombo.getSelectedItem() == null || semCombo.getSelectedItem() == null) return;

        String deptCode = deptCombo.getSelectedItem().toString().split(" - ")[0].trim();
        int sem = Integer.parseInt(semCombo.getSelectedItem().toString());

        try (Conn c = new Conn();
             PreparedStatement ps = c.c.prepareStatement("SELECT coursecode, coursename FROM courses WHERE Departmentcode = ? AND semoryear = ?")) {
            ps.setString(1, deptCode);
            ps.setInt(2, sem);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                courseCombo.addItem(rs.getString("coursecode") + " - " + rs.getString("coursename"));
            }
            if(courseCombo.getItemCount() > 0) courseCombo.setSelectedIndex(0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load courses: " + ex.getMessage());
        }
    }
    private void loadResults() {
        tableModel.setRowCount(0);

        if (deptCombo.getSelectedItem() == null || semCombo.getSelectedItem() == null || courseCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select Department, Semester, and Course");
            return;
        }

        try {
            String deptCode = deptCombo.getSelectedItem().toString().split(" - ")[0].trim();
            int sem = Integer.parseInt(semCombo.getSelectedItem().toString());
            String courseCode = courseCombo.getSelectedItem().toString().split(" - ")[0].trim();

            Conn c = new Conn();

            String sql = "SELECT s.registration, CONCAT(s.firstname, ' ', s.lastname) AS student_name, m.marks " +
                    "FROM marks m JOIN students s ON m.registration = s.registration " +
                    "WHERE m.Departmentcode = ? AND m.semoryear = ? AND m.coursecode LIKE CONCAT(?, '%')";

            PreparedStatement ps = c.c.prepareStatement(sql);
            ps.setString(1, deptCode);
            ps.setInt(2, sem);
            ps.setString(3, courseCode);

            ResultSet rs = ps.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;
                String reg = rs.getString("registration");
                String name = rs.getString("student_name");
                int marks = rs.getInt("marks");
                double gpa = calculateGPA(marks);
                tableModel.addRow(new Object[]{reg, name, marks, gpa});
            }

            if (!found) {
                JOptionPane.showMessageDialog(this, "No results found for the selected criteria.");
            }

            rs.close();
            ps.close();
            c.c.close();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load results: " + e.getMessage());
        }
    }


    private double calculateGPA(int marks) {
        return switch (marks / 10) {
            case 10, 9, 8 -> 4.0;
            case 7 -> 3.5;
            case 6 -> 3.0;
            case 5 -> 2.5;
            case 4 -> 2.0;
            default -> 0.0;
        };
    }
}
