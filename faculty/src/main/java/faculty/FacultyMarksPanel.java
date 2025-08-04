package faculty;

//import com.itextpdf.text.Font;
import com.itextpdf.text.Font;
import common.Conn;
import com.itextpdf.text.*;  // or avoid importing Font explicitly

import com.itextpdf.text.pdf.*;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.Element;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.BaseColor;
import java.io.FileOutputStream;
import java.sql.*;
import java.util.Vector;
//import java.awt.Font;
public class FacultyMarksPanel extends JPanel {
    private JComboBox<String> deptCombo, semCombo, courseCombo;
    private JButton loadStudentsBtn, saveMarksBtn, generatePdfBtn;
    private JTable marksTable;
    private DefaultTableModel tableModel;

    private final Color primaryColor = new Color(72, 145, 220);
    private final Color secondaryColor = new Color(230, 240, 250);
    private final Color accentColor = new Color(45, 110, 180);
    private final java.awt.Font headerFont = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14);
    private final java.awt.Font tableFont = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14);

    private String facultyId;

    public FacultyMarksPanel(String facultyId) {
        this.facultyId = facultyId;

        setLayout(new BorderLayout(15, 15));
        setBackground(secondaryColor);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Panel for selections and buttons
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
        semCombo.addActionListener(e -> loadCourses());

        courseCombo = new JComboBox<>();
        decorateComboBox(courseCombo);

        loadStudentsBtn = new JButton("Load Students");
        decorateButton(loadStudentsBtn);

        saveMarksBtn = new JButton("Save Marks");
        decorateButton(saveMarksBtn);
        saveMarksBtn.setEnabled(false);

        generatePdfBtn = new JButton("Generate PDF");
        decorateButton(generatePdfBtn);
        generatePdfBtn.setEnabled(false);

        // Add components
        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1;
        topPanel.add(deptCombo, gbc);

        gbc.gridx = 2;
        topPanel.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 3;
        topPanel.add(semCombo, gbc);

        gbc.gridx = 4;
        topPanel.add(new JLabel("Course:"), gbc);
        gbc.gridx = 5;
        topPanel.add(courseCombo, gbc);

        gbc.gridx = 6;
        topPanel.add(loadStudentsBtn, gbc);

        gbc.gridx = 7;
        topPanel.add(saveMarksBtn, gbc);

        gbc.gridx = 8;
        topPanel.add(generatePdfBtn, gbc);

        add(topPanel, BorderLayout.NORTH);

        // Table setup
        // Updated columns (Removed "Practical Marks")
        String[] columns = {"Registration", "Student Name", "Theory Marks"};
        tableModel = new DefaultTableModel(null, columns) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 2) ? Integer.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 2; // only theory marks editable
            }
        };


        marksTable = new JTable(tableModel);
        marksTable.setFont(tableFont);
        marksTable.getTableHeader().setFont(headerFont);
        marksTable.setRowHeight(28);
        marksTable.setSelectionBackground(primaryColor);
        marksTable.setSelectionForeground(Color.WHITE);
        marksTable.getTableHeader().setBackground(accentColor);
        marksTable.getTableHeader().setForeground(Color.WHITE);
        marksTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(marksTable);
        scrollPane.setBorder(new CompoundBorder(
                new LineBorder(accentColor, 2, true),
                new EmptyBorder(10, 10, 10, 10)));
        add(scrollPane, BorderLayout.CENTER);

        // Listeners
        loadStudentsBtn.addActionListener(e -> loadStudents());
        saveMarksBtn.addActionListener(e -> saveMarks());
        generatePdfBtn.addActionListener(e -> generatePdf());

        loadSemesters(); // initial load
    }

    private String[] getFacultyDepartments() {
        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement(
                    "SELECT DISTINCT department_code FROM teacher_course_map WHERE teacher_id = ?");
            ps.setString(1, facultyId);
            ResultSet rs = ps.executeQuery();
            Vector<String> depts = new Vector<>();
            while (rs.next()) {
                depts.add(rs.getString("department_code"));
            }
            if (depts.isEmpty()) depts.add("NOT ASSIGNED");
            return depts.toArray(new String[0]);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new String[]{"NOT ASSIGNED"};
        }
    }

    private void loadSemesters() {
        semCombo.removeAllItems();
        String dept = (String) deptCombo.getSelectedItem();
        if (dept == null) return;
        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement(
                    "SELECT DISTINCT semoryear FROM teacher_course_map WHERE teacher_id = ? AND department_code = ?");
            ps.setString(1, facultyId);
            ps.setString(2, dept);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                semCombo.addItem(String.valueOf(rs.getInt("semoryear")));
            }
            if (semCombo.getItemCount() > 0)
                semCombo.setSelectedIndex(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        loadCourses();
    }

    private void loadCourses() {
        courseCombo.removeAllItems();
        String dept = (String) deptCombo.getSelectedItem();
        String sem = (String) semCombo.getSelectedItem();
        if (dept == null || sem == null) return;
        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement(
                    "SELECT DISTINCT coursecode FROM teacher_course_map WHERE teacher_id = ? AND department_code = ? AND semoryear = ?");
            ps.setString(1, facultyId);
            ps.setString(2, dept);
            ps.setInt(3, Integer.parseInt(sem));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                courseCombo.addItem(rs.getString("coursecode"));
            }
            if (courseCombo.getItemCount() > 0)
                courseCombo.setSelectedIndex(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadStudents() {
        String dept = (String) deptCombo.getSelectedItem();
        String sem = (String) semCombo.getSelectedItem();
        String course = (String) courseCombo.getSelectedItem();

        if (dept == null || sem == null || course == null) {
            JOptionPane.showMessageDialog(this, "Please select Department, Semester and Course");
            return;
        }

        tableModel.setRowCount(0);

        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement(
                    "SELECT registration, firstname, lastname FROM students WHERE Departmentcode = ? AND semoryear = ?");
            ps.setString(1, dept);
            ps.setInt(2, Integer.parseInt(sem));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String roll = rs.getString("registration");
                String name = rs.getString("firstname") + " " + rs.getString("lastname");

                // Fetch existing marks (theory and practical)
                PreparedStatement psTheory = c.c.prepareStatement(
                        "SELECT marks FROM marks WHERE registration = ? AND coursecode = ?");
                psTheory.setString(1, roll);
                psTheory.setString(2, course + "-T");
                ResultSet rsTheory = psTheory.executeQuery();
                int theoryMarks = rsTheory.next() ? rsTheory.getInt("marks") : 0;

                tableModel.addRow(new Object[]{roll, name, theoryMarks});
            }
            saveMarksBtn.setEnabled(true);
            generatePdfBtn.setEnabled(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading students: " + ex.getMessage());
        }
    }

    private void saveMarks() {
        String dept = (String) deptCombo.getSelectedItem();
        String sem = (String) semCombo.getSelectedItem();
        String course = (String) courseCombo.getSelectedItem();

        if (dept == null || sem == null || course == null) {
            JOptionPane.showMessageDialog(this, "Please select Department, Semester and Course");
            return;
        }

        try {
            Conn c = new Conn();
            c.c.setAutoCommit(false);

            PreparedStatement checkStmt = c.c.prepareStatement(
                    "SELECT COUNT(*) FROM marks WHERE registration = ? AND coursecode = ?");
            PreparedStatement insertStmt = c.c.prepareStatement(
                    "INSERT INTO marks (Departmentcode, semoryear, coursecode, coursename, rollnumber, registration, marks) VALUES (?, ?, ?, ?, ?, ?, ?)");
            PreparedStatement updateStmt = c.c.prepareStatement(
                    "UPDATE marks SET marks = ? WHERE registration = ? AND coursecode = ?");

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String roll = tableModel.getValueAt(i, 0).toString();
                Integer theory = (Integer) tableModel.getValueAt(i, 2);

                if (theory == null) theory = 0;

                checkStmt.setString(1, roll);
                checkStmt.setString(2, course + "-T");
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                int count = rs.getInt(1);

                if (count == 0) {
                    insertStmt.setString(1, dept);
                    insertStmt.setInt(2, Integer.parseInt(sem));
                    insertStmt.setString(3, course + "-T");
                    insertStmt.setString(4, course + " (Theory)");
                    insertStmt.setString(5, roll);          // <-- Use setString here!
                    insertStmt.setString(6, roll);
                    insertStmt.setInt(7, theory);

                    insertStmt.executeUpdate();
                } else {
                    updateStmt.setInt(1, theory);
                    updateStmt.setString(2, roll);
                    updateStmt.setString(3, course + "-T");
                    updateStmt.executeUpdate();
                }
            }
            c.c.commit();
            JOptionPane.showMessageDialog(this, "Marks saved successfully.");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to save marks: " + ex.getMessage());
        }
    }


    private void generatePdf() {
        String dept = (String) deptCombo.getSelectedItem();
        String sem = (String) semCombo.getSelectedItem();
        String course = (String) courseCombo.getSelectedItem();

        if (dept == null || sem == null || course == null) {
            JOptionPane.showMessageDialog(this, "Please select Department, Semester and Course");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File(course + "_Marks_" + sem + ".pdf"));
        int option = fileChooser.showSaveDialog(this);
        if (option != JFileChooser.APPROVE_OPTION) return;

        try {
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(fileChooser.getSelectedFile()));
            doc.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Paragraph title = new Paragraph(course + " Marksheet - Semester " + sem, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(new Paragraph("Department: " + dept));
            doc.add(new Paragraph(" ")); // empty line

            // Table
            PdfPTable pdfTable = new PdfPTable(3);
            pdfTable.setWidthPercentage(100);
            pdfTable.setWidths(new int[]{2, 5, 2});

            pdfTable.addCell(getPdfCell("Roll Number", true));
            pdfTable.addCell(getPdfCell("Student Name", true));
            pdfTable.addCell(getPdfCell("Theory Marks", true));
          //  pdfTable.addCell(getPdfCell("Practical Marks", true));

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                pdfTable.addCell(getPdfCell(tableModel.getValueAt(i, 0).toString(), false));
                pdfTable.addCell(getPdfCell(tableModel.getValueAt(i, 1).toString(), false));
                pdfTable.addCell(getPdfCell(String.valueOf(tableModel.getValueAt(i, 2)), false));
              //  pdfTable.addCell(getPdfCell(String.valueOf(tableModel.getValueAt(i, 3)), false));
            }

            doc.add(pdfTable);
            doc.close();
            JOptionPane.showMessageDialog(this, "PDF generated successfully.");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating PDF: " + ex.getMessage());
        }
    }

    private PdfPCell getPdfCell(String text, boolean header) {
        Font font = header ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12) :
                FontFactory.getFont(FontFactory.HELVETICA, 12);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        if (header) {
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        }
        return cell;
    }

    private void decorateComboBox(JComboBox<String> combo) {
        combo.setBackground(Color.WHITE);
        combo.setForeground(Color.DARK_GRAY);
        combo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        combo.setBorder(new LineBorder(accentColor, 1, true));
    }

    private void decorateButton(JButton btn) {
        btn.setBackground(primaryColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
