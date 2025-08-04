package faculty;

import com.itextpdf.text.*;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import common.Conn;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.Font;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class FacultyAttendancePanel extends JPanel {
    private JComboBox<String> deptCombo, semCombo, courseCombo;
    private JTextField dateField;
    private JButton loadStudentsBtn, saveAttendanceBtn;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;

    private JButton exportPdfBtn;

    private final Color primaryColor = new Color(72, 145, 220);
    private final Color secondaryColor = new Color(230, 240, 250);
    private final Color accentColor = new Color(45, 110, 180);
    private final Font headerFont = new Font("Segoe UI", Font.BOLD, 14);
    private final Font tableFont = new Font("Segoe UI", Font.PLAIN, 14);

    private String facultyId; // logged in faculty id or name (pass from login)

    public FacultyAttendancePanel(String facultyId) {
        this.facultyId = facultyId;

        setLayout(new BorderLayout(15, 15));
        setBackground(secondaryColor);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Panel: Department, Semester, Course, Date, Load Button
        // Create topPanel
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(secondaryColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        deptCombo = new JComboBox<>(getFacultyDepartments());
        decorateComboBox(deptCombo);
        deptCombo.addActionListener(e -> loadSemesters());

        semCombo = new JComboBox<>();
        decorateComboBox(semCombo);

        courseCombo = new JComboBox<>();
        decorateComboBox(courseCombo);

        dateField = new JTextField(10);
        dateField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));

        loadStudentsBtn = new JButton("Load Students");
        decorateButton(loadStudentsBtn);

        saveAttendanceBtn = new JButton("Save Attendance");
        decorateButton(saveAttendanceBtn);
        saveAttendanceBtn.setEnabled(false);

        exportPdfBtn = new JButton("Export PDF");
        decorateButton(exportPdfBtn);
        exportPdfBtn.setEnabled(false);

// Layout positioning
        gbc.gridy = 0;

        gbc.gridx = 0; topPanel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1; topPanel.add(deptCombo, gbc);

        gbc.gridx = 2; topPanel.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 3; topPanel.add(semCombo, gbc);

        gbc.gridx = 4; topPanel.add(new JLabel("Course:"), gbc);
        gbc.gridx = 5; topPanel.add(courseCombo, gbc);

        gbc.gridx = 6; topPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 7; topPanel.add(dateField, gbc);

        gbc.gridx = 8; topPanel.add(loadStudentsBtn, gbc);
        gbc.gridx = 9; topPanel.add(saveAttendanceBtn, gbc);
        gbc.gridx = 10; topPanel.add(exportPdfBtn, gbc); // ✅ properly placed

// Add to main layout
        add(topPanel, BorderLayout.NORTH);
        exportPdfBtn.addActionListener(e -> {
            System.out.println("PDF Export Clicked"); // For debug
            exportAttendanceToPDF();
        });


        // Table
        String[] columns = {"Roll Number", "Student Name", "Present"};
        tableModel = new DefaultTableModel(null, columns) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return Boolean.class; // checkbox
                return String.class;
            }
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 2; // only Present checkbox editable
            }
        };
        attendanceTable = new JTable(tableModel);
        attendanceTable.setFont(tableFont);
        attendanceTable.getTableHeader().setFont(headerFont);
        attendanceTable.setRowHeight(28);
        attendanceTable.setSelectionBackground(primaryColor);
        attendanceTable.setSelectionForeground(Color.WHITE);
        attendanceTable.getTableHeader().setBackground(accentColor);
        attendanceTable.getTableHeader().setForeground(Color.WHITE);
        attendanceTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setBorder(new CompoundBorder(
                new LineBorder(accentColor, 2, true),
                new EmptyBorder(10, 10, 10, 10)));

        add(scrollPane, BorderLayout.CENTER);

        // Action Listeners
        loadStudentsBtn.addActionListener(e -> loadStudents());
        saveAttendanceBtn.addActionListener(e -> saveAttendance());

      //  exportPdfBtn.addActionListener(e -> exportAttendanceToPDF());
        // Load semesters & courses initially
        loadSemesters();
    }

    // Load Departments for this faculty from database
    private String[] getFacultyDepartments() {
        try {
            Conn c = new Conn();
            // Assuming facultyId is unique identifier (like username)
            PreparedStatement ps = c.c.prepareStatement(
                    "SELECT DISTINCT department_code FROM teacher_course_map WHERE teacher_id = ?");
            ps.setString(1, facultyId);
            ResultSet rs = ps.executeQuery();
            Vector<String> depts = new Vector<>();
            while (rs.next()) {
                depts.add(rs.getString("department_code"));
            }
            if(depts.isEmpty()) {
                depts.add("NOT ASSIGNED");
            }
            return depts.toArray(new String[0]);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new String[]{"NOT ASSIGNED"};
        }
    }

    // Load semesters when dept changes
    private void loadSemesters() {
        semCombo.removeAllItems();
        String dept = (String) deptCombo.getSelectedItem();
        if(dept == null) return;
        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement(
                    "SELECT DISTINCT semoryear FROM teacher_course_map WHERE teacher_id = ? AND department_code = ?");
            ps.setString(1, facultyId);
            ps.setString(2, dept);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                semCombo.addItem(String.valueOf(rs.getInt("semoryear")));
            }
            if(semCombo.getItemCount() > 0)
                semCombo.setSelectedIndex(0);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        loadCourses(); // update courses too
    }

    // Load courses when dept or semester changes
    private void loadCourses() {
        courseCombo.removeAllItems();
        String dept = (String) deptCombo.getSelectedItem();
        String sem = (String) semCombo.getSelectedItem();
        if(dept == null || sem == null) return;
        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement(
                    "SELECT DISTINCT coursecode FROM teacher_course_map WHERE teacher_id  = ? AND department_code = ? AND semoryear = ?");
            ps.setString(1, facultyId);
            ps.setString(2, dept);
            ps.setInt(3, Integer.parseInt(sem));
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                courseCombo.addItem(rs.getString("coursecode"));
            }
            if(courseCombo.getItemCount() > 0)
                courseCombo.setSelectedIndex(0);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    // Load students from database for selected dept & sem
    private void loadStudents() {
        String dept = (String) deptCombo.getSelectedItem();
        String sem = (String) semCombo.getSelectedItem();
        if(dept == null || sem == null) {
            JOptionPane.showMessageDialog(this, "Select department and semester.");
            return;
        }
        String course = (String) courseCombo.getSelectedItem();
        if(course == null) {
            JOptionPane.showMessageDialog(this, "Select course.");
            return;
        }
        tableModel.setRowCount(0); // clear table

        try {
            Conn c = new Conn();
            // Fetch students from studentinfo1 or students table with matching dept and semester
            PreparedStatement ps = c.c.prepareStatement(
                    "SELECT rollnumber, firstname, lastname FROM students WHERE Departmentcode = ? AND semoryear = ?");
            ps.setString(1, dept);
            ps.setInt(2, Integer.parseInt(sem));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String roll = rs.getString("rollnumber");
                String name = rs.getString("firstname") + " " + rs.getString("lastname");

                // Check if attendance already marked for this student on this date and course
                PreparedStatement ps2 = c.c.prepareStatement(
                        "SELECT present FROM attandance WHERE rollnumber = ? AND coursecode = ? AND date = ?");
                ps2.setString(1, roll);
                ps2.setString(2, course);
                ps2.setString(3, dateField.getText().trim());
                ResultSet rs2 = ps2.executeQuery();

                boolean present = false;
                if (rs2.next()) {
                    present = rs2.getInt("present") == 1;
                }

                tableModel.addRow(new Object[]{roll, name, present});
            }

            saveAttendanceBtn.setEnabled(true);

            exportPdfBtn.setEnabled(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading students: " + ex.getMessage());
        }
    }

    // Save attendance data to database
    private void saveAttendance() {
        String dept = (String) deptCombo.getSelectedItem();
        String sem = (String) semCombo.getSelectedItem();
        String course = (String) courseCombo.getSelectedItem();
        String date = dateField.getText().trim();

        if(dept == null || sem == null || course == null || date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select all fields and enter date.");
            return;
        }

        try {
            Conn c = new Conn();
            c.c.setAutoCommit(false);

            PreparedStatement checkStmt = c.c.prepareStatement(
                    "SELECT COUNT(*) FROM attandance WHERE rollnumber = ? AND coursecode = ? AND date = ?");
            PreparedStatement insertStmt = c.c.prepareStatement(
                    "INSERT INTO attandance (coursecode, date, rollnumber, present, Departmentcode, semoryear) VALUES (?, ?, ?, ?, ?, ?)");
            PreparedStatement updateStmt = c.c.prepareStatement(
                    "UPDATE attandance SET present = ? WHERE rollnumber = ? AND coursecode = ? AND date = ?");

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String roll = tableModel.getValueAt(i, 0).toString();
                boolean present = (Boolean) tableModel.getValueAt(i, 2);

                // Check if record exists
                checkStmt.setString(1, roll);
                checkStmt.setString(2, course);
                checkStmt.setString(3, date);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                int count = rs.getInt(1);

                if(count == 0) {
                    // Insert new record
                    insertStmt.setString(1, course);
                    insertStmt.setString(2, date);
                    insertStmt.setString(3, roll);
                    insertStmt.setInt(4, present ? 1 : 0);
                    insertStmt.setString(5, dept);
                    insertStmt.setInt(6, Integer.parseInt(sem));
                    insertStmt.executeUpdate();
                } else {
                    // Update existing record
                    updateStmt.setInt(1, present ? 1 : 0);
                    updateStmt.setString(2, roll);
                    updateStmt.setString(3, course);
                    updateStmt.setString(4, date);
                    updateStmt.executeUpdate();
                }
            }

            c.c.commit();
            JOptionPane.showMessageDialog(this, "Attendance saved successfully.");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to save attendance: " + ex.getMessage());
        }
    }

    private void exportAttendanceToPDF() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("attendance_" + dateField.getText().trim() + ".pdf"));
        int option = fileChooser.showSaveDialog(this);
        if (option != JFileChooser.APPROVE_OPTION) return;

        try {
            String department = (String) deptCombo.getSelectedItem();
            String semester = (String) semCombo.getSelectedItem();
            String course = (String) courseCombo.getSelectedItem();
            String date = dateField.getText().trim();

            Document document = new Document();
            PdfWriter.getInstance(document, new java.io.FileOutputStream(fileChooser.getSelectedFile()));
            document.open();

            // 🔵 Add institution logo (must be in src/resources/logo.png)
            try {
                Image logo = Image.getInstance("src/main/resources/logo.jpg"); // ✅ Replace path if needed
                logo.scaleToFit(100, 100);
                logo.setAlignment(Element.ALIGN_CENTER);
                document.add(logo);
            } catch (Exception logoEx) {
                System.err.println("Logo not found or error: " + logoEx.getMessage());
            }

            // 🔵 Add Title and Info
            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            com.itextpdf.text.Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            Paragraph title = new Paragraph("Attendance Report\n\n", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph info = new Paragraph(
                    "Department: " + department + "\n" +
                            "Semester/Year: " + semester + "\n" +
                            "Course: " + course + "\n" +
                            "Date: " + date + "\n\n", infoFont);
            info.setAlignment(Element.ALIGN_CENTER);
            document.add(info);

            // 🔵 Table
            PdfPTable pdfTable = new PdfPTable(tableModel.getColumnCount());
            pdfTable.setWidthPercentage(100);
            pdfTable.setSpacingBefore(10f);
            pdfTable.setSpacingAfter(10f);

            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                PdfPCell headerCell = new PdfPCell(new Phrase(tableModel.getColumnName(i)));
                headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfTable.addCell(headerCell);
            }

            for (int row = 0; row < tableModel.getRowCount(); row++) {
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    Object value = tableModel.getValueAt(row, col);
                    String cellText;
                    if (value instanceof Boolean) {
                        cellText = ((Boolean) value) ? "Present" : "Absent";
                    } else {
                        cellText = value == null ? "" : value.toString();
                    }
                    PdfPCell cell = new PdfPCell(new Phrase(cellText));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    pdfTable.addCell(cell);
                }
            }

            document.add(pdfTable);
            document.close();

            JOptionPane.showMessageDialog(this, "PDF exported successfully.");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error exporting PDF: " + ex.getMessage());
        }
    }

    // Utility methods for UI decoration
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
}
