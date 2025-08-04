package student;

import common.Conn;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StudentProfilePanel extends JPanel {
    private String registration;

    private JLabel nameLabel, deptLabel, semLabel, emailLabel, phoneLabel, dobLabel, genderLabel;
    private JLabel photoLabel;

    public StudentProfilePanel(String registration) {
        this.registration = registration;

        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Left: Photo
        photoLabel = new JLabel();
        photoLabel.setPreferredSize(new Dimension(160, 200));
        photoLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        photoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Right: Details
        JPanel detailsPanel = new JPanel(new GridLayout(8, 1, 10, 10));
        detailsPanel.setBackground(Color.WHITE);

        nameLabel = new JLabel();
        deptLabel = new JLabel();
        semLabel = new JLabel();
        emailLabel = new JLabel();
        phoneLabel = new JLabel();
        dobLabel = new JLabel();
        genderLabel = new JLabel();

        detailsPanel.add(nameLabel);
        detailsPanel.add(deptLabel);
        detailsPanel.add(semLabel);
        detailsPanel.add(emailLabel);
        detailsPanel.add(phoneLabel);
        detailsPanel.add(dobLabel);
        detailsPanel.add(genderLabel);

        // Title
        JLabel title = new JLabel("Student Profile", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));

        JPanel main = new JPanel(new BorderLayout(20, 0));
        main.setBackground(Color.WHITE);
        main.add(photoLabel, BorderLayout.WEST);
        main.add(detailsPanel, BorderLayout.CENTER);

        add(title, BorderLayout.NORTH);
        add(main, BorderLayout.CENTER);

        loadProfile();
    }

    private void loadProfile() {
        try (Conn c = new Conn()) {
            PreparedStatement ps = c.c.prepareStatement(
                    "SELECT firstname, lastname, Departmentcode, semoryear, emailid, contactnumber, dateofbirth, gender, profilepic " +
                            "FROM students WHERE registration = ?"
            );
            ps.setString(1, registration);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String fullName = rs.getString("firstname") + " " + rs.getString("lastname");
                nameLabel.setText("Name: " + fullName);
                deptLabel.setText("Department: " + rs.getString("Departmentcode"));
                semLabel.setText("Semester/Year: " + rs.getInt("semoryear"));
                emailLabel.setText("Email: " + rs.getString("emailid"));
                phoneLabel.setText("Phone: " + rs.getString("contactnumber"));
                dobLabel.setText("DOB: " + rs.getString("dateofbirth"));
                genderLabel.setText("Gender: " + rs.getString("gender"));

                // Load photo from BLOB
                InputStream is = rs.getBinaryStream("profilepic");
                if (is != null) {
                    BufferedImage img = ImageIO.read(is);
                    Image scaled = img.getScaledInstance(160, 200, Image.SCALE_SMOOTH);
                    photoLabel.setIcon(new ImageIcon(scaled));
                } else {
                    photoLabel.setText("No Photo");
                }
            } else {
                JOptionPane.showMessageDialog(this, "No student found for registration: " + registration);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading profile: " + ex.getMessage());
        }
    }
}
