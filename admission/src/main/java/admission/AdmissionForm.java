package admission;

import common.Conn;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.LinkedHashMap;

public class AdmissionForm extends JFrame {
    private JTextField firstNameField, lastNameField, emailField, phoneField, birthDateField;
    private JTextField fatherNameField, fatherOccField, motherNameField, motherOccField;
    private JTextField presentAddressField, permanentAddressField;
    private JComboBox<String> genderCombo, departmentCombo, semesterCombo, admissionTypeCombo;

    private JButton uploadProfileBtn, uploadNIDBtn, submitBtn;
    private JLabel profilePicPreview, nidDocPreview;
    private byte[] profilePicBytes = null, nidBytes = null;

    private JTable educationTable;

    public AdmissionForm() {
        setTitle("Student Admission Form");
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 🌈 Stylish Gradient Background Panel
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); // Always call super first
                Graphics2D g2d = (Graphics2D) g;
                Color color = Color.WHITE;
                g2d.setPaint(color);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        backgroundPanel.setLayout(new BorderLayout());
        add(backgroundPanel);

        JPanel formPanel = new JPanel(new GridBagLayout()) {
            {
                setOpaque(false); // Transparent over gradient
            }
        };
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        backgroundPanel.add(scrollPane, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Initialize fields
        genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        departmentCombo = new JComboBox<>(getDepartmentCodes());
        semesterCombo = new JComboBox<>(new String[]{"1", "2", "3", "4", "5", "6", "7", "8"});
        admissionTypeCombo = new JComboBox<>(new String[]{"Regular", "Transfer", "Migration"});

        firstNameField = new JTextField(15);
        lastNameField = new JTextField(15);
        emailField = new JTextField(15);
        phoneField = new JTextField(15);
        birthDateField = new JTextField("YYYY-MM-DD", 15);

        presentAddressField = new JTextField(15);
        permanentAddressField = new JTextField(15);

        fatherNameField = new JTextField(15);
        fatherOccField = new JTextField(15);
        motherNameField = new JTextField(15);
        motherOccField = new JTextField(15);

        profilePicPreview = new JLabel("No Photo", SwingConstants.CENTER);
        profilePicPreview.setPreferredSize(new Dimension(120, 140));
        profilePicPreview.setBorder(new LineBorder(Color.GRAY));
        uploadProfileBtn = new JButton("Upload Profile Picture");

        nidDocPreview = new JLabel("No Doc", SwingConstants.CENTER);
        nidDocPreview.setPreferredSize(new Dimension(120, 140));
        nidDocPreview.setBorder(new LineBorder(Color.GRAY));
        uploadNIDBtn = new JButton("Upload NID/BirthDoc");

        uploadProfileBtn.addActionListener(e -> {
            profilePicBytes = chooseImageFile(profilePicPreview);
        });

        uploadNIDBtn.addActionListener(e -> {
            nidBytes = chooseImageFile(nidDocPreview);
        });

        // Education table (SSC, HSC)
        String[] cols = {"Exam", "Board", "Year", "GPA"};
        Object[][] data = {
                {"SSC", "", "", ""},
                {"HSC", "", "", ""}
        };
        educationTable = new JTable(data, cols);
        //JScrollPane eduScroll = new JScrollPane(educationTable);
       // eduScroll.setPreferredSize(new Dimension(350, 60));

        // 🔄 Add hover effects to buttons
        Color btnColor = new Color(44, 62, 80);
        Color hoverColor = new Color(52, 73, 94);
        UIManager.put("Button.foreground", Color.WHITE);

        submitBtn = new JButton("Submit Admission");
        submitBtn.setBackground(btnColor);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorder(new LineBorder(Color.WHITE));
        submitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        submitBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                submitBtn.setBackground(hoverColor);
            }

            public void mouseExited(MouseEvent e) {
                submitBtn.setBackground(btnColor);
            }
        });
        submitBtn.addActionListener(e -> submitForm());

        // ➕ Add hover to upload buttons
        setHoverEffect(uploadProfileBtn);
        setHoverEffect(uploadNIDBtn);

        int y = 0;
        addRow(formPanel, gbc, y++, "Department:", departmentCombo, "Semester:", semesterCombo);
        addRow(formPanel, gbc, y++, "Admission Type:", admissionTypeCombo, "", null);
        addRow(formPanel, gbc, y++, "First Name:", firstNameField, "Last Name:", lastNameField);
        addRow(formPanel, gbc, y++, "Email:", emailField, "Phone:", phoneField);
        addRow(formPanel, gbc, y++, "Birth Date:", birthDateField, "Gender:", genderCombo);
        addRow(formPanel, gbc, y++, "Present Address:", presentAddressField, "Permanent Address:", permanentAddressField);
        addRow(formPanel, gbc, y++, "Father's Name:", fatherNameField, "Occupation:", fatherOccField);
        addRow(formPanel, gbc, y++, "Mother's Name:", motherNameField, "Occupation:", motherOccField);
        JScrollPane eduScroll = new JScrollPane(educationTable);
        eduScroll.setPreferredSize(new Dimension(350, 60));
        addRow(formPanel, gbc, y++, "Education Info (SSC & HSC):", eduScroll, "", null);
        addRow(formPanel, gbc, y++, "Profile Photo:", profilePicPreview, "", uploadProfileBtn);
        addRow(formPanel, gbc, y++, "NID/Birth Doc:", nidDocPreview, "", uploadNIDBtn);

        gbc.gridx = 0; gbc.gridy = y;
        gbc.gridwidth = 2;
        formPanel.add(submitBtn, gbc);
    }

    private void setHoverEffect(JButton button) {
        button.setBackground(Color.WHITE); // Default background white
        button.setForeground(Color.BLACK); // Text color black for contrast
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new LineBorder(Color.LIGHT_GRAY)); // Optional subtle border

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(230, 230, 230)); // Slight gray on hover
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE); // Back to white
            }
        });
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int y, String label1, Component comp1, String label2, Component comp2) {
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1;
        if (label1 != null && !label1.isEmpty()) panel.add(new JLabel(label1), gbc);
        gbc.gridx = 1;
        if (comp1 != null) panel.add(comp1, gbc);
        if (label2 != null && comp2 != null) {
            gbc.gridx = 2;
            panel.add(new JLabel(label2), gbc);
            gbc.gridx = 3;
            panel.add(comp2, gbc);
        }
    }

    private byte[] chooseImageFile(JLabel previewLabel) {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] imgBytes = fis.readAllBytes();
                ImageIcon icon = new ImageIcon(imgBytes);
                Image scaled = icon.getImage().getScaledInstance(120, 140, Image.SCALE_SMOOTH);
                previewLabel.setIcon(new ImageIcon(scaled));
                previewLabel.setText("");
                return imgBytes;
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading image.");
            }
        }
        return null;
    }

    private void submitForm() {
        try {
            String eduJson = generateEducationJSON();
            Conn c = new Conn();
            String sql = "INSERT INTO admission_requests (Departmentcode, semester, admission_type, first_name, last_name, email, phone, gender, birthdate, present_address, permanent_address, father_name, father_occupation, mother_name, mother_occupation, education_json, profile_pic, nid_or_birthdoc) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = c.c.prepareStatement(sql);
            ps.setString(1, departmentCombo.getSelectedItem().toString());
            ps.setInt(2, Integer.parseInt(semesterCombo.getSelectedItem().toString()));
            ps.setString(3, admissionTypeCombo.getSelectedItem().toString());
            ps.setString(4, firstNameField.getText());
            ps.setString(5, lastNameField.getText());
            ps.setString(6, emailField.getText());
            ps.setString(7, phoneField.getText());
            ps.setString(8, genderCombo.getSelectedItem().toString());
            ps.setDate(9, Date.valueOf(birthDateField.getText()));
            ps.setString(10, presentAddressField.getText());
            ps.setString(11, permanentAddressField.getText());
            ps.setString(12, fatherNameField.getText());
            ps.setString(13, fatherOccField.getText());
            ps.setString(14, motherNameField.getText());
            ps.setString(15, motherOccField.getText());
            ps.setString(16, eduJson);
            ps.setBytes(17, profilePicBytes);
            ps.setBytes(18, nidBytes);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Admission request submitted successfully!");
            dispose();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: Check all fields are filled properly.");
        }
    }

    private String generateEducationJSON() {
        LinkedHashMap<String, LinkedHashMap<String, String>> map = new LinkedHashMap<>();
        for (int i = 0; i < educationTable.getRowCount(); i++) {
            String exam = educationTable.getValueAt(i, 0).toString();
            LinkedHashMap<String, String> record = new LinkedHashMap<>();
            record.put("Board", educationTable.getValueAt(i, 1).toString());
            record.put("Year", educationTable.getValueAt(i, 2).toString());
            record.put("GPA", educationTable.getValueAt(i, 3).toString());
            map.put(exam, record);
        }
        return new org.json.JSONObject(map).toString();
    }

    private String[] getDepartmentCodes() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT DISTINCT Departmentcode FROM departments");
            java.util.List<String> list = new java.util.ArrayList<>();
            while (rs.next()) list.add(rs.getString(1));
            return list.toArray(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return new String[]{"N/A"};
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdmissionForm().setVisible(true));
    }
}
