package faculty;

import common.Conn;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class MyCoursesPanel extends JPanel {
    private JTable coursesTable;
    private DefaultTableModel tableModel;

    private final Color primaryColor = new Color(85, 150, 220);
    private final Color secondaryColor = new Color(230, 240, 250);
    private final Color accentColor = new Color(70, 130, 180);
    private final Font headerFont = new Font("Segoe UI", Font.BOLD, 14);
    private final Font tableFont = new Font("Segoe UI", Font.PLAIN, 13);

    private int teacherId;

    public MyCoursesPanel(String teacherId) {
        this.teacherId = Integer.parseInt(teacherId);

        setLayout(new BorderLayout(15, 15));
        setBackground(secondaryColor);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("My Courses");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(primaryColor);
        titleLabel.setBorder(new EmptyBorder(10, 10, 20, 10));
        add(titleLabel, BorderLayout.NORTH);

        // Table Setup
        String[] columns = {"Course Code", "Department", "Semester/Year"};

        tableModel = new DefaultTableModel(columns, 0) {
            // Make table cells non-editable
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        coursesTable = new JTable(tableModel);
        coursesTable.setFont(tableFont);
        coursesTable.setRowHeight(28);
        coursesTable.setSelectionBackground(primaryColor);
        coursesTable.setSelectionForeground(Color.WHITE);
        coursesTable.getTableHeader().setFont(headerFont);
        coursesTable.getTableHeader().setBackground(accentColor);
        coursesTable.getTableHeader().setForeground(Color.WHITE);
        coursesTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(coursesTable);
        scrollPane.setBorder(new CompoundBorder(
                new LineBorder(accentColor, 2, true),
                new EmptyBorder(10, 10, 10, 10)
        ));
        add(scrollPane, BorderLayout.CENTER);

        loadCourses();
    }

    private void loadCourses() {
        tableModel.setRowCount(0); // Clear existing rows
        try {
            Conn c = new Conn();

            String sql = "SELECT coursecode, department_code, semoryear FROM teacher_course_map WHERE teacher_id = ?";
            PreparedStatement ps = c.c.prepareStatement(sql);
            ps.setInt(1, teacherId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("coursecode"));
                row.add(rs.getString("department_code"));
                row.add(rs.getInt("semoryear"));

                tableModel.addRow(row);
            }
            rs.close();
            ps.close();
            c.c.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading courses from database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
