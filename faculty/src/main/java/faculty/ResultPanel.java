package faculty;

import common.Conn;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ResultPanel extends JPanel {

    private JComboBox<String> deptCombo, semCombo, courseCombo;
    private JButton publishBtn;
    private String facultyId;

    public ResultPanel(String facultyId) {
        this.facultyId = facultyId;

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel heading = new JLabel("Publish Result");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 22));
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        add(heading, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setBackground(Color.WHITE);

        deptCombo = new JComboBox<>(getDepartments());
        semCombo = new JComboBox<>();
        courseCombo = new JComboBox<>();

        deptCombo.addActionListener(e -> loadSemesters());
        semCombo.addActionListener(e -> loadCourses());

        form.add(new JLabel("Department:"));
        form.add(deptCombo);

        form.add(new JLabel("Semester:"));
        form.add(semCombo);

        form.add(new JLabel("Course:"));
        form.add(courseCombo);

        publishBtn = new JButton("Publish Result");
        publishBtn.setBackground(new Color(44, 102, 190));
        publishBtn.setForeground(Color.WHITE);
        publishBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        publishBtn.addActionListener(e -> publishResult());

        form.add(new JLabel());
        form.add(publishBtn);

        add(form, BorderLayout.CENTER);

        loadSemesters();
    }

    private void publishResult() {
    }

    private String[] getDepartments() {
        Vector<String> list = new Vector<>();
        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement(
                    "SELECT DISTINCT department_code FROM teacher_course_map WHERE teacher_id = ?");
            ps.setString(1, facultyId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("department_code"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list.toArray(new String[0]);
    }

    private void loadSemesters() {
        semCombo.removeAllItems();
        String dept = (String) deptCombo.getSelectedItem();
        if (dept == null) return;
        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement(
                    "SELECT DISTINCT semoryear FROM teacher_course_map WHERE teacher_id = ? AND department_code = ?");
            ps.setString(1, facultyId);
            ps.setString(2, dept);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                semCombo.addItem(String.valueOf(rs.getInt("semoryear")));
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                    "SELECT DISTINCT coursecode FROM teacher_course_map WHERE teacher_id = ? AND department_code = ? AND semoryear = ?");
            ps.setString(1, facultyId);
            ps.setString(2, dept);
            ps.setInt(3, Integer.parseInt(sem));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                courseCombo.addItem(rs.getString("coursecode"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double calculateGPA(int marks) {
        if (marks >= 80) return 4.0;
        else if (marks >= 75) return 3.75;
        else if (marks >= 70) return 3.5;
        else if (marks >= 65) return 3.25;
        else if (marks >= 60) return 3.0;
        else if (marks >= 55) return 2.75;
        else if (marks >= 50) return 2.5;
        else if (marks >= 45) return 2.25;
        else if (marks >= 40) return 2.0;
        else return 0.0;
    }

    public void publishResults(String dept, int sem) {
        try {
            Conn c = new Conn();

            // Step 1: Get all students in this department & semester
            PreparedStatement studentStmt = c.c.prepareStatement(
                    "SELECT registration FROM students WHERE Departmentcode = ? AND semoryear = ?");
            studentStmt.setString(1, dept);
            studentStmt.setInt(2, sem);
            ResultSet studentRs = studentStmt.executeQuery();

            while (studentRs.next()) {
                String reg = studentRs.getString("registration");

                // Step 2: Get all theory marks for the student
                PreparedStatement markStmt = c.c.prepareStatement(
                        "SELECT coursecode, coursename, marks FROM marks WHERE registration = ? AND Departmentcode = ? AND semoryear = ?");
                markStmt.setString(1, reg);
                markStmt.setString(2, dept);
                markStmt.setInt(3, sem);
                ResultSet markRs = markStmt.executeQuery();

                Map<String, Integer> theoryMap = new HashMap<>();
                Map<String, Integer> practicalMap = new HashMap<>();
                Map<String, String> courseNameMap = new HashMap<>();

                while (markRs.next()) {
                    String coursecode = markRs.getString("coursecode");
                    String coursename = markRs.getString("coursename");
                    int mark = markRs.getInt("marks");

                    courseNameMap.put(coursecode, coursename);

                    if (coursecode.endsWith("-T")) {
                        theoryMap.put(coursecode.replace("-T", ""), mark);
                    } else if (coursecode.endsWith("-P")) {
                        practicalMap.put(coursecode.replace("-P", ""), mark);
                    }
                }

                // Step 3: For each course, compute GPA and store result
                double totalGpa = 0;
                int courseCount = 0;

                for (String courseCode : theoryMap.keySet()) {
                    int theory = theoryMap.getOrDefault(courseCode, 0);
                    int practical = practicalMap.getOrDefault(courseCode, 0);
                    int avg = (theory + practical) / 2;
                    double gpa = calculateGPA(avg);

                    totalGpa += gpa;
                    courseCount++;

                    String fullCourseCode = courseCode;
                    String courseName = courseNameMap.getOrDefault(courseCode + "-T", courseCode);

                    // Insert per-course result
                    PreparedStatement insertStmt = c.c.prepareStatement(
                            "REPLACE INTO results (registration, departmentcode, semoryear, coursecode, coursename, gpa, cgpa, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                    insertStmt.setString(1, reg);
                    insertStmt.setString(2, dept);
                    insertStmt.setInt(3, sem);
                    insertStmt.setString(4, fullCourseCode);
                    insertStmt.setString(5, courseName);
                    insertStmt.setDouble(6, gpa);
                    insertStmt.setNull(7, java.sql.Types.FLOAT); // temporary cgpa null
                    insertStmt.setString(8, "Published");
                    insertStmt.executeUpdate();
                }

                double cgpa = courseCount > 0 ? totalGpa / courseCount : 0.0;

                // Step 4: Update all rows for this student with CGPA
                PreparedStatement updateCgpaStmt = c.c.prepareStatement(
                        "UPDATE results SET cgpa = ? WHERE registration = ? AND departmentcode = ? AND semoryear = ?");
                updateCgpaStmt.setDouble(1, cgpa);
                updateCgpaStmt.setString(2, reg);
                updateCgpaStmt.setString(3, dept);
                updateCgpaStmt.setInt(4, sem);
                updateCgpaStmt.executeUpdate();
            }

            JOptionPane.showMessageDialog(null, "Results published successfully!");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error publishing results: " + ex.getMessage());
        }
    }

}
