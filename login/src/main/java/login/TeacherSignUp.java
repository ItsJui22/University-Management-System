package login;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import common.Conn;

public class TeacherSignUp extends JFrame {

    private JTextField regField, emailField;
    private JPasswordField passField, confirmPassField;

    public TeacherSignUp() {
        setTitle("Teacher Sign Up");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 2, 10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        add(new JLabel("Teacher  Id:"));
        regField = new JTextField();
        add(regField);

        add(new JLabel("Email ID:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Password:"));
        passField = new JPasswordField();
        add(passField);

        add(new JLabel("Confirm Password:"));
        confirmPassField = new JPasswordField();
        add(confirmPassField);

        JButton signUpBtn = new JButton("Sign Up");
        add(signUpBtn);

        JButton cancelBtn = new JButton("Cancel");
        add(cancelBtn);

        signUpBtn.addActionListener(e -> signUpAction());
        cancelBtn.addActionListener(e -> dispose());

        setVisible(true);
    }

    private void signUpAction() {
        String reg = regField.getText().trim();
        String email = emailField.getText().trim();
        String pass = String.valueOf(passField.getPassword());
        String confirmPass = String.valueOf(confirmPassField.getPassword());

        if (reg.isEmpty() || email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!pass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String encrypted = AuthUtil.encryptPassword(pass);

        try {
            Conn c = new Conn();
            var rs = c.s.executeQuery("SELECT * FROM teacherlogin WHERE teacher_id='" + reg + "'");
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Teacher Id  already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int inserted = c.s.executeUpdate("INSERT INTO teacherlogin(teacher_id, password) VALUES('" + reg + "','" + encrypted + "')");
            if (inserted > 0) {
                JOptionPane.showMessageDialog(this, "Sign up successful!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Sign up failed!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

