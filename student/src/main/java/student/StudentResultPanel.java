package student;

//import com.itextpdf.text.Font;
import common.Conn;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Font;
import java.io.FileOutputStream;
import java.sql.*;

// ✅ StudentResultPanel.java (Student View with GPA + CGPA + PDF Export)

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

public class StudentResultPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JLabel cgpaLabel;
    private JButton pdfButton;
    private String userRegistration;

    public StudentResultPanel(String registration) {
        this.userRegistration = registration;
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new String[]{"Course", "Marks", "GPA"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        cgpaLabel = new JLabel("CGPA: 0.00");
        pdfButton = new JButton("Download PDF");
        pdfButton.addActionListener(e -> exportPDF());

        bottom.add(cgpaLabel, BorderLayout.WEST);
        bottom.add(pdfButton, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        loadResults();
    }

    private double calculateGPA(int mark) {
        if (mark >= 80) return 4.0;
        else if (mark >= 75) return 3.75;
        else if (mark >= 70) return 3.5;
        else if (mark >= 65) return 3.25;
        else if (mark >= 60) return 3.0;
        else if (mark >= 55) return 2.75;
        else if (mark >= 50) return 2.5;
        else if (mark >= 45) return 2.25;
        else if (mark >= 40) return 2.0;
        else return 0.0;
    }

    private void loadResults() {
        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement("SELECT * FROM marks WHERE registration=?");
            ps.setString(1, userRegistration);
            ResultSet rs = ps.executeQuery();

            double totalGPA = 0;
            int totalCourses = 0;

            while (rs.next()) {
                String course = rs.getString("coursename");
                int marks = rs.getInt("marks");
                double gpa = calculateGPA(marks);

                model.addRow(new Object[]{course, marks, String.format("%.2f", gpa)});
                totalGPA += gpa;
                totalCourses++;
            }

            double cgpa = (totalCourses > 0) ? (totalGPA / totalCourses) : 0;
            cgpaLabel.setText("CGPA: " + String.format("%.2f", cgpa));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportPDF() {
        try {
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream("Student_Result.pdf"));
            doc.open();

            doc.add(new Paragraph("Student Result"));
            PdfPTable pdfTable = new PdfPTable(3);
            pdfTable.addCell("Course");
            pdfTable.addCell("Marks");
            pdfTable.addCell("GPA");

            for (int i = 0; i < model.getRowCount(); i++) {
                pdfTable.addCell(model.getValueAt(i, 0).toString());
                pdfTable.addCell(model.getValueAt(i, 1).toString());
                pdfTable.addCell(model.getValueAt(i, 2).toString());
            }

            doc.add(pdfTable);
            doc.add(new Paragraph(cgpaLabel.getText()));
            doc.close();

            JOptionPane.showMessageDialog(this, "PDF Exported");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Export Failed");
        }
    }
}
