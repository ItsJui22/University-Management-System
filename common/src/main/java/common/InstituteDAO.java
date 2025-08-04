package common;

import java.sql.*;
import javax.swing.*;
import java.awt.*;

public class InstituteDAO {
    public static void loadInstitute(JLabel nameLabel, JLabel logoLabel) {
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT name, logo FROM institute WHERE id=" + Session.instituteId);
            if (rs.next()) {
                nameLabel.setText(rs.getString("name"));
                byte[] logoBytes = rs.getBytes("logo");
                if (logoBytes != null) {
                    ImageIcon icon = new ImageIcon(logoBytes);
                    Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                    logoLabel.setIcon(new ImageIcon(img));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
