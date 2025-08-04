package faculty;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.toedter.calendar.JDateChooser;
import common.Conn;
import com.itextpdf.text.Image;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.Font;
import java.awt.Graphics;
//import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class ProfilePanel extends JPanel {
    private JTextField nameField, emailField, phoneField, addressField, qualificationField, experienceField;
    private JPasswordField passwordField;
    private JDateChooser birthdateChooser;
    private JComboBox<String> genderCombo;
    private JLabel picLabel;
    private JButton updateBtn, exportBtn;
    private byte[] profileImageBytes;
    private final String teacherId;

    public ProfilePanel(String teacherId) {
        this.teacherId = teacherId;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 40, 20, 40));
        setBackground(new Color(230, 240, 250));

        JLabel title = new JLabel("👨‍🏫 Teacher Profile", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(45, 110, 180));
        add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(getBackground());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0;
        nameField = createField("Name:", formPanel, gbc, 0, y++);
        emailField = createField("Email:", formPanel, gbc, 0, y++);
        phoneField = createField("Phone:", formPanel, gbc, 0, y++);
        addressField = createField("Address:", formPanel, gbc, 0, y++);
        qualificationField = createField("Qualification:", formPanel, gbc, 0, y++);
        experienceField = createField("Experience:", formPanel, gbc, 0, y++);

        // Profile Picture
        gbc.gridx = 2; gbc.gridy = 0; gbc.gridheight = 4;
        picLabel = new JLabel("📷", JLabel.CENTER);
        picLabel.setPreferredSize(new Dimension(140, 140));
        picLabel.setOpaque(true);
        picLabel.setBackground(Color.LIGHT_GRAY);
        picLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        picLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        picLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                chooseProfileImage();
            }
        });
        formPanel.add(picLabel, gbc);
        gbc.gridheight = 1;

        // Birthdate
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Birthdate:"), gbc);
        gbc.gridx = 1;
        birthdateChooser = new JDateChooser();
        birthdateChooser.setDateFormatString("yyyy-MM-dd");
        birthdateChooser.setPreferredSize(new Dimension(200, 25));
        formPanel.add(birthdateChooser, gbc);
        y++;

        // Gender
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Gender:"), gbc);
        gbc.gridx = 1;
        genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        genderCombo.setPreferredSize(new Dimension(200, 25));
        formPanel.add(genderCombo, gbc);
        y++;

        // Password
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        formPanel.add(passwordField, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        updateBtn = new JButton("Update Profile");
        exportBtn = new JButton("Export PDF");
        btnPanel.add(updateBtn);
        btnPanel.add(exportBtn);
        add(btnPanel, BorderLayout.SOUTH);

        updateBtn.addActionListener(e -> updateProfile());
        exportBtn.addActionListener(e -> exportPDF());

        loadProfile();
    }

    private JTextField createField(String label, JPanel panel, GridBagConstraints gbc, int x, int y) {
        gbc.gridx = 0; gbc.gridy = y;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        JTextField tf = new JTextField(20);
        panel.add(tf, gbc);
        return tf;
    }

    private void chooseProfileImage() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                BufferedImage img = ImageIO.read(file);
                java.awt.Image scaled = img.getScaledInstance(140, 140, java.awt.Image.SCALE_SMOOTH);
                BufferedImage rounded = makeRounded(scaled, 140);
                picLabel.setIcon(new ImageIcon(rounded));

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(rounded, "png", baos);
                profileImageBytes = baos.toByteArray();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void loadProfile() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT * FROM teachers WHERE teacher_id = '" + teacherId + "'");
            if (rs.next()) {
                nameField.setText(rs.getString("teacherName"));
                emailField.setText(rs.getString("emailid"));
                phoneField.setText(rs.getString("contactnumber"));
                addressField.setText(rs.getString("address"));
                qualificationField.setText(rs.getString("qualification"));
                experienceField.setText(rs.getString("experience"));
                genderCombo.setSelectedItem(rs.getString("gender"));
                String bd = rs.getString("birthdate");
                if (bd != null) birthdateChooser.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(bd));

                profileImageBytes = rs.getBytes("profilepic");
                if (profileImageBytes != null) {
                    ImageIcon icon = new ImageIcon(profileImageBytes);
                    java.awt.Image img = icon.getImage().getScaledInstance(140, 140, java.awt.Image.SCALE_SMOOTH);
                    picLabel.setIcon(new ImageIcon(makeRounded(img, 140)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateProfile() {
        try {
            String email = emailField.getText().trim();
            if (!Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", email))
                throw new IllegalArgumentException("Invalid email");
            if (!phoneField.getText().trim().matches("\\d{10,13}"))
                throw new IllegalArgumentException("Invalid phone");

            Conn c = new Conn();
            String sql = "UPDATE teachers SET teacherName=?, emailid=?, contactnumber=?, address=?, qualification=?, experience=?, birthdate=?, gender=?, profilepic=?"
                    + (!new String(passwordField.getPassword()).trim().isEmpty() ? ", password=?" : "")
                    + " WHERE teacher_id=?";
            PreparedStatement ps = c.c.prepareStatement(sql);
            int i = 1;
            ps.setString(i++, nameField.getText().trim());
            ps.setString(i++, email);
            ps.setString(i++, phoneField.getText().trim());
            ps.setString(i++, addressField.getText().trim());
            ps.setString(i++, qualificationField.getText().trim());
            ps.setString(i++, experienceField.getText().trim());
            ps.setString(i++, new SimpleDateFormat("yyyy-MM-dd").format(birthdateChooser.getDate()));
            ps.setString(i++, (String) genderCombo.getSelectedItem());
            ps.setBytes(i++, profileImageBytes);

            String pwd = new String(passwordField.getPassword()).trim();
            if (!pwd.isEmpty()) ps.setString(i++, hashPassword(pwd));

            ps.setString(i, teacherId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Profile updated!");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Update failed.");
        }
    }

    private void exportPDF() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("Teacher_Profile_" + teacherId + ".pdf"));
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(chooser.getSelectedFile()));
            doc.open();

            // Add logo
            try {
                Image logo = Image.getInstance("src/main/resources/logo.jpg");
                logo.scaleToFit(80, 80);
                logo.setAlignment(Element.ALIGN_CENTER);
                doc.add(logo);
            } catch (Exception e) {}

            doc.add(new Paragraph("Teacher Profile", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            doc.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            table.addCell("Name"); table.addCell(nameField.getText());
            table.addCell("Email"); table.addCell(emailField.getText());
            table.addCell("Phone"); table.addCell(phoneField.getText());
            table.addCell("Address"); table.addCell(addressField.getText());
            table.addCell("Qualification"); table.addCell(qualificationField.getText());
            table.addCell("Experience"); table.addCell(experienceField.getText());
            table.addCell("Birthdate"); table.addCell(new SimpleDateFormat("yyyy-MM-dd").format(birthdateChooser.getDate()));
            table.addCell("Gender"); table.addCell((String) genderCombo.getSelectedItem());

            doc.add(table);

            // Add profile picture
            if (profileImageBytes != null) {
                Image profilePic = Image.getInstance(profileImageBytes);
                profilePic.scaleToFit(100, 100);
                profilePic.setAlignment(Element.ALIGN_CENTER);
                doc.add(new Paragraph("\n"));
                doc.add(profilePic);
            }

            doc.close();
            JOptionPane.showMessageDialog(this, "PDF exported!");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Export failed.");
        }
    }

    private String hashPassword(String pwd) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pwd.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private BufferedImage makeRounded(java.awt.Image img, int size) {
        BufferedImage mask = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = mask.createGraphics();

        // Anti-aliasing for smooth edges
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Clip with rounded ellipse
        g2.setClip(new Ellipse2D.Double(0, 0, size, size));

        // Draw image
        g2.drawImage(img, 0, 0, size, size, null);
        g2.dispose();

        return mask;
    }

}
