/*package admin;

import common.Conn;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class AssignCoursePanel extends JPanel {
    private JComboBox<String> deptCombo, semesterCombo, courseCodeCombo, teacherCombo;
    private JButton assignBtn, deleteBtn;
    private JTable assignedTable;
    private DefaultTableModel tableModel;

    private final Color primaryColor = new Color(85, 150, 220);
    private final Color secondaryColor = new Color(230, 240, 250);
    private final Color accentColor = new Color(70, 130, 180);
    private final Font headerFont = new Font("Segoe UI", Font.BOLD, 14);
    private final Font tableFont = new Font("Segoe UI", Font.PLAIN, 13);

    public AssignCoursePanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(secondaryColor);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Assign Course to Teacher");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(primaryColor);
        add(titleLabel, BorderLayout.NORTH);

        // ========== Form Panel ========== //
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new CompoundBorder(new LineBorder(accentColor, 2, true), new EmptyBorder(20, 20, 20, 20)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        deptCombo = new JComboBox<>(getDepartments());
        semesterCombo = new JComboBox<>(new String[]{"1", "2", "3", "4", "5", "6", "7", "8"});
        courseCodeCombo = new JComboBox<>(getCourses());
        teacherCombo = new JComboBox<>(getTeachers());

        decorateCombo(deptCombo);
        decorateCombo(semesterCombo);
        decorateCombo(courseCodeCombo);
        decorateCombo(teacherCombo);

        assignBtn = new JButton("Assign Course");
        deleteBtn = new JButton("Unassign Selected");
        decorateButton(assignBtn);
        decorateButton(deleteBtn);

        int y = 0;
        addRow(formPanel, gbc, y++, "Department:", deptCombo);
        addRow(formPanel, gbc, y++, "Semester:", semesterCombo);
        addRow(formPanel, gbc, y++, "Course Code:", courseCodeCombo);
        addRow(formPanel, gbc, y++, "Teacher:", teacherCombo);
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        formPanel.add(assignBtn, gbc);
        gbc.gridy++;
        formPanel.add(deleteBtn, gbc);

        add(formPanel, BorderLayout.WEST);

        // ========== Table Section ========== //
        String[] cols = {"ID", "Course Code", "Department", "Semester", "Teacher"};
        tableModel = new DefaultTableModel(cols, 0);
        assignedTable = new JTable(tableModel);
        assignedTable.setFont(tableFont);
        assignedTable.setRowHeight(26);
        assignedTable.setSelectionBackground(primaryColor);
        assignedTable.getTableHeader().setFont(headerFont);
        assignedTable.getTableHeader().setBackground(accentColor);
        assignedTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane tableScroll = new JScrollPane(assignedTable);
        tableScroll.setBorder(new CompoundBorder(new LineBorder(accentColor, 2), new EmptyBorder(10, 10, 10, 10)));
        add(tableScroll, BorderLayout.CENTER);

        assignBtn.addActionListener(e -> assignCourse());
        deleteBtn.addActionListener(e -> deleteAssignment());

        loadAssignedCourses();
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int y, String label, Component field) {
        gbc.gridx = 0; gbc.gridy = y;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void decorateCombo(JComboBox<String> box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.setBackground(Color.WHITE);
        box.setForeground(Color.DARK_GRAY);
        box.setBorder(new LineBorder(accentColor, 1, true));
    }

    private void decorateButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(primaryColor);
        btn.setForeground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
    }

    private String[] getDepartments() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT DISTINCT Departmentcode FROM departments");
            Vector<String> list = new Vector<>();
            while (rs.next()) list.add(rs.getString(1));
            return list.toArray(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return new String[]{"NONE"};
        }
    }

    private String[] getCourses() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT coursecode FROM courses");
            Vector<String> list = new Vector<>();
            while (rs.next()) list.add(rs.getString(1));
            return list.toArray(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return new String[]{"NONE"};
        }
    }

    private String[] getTeachers() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT teacher_id, teacherName FROM teachers");
            Vector<String> list = new Vector<>();
            while (rs.next()) {
                list.add(rs.getInt("teacher_id") + " - " + rs.getString("teacherName"));
            }
            return list.toArray(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return new String[]{"NONE"};
        }
    }

    private void assignCourse() {
        try {
            Conn c = new Conn();

            String dept = deptCombo.getSelectedItem().toString();
            int sem = Integer.parseInt(semesterCombo.getSelectedItem().toString());
            String courseCode = courseCodeCombo.getSelectedItem().toString();
            String teacherStr = teacherCombo.getSelectedItem().toString();
            int teacherId = Integer.parseInt(teacherStr.split(" - ")[0]);

            String sql = "INSERT INTO teacher_course_map (teacher_id, coursecode, department_code, semoryear) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = c.c.prepareStatement(sql);
            ps.setInt(1, teacherId);
            ps.setString(2, courseCode);
            ps.setString(3, dept);
            ps.setInt(4, sem);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Course assigned successfully!");
            loadAssignedCourses();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error assigning course.");
        }
    }

    private void loadAssignedCourses() {
        tableModel.setRowCount(0);
        try {
            Conn c = new Conn();
            String sql = "SELECT m.id, c.coursecode, m.department_code, m.semoryear, t.teacherName " +
                    "FROM teacher_course_map m " +
                    "JOIN teachers t ON m.teacher_id = t.teacher_id " +
                    "JOIN courses c ON m.coursecode = c.coursecode";

            ResultSet rs = c.s.executeQuery(sql);

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("coursecode"));  // fixed: courseode → coursecode
                row.add(rs.getString("department_code"));
                row.add(rs.getInt("semoryear"));
                row.add(rs.getString("teacherName"));
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void deleteAssignment() {
        int row = assignedTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to unassign.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement("DELETE FROM teacher_course_map WHERE id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Assignment removed.");
            loadAssignedCourses();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting assignment.");
        }
    }
}
*/
package admin;

import common.Conn;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class AssignCoursePanel extends JPanel {
    // Combos for assignment
    private JComboBox<String> deptCombo, semesterCombo, courseCodeCombo, teacherCombo, studentCombo;
    // Buttons for assign/unassign
    private JButton assignTeacherBtn, deleteTeacherBtn, assignStudentBtn, deleteStudentBtn;

    // Tables and models for viewing assigned courses
    private JTable teacherAssignedTable, studentAssignedTable;
    private DefaultTableModel teacherTableModel, studentTableModel;

    private final Color primaryColor = new Color(85, 150, 220);
    private final Color secondaryColor = new Color(230, 240, 250);
    private final Color accentColor = new Color(70, 130, 180);
    private final Font headerFont = new Font("Segoe UI", Font.BOLD, 14);
    private final Font tableFont = new Font("Segoe UI", Font.PLAIN, 13);

    public AssignCoursePanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(secondaryColor);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Assign Courses to Teachers & Students");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(primaryColor);
        add(titleLabel, BorderLayout.NORTH);

        // ========== Form Panel ========== //
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new CompoundBorder(new LineBorder(accentColor, 2, true), new EmptyBorder(20, 20, 20, 20)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // at the end of the formPanel creation and setup
        JScrollPane formScroll = new JScrollPane(formPanel);
        formScroll.setPreferredSize(new Dimension(350, 600)); // adjust as per your UI size
        formScroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        add(formScroll, BorderLayout.WEST);


        // Initialize combos
        deptCombo = new JComboBox<>(getDepartments());
        semesterCombo = new JComboBox<>(new String[]{"1","2","3","4","5","6","7","8"});
        courseCodeCombo = new JComboBox<>(getCourses());
        teacherCombo = new JComboBox<>(getTeachers());
        studentCombo = new JComboBox<>(getStudents());

        decorateCombo(deptCombo);
        decorateCombo(semesterCombo);
        decorateCombo(courseCodeCombo);
        decorateCombo(teacherCombo);
        decorateCombo(studentCombo);

        // Buttons
        assignTeacherBtn = new JButton("Assign Course to Teacher");
        deleteTeacherBtn = new JButton("Unassign Selected Teacher Course");
        assignStudentBtn = new JButton("Assign Course to Student");
        deleteStudentBtn = new JButton("Unassign Selected Student Course");

        decorateButton(assignTeacherBtn);
        decorateButton(deleteTeacherBtn);
        decorateButton(assignStudentBtn);
        decorateButton(deleteStudentBtn);

        int y = 0;
        addRow(formPanel, gbc, y++, "Department:", deptCombo);
        addRow(formPanel, gbc, y++, "Semester:", semesterCombo);
        addRow(formPanel, gbc, y++, "Course Code:", courseCodeCombo);
        addRow(formPanel, gbc, y++, "Teacher:", teacherCombo);
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        formPanel.add(assignTeacherBtn, gbc);
        gbc.gridy++;
        formPanel.add(deleteTeacherBtn, gbc);

        gbc.gridy++;
        formPanel.add(new JSeparator(SwingConstants.HORIZONTAL), gbc);
        gbc.gridy++;

        addRow(formPanel, gbc, y++, "Department:", new JLabel()); // spacer
        addRow(formPanel, gbc, y++, "Semester:", new JLabel());
        addRow(formPanel, gbc, y++, "Course Code:", new JLabel());

        addRow(formPanel, gbc, y++, "Student:", studentCombo);
        gbc.gridy++;
        formPanel.add(assignStudentBtn, gbc);
        gbc.gridy++;
        formPanel.add(deleteStudentBtn, gbc);

        add(formPanel, BorderLayout.WEST);

        // ========== Tables ========== //
        String[] teacherCols = {"ID", "Course Code", "Department", "Semester", "Teacher"};
        teacherTableModel = new DefaultTableModel(teacherCols, 0);
        teacherAssignedTable = new JTable(teacherTableModel);
        styleTable(teacherAssignedTable);

        String[] studentCols = {"ID", "Course Code", "Department", "Semester", "Student"};
        studentTableModel = new DefaultTableModel(studentCols, 0);
        studentAssignedTable = new JTable(studentTableModel);
        styleTable(studentAssignedTable);

        JPanel tablePanel = new JPanel(new GridLayout(2,1, 10, 10));
        tablePanel.add(new JScrollPane(teacherAssignedTable));
        tablePanel.add(new JScrollPane(studentAssignedTable));
        add(tablePanel, BorderLayout.CENTER);

        // Button listeners
        assignTeacherBtn.addActionListener(e -> assignCourseToTeacher());
        deleteTeacherBtn.addActionListener(e -> deleteTeacherAssignment());

        assignStudentBtn.addActionListener(e -> assignCourseToStudent());
        deleteStudentBtn.addActionListener(e -> deleteStudentAssignment());

        loadAssignedTeacherCourses();
        loadAssignedStudentCourses();
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int y, String label, Component field) {
        gbc.gridx = 0; gbc.gridy = y;
        if(!label.isEmpty()) panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void decorateCombo(JComboBox<String> box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.setBackground(Color.WHITE);
        box.setForeground(Color.DARK_GRAY);
        box.setBorder(new LineBorder(accentColor, 1, true));
    }

    private void decorateButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(primaryColor);
        btn.setForeground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
    }

    private void styleTable(JTable table) {
        table.setFont(tableFont);
        table.setRowHeight(26);
        table.setSelectionBackground(primaryColor);
        table.getTableHeader().setFont(headerFont);
        table.getTableHeader().setBackground(accentColor);
        table.getTableHeader().setForeground(Color.WHITE);
    }

    private String[] getDepartments() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT DISTINCT Departmentcode FROM departments ORDER BY Departmentcode");
            Vector<String> list = new Vector<>();
            while(rs.next()) list.add(rs.getString(1));
            return list.toArray(new String[0]);
        } catch(Exception e) {
            e.printStackTrace();
            return new String[]{"NONE"};
        }
    }

    private String[] getCourses() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT coursecode FROM courses ORDER BY coursecode");
            Vector<String> list = new Vector<>();
            while(rs.next()) list.add(rs.getString(1));
            return list.toArray(new String[0]);
        } catch(Exception e) {
            e.printStackTrace();
            return new String[]{"NONE"};
        }
    }

    private String[] getTeachers() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT teacher_id, teacherName FROM teachers ORDER BY teacherName");
            Vector<String> list = new Vector<>();
            while(rs.next()) {
                list.add(rs.getInt("teacher_id") + " - " + rs.getString("teacherName"));
            }
            return list.toArray(new String[0]);
        } catch(Exception e) {
            e.printStackTrace();
            return new String[]{"NONE"};
        }
    }

    private String[] getStudents() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT registration, CONCAT(firstname, ' ', lastname) AS studentName FROM students ORDER BY firstname");
            Vector<String> list = new Vector<>();
            while(rs.next()) {
                list.add(rs.getString("registration") + " - " + rs.getString("studentName"));
            }
            return list.toArray(new String[0]);
        } catch(Exception e) {
            e.printStackTrace();
            return new String[]{"NONE"};
        }
    }

    private void assignCourseToTeacher() {
        try {
            Conn c = new Conn();

            String dept = deptCombo.getSelectedItem().toString();
            int sem = Integer.parseInt(semesterCombo.getSelectedItem().toString());
            String courseCode = courseCodeCombo.getSelectedItem().toString();
            String teacherStr = teacherCombo.getSelectedItem().toString();
            int teacherId = Integer.parseInt(teacherStr.split(" - ")[0]);

            // Check if already assigned to avoid duplicates
            PreparedStatement checkPs = c.c.prepareStatement(
                    "SELECT COUNT(*) FROM teacher_course_map WHERE teacher_id = ? AND coursecode = ? AND department_code = ? AND semoryear = ?");
            checkPs.setInt(1, teacherId);
            checkPs.setString(2, courseCode);
            checkPs.setString(3, dept);
            checkPs.setInt(4, sem);
            ResultSet rs = checkPs.executeQuery();
            if(rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "This course is already assigned to the teacher.");
                return;
            }

            String sql = "INSERT INTO teacher_course_map (teacher_id, coursecode, department_code, semoryear) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = c.c.prepareStatement(sql);
            ps.setInt(1, teacherId);
            ps.setString(2, courseCode);
            ps.setString(3, dept);
            ps.setInt(4, sem);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Course assigned to teacher successfully!");
            loadAssignedTeacherCourses();
        } catch(Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error assigning course to teacher.");
        }
    }

    private void loadAssignedTeacherCourses() {
        teacherTableModel.setRowCount(0);
        try {
            Conn c = new Conn();
            String sql = "SELECT m.id, c.coursecode, m.department_code, m.semoryear, t.teacherName " +
                    "FROM teacher_course_map m " +
                    "JOIN teachers t ON m.teacher_id = t.teacher_id " +
                    "JOIN courses c ON m.coursecode = c.coursecode " +
                    "ORDER BY m.department_code, m.semoryear, c.coursecode";

            ResultSet rs = c.s.executeQuery(sql);

            while(rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("coursecode"));
                row.add(rs.getString("department_code"));
                row.add(rs.getInt("semoryear"));
                row.add(rs.getString("teacherName"));
                teacherTableModel.addRow(row);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteTeacherAssignment() {
        int row = teacherAssignedTable.getSelectedRow();
        if(row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a teacher assignment to unassign.");
            return;
        }

        int id = (int) teacherTableModel.getValueAt(row, 0);
        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement("DELETE FROM teacher_course_map WHERE id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Teacher course assignment removed.");
            loadAssignedTeacherCourses();
        } catch(Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting teacher assignment.");
        }
    }

    private void assignCourseToStudent() {
        try {
            Conn c = new Conn();

            String dept = deptCombo.getSelectedItem().toString();
            int sem = Integer.parseInt(semesterCombo.getSelectedItem().toString());
            String courseCode = courseCodeCombo.getSelectedItem().toString();
            String studentStr = studentCombo.getSelectedItem().toString();
            String registration = studentStr.split(" - ")[0];

            // Check if already assigned
            PreparedStatement checkPs = c.c.prepareStatement(
                    "SELECT COUNT(*) FROM student_course_map WHERE registration = ? AND coursecode = ? AND department_code = ? AND semoryear = ?");
            checkPs.setString(1, registration);
            checkPs.setString(2, courseCode);
            checkPs.setString(3, dept);
            checkPs.setInt(4, sem);
            ResultSet rs = checkPs.executeQuery();
            if(rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "This course is already assigned to the student.");
                return;
            }

            String sql = "INSERT INTO student_course_map (registration, coursecode, department_code, semoryear) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = c.c.prepareStatement(sql);
            ps.setString(1, registration);
            ps.setString(2, courseCode);
            ps.setString(3, dept);
            ps.setInt(4, sem);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Course assigned to student successfully!");
            loadAssignedStudentCourses();
        } catch(Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error assigning course to student.");
        }
    }

    private void loadAssignedStudentCourses() {
        studentTableModel.setRowCount(0);
        try {
            Conn c = new Conn();
            String sql = "SELECT m.id, c.coursecode, m.department_code, m.semoryear, CONCAT(s.firstname, ' ', s.lastname) AS studentName " +
                    "FROM student_course_map m " +
                    "JOIN students s ON m.registration = s.registration " +
                    "JOIN courses c ON m.coursecode = c.coursecode " +
                    "ORDER BY m.department_code, m.semoryear, c.coursecode";

            ResultSet rs = c.s.executeQuery(sql);

            while(rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("coursecode"));
                row.add(rs.getString("department_code"));
                row.add(rs.getInt("semoryear"));
                row.add(rs.getString("studentName"));
                studentTableModel.addRow(row);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteStudentAssignment() {
        int row = studentAssignedTable.getSelectedRow();
        if(row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student assignment to unassign.");
            return;
        }

        int id = (int) studentTableModel.getValueAt(row, 0);
        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement("DELETE FROM student_course_map WHERE id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Student course assignment removed.");
            loadAssignedStudentCourses();
        } catch(Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting student assignment.");
        }
    }
}
