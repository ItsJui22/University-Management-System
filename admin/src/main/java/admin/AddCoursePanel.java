package admin;

import common.Conn;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class AddCoursePanel extends JPanel {
    private JTextField courseCodeField, courseNameField, courseTypeField, marksField, searchField;
    private JComboBox<String> deptCombo, semesterCombo;
    private JTable courseTable;
    private DefaultTableModel tableModel;

    public AddCoursePanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(255, 255, 255));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 40, 20, 40));
        formPanel.setBackground(new Color(255, 255, 255, 200));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel header = new JLabel("📘 Add New Course");
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setForeground(new Color(102, 0, 153));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(header, gbc);

        gbc.gridwidth = 1;

        courseCodeField = new JTextField(15);
        courseNameField = new JTextField(15);
        courseTypeField = new JTextField(15);
        marksField = new JTextField(15);

        deptCombo = new JComboBox<>(getDepartmentCodes());
        semesterCombo = new JComboBox<>(new String[]{"1", "2", "3", "4", "5", "6", "7", "8"});

        int y = 1;
        addToForm(formPanel, gbc, y++, "Course Code:", courseCodeField);
        addToForm(formPanel, gbc, y++, "Course Name:", courseNameField);
        addToForm(formPanel, gbc, y++, "Department:", deptCombo);
        addToForm(formPanel, gbc, y++, "Semester:", semesterCombo);
        addToForm(formPanel, gbc, y++, "Course Type:", courseTypeField);
        addToForm(formPanel, gbc, y++, "Marks:", marksField);

        JButton addButton = createStyledButton("Add Course", new Color(0, 123, 255));
        JButton updateButton = createStyledButton("Update", new Color(255, 193, 7));
        JButton deleteButton = createStyledButton("Delete", new Color(220, 53, 69));

        gbc.gridx = 1;
        gbc.gridy = y++;
        formPanel.add(addButton, gbc);
        gbc.gridy = y++;
        formPanel.add(updateButton, gbc);
        gbc.gridy = y;
        formPanel.add(deleteButton, gbc);

        add(formPanel, BorderLayout.WEST);

        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        tablePanel.setBackground(new Color(245, 245, 255));

        JLabel tableTitle = new JLabel("📋 Course List");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        tableTitle.setForeground(new Color(102, 0, 153));
        tablePanel.add(tableTitle, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        courseTable = new JTable(tableModel);
        tableModel.setColumnIdentifiers(new String[]{"Code", "Name", "Dept", "Sem", "Type", "Marks"});
        courseTable.setRowHeight(25);
        courseTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        courseTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        courseTable.setSelectionBackground(new Color(204, 229, 255));

        JScrollPane scrollPane = new JScrollPane(courseTable);

        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setOpaque(false);
        searchField = new JTextField();
        JButton searchBtn = createStyledButton("Search", new Color(40, 167, 69));
        searchBtn.addActionListener(e -> filterCourses());

        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);

        tablePanel.add(searchPanel, BorderLayout.SOUTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);

        loadCoursesWithFade();

        addButton.addActionListener(e -> insertCourse());
        updateButton.addActionListener(e -> updateSelectedCourse());
        deleteButton.addActionListener(e -> deleteSelectedCourse());

        courseTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = courseTable.getSelectedRow();
                if (row != -1) {
                    courseCodeField.setText(tableModel.getValueAt(row, 0).toString());
                    courseNameField.setText(tableModel.getValueAt(row, 1).toString());
                    deptCombo.setSelectedItem(tableModel.getValueAt(row, 2).toString());
                    semesterCombo.setSelectedItem(tableModel.getValueAt(row, 3).toString());
                    courseTypeField.setText(tableModel.getValueAt(row, 4).toString());
                    marksField.setText(tableModel.getValueAt(row, 5).toString());
                }
            }
        });
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return btn;
    }

    private void addToForm(JPanel panel, GridBagConstraints gbc, int y, String label, Component field) {
        gbc.gridx = 0;
        gbc.gridy = y;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(lbl, gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private String[] getDepartmentCodes() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT DISTINCT Departmentcode FROM departments");
            Vector<String> codes = new Vector<>();
            while (rs.next()) {
                codes.add(rs.getString(1));
            }
            return codes.toArray(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return new String[]{"NOT ASSIGNED"};
        }
    }

    private void insertCourse() {
        String code = courseCodeField.getText();
        String name = courseNameField.getText();
        String dept = deptCombo.getSelectedItem().toString();
        String sem = semesterCombo.getSelectedItem().toString();
        String type = courseTypeField.getText();
        String marks = marksField.getText();

        if (code.isEmpty() || name.isEmpty() || type.isEmpty() || marks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled.");
            return;
        }

        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement("INSERT INTO courses VALUES (?, ?, ?, ?, ?, ?)");
            ps.setString(1, code);
            ps.setString(2, name);
            ps.setString(3, dept);
            ps.setInt(4, Integer.parseInt(sem));
            ps.setString(5, type);
            ps.setInt(6, Integer.parseInt(marks));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Course added successfully.");
            loadCourses();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateSelectedCourse() {
        int row = courseTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a course to update.");
            return;
        }

        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement("UPDATE courses SET coursename=?, Departmentcode=?, semoryear=?, coursetype=?, marks=? WHERE coursecode=?");
            ps.setString(1, courseNameField.getText());
            ps.setString(2, deptCombo.getSelectedItem().toString());
            ps.setInt(3, Integer.parseInt(semesterCombo.getSelectedItem().toString()));
            ps.setString(4, courseTypeField.getText());
            ps.setInt(5, Integer.parseInt(marksField.getText()));
            ps.setString(6, courseCodeField.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Updated successfully.");
            loadCourses();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteSelectedCourse() {
        int row = courseTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a course to delete.");
            return;
        }

        String code = tableModel.getValueAt(row, 0).toString();
        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement("DELETE FROM courses WHERE coursecode=?");
            ps.setString(1, code);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Deleted successfully.");
            loadCourses();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterCourses() {
        String keyword = searchField.getText().toLowerCase();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        courseTable.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword));
    }

    private void loadCoursesWithFade() {
        Timer timer = new Timer(20, null);
        final float[] alpha = {0};
        timer.addActionListener(e -> {
            alpha[0] += 0.05f;
            if (alpha[0] >= 1f) {
                alpha[0] = 1f;
                ((Timer) e.getSource()).stop();
            }
            setOpaque(true);
            repaint();
        });
        timer.start();
        loadCourses();
    }

    private void loadCourses() {
        tableModel.setRowCount(0);
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT * FROM courses");
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("coursecode"),
                        rs.getString("coursename"),
                        rs.getString("Departmentcode"),
                        rs.getInt("semoryear"),
                        rs.getString("coursetype"),
                        rs.getInt("marks")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
