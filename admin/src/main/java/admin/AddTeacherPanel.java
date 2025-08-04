package admin;

import common.Conn;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.Vector;

public class AddTeacherPanel extends JPanel {
    private JTextField nameField, emailField, phoneField, addressField, userIdField, joinDateField;
    private JPasswordField passwordField;
    private JComboBox<String> deptCombo, genderCombo;
    private JLabel profilePicLabel;
    private JButton uploadBtn;
    private byte[] profileImageBytes = null;
    private JTable teacherTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    private JList<String> courseList;
    private DefaultListModel<String> courseListModel;
    private JTextField semesterField;

    private final Color primaryColor = new Color(85, 150, 220);
    private final Color secondaryColor = new Color(230, 240, 250);
    private final Color accentColor = new Color(70, 130, 180);

    public AddTeacherPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(secondaryColor);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel mainCard = new RoundedPanel(20, Color.WHITE);
        mainCard.setLayout(new BorderLayout(20, 20));
        mainCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(mainCard, BorderLayout.CENTER);

        JPanel formPanel = new RoundedPanel(15, secondaryColor);
        formPanel.setLayout(new GridBagLayout());
        JScrollPane formScroll = new JScrollPane(formPanel);
        formScroll.setPreferredSize(new Dimension(480, 0));
        formScroll.setBorder(null);
        mainCard.add(formScroll, BorderLayout.WEST);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        nameField = new JTextField(15);
        emailField = new JTextField(15);
        phoneField = new JTextField(15);
        addressField = new JTextField(15);
        userIdField = new JTextField(15);
        joinDateField = new JTextField(15);
        passwordField = new JPasswordField(15);
        genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        deptCombo = new JComboBox<>(getDepartmentCodes());

        decorateField(nameField);
        decorateField(emailField);
        decorateField(phoneField);
        decorateField(addressField);
        decorateField(userIdField);
        decorateField(joinDateField);
        decorateField(passwordField);
        decorateComboBox(deptCombo);
        decorateComboBox(genderCombo);

        int y = 0;
        addFormRow(formPanel, gbc, y++, "Name:", nameField);
        addFormRow(formPanel, gbc, y++, "Gender:", genderCombo);
        addFormRow(formPanel, gbc, y++, "Email:", emailField);
        addFormRow(formPanel, gbc, y++, "Phone:", phoneField);
        addFormRow(formPanel, gbc, y++, "Address:", addressField);
        addFormRow(formPanel, gbc, y++, "Department:", deptCombo);
        addFormRow(formPanel, gbc, y++, "Join Date (YYYY-MM-DD):", joinDateField);
        addFormRow(formPanel, gbc, y++, "User ID:", userIdField);
      //  addFormRow(formPanel, gbc, y++, "Password:", passwordField);

        semesterField = new JTextField(15);
        decorateField(semesterField);
        addFormRow(formPanel, gbc, y++, "Semester/Year:", semesterField);

        courseListModel = new DefaultListModel<>();
        courseList = new JList<>(courseListModel);
        courseList.setVisibleRowCount(5);
        courseList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        courseList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane courseScroll = new JScrollPane(courseList);
        courseScroll.setPreferredSize(new Dimension(200, 80));
        loadCoursesIntoList();  // helper method to fetch courses
        addFormRow(formPanel, gbc, y++, "Courses:", courseScroll);

        profilePicLabel = new JLabel();
        profilePicLabel.setPreferredSize(new Dimension(140, 160));
        profilePicLabel.setOpaque(true);
        profilePicLabel.setBackground(Color.LIGHT_GRAY);
        profilePicLabel.setBorder(new LineBorder(accentColor, 2, true));
        profilePicLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        formPanel.add(profilePicLabel, gbc);

        uploadBtn = new JButton("Upload Profile Pic");
        decorateButton(uploadBtn);
        uploadBtn.addActionListener(e -> uploadProfileImage());
        gbc.gridy++;
        formPanel.add(uploadBtn, gbc);

        JButton addBtn = new JButton("Add Teacher");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");

        decorateButton(addBtn);
        decorateButton(updateBtn);
        decorateButton(deleteBtn);

        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        formPanel.add(addBtn, gbc);
        gbc.gridx = 1;
        formPanel.add(updateBtn, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(deleteBtn, gbc);

        // Table Section
        tableModel = new DefaultTableModel(new String[]{
                "ID", "Name", "Gender", "Email", "Phone", "Address", "Department", "Join Date", "UserID", "Courses"
        }, 0);


        teacherTable = new JTable(tableModel);
        teacherTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // 👈 Required for horizontal scrolling
        teacherTable.setRowHeight(28);
        teacherTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        teacherTable.getTableHeader().setBackground(accentColor);
        teacherTable.getTableHeader().setForeground(Color.WHITE);
        teacherTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JScrollPane tableScroll = new JScrollPane(teacherTable);
        tableScroll.setBorder(new LineBorder(accentColor, 2, true));
        tableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel rightPanel = new RoundedPanel(15, secondaryColor);
        rightPanel.setLayout(new BorderLayout(10, 10));

        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setOpaque(false);
        searchField = new JTextField();
        decorateField(searchField);
        JButton searchBtn = new JButton("Search");
        decorateButton(searchBtn);
        searchBtn.addActionListener(e -> searchTeacher());

        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);

        rightPanel.add(searchPanel, BorderLayout.NORTH);
        rightPanel.add(tableScroll, BorderLayout.CENTER);
        mainCard.add(rightPanel, BorderLayout.CENTER);

        addBtn.addActionListener(e -> insertTeacher());
        updateBtn.addActionListener(e -> updateTeacher());
        deleteBtn.addActionListener(e -> deleteTeacher());
        teacherTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = teacherTable.getSelectedRow();
                if (row != -1) {
                    nameField.setText(tableModel.getValueAt(row, 0).toString());
                    genderCombo.setSelectedItem(tableModel.getValueAt(row, 1).toString());
                    emailField.setText(tableModel.getValueAt(row, 2).toString());
                    phoneField.setText(tableModel.getValueAt(row, 3).toString());
                    deptCombo.setSelectedItem(tableModel.getValueAt(row, 4).toString());
                    joinDateField.setText(tableModel.getValueAt(row, 5).toString());
                    userIdField.setText(tableModel.getValueAt(row,6).toString());
                }
            }
        });teacherTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = teacherTable.getSelectedRow();
                if (row != -1) {
                    userIdField.setText(tableModel.getValueAt(row, 0).toString()); // teacher_id
                    nameField.setText(tableModel.getValueAt(row, 1).toString());
                    genderCombo.setSelectedItem(tableModel.getValueAt(row, 2).toString());
                    emailField.setText(tableModel.getValueAt(row, 3).toString());
                    phoneField.setText(tableModel.getValueAt(row, 4).toString());
                    addressField.setText(tableModel.getValueAt(row, 5).toString());
                    deptCombo.setSelectedItem(tableModel.getValueAt(row, 6).toString());
                    joinDateField.setText(tableModel.getValueAt(row, 7).toString());
                }
            }
        });


        loadTeachers();
    }
    private void loadCoursesIntoList() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT DISTINCT coursecode FROM courses");
            while (rs.next()) {
                courseListModel.addElement(rs.getString("coursecode"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void insertTeacher() {
        try {
            Conn c = new Conn();

            // Insert into teachers table
            String sql = "INSERT INTO teachers (teacherName, gender, emailid, contactnumber, address, Departmentcode, joineddate, teacher_id,  profilepic) VALUES (?,  ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = c.c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, nameField.getText());
            ps.setString(2, genderCombo.getSelectedItem().toString());
            ps.setString(3, emailField.getText());
            ps.setString(4, phoneField.getText());
            ps.setString(5, addressField.getText());
            ps.setString(6, deptCombo.getSelectedItem().toString());
            ps.setString(7, joinDateField.getText());
            ps.setString(8, userIdField.getText());
           // ps.setString(9, new String(passwordField.getPassword()));
            if (profileImageBytes != null) {
                ps.setBytes(9, profileImageBytes);
            } else {
                ps.setNull(9, Types.BLOB);
            }

            ps.executeUpdate();

            // Get inserted teacher ID
            ResultSet rs = ps.getGeneratedKeys();
            int teacherId = -1;
            if (rs.next()) {
                teacherId = rs.getInt(1);
            }

            // Insert into teacher_course_map
            if (teacherId != -1) {
                String insertCourseSQL = "INSERT INTO teacher_course_map (teacher_id, coursecode, department_code, semoryear) VALUES (?, ?, ?, ?)";
                PreparedStatement psCourse = c.c.prepareStatement(insertCourseSQL);
                for (String courseCode : courseList.getSelectedValuesList()) {
                    psCourse.setInt(1, teacherId);
                    psCourse.setString(2, courseCode);
                    psCourse.setString(3, deptCombo.getSelectedItem().toString());
                    psCourse.setInt(4, Integer.parseInt(semesterField.getText().trim()));
                    psCourse.addBatch();
                }
                psCourse.executeBatch();
            }

            JOptionPane.showMessageDialog(this, "Teacher added successfully.");
            clearForm();
            loadTeachers();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding teacher.");
        }
    }


    private void updateTeacher() {
        int row = teacherTable.getSelectedRow();
        if (row == -1) return;
        try {
            Conn c = new Conn();
            String sql = "UPDATE teachers SET gender=?, emailid=?, contactnumber=?, address=?, Departmentcode=?, joineddate=? WHERE teacherName=?";
            PreparedStatement ps = c.c.prepareStatement(sql);
            ps.setString(1, genderCombo.getSelectedItem().toString());
            ps.setString(2, emailField.getText());
            ps.setString(3, phoneField.getText());
            ps.setString(4, addressField.getText());
            ps.setString(5, deptCombo.getSelectedItem().toString());
            ps.setString(6, joinDateField.getText());
            ps.setString(7, nameField.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Teacher updated.");
            loadTeachers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteTeacher() {
        int row = teacherTable.getSelectedRow();
        if (row == -1) return;
        try {
            Conn c = new Conn();
            String sql = "DELETE FROM teachers WHERE teacherName=?";
            PreparedStatement ps = c.c.prepareStatement(sql);
            ps.setString(1, nameField.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Teacher deleted.");
            loadTeachers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTeachers() {
        tableModel.setRowCount(0);  // Clear existing data
        try {
            Conn c = new Conn();
            String teacherQuery = "SELECT * FROM teachers";
            ResultSet rs = c.s.executeQuery(teacherQuery);

            while (rs.next()) {
                int teacherId = rs.getInt("teacher_id");
                String name = rs.getString("teacherName");
                String gender = rs.getString("gender");
                String email = rs.getString("emailid");
                String phone = rs.getString("contactnumber");
                String address = rs.getString("address");
                String dept = rs.getString("Departmentcode");
                String joinDate = rs.getString("joineddate");
                String userId = rs.getString("teacher_id");

                // Fetch assigned courses
                StringBuilder assignedCourses = new StringBuilder();
                String courseQuery = "SELECT coursecode FROM teacher_course_map WHERE teacher_id = ?";
                PreparedStatement ps = c.c.prepareStatement(courseQuery);
                ps.setInt(1, teacherId);
                ResultSet crs = ps.executeQuery();
                while (crs.next()) {
                    assignedCourses.append(crs.getString("coursecode")).append(", ");
                }
                String courseText = assignedCourses.length() > 0
                        ? assignedCourses.substring(0, assignedCourses.length() - 2)
                        : "";

                // Add to table
                tableModel.addRow(new Object[]{
                        teacherId, name, gender, email, phone, address, dept, joinDate, userId, courseText
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void searchTeacher() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        teacherTable.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchField.getText()));
    }

    private void uploadProfileImage() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (FileInputStream fis = new FileInputStream(file);
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buf = new byte[1024];
                int read;
                while ((read = fis.read(buf)) != -1) bos.write(buf, 0, read);
                profileImageBytes = bos.toByteArray();
                ImageIcon icon = new ImageIcon(profileImageBytes);
                Image img = icon.getImage().getScaledInstance(profilePicLabel.getWidth(), profilePicLabel.getHeight(), Image.SCALE_SMOOTH);
                profilePicLabel.setIcon(new ImageIcon(img));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void clearForm() {
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        addressField.setText("");
        userIdField.setText("");
        passwordField.setText("");
        joinDateField.setText("");
        semesterField.setText("");
        courseList.clearSelection();
        profilePicLabel.setIcon(null);
        profileImageBytes = null;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int y, String labelText, Component field) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(lbl, gbc);
        gbc.gridx = 1;
        if (field instanceof JComponent) ((JComponent) field).setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(field, gbc);
    }

    private void decorateField(JTextField field) {
        field.setBorder(new CompoundBorder(new LineBorder(accentColor, 1, true), new EmptyBorder(5, 8, 5, 8)));
        field.setBackground(Color.WHITE);
        field.setForeground(Color.DARK_GRAY);
        field.setCaretColor(accentColor);
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
    }

    private String[] getDepartmentCodes() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT DISTINCT Departmentcode FROM departments");
            Vector<String> codes = new Vector<>();
            while (rs.next()) codes.add(rs.getString(1));
            return codes.toArray(new String[0]);
        } catch (Exception e) {
            return new String[]{"Unknown"};
        }
    }

    static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;

        public RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
