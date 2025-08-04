package common;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class Conn implements AutoCloseable {
    public Connection c;
    public Statement s;

    public Conn() {
        try {
            // Load config.properties
            Properties props = new Properties();
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }
            props.load(input);

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            Class.forName("com.mysql.cj.jdbc.Driver");
            c = DriverManager.getConnection(url, user, password);

            // Initialize Statement here!
            s = c.createStatement();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void close() throws Exception {
        if (s != null) s.close();
        if (c != null) c.close();
    }

    // Execute SELECT query and return ResultSet
    public ResultSet executeQuery(String query) {
        try {
            return s.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Execute INSERT, UPDATE, DELETE queries
    public int executeUpdate(String query) {
        try {
            return s.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
