package student;

import common.Conn;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

public class StudentAssignedCoursesPanel extends JPanel {
    private JTable assignedCoursesTable;
    private DefaultTableModel tableModel;
    private String studentRegistration;  // লগইন করা student's registration

    private final Color primaryColor = new Color(72, 145, 220);
    private final Color secondaryColor = new Color(230, 240, 250);
    private final Color accentColor = new Color(45, 110, 180);

    public StudentAssignedCoursesPanel(String studentRegistration) {
        this.studentRegistration = studentRegistration;

        setLayout(new BorderLayout(10, 10));
        setBackground(secondaryColor);

        JLabel titleLabel = new JLabel("Your Assigned Courses");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(primaryColor);
        add(titleLabel, BorderLayout.NORTH);

        String[] cols = {"Course Code", "Course Name", "Department", "Semester"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        assignedCoursesTable = new JTable(tableModel);
        assignedCoursesTable.setFillsViewportHeight(true);
        assignedCoursesTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        assignedCoursesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        assignedCoursesTable.setSelectionBackground(primaryColor);

        JScrollPane scrollPane = new JScrollPane(assignedCoursesTable);
        scrollPane.setBorder(new LineBorder(accentColor, 2, true));
        add(scrollPane, BorderLayout.CENTER);

        loadAssignedCourses();
    }

    private void loadAssignedCourses() {
        tableModel.setRowCount(0);

        try (Conn c = new Conn()) {
            String sql = "SELECT m.coursecode, c.coursename, m.department_code, m.semoryear " +
                    "FROM student_course_map m " +
                    "JOIN courses c ON m.coursecode = c.coursecode " +
                    "WHERE m.registration = ? " +
                    "ORDER BY m.department_code, m.semoryear, m.coursecode";

            PreparedStatement ps = c.c.prepareStatement(sql);
            ps.setString(1, studentRegistration);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("coursecode"));
                row.add(rs.getString("coursename"));
                row.add(rs.getString("department_code"));
                row.add(rs.getInt("semoryear"));

                tableModel.addRow(row);
            }

            rs.close();
            ps.close();
            c.c.close();

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No courses assigned to you yet.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load assigned courses: " + e.getMessage());
        }
    }
}
