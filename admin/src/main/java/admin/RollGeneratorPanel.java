package admin;

import common.Conn;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class RollGeneratorPanel extends JPanel {

    private JComboBox<String> deptCombo, semCombo;
    private JButton generateRollsBtn;
    private JTextArea logArea;

    private RollGenerationListener listener;

    private final Color primaryColor = new Color(72, 145, 220);
    private final Color secondaryColor = new Color(230, 240, 250);
    private final Color accentColor = new Color(45, 110, 180);

   // private RollGenerationListener listener;  // 👈 Listener field

    public RollGeneratorPanel(RollGenerationListener listener) {
        this.listener = listener; // 👈 Save reference to AddStudentPanel
        setLayout(new BorderLayout(15, 15));
        setBackground(secondaryColor);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(secondaryColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        deptCombo = new JComboBox<>(loadDepartments());
        decorateComboBox(deptCombo);
        deptCombo.addActionListener(e -> loadSemesters());

        semCombo = new JComboBox<>();
        decorateComboBox(semCombo);

        generateRollsBtn = new JButton("Generate Rolls");
        decorateButton(generateRollsBtn);

        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1;
        topPanel.add(deptCombo, gbc);

        gbc.gridx = 2;
        topPanel.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 3;
        topPanel.add(semCombo, gbc);

        gbc.gridx = 4;
        topPanel.add(generateRollsBtn, gbc);

        add(topPanel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(new CompoundBorder(
                new LineBorder(accentColor, 2, true),
                new EmptyBorder(10,10,10,10)
        ));
        add(scrollPane, BorderLayout.CENTER);

        generateRollsBtn.addActionListener(e -> {
            long generatedRoll = generateSingleRoll(); // 👈 New method for single roll
            listener.setGeneratedRoll(generatedRoll);  // 👈 Send roll to AddStudentPanel
            logArea.setText("✅ Roll Generated: " + generatedRoll);
        });

        loadSemesters();  // initial load
    }
    private long generateSingleRoll() {
        try {
            Conn c = new Conn();
            String dept = deptCombo.getSelectedItem().toString();
            String sem = semCombo.getSelectedItem().toString();

            String sql = "SELECT MAX(rollnumber) FROM students WHERE Departmentcode=? AND semoryear=?";
            PreparedStatement ps = c.c.prepareStatement(sql);
            ps.setString(1, dept);
            ps.setInt(2, Integer.parseInt(sem));
            ResultSet rs = ps.executeQuery();

            long nextRoll = 1;
            if (rs.next()) {
                long max = rs.getLong(1);
                nextRoll = (max == 0) ? 1 : max + 1;
            }
            return nextRoll;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating roll");
            return 1; // fallback
        }
    }


    public void setRollGenerationListener(RollGenerationListener listener) {
        this.listener = listener;
    }

    private String[] loadDepartments() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.c.createStatement().executeQuery("SELECT DISTINCT Departmentcode FROM departments");
            Vector<String> depts = new Vector<>();
            while(rs.next()) {
                depts.add(rs.getString("Departmentcode"));
            }
            if(depts.isEmpty()) depts.add("NOT ASSIGNED");
            return depts.toArray(new String[0]);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new String[]{"NOT ASSIGNED"};
        }
    }

    private void loadSemesters() {
        semCombo.removeAllItems();
        for (int i = 1; i <= 8; i++) {
            semCombo.addItem(String.valueOf(i));
        }
    }


    private void generateRollNumbers() {
        String dept = (String) deptCombo.getSelectedItem();
        String sem = (String) semCombo.getSelectedItem();

        if(dept == null || sem == null) {
            JOptionPane.showMessageDialog(this, "Please select Department and Semester");
            return;
        }

        logArea.setText("");
        try {
            Conn c = new Conn();
            c.c.setAutoCommit(false);

            PreparedStatement psLastRoll = c.c.prepareStatement(
                    "SELECT rollnumber FROM rollgenerator WHERE Departmentcode = ? AND semoryear = ?"
            );
            psLastRoll.setString(1, dept);
            psLastRoll.setInt(2, Integer.parseInt(sem));
            ResultSet rs = psLastRoll.executeQuery();
            long lastRoll = 0;
            if(rs.next()) {
                lastRoll = rs.getLong("rollnumber");
            } else {
                PreparedStatement psInsert = c.c.prepareStatement(
                        "INSERT INTO rollgenerator (Departmentcode, semoryear, rollnumber) VALUES (?, ?, ?)"
                );
                psInsert.setString(1, dept);
                psInsert.setInt(2, Integer.parseInt(sem));
                psInsert.setLong(3, 0L);
                psInsert.executeUpdate();
            }

            PreparedStatement psStudents = c.c.prepareStatement(
                    "SELECT sr_no FROM students WHERE Departmentcode = ? AND semoryear = ? AND (rollnumber IS NULL OR rollnumber = 0)"
            );
            psStudents.setString(1, dept);
            psStudents.setInt(2, Integer.parseInt(sem));
            ResultSet rsStudents = psStudents.executeQuery();

            int count = 0;
            PreparedStatement psUpdate = c.c.prepareStatement(
                    "UPDATE students SET rollnumber = ? WHERE sr_no = ?"
            );

            while(rsStudents.next()) {
                long newRoll = ++lastRoll;
                psUpdate.setLong(1, newRoll);
                psUpdate.setInt(2, rsStudents.getInt("sr_no"));
                psUpdate.executeUpdate();
                count++;
                logArea.append("Generated Roll: " + newRoll + " for student sr_no: " + rsStudents.getInt("sr_no") + "\n");
            }

            PreparedStatement psUpdateRollGen = c.c.prepareStatement(
                    "UPDATE rollgenerator SET rollnumber = ? WHERE Departmentcode = ? AND semoryear = ?"
            );
            psUpdateRollGen.setLong(1, lastRoll);
            psUpdateRollGen.setString(2, dept);
            psUpdateRollGen.setInt(3, Integer.parseInt(sem));
            psUpdateRollGen.executeUpdate();

            c.c.commit();
            JOptionPane.showMessageDialog(this, "Roll numbers generated for " + count + " students.");

            if (listener != null) listener.onRollsGenerated(); // Notify AddStudentPanel

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private long generateNextRollNumber() {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT MAX(rollnumber) FROM students");
            if (rs.next()) {
                long maxRoll = rs.getLong(1); // If no data, returns 0
                return maxRoll + 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1; // Default roll number if table is empty
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
}
