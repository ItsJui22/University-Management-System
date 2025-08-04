package student;

import common.Conn;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class StudentAttendancePanel extends JPanel {
    private String registrationId; // student login ID

    private JComboBox<String> courseCombo;
    private JComboBox<String> semCombo;
    private JButton loadBtn;
    private JTable table;
    private JLabel percentageLabel, statusLabel;

    public StudentAttendancePanel(String registrationId) {
        this.registrationId = registrationId;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(240, 250, 255));

        // Top Panel
        JPanel topPanel = new JPanel();
        courseCombo = new JComboBox<>();
        semCombo = new JComboBox<>(new String[]{"1", "2", "3", "4", "5", "6", "7", "8"});
        loadBtn = new JButton("Load Attendance");

        topPanel.add(new JLabel("Semester:"));
        topPanel.add(semCombo);
        topPanel.add(new JLabel("Course:"));
        topPanel.add(courseCombo);
        topPanel.add(loadBtn);

        add(topPanel, BorderLayout.NORTH);

        // Table
        table = new JTable(new DefaultTableModel(new String[]{"Date", "Present"}, 0));
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Labels
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1));
        percentageLabel = new JLabel("Attendance: ");
        statusLabel = new JLabel("Status: ");
        bottomPanel.add(percentageLabel);
        bottomPanel.add(statusLabel);
        add(bottomPanel, BorderLayout.SOUTH);

        loadCourses();

        loadBtn.addActionListener(e -> loadAttendance());
    }

    private void loadCourses() {
        courseCombo.removeAllItems();
        try (Conn c = new Conn()) {
            PreparedStatement ps = c.c.prepareStatement(
                    "SELECT DISTINCT coursecode FROM student_course_map WHERE registration = ?"
            );
            ps.setString(1, registrationId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                courseCombo.addItem(rs.getString("coursecode"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadAttendance() {
        String course = (String) courseCombo.getSelectedItem();
        String sem = (String) semCombo.getSelectedItem();

        if (course == null || sem == null) {
            JOptionPane.showMessageDialog(this, "Please select course and semester.");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        int total = 0, present = 0;

        try (Conn c = new Conn()) {
            String query = "SELECT date, present FROM attandance WHERE registration = ? AND coursecode = ? AND semoryear = ?";
            PreparedStatement ps = c.c.prepareStatement(query);
            ps.setString(1, registrationId);
            ps.setString(2, course);
            ps.setInt(3, Integer.parseInt(sem));

            System.out.println("Executing query for: " + registrationId + " | " + course + " | " + sem);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String date = rs.getString("date");
                boolean isPresent = rs.getInt("present") == 1;
                model.addRow(new Object[]{date, isPresent ? "Present" : "Absent"});
                total++;
                if (isPresent) present++;
            }

            double percent = total == 0 ? 0 : (present * 100.0) / total;
            percentageLabel.setText("Attendance: " + present + "/" + total + " (" + String.format("%.2f", percent) + "%)");

            String status = percent >= 75 ? "Collegiate" : percent >= 60 ? "Non-Collegiate" : "Dis-Collegiate";
            statusLabel.setText("Status: " + status);

            if (total == 0) {
                JOptionPane.showMessageDialog(this, "No attendance data found.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load attendance: " + ex.getMessage());
        }
    }}
