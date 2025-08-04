package admin;

import common.Conn;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class AddDepartmentPanel extends JPanel {
    private JTextField codeField, nameField, totalSemField, searchField;
    private JComboBox<String> typeCombo;
    private JTable deptTable;
    private DefaultTableModel tableModel;

    public AddDepartmentPanel() {
        setBackground(new Color(245, 250, 255));
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // === Left Form Card ===
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(new Color(230, 240, 255));
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(102, 0, 153), 2),
                new EmptyBorder(20, 20, 20, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        codeField = new JTextField(15);
        nameField = new JTextField(15);
        totalSemField = new JTextField(15);
        typeCombo = new JComboBox<>(new String[]{"Semester", "Year"});

        int y = 0;
        addToForm(formCard, gbc, y++, "Department Code:", codeField);
        addToForm(formCard, gbc, y++, "Department Name:", nameField);
        addToForm(formCard, gbc, y++, "Semester/Year:", typeCombo);
        addToForm(formCard, gbc, y++, "Total Sem/Year:", totalSemField);

        JButton addBtn = createStyledButton("Add");
        JButton updateBtn = createStyledButton("Update");
        JButton deleteBtn = createStyledButton("Delete");

        addBtn.addActionListener(e -> insertDepartment());
        updateBtn.addActionListener(e -> updateSelectedDepartment());
        deleteBtn.addActionListener(e -> deleteSelectedDepartment());

        gbc.gridx = 1;
        gbc.gridy = y++;
        formCard.add(addBtn, gbc);
        gbc.gridy++;
        formCard.add(updateBtn, gbc);
        gbc.gridy++;
        formCard.add(deleteBtn, gbc);

        add(formCard, BorderLayout.WEST);

        // === Table Section ===
        tableModel = new DefaultTableModel();
        deptTable = new JTable(tableModel);
        tableModel.setColumnIdentifiers(new String[]{"Code", "Name", "Type", "Total"});

        deptTable.setRowHeight(28);
        deptTable.setShowGrid(false);
        deptTable.setIntercellSpacing(new Dimension(0, 0));
        deptTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        deptTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        deptTable.getTableHeader().setBackground(new Color(102, 0, 153));
        deptTable.getTableHeader().setForeground(Color.WHITE);
        ((DefaultTableCellRenderer) deptTable.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.LEFT);

        JScrollPane scrollPane = new JScrollPane(deptTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(102, 0, 153), 2));

        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setOpaque(false);

        searchField = new JTextField();
        JButton searchBtn = createStyledButton("Search");
        searchBtn.addActionListener(e -> filterDepartments());

        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("🔍 Search Department"));
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);

        rightPanel.add(searchPanel, BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        add(rightPanel, BorderLayout.CENTER);

        loadDepartmentsWithFade();

        deptTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = deptTable.getSelectedRow();
                if (row != -1) {
                    codeField.setText(tableModel.getValueAt(row, 0).toString());
                    nameField.setText(tableModel.getValueAt(row, 1).toString());
                    typeCombo.setSelectedItem(tableModel.getValueAt(row, 2).toString());
                    totalSemField.setText(tableModel.getValueAt(row, 3).toString());
                }
            }
        });
    }

    private void addToForm(JPanel panel, GridBagConstraints gbc, int y, String label, Component field) {
        gbc.gridx = 0;
        gbc.gridy = y;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(lbl, gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(102, 0, 153));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return btn;
    }

    private void insertDepartment() {
        String code = codeField.getText().trim();
        String name = nameField.getText().trim();
        String type = typeCombo.getSelectedItem().toString();
        String total = totalSemField.getText().trim();

        if (code.isEmpty() || name.isEmpty() || total.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement("INSERT INTO departments (Departmentcode, DepartmentName, semoryear, totalsemoryear) VALUES (?, ?, ?, ?)");
            ps.setString(1, code);
            ps.setString(2, name);
            ps.setString(3, type);
            ps.setInt(4, Integer.parseInt(total));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Department added!");
            loadDepartments();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSelectedDepartment() {
        int row = deptTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a department to update.");
            return;
        }

        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement("UPDATE departments SET DepartmentName=?, semoryear=?, totalsemoryear=? WHERE Departmentcode=?");
            ps.setString(1, nameField.getText());
            ps.setString(2, typeCombo.getSelectedItem().toString());
            ps.setInt(3, Integer.parseInt(totalSemField.getText()));
            ps.setString(4, codeField.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Updated successfully.");
            loadDepartments();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteSelectedDepartment() {
        int row = deptTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a department to delete.");
            return;
        }

        try {
            String code = tableModel.getValueAt(row, 0).toString();
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement("DELETE FROM departments WHERE Departmentcode=?");
            ps.setString(1, code);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Deleted.");
            loadDepartments();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterDepartments() {
        String keyword = searchField.getText().toLowerCase();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        deptTable.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword));
    }

    private void loadDepartmentsWithFade() {
        Timer timer = new Timer(20, null);
        final float[] alpha = {0};
        timer.addActionListener(e -> {
            alpha[0] += 0.05f;
            if (alpha[0] >= 1f) {
                alpha[0] = 1f;
                ((Timer) e.getSource()).stop();
            }
            setOpaque(true);
            repaint();
        });
        timer.start();
        loadDepartments();
    }

    private void loadDepartments() {
        tableModel.setRowCount(0);
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT Departmentcode, DepartmentName, semoryear, totalsemoryear FROM departments");
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("Departmentcode"),
                        rs.getString("DepartmentName"),
                        rs.getString("semoryear"),
                        rs.getInt("totalsemoryear")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
