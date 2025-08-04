package admin;

import common.Conn;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

public class AdmissionApprovalPanel extends JFrame {
    private final JTable table;
    private JLabel profilePreview, nidPreview;
    private JButton approveBtn, rejectBtn, exportPdfBtn;
    private DefaultTableModel model;
    private Set<Integer> previousRequestIds = new HashSet<>();

    public AdmissionApprovalPanel() {
        super("📝 Admission Requests");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        model = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Dept", "Sem.", "Type", "Status"}, 0);
        table = new JTable(model);
        table.setRowHeight(30);
        table.setSelectionBackground(new Color(0x4CAF50));
        table.setSelectionForeground(Color.WHITE);
        loadRequests();

        checkForNewAdmissionRequests();

        profilePreview = makePreviewLabel("Profile Preview");
        nidPreview = makePreviewLabel("PDF Preview");

        approveBtn = styledButton("✅ Approve", new Color(46, 204, 113));
        rejectBtn = styledButton("❌ Reject", new Color(231, 76, 60));
        exportPdfBtn = styledButton("⬇ Export PDF", new Color(52, 152, 219));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(approveBtn);
        buttonPanel.add(rejectBtn);
        buttonPanel.add(exportPdfBtn);

        // 🔁 Reload button to refresh the admission requests table
        JButton reloadBtn = styledButton("🔁 Reload", new Color(127, 140, 141));
        reloadBtn.addActionListener(e -> loadRequests());
        buttonPanel.add(reloadBtn);


        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.add(profilePreview);
        centerPanel.add(nidPreview);

        add(new JScrollPane(table), BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> showPreview());
        approveBtn.addActionListener(e -> approve());
        rejectBtn.addActionListener(e -> reject());
        exportPdfBtn.addActionListener(e -> exportPDF());

        setVisible(true);
        addTrayNotification("Admission Panel Ready ✔️");
    }

    private JLabel makePreviewLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setPreferredSize(new Dimension(420, 400));
        lbl.setBorder(new LineBorder(Color.LIGHT_GRAY));
        return lbl;
    }

    private JButton styledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(160, 35));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(color.darker()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(color); }
        });
        return btn;
    }

    private void loadRequests() {

        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT id, first_name, last_name, email, Departmentcode, semester, admission_type, status FROM admission_requests WHERE status = 'pending'");
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("first_name") + " " + rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("Departmentcode"),
                        rs.getInt("semester"),
                        rs.getString("admission_type"),
                        "pending"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPreview() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) table.getValueAt(row, 0);

        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement("SELECT profile_pic, nid_or_birthdoc FROM admission_requests WHERE id=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                byte[] profileBytes = rs.getBytes("profile_pic");
                byte[] pdfBytes = rs.getBytes("nid_or_birthdoc");

                profilePreview.setIcon(toImageIcon(profileBytes));
                profilePreview.setText("");

                if (pdfBytes != null) {
                    nidPreview.setText("📄 Click to View NID / BirthDoc");
                    nidPreview.setIcon(new ImageIcon("src/main/resources/pdf_icon.png")); // optional PDF icon
                    nidPreview.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    nidPreview.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent e) {
                            try {
                                File temp = File.createTempFile("nid_preview", ".pdf");
                                Files.write(temp.toPath(), pdfBytes);
                                Desktop.getDesktop().open(temp);
                            } catch (IOException ex) {
                                JOptionPane.showMessageDialog(null, "Failed to open PDF.");
                            }
                        }
                    });
                } else {
                    nidPreview.setText("❌ No PDF Uploaded");
                    nidPreview.setCursor(Cursor.getDefaultCursor());
                    nidPreview.setIcon(null);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private ImageIcon toImageIcon(byte[] imageData) throws IOException {
        if (imageData == null) return null;
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));
        if (img == null) return null;
        return new ImageIcon(img.getScaledInstance(360, 440, Image.SCALE_SMOOTH));
    }

    private void approve() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return;
        }
        int id = (int) table.getValueAt(row, 0);
        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement("SELECT * FROM admission_requests WHERE id=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Generate registration number starting from 101 by checking max in students table
                int newRegNumber = 1000; // default start
                try (Statement stmt = c.c.createStatement()) {
                    ResultSet rsReg = stmt.executeQuery("SELECT MAX(CAST(registration AS UNSIGNED)) AS max_reg FROM students");
                    if (rsReg.next()) {
                        int maxReg = rsReg.getInt("max_reg");
                        if (maxReg >= 1000) {
                            newRegNumber = maxReg + 1;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    // fallback to default 101 if error occurs
                }

                PreparedStatement ins = c.c.prepareStatement(
                        "INSERT INTO students " +
                                "(registration, Departmentcode, semoryear, optionalcourse, firstname, lastname, emailid, contactnumber, dateofbirth, gender, present_address, permanent_address, fathername, fatheroccupation, mothername, motheroccupation, education_json, profilepic, nid_or_birthdoc, admissiondate) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_DATE())");

                ins.setString(1, String.valueOf(newRegNumber)); // Use simple numeric reg no
                ins.setString(2, rs.getString("Departmentcode"));
                ins.setInt(3, rs.getInt("semester"));
                ins.setString(4, rs.getString("admission_type"));
                ins.setString(5, rs.getString("first_name"));
                ins.setString(6, rs.getString("last_name"));
                ins.setString(7, rs.getString("email"));
                ins.setString(8, rs.getString("phone"));
                ins.setDate(9, rs.getDate("birthdate"));
                ins.setString(10, rs.getString("gender"));
                ins.setString(11, rs.getString("present_address"));
                ins.setString(12, rs.getString("permanent_address"));
                ins.setString(13, rs.getString("father_name"));
                ins.setString(14, rs.getString("father_occupation"));
                ins.setString(15, rs.getString("mother_name"));
                ins.setString(16, rs.getString("mother_occupation"));
                ins.setString(17, rs.getString("education_json"));
                ins.setBytes(18, rs.getBytes("profile_pic"));
                ins.setBytes(19, rs.getBytes("nid_or_birthdoc"));
                ins.executeUpdate();

                c.s.executeUpdate("UPDATE admission_requests SET status='approved' WHERE id=" + id);
                model.removeRow(row);
                addTrayNotification("Admission Approved for ID: " + id);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Approval failed.");
        }
    }


    private void reject() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) table.getValueAt(row, 0);
        try {
            Conn c = new Conn();
            c.s.executeUpdate("UPDATE admission_requests SET status='rejected' WHERE id=" + id);
            model.removeRow(row);
            addTrayNotification("Admission Rejected for ID: " + id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportPDF() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a student row to export.");
            return;
        }
        int id = (int) table.getValueAt(row, 0);
        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement("SELECT * FROM admission_requests WHERE id=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File("Student_" + id + ".pdf"));
                if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

                Document doc = new Document();
                PdfWriter.getInstance(doc, new FileOutputStream(chooser.getSelectedFile()));
                doc.open();

                doc.add(new Paragraph("🎓 Student Admission Details", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
                doc.add(new Paragraph("Generated on: " + new Date().toString()));
                doc.add(new Paragraph(" "));

                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.addCell("Full Name");
                table.addCell(rs.getString("first_name") + " " + rs.getString("last_name"));
                table.addCell("Email");
                table.addCell(rs.getString("email"));
                table.addCell("Phone");
                table.addCell(rs.getString("phone"));
                table.addCell("Department");
                table.addCell(rs.getString("Departmentcode"));
                table.addCell("Semester");
                table.addCell(String.valueOf(rs.getInt("semester")));
                table.addCell("Admission Type");
                table.addCell(rs.getString("admission_type"));
                doc.add(table);
                doc.close();

                JOptionPane.showMessageDialog(this, "✅ PDF Exported!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to export PDF.");
        }
    }

    private void checkForNewAdmissionRequests() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            public void run() {
                try {
                    Conn c = new Conn();
                    ResultSet rs = c.s.executeQuery("SELECT id FROM admission_requests WHERE status IS NULL");
                    Set<Integer> current = new HashSet<>();
                    while (rs.next()) current.add(rs.getInt("id"));

                    for (int id : current) {
                        if (!previousRequestIds.contains(id)) {
                            showTrayNotification("📥 New Request", "Request ID: " + id);
                        }
                    }
                    previousRequestIds = current;
                } catch (Exception ignored) {}
            }
        }, 0, 10000); // every 10 seconds
    }

    private void showTrayNotification(String title, String msg) {
        if (!SystemTray.isSupported()) return;
        try {
            Image img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            TrayIcon tray = new TrayIcon(img, "Notification");
            tray.setImageAutoSize(true);
            SystemTray.getSystemTray().add(tray);
            tray.displayMessage(title, msg, TrayIcon.MessageType.INFO);
            new Timer().schedule(new TimerTask() {
                public void run() {
                    SystemTray.getSystemTray().remove(tray);
                }
            }, 5000);
        } catch (Exception ignored) {}
    }

    private void addTrayNotification(String msg) {
        showTrayNotification("✔️ Info", msg);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdmissionApprovalPanel::new);
    }
}
