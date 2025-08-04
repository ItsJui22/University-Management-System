package admin;

import common.Conn;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class AttendanceViewerPanel extends JPanel {
    private JComboBox<String> deptCombo, semCombo, courseCombo;
    private JTextField dateField;
    private JButton loadAttendanceBtn;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;

    private final Color primaryColor = new Color(72, 145, 220);
    private final Color secondaryColor = new Color(230, 240, 250);
    private final Color accentColor = new Color(45, 110, 180);
    private final Font headerFont = new Font("Segoe UI", Font.BOLD, 14);
    private final Font tableFont = new Font("Segoe UI", Font.PLAIN, 14);

    public AttendanceViewerPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(secondaryColor);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(secondaryColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Dept Combo
        deptCombo = new JComboBox<>(loadDepartments());
        decorateComboBox(deptCombo);
        deptCombo.addActionListener(e -> loadSemesters());

        semCombo = new JComboBox<>();
        decorateComboBox(semCombo);
        semCombo.addActionListener(e -> loadCourses());

        courseCombo = new JComboBox<>();
        decorateComboBox(courseCombo);

        dateField = new JTextField(10);
        dateField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateField.setText(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));

        loadAttendanceBtn = new JButton("Load Attendance");
        decorateButton(loadAttendanceBtn);

        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1;
        topPanel.add(deptCombo, gbc);

        gbc.gridx = 2;
        topPanel.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 3;
        topPanel.add(semCombo, gbc);

        gbc.gridx = 4;
        topPanel.add(new JLabel("Course:"), gbc);
        gbc.gridx = 5;
        topPanel.add(courseCombo, gbc);

        gbc.gridx = 6;
        topPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 7;
        topPanel.add(dateField, gbc);

        gbc.gridx = 8;
        topPanel.add(loadAttendanceBtn, gbc);

        add(topPanel, BorderLayout.NORTH);

        String[] columns = {"Roll Number", "Student Name", "Present"};
        tableModel = new DefaultTableModel(null, columns) {
            @Override
            public Class<?> getColumnClass(int col) {
                return col == 2 ? Boolean.class : String.class;
            }
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // readonly table
            }
        };

        attendanceTable = new JTable(tableModel);
        attendanceTable.setFont(tableFont);
        attendanceTable.getTableHeader().setFont(headerFont);
        attendanceTable.setRowHeight(28);
        attendanceTable.getTableHeader().setBackground(accentColor);
        attendanceTable.getTableHeader().setForeground(Color.WHITE);
        attendanceTable.setSelectionBackground(primaryColor);
        attendanceTable.setSelectionForeground(Color.WHITE);
        attendanceTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(new CompoundBorder(
                new LineBorder(accentColor, 2, true),
                new EmptyBorder(10, 10, 10, 10)));

        add(scrollPane, BorderLayout.CENTER);

        loadAttendanceBtn.addActionListener(e -> loadAttendance());

        loadSemesters(); // load semesters for initial dept
    }

    private String[] loadDepartments() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT DISTINCT Departmentcode FROM attandance");
            Vector<String> depts = new Vector<>();
            while(rs.next()) {
                depts.add(rs.getString("Departmentcode"));
            }
            if(depts.isEmpty()) depts.add("NOT ASSIGNED");
            return depts.toArray(new String[0]);
        } catch(Exception ex) {
            ex.printStackTrace();
            return new String[]{"NOT ASSIGNED"};
        }
    }

    private void loadSemesters() {
        semCombo.removeAllItems();
        String dept = (String) deptCombo.getSelectedItem();
        if(dept == null) return;
        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement(
                    "SELECT DISTINCT semoryear FROM attandance WHERE Departmentcode = ?");
            ps.setString(1, dept);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                semCombo.addItem(String.valueOf(rs.getInt("semoryear")));
            }
            if(semCombo.getItemCount() > 0)
                semCombo.setSelectedIndex(0);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        loadCourses();
    }

    private void loadCourses() {
        courseCombo.removeAllItems();
        String dept = (String) deptCombo.getSelectedItem();
        String sem = (String) semCombo.getSelectedItem();
        if(dept == null || sem == null) return;
        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement(
                    "SELECT DISTINCT coursecode FROM attandance WHERE Departmentcode = ? AND semoryear = ?");
            ps.setString(1, dept);
            ps.setInt(2, Integer.parseInt(sem));
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                courseCombo.addItem(rs.getString("coursecode"));
            }
            if(courseCombo.getItemCount() > 0)
                courseCombo.setSelectedIndex(0);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadAttendance() {
        tableModel.setRowCount(0);
        String dept = (String) deptCombo.getSelectedItem();
        String sem = (String) semCombo.getSelectedItem();
        String course = (String) courseCombo.getSelectedItem();
        String date = dateField.getText().trim();

        if(dept == null || sem == null || course == null || date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select all fields and enter a date.");
            return;
        }

        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement(
                    "SELECT rollnumber, present FROM attandance WHERE Departmentcode = ? AND semoryear = ? AND coursecode = ? AND date = ?");
            ps.setString(1, dept);
            ps.setInt(2, Integer.parseInt(sem));
            ps.setString(3, course);
            ps.setString(4, date);
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                String roll = rs.getString("rollnumber");
                boolean present = rs.getInt("present") == 1;

                // Get student name from students table
                PreparedStatement ps2 = c.c.prepareStatement(
                        "SELECT firstname, lastname FROM students WHERE rollnumber = ?");
                ps2.setString(1, roll);
                ResultSet rs2 = ps2.executeQuery();
                String name = "Unknown";
                if(rs2.next()) {
                    name = rs2.getString("firstname") + " " + rs2.getString("lastname");
                }

                tableModel.addRow(new Object[]{roll, name, present});
            }

        } catch(Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load attendance: " + ex.getMessage());
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
