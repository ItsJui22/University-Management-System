package admin;

import common.Conn;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.Vector;

public class AddStudentPanel extends JPanel implements RollGenerationListener {
    private JComboBox<String> deptCombo, semesterCombo, genderCombo;
    private JTextField rollNumberField, optionalCourseField, firstNameField, lastNameField,
            emailField, contactField, birthDateField, addressField,
            fatherNameField, fatherOccField, motherNameField, motherOccField,
            userIdField, admissionDateField,registrationField;
    private JPasswordField passwordField;
    private JLabel profilePicLabel;
    private JButton uploadPicBtn;
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private byte[] profileImageBytes ;
    private JTextField searchField;

    private final Color primaryColor = new Color(85, 150, 220);
    private final Color secondaryColor = new Color(230, 240, 250);
    private final Color accentColor = new Color(70, 130, 180);
    private final Font headerFont = new Font("Segoe UI", Font.BOLD, 14);
    private final Font tableFont = new Font("Segoe UI", Font.PLAIN, 13);



    public AddStudentPanel() {
        setBackground(secondaryColor);
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));
        setOpaque(true);

        JPanel mainCard = new RoundedPanel(20, Color.WHITE);
        mainCard.setLayout(new BorderLayout(20, 20));
        mainCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(mainCard, BorderLayout.CENTER);

        JPanel formPanel = new RoundedPanel(15, secondaryColor);
        formPanel.setLayout(new GridBagLayout());
        formPanel.setOpaque(true);
        JScrollPane formScroll = new JScrollPane(formPanel);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        formScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.setPreferredSize(new Dimension(500, 0));
        mainCard.add(formScroll, BorderLayout.WEST);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        profilePicLabel = new JLabel();
        profilePicLabel.setPreferredSize(new Dimension(150, 150));
        profilePicLabel.setMinimumSize(new Dimension(150, 150));
        profilePicLabel.setMaximumSize(new Dimension(150, 150));
        profilePicLabel.setSize(new Dimension(150, 150));
        profilePicLabel.setBorder(new LineBorder(accentColor, 2, true));
        profilePicLabel.setHorizontalAlignment(SwingConstants.CENTER);
        profilePicLabel.setOpaque(true);
        profilePicLabel.setBackground(secondaryColor);

        uploadPicBtn = new JButton("Upload Profile Pic");
        decorateButton(uploadPicBtn);
        uploadPicBtn.addActionListener(e -> uploadProfileImage());

        // Add profile pic and upload button together in a small panel
        JPanel picPanel = new JPanel(new BorderLayout(5, 5));
        picPanel.setOpaque(false);
        picPanel.add(profilePicLabel, BorderLayout.CENTER);
        picPanel.add(uploadPicBtn, BorderLayout.SOUTH);

        // Add this picPanel to formPanel
        gbc.gridx = 0;
        int y = 0;
        gbc.gridy = y++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(picPanel, gbc);
        // Initialize all components
        deptCombo = new JComboBox<>(getDepartmentCodes());
        semesterCombo = new JComboBox<>(new String[]{"1", "2", "3", "4", "5", "6", "7", "8"});
        genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});

        decorateComboBox(deptCombo);
        decorateComboBox(semesterCombo);
        decorateComboBox(genderCombo);

        rollNumberField = new JTextField(15);
        decorateTextField(rollNumberField);

        JPanel rollPanel = new JPanel(new BorderLayout(5, 0));
        rollPanel.setOpaque(false);
        rollPanel.add(rollNumberField, BorderLayout.CENTER);

        JButton generateRollBtn = new JButton("🎲");
        generateRollBtn.setToolTipText("Generate Roll");
        generateRollBtn.setFocusPainted(false);
        generateRollBtn.setMargin(new Insets(2, 8, 2, 8));
        generateRollBtn.setBackground(primaryColor);
        generateRollBtn.setForeground(Color.WHITE);
        generateRollBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        generateRollBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rollPanel.add(generateRollBtn, BorderLayout.EAST);

        // 👉 This will open the RollGeneratorPanel in a popup and return selected roll
        generateRollBtn.addActionListener(e -> {
            JDialog dialog = new JDialog();
            dialog.setTitle("Generate Roll");
            dialog.setModal(true);
            dialog.getContentPane().add(new RollGeneratorPanel(this)); // this = current AddStudentPanel
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        });

        registrationField = new JTextField(15);
        decorateTextField(registrationField);
        registrationField.setEditable(false); // Prevent manual editing

        JPanel regPanel = new JPanel(new BorderLayout(5, 0));
        regPanel.setOpaque(false);
        regPanel.add(registrationField, BorderLayout.CENTER);

        JButton generateRegBtn = new JButton("🎯");
        generateRegBtn.setToolTipText("Generate Registration Number");
        generateRegBtn.setFocusPainted(false);
        generateRegBtn.setMargin(new Insets(2, 8, 2, 8));
        generateRegBtn.setBackground(primaryColor);
        generateRegBtn.setForeground(Color.WHITE);
        generateRegBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        generateRegBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        regPanel.add(generateRegBtn, BorderLayout.EAST);

// Register listener to auto-generate registration number
        generateRegBtn.addActionListener(e -> {
            int generatedReg = generateRegistrationNumber();
            registrationField.setText(String.valueOf(generatedReg));
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        // JButton viewProfileBtn = new JButton("👁 View Full Profile");
        //viewProfileBtn.setBackground(new Color(0, 123, 255));
        //viewProfileBtn.setForeground(Color.WHITE);
        //viewProfileBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

       /* viewProfileBtn.addActionListener(e -> {
            int row = studentTable.getSelectedRow();
            if (row >= 0) {
                int modelRow = studentTable.convertRowIndexToModel(row);
                try {
                    long roll = Long.parseLong(tableModel.getValueAt(modelRow, 0).toString());
                    ViewStudentProfileDialog dialog = new ViewStudentProfileDialog(SwingUtilities.getWindowAncestor(this), roll);
                    dialog.setVisible(true);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid roll number format.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a student to view profile.");
            }
        });
*/
        JPanel contentPanel = new JPanel(); // ✅ now it's not null
        // contentPanel.add(button);

        optionalCourseField = new JTextField(15);
        firstNameField = new JTextField(15);
        lastNameField = new JTextField(15);
        emailField = new JTextField(15);
        contactField = new JTextField(15);
        birthDateField = new JTextField(15);
        addressField = new JTextField(15);
        fatherNameField = new JTextField(15);
        fatherOccField = new JTextField(15);
        motherNameField = new JTextField(15);
        motherOccField = new JTextField(15);
        userIdField = new JTextField(15);
        admissionDateField = new JTextField(15);
        // passwordField = new JPasswordField(15);

        decorateTextField(optionalCourseField);
        decorateTextField(firstNameField);
        decorateTextField(lastNameField);
        decorateTextField(emailField);
        decorateTextField(contactField);
        decorateTextField(birthDateField);
        decorateTextField(addressField);
        decorateTextField(fatherNameField);
        decorateTextField(fatherOccField);
        decorateTextField(motherNameField);
        decorateTextField(motherOccField);
        decorateTextField(userIdField);
        decorateTextField(admissionDateField);
        // decorateTextField(passwordField);

        profilePicLabel = new JLabel();
        profilePicLabel.setPreferredSize(new Dimension(140, 160));
        profilePicLabel.setBorder(new LineBorder(accentColor, 2, true));
        profilePicLabel.setHorizontalAlignment(SwingConstants.CENTER);
        profilePicLabel.setOpaque(true);
        profilePicLabel.setBackground(secondaryColor);

        uploadPicBtn = new JButton("Upload Profile Pic");
        decorateButton(uploadPicBtn);
        uploadPicBtn.addActionListener(e -> uploadProfileImage());

        y = 0;
        addFormRow(formPanel, gbc, y++, "Department:", deptCombo);
        addFormRow(formPanel, gbc, y++, "Semester:", semesterCombo);
        addFormRow(formPanel, gbc, y++, "Registration No:", regPanel);

        addFormRow(formPanel, gbc, y++, "Roll Number:", rollPanel);
        addFormRow(formPanel, gbc, y++, "Optional Course:", optionalCourseField);
        addFormRow(formPanel, gbc, y++, "First Name:", firstNameField);
        addFormRow(formPanel, gbc, y++, "Last Name:", lastNameField);
        addFormRow(formPanel, gbc, y++, "Email:", emailField);
        addFormRow(formPanel, gbc, y++, "Contact Number:", contactField);
        addFormRow(formPanel, gbc, y++, "Date of Birth:", birthDateField);
        addFormRow(formPanel, gbc, y++, "Gender:", genderCombo);
        addFormRow(formPanel, gbc, y++, "Address:", addressField);
        addFormRow(formPanel, gbc, y++, "Father's Name:", fatherNameField);
        addFormRow(formPanel, gbc, y++, "Father's Occupation:", fatherOccField);
        addFormRow(formPanel, gbc, y++, "Mother's Name:", motherNameField);
        addFormRow(formPanel, gbc, y++, "Mother's Occupation:", motherOccField);
        addFormRow(formPanel, gbc, y++, "User ID:", userIdField);
        //addFormRow(formPanel, gbc, y++, "Password:", passwordField);
        addFormRow(formPanel, gbc, y++, "Admission Date:", admissionDateField);

        // Existing buttons
        // Existing Buttons
        JButton addBtn = new JButton("Add Student");
        JButton updateBtn = new JButton("Update Student");
        JButton deleteBtn = new JButton("Delete Student");
        decorateButton(addBtn);
        decorateButton(updateBtn);
        decorateButton(deleteBtn);

// 🔵 New: View Profile Button
        JButton viewBtn = new JButton("👁 View Profile");
        decorateButton(viewBtn);
        viewBtn.setBackground(new Color(0, 123, 255));
        viewBtn.setForeground(Color.WHITE);
        viewBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        viewBtn.addActionListener(e -> {
            int row = studentTable.getSelectedRow();
            if (row >= 0) {
                int modelRow = studentTable.convertRowIndexToModel(row);
                long roll = Long.parseLong(tableModel.getValueAt(modelRow, 0).toString());
                Window parentWindow = SwingUtilities.getWindowAncestor(formPanel);
                new ViewStudentProfileDialog(parentWindow, roll).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "Please select a student first.");
            }
        });

        gbc.gridx = 1;
        formPanel.add(viewBtn, gbc);


// Add Buttons to Form Panel
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        formPanel.add(addBtn, gbc);

        gbc.gridx = 1;
        formPanel.add(updateBtn, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(deleteBtn, gbc);

// 🔵 Add View Profile Button below Delete
        gbc.gridx = 1;
        formPanel.add(viewBtn, gbc);

// Add Listeners for CRUD buttons (if not already added)
        addBtn.addActionListener(e -> insertStudent());
        updateBtn.addActionListener(e -> updateSelectedStudent());
        deleteBtn.addActionListener(e -> deleteSelectedStudent());

        //   mainCard.add(formPanel, BorderLayout.WEST);
// 🔁 Optionally set scroll policy for smoother user experience
        SwingUtilities.invokeLater(() -> {
            formScroll.getVerticalScrollBar().setValue(0);
        });

// --- Table Section (Right Side) ---
        tableModel = new DefaultTableModel();
        studentTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    comp.setBackground(row % 2 == 0 ? new Color(245, 250, 255) : Color.WHITE);
                    comp.setForeground(Color.BLACK);
                } else {
                    comp.setBackground(primaryColor);
                    comp.setForeground(Color.WHITE);
                }
                return comp;
            }
        };


// Column headers
        String[] columns = {
                "Roll No", "Registration No", "First Name", "Last Name", "Dept", "Sem",
                "Email", "Contact", "DOB", "Gender", "Address"
        };
        tableModel.setColumnIdentifiers(columns);

        studentTable.setFont(tableFont);
        studentTable.setRowHeight(28);
        studentTable.setSelectionBackground(primaryColor);
        studentTable.setSelectionForeground(Color.WHITE);
        studentTable.getTableHeader().setFont(headerFont);
        studentTable.getTableHeader().setBackground(accentColor);
        studentTable.getTableHeader().setForeground(Color.WHITE);
        studentTable.setAutoCreateRowSorter(true);

        JScrollPane tableScrollPane = new JScrollPane(studentTable);
        tableScrollPane.setBorder(new CompoundBorder(
                new LineBorder(accentColor, 2, true),
                new EmptyBorder(10, 10, 10, 10)));

        JPanel rightPanel = new RoundedPanel(15, secondaryColor);
        rightPanel.setLayout(new BorderLayout(15, 15));
        rightPanel.setOpaque(true);

// Search bar
        searchField = new JTextField();
        decorateTextField(searchField);
        JButton searchBtn = new JButton("Search");
        decorateButton(searchBtn);
        searchBtn.addActionListener(e -> filterStudents());

        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setOpaque(false);
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(headerFont);
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);

        rightPanel.add(searchPanel, BorderLayout.NORTH);
        rightPanel.add(tableScrollPane, BorderLayout.CENTER);

// 👉 Add this right side panel to mainCard
        mainCard.add(rightPanel, BorderLayout.CENTER);

        // mainCard.add(rightPanel, BorderLayout.CENTER);

        loadStudents();

        // Table click loads data into form
        studentTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = studentTable.getSelectedRow();
                if (row != -1) {
                    int modelRow = studentTable.convertRowIndexToModel(row);

                    Object rollObj = tableModel.getValueAt(modelRow, 0);
                    Object regObj = tableModel.getValueAt(modelRow, 1);
                    Object fnameObj = tableModel.getValueAt(modelRow, 2);
                    Object lnameObj = tableModel.getValueAt(modelRow, 3);
                    Object deptObj = tableModel.getValueAt(modelRow, 4);
                    Object semObj = tableModel.getValueAt(modelRow, 5);
                    Object emailObj = tableModel.getValueAt(modelRow, 6);
                    Object contactObj = tableModel.getValueAt(modelRow, 7);
                    Object birthObj = tableModel.getValueAt(modelRow, 8);
                    Object genderObj = tableModel.getValueAt(modelRow, 9);
                    Object addressObj = tableModel.getValueAt(modelRow, 10);

                    rollNumberField.setText(rollObj != null ? rollObj.toString() : "");
                    registrationField.setText(regObj != null ? regObj.toString() : "");
                    firstNameField.setText(fnameObj != null ? fnameObj.toString() : "");
                    lastNameField.setText(lnameObj != null ? lnameObj.toString() : "");
                    deptCombo.setSelectedItem(deptObj != null ? deptObj.toString() : "");
                    semesterCombo.setSelectedItem(semObj != null ? semObj.toString() : "");
                    emailField.setText(emailObj != null ? emailObj.toString() : "");
                    contactField.setText(contactObj != null ? contactObj.toString() : "");
                    birthDateField.setText(birthObj != null ? birthObj.toString() : "");
                    genderCombo.setSelectedItem(genderObj != null ? genderObj.toString() : "");
                    addressField.setText(addressObj != null ? addressObj.toString() : "");

                    if (rollObj != null) {
                        try {
                            long registration = Long.parseLong(rollObj.toString());
                            loadStudentDetailsByRegistration(String.valueOf(registration));
                        } catch (NumberFormatException ex) {
                            System.err.println("Invalid registration number: " + rollObj);
                        }
                    }

                }
            }
        });


        // Fade in animation on panel show
        Timer fadeTimer = new Timer(30, null);
        fadeTimer.addActionListener(new ActionListener() {
            float opacity = 0f;

            @Override
            public void actionPerformed(ActionEvent e) {
                opacity += 0.05f;
                if (opacity > 1f) opacity = 1f;
                setOpaque(false);
                mainCard.setOpaque(true);
                mainCard.repaint();
                mainCard.setBackground(new Color(1f, 1f, 1f, opacity));
                if (opacity >= 1f) fadeTimer.stop();
            }
        });
        fadeTimer.start();
    }

    private int generateRegistrationNumber() {
        int newReg = 1000; // ekhane 100 diye suru korbe
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT MAX(CAST(registration AS UNSIGNED)) FROM students");
            if (rs.next()) {
                newReg = rs.getInt(1) + 1;  // max registration number er porer number ta nibe
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newReg;
    }



    private void initComponents() {
    }

    private void reloadStudentTable() {
        // Fetch updated students from DB and repopulate the table
    }

    public void setGeneratedRoll(long roll) {
        rollNumberField.setText(String.valueOf(roll));
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int y, String labelText, Component field) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        if (field instanceof JComponent j) j.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(field, gbc);
    }



    private void decorateTextField(JTextField field) {
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
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    private String[] getDepartmentCodes() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT DISTINCT Departmentcode FROM departments");
            Vector<String> codes = new Vector<>();
            while (rs.next()) codes.add(rs.getString(1));
            return codes.toArray(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return new String[]{"NOT ASSIGNED"};
        }
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

                int labelWidth = profilePicLabel.getWidth();
                int labelHeight = profilePicLabel.getHeight();

                // If width or height is zero, set default size
                if (labelWidth <= 0) labelWidth = 150;
                if (labelHeight <= 0) labelHeight = 150;

                Image img = icon.getImage().getScaledInstance(labelWidth, labelHeight, Image.SCALE_SMOOTH);
                profilePicLabel.setIcon(new ImageIcon(img));
                profilePicLabel.revalidate();
                profilePicLabel.repaint();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }





    private boolean validateFields() {
        if (rollNumberField.getText().trim().isEmpty() ||
                firstNameField.getText().trim().isEmpty() ||
                lastNameField.getText().trim().isEmpty() ||
                userIdField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all mandatory fields (Roll No, First Name, Last Name, User ID).");
            return false;
        }
        return true;
    }

    private void insertStudent() {
        if (!validateFields()) return;

        try {
            Conn c = new Conn();
            String sql = "INSERT INTO students (Departmentcode, semoryear, rollnumber, registration, optionalcourse, firstname, lastname, emailid, contactnumber, dateofbirth, gender, address, fathername, fatheroccupation, mothername, motheroccupation, profilepic, lastlogin, userid, admissiondate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = c.c.prepareStatement(sql);

            ps.setString(1, deptCombo.getSelectedItem().toString());
            ps.setInt(2, Integer.parseInt(semesterCombo.getSelectedItem().toString()));
            ps.setLong(3, Long.parseLong(rollNumberField.getText()));
            ps.setString(4, registrationField.getText()); // new line
            ps.setString(5, optionalCourseField.getText().trim());
            ps.setString(6, firstNameField.getText().trim());
            ps.setString(7, lastNameField.getText().trim());
            ps.setString(8, emailField.getText().trim());
            ps.setString(9, contactField.getText().trim());
            ps.setString(10, birthDateField.getText().trim());
            ps.setString(11, genderCombo.getSelectedItem().toString());
            ps.setString(12, addressField.getText().trim());
            ps.setString(13, fatherNameField.getText().trim());
            ps.setString(14, fatherOccField.getText().trim());
            ps.setString(15, motherNameField.getText().trim());
            ps.setString(16, motherOccField.getText().trim());
            if (profileImageBytes != null) {
                ps.setBytes(17, profileImageBytes);
            } else {
                ps.setNull(17, Types.BLOB);
            }
            ps.setString(18, "");  // lastlogin can be empty at insert
            ps.setString(19, userIdField.getText().trim());
         //   ps.setString(19, new String(passwordField.getPassword()));
            ps.setString(20, admissionDateField.getText().trim());

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Student added successfully.");
            clearForm();
            loadStudents();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error inserting student.");
        }
    }

    private void updateSelectedStudent() {
        int row = studentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a student to update.");
            return;
        }
        if (!validateFields()) return;

        try {
            Conn c = new Conn();
            String sql = "UPDATE students SET Departmentcode=?, semoryear=?, rollnumber=?, optionalcourse=?, firstname=?, lastname=?, emailid=?, contactnumber=?, dateofbirth=?, gender=?, address=?, fathername=?, fatheroccupation=?, mothername=?, motheroccupation=?, profilepic=?, userid=?, admissiondate=? WHERE registration=?";
            PreparedStatement ps = c.c.prepareStatement(sql);

            ps.setString(1, deptCombo.getSelectedItem().toString());
            ps.setInt(2, Integer.parseInt(semesterCombo.getSelectedItem().toString()));
            ps.setLong(3, Long.parseLong(rollNumberField.getText()));
            ps.setString(4, optionalCourseField.getText().trim());
            ps.setString(5, firstNameField.getText().trim());
            ps.setString(6, lastNameField.getText().trim());
            ps.setString(7, emailField.getText().trim());
            ps.setString(8, contactField.getText().trim());
            ps.setString(9, birthDateField.getText().trim());
            ps.setString(10, genderCombo.getSelectedItem().toString());
            ps.setString(11, addressField.getText().trim());
            ps.setString(12, fatherNameField.getText().trim());
            ps.setString(13, fatherOccField.getText().trim());
            ps.setString(14, motherNameField.getText().trim());
            ps.setString(15, motherOccField.getText().trim());
            if (profileImageBytes != null) {
                ps.setBytes(16, profileImageBytes);
            } else {
                ps.setNull(16, Types.BLOB);
            }
            ps.setString(17, userIdField.getText().trim());
            ps.setString(18, admissionDateField.getText().trim());
            ps.setString(19, registrationField.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Student updated successfully.");
            clearForm();
            loadStudents();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating student.");
        }
    }

    private void deleteSelectedStudent() {
        int row = studentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a student to delete.");
            return;
        }

        try {
            Conn c = new Conn();
            String sql = "DELETE FROM students WHERE registration = ?";
            PreparedStatement ps = c.c.prepareStatement(sql);

            // get registration string directly, don't parse it as number
            String registrationId = tableModel.getValueAt(row, 0).toString();
            ps.setString(1, registrationId);

            int result = ps.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Student deleted successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "No matching student found.");
            }

            clearForm();
            loadStudents();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting student.");
        }
    }


    private void loadStudents() {
        tableModel.setRowCount(0);
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT rollnumber, registration, firstname, lastname, Departmentcode, semoryear, emailid, contactnumber, dateofbirth, gender, address FROM students");
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getLong("rollnumber"),
                        rs.getString("registration"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("Departmentcode"),
                        rs.getInt("semoryear"),
                        rs.getString("emailid"),
                        rs.getString("contactnumber"),
                        rs.getString("dateofbirth"),
                        rs.getString("gender"),
                        rs.getString("address")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterStudents() {
        String keyword = searchField.getText().toLowerCase();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        studentTable.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword));
    }

    private void clearForm() {
        rollNumberField.setText("");
        optionalCourseField.setText("");
        firstNameField.setText("");
        lastNameField.setText("");
        emailField.setText("");
        contactField.setText("");
        birthDateField.setText("");
        addressField.setText("");
        fatherNameField.setText("");
        fatherOccField.setText("");
        motherNameField.setText("");
        motherOccField.setText("");
        userIdField.setText("");
       // passwordField.setText("");
        admissionDateField.setText("");
        profilePicLabel.setIcon(null);
        profileImageBytes = null;
    }

    private void loadStudentDetailsByRegistration(String registration) {
        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement("SELECT * FROM students WHERE registration = ?");
            ps.setString(1, registration);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Fill all form fields:
                registrationField.setText(rs.getString("registration"));
                rollNumberField.setText(String.valueOf(rs.getLong("rollnumber")));
                optionalCourseField.setText(rs.getString("optionalcourse"));
                firstNameField.setText(rs.getString("firstname"));
                lastNameField.setText(rs.getString("lastname"));
                deptCombo.setSelectedItem(rs.getString("Departmentcode"));
                semesterCombo.setSelectedItem(String.valueOf(rs.getInt("semoryear")));
                emailField.setText(rs.getString("emailid"));
                contactField.setText(rs.getString("contactnumber"));
                birthDateField.setText(rs.getString("dateofbirth"));
                genderCombo.setSelectedItem(rs.getString("gender"));
                addressField.setText(rs.getString("address"));
                fatherNameField.setText(rs.getString("fathername"));
                fatherOccField.setText(rs.getString("fatheroccupation"));
                motherNameField.setText(rs.getString("mothername"));
                motherOccField.setText(rs.getString("motheroccupation"));
                userIdField.setText(rs.getString("userid"));
                admissionDateField.setText(rs.getString("admissiondate"));

                // Load profile pic if any
                byte[] imgBytes = rs.getBytes("profilepic");
                if (imgBytes != null && imgBytes.length > 0) {
                    profileImageBytes = imgBytes;
                    ImageIcon icon = new ImageIcon(imgBytes);
                    Image img = icon.getImage().getScaledInstance(profilePicLabel.getWidth(), profilePicLabel.getHeight(), Image.SCALE_SMOOTH);
                    profilePicLabel.setIcon(new ImageIcon(img));
                } else {
                    profileImageBytes = null;
                    profilePicLabel.setIcon(null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRollsGenerated() {
        // Reload student list from DB or refresh UI elements to show updated roll numbers
        JOptionPane.showMessageDialog(this, "Roll numbers have been updated. Refreshing student list...");
        loadStudentData();  // Assume you have a method to load/reload student data
    }

    private void loadStudentData() {
        loadStudents();
    }

    // ⬇ RoundedPanel class for card-style design ⬇
    static class RoundedPanel extends JPanel {
        private int cornerRadius;
        private Color backgroundColor;

        public RoundedPanel(int radius, Color bgColor) {
            this.cornerRadius = radius;
            this.backgroundColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Dimension arcs = new Dimension(cornerRadius, cornerRadius);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arcs.width, arcs.height);
        }
    }
}
