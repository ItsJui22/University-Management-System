package login;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import common.Conn;

public class ChangePassword extends JFrame {

    private JTextField userField;
    private JPasswordField oldPassField;
    private JPasswordField newPassField;
    private JPasswordField confirmPassField;
    private String userType;  // "admin", "teacher", "student"

    public ChangePassword(String userType) {
        this.userType = userType;
        setTitle("Change Password - " + userType.toUpperCase());
        setSize(400, 350);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 2, 10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        add(new JLabel(userType.equals("admin") ? "User Id:" : userType.equals("teacher") ? "Teacher Id:" : "Registration No:"));
        userField = new JTextField();
        add(userField);

        add(new JLabel("Old Password:"));
        oldPassField = new JPasswordField();
        add(oldPassField);

        add(new JLabel("New Password:"));
        newPassField = new JPasswordField();
        add(newPassField);

        add(new JLabel("Confirm Password:"));
        confirmPassField = new JPasswordField();
        add(confirmPassField);

        JButton changeBtn = new JButton("Change Password");
        add(changeBtn);

        JButton cancelBtn = new JButton("Cancel");
        add(cancelBtn);

        changeBtn.addActionListener(e -> changePasswordAction());
        cancelBtn.addActionListener(e -> dispose());

        setVisible(true);
    }

    private void changePasswordAction() {
        String userId = userField.getText().trim();
        String oldPass = String.valueOf(oldPassField.getPassword());
        String newPass = String.valueOf(newPassField.getPassword());
        String confirmPass = String.valueOf(confirmPassField.getPassword());

        if (userId.isEmpty() || oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "New password and Confirm password do not match!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String encryptedOld = AuthUtil.encryptPassword(oldPass);
        String encryptedNew = AuthUtil.encryptPassword(newPass);

        try {
            Conn c = new Conn();
            String table = "";
            String idCol = "";
            switch (userType) {
                case "admin":
                    table = "admin";
                    idCol = "userid";
                    break;
                case "teacher":
                    table = "teacherlogin";
                    idCol = "teacher_id";
                    break;
                case "student":
                    table = "studentlogin";
                    idCol = "registration";
                    break;
            }
            var rs = c.s.executeQuery("SELECT * FROM " + table + " WHERE " + idCol + "='" + userId + "' AND password='" + encryptedOld + "'");
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Old password incorrect!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int updated = c.s.executeUpdate("UPDATE " + table + " SET password='" + encryptedNew + "' WHERE " + idCol + "='" + userId + "'");
            if (updated > 0) {
                JOptionPane.showMessageDialog(this, "Password changed successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Password change failed!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
