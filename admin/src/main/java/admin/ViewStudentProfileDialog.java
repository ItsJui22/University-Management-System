package admin;

import common.Conn;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;

public class ViewStudentProfileDialog extends JDialog {
    private long roll;
    private JLabel profilePicLabel;
    private JTextArea detailsArea;

    public ViewStudentProfileDialog(Window owner, long roll) {
        super(owner, "Student Profile - Roll: " + roll, ModalityType.APPLICATION_MODAL);
        this.roll = roll;

        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(620, 720));
        setResizable(false);

        // Gradient Header
        JPanel headerPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(58, 123, 213);
                Color color2 = new Color(0, 210, 255);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setPreferredSize(new Dimension(620, 80));
        JLabel title = new JLabel("Student Full Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 20));
        headerPanel.add(title);
        add(headerPanel, BorderLayout.NORTH);

        // Profile Picture
        profilePicLabel = new JLabel("No Image");
        profilePicLabel.setPreferredSize(new Dimension(160, 190));
        profilePicLabel.setHorizontalAlignment(SwingConstants.CENTER);
        profilePicLabel.setVerticalAlignment(SwingConstants.CENTER);
        profilePicLabel.setBorder(new LineBorder(Color.GRAY, 2, true));
        profilePicLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Details Text Area
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        detailsArea.setBackground(new Color(255, 255, 255));
        detailsArea.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JScrollPane scrollPane = new JScrollPane(detailsArea);
        scrollPane.setPreferredSize(new Dimension(400, 500));
        scrollPane.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));

        // Center Panel
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(profilePicLabel);
        centerPanel.add(scrollPane);
        add(centerPanel, BorderLayout.CENTER);

        // Close Button
        JButton closeBtn = new JButton("Close");
        closeBtn.setBackground(new Color(58, 123, 213));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(closeBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        loadStudentData();

        pack();
        setLocationRelativeTo(owner);
    }

    private void loadStudentData() {
        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement("SELECT * FROM students WHERE rollnumber = ?");
            ps.setLong(1, roll);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Profile Picture
                byte[] imgBytes = rs.getBytes("profilepic");
                if (imgBytes != null && imgBytes.length > 0) {
                    ImageIcon icon = new ImageIcon(imgBytes);
                    Image img = icon.getImage().getScaledInstance(160, 190, Image.SCALE_SMOOTH);
                    profilePicLabel.setIcon(new ImageIcon(img));
                    profilePicLabel.setText("");
                } else {
                    profilePicLabel.setIcon(null);
                    profilePicLabel.setText("No Image");
                }

                // Details
                StringBuilder sb = new StringBuilder();
                sb.append("Roll Number: ").append(rs.getLong("rollnumber")).append("\n\n");
                sb.append("Registration No: ").append(rs.getString("registration")).append("\n");
                sb.append("Department: ").append(rs.getString("Departmentcode")).append("\n");
                sb.append("Semester: ").append(rs.getInt("semoryear")).append("\n");
                sb.append("Optional Course: ").append(rs.getString("optionalcourse")).append("\n\n");
                sb.append("Name: ").append(rs.getString("firstname")).append(" ").append(rs.getString("lastname")).append("\n");
                sb.append("Email: ").append(rs.getString("emailid")).append("\n");
                sb.append("Contact: ").append(rs.getString("contactnumber")).append("\n\n");
                sb.append("DOB: ").append(rs.getString("dateofbirth")).append("\n");
                sb.append("Gender: ").append(rs.getString("gender")).append("\n");
                sb.append("Address: ").append(rs.getString("address")).append("\n\n");
                sb.append("Father: ").append(rs.getString("fathername")).append(" (").append(rs.getString("fatheroccupation")).append(")\n");
                sb.append("Mother: ").append(rs.getString("mothername")).append(" (").append(rs.getString("motheroccupation")).append(")\n");
                sb.append("User ID: ").append(rs.getString("userid")).append("\n");
                sb.append("Admission Date: ").append(rs.getString("admissiondate")).append("\n");

                detailsArea.setText(sb.toString());
            } else {
                JOptionPane.showMessageDialog(this, "Student not found.");
                dispose();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading student data.");
            dispose();
        }
    }
}
