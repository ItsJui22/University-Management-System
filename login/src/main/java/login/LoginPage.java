/*package login;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import common.Conn;
import common.SecurityUtil;

public class LoginPage extends JFrame {

    private FadeLabel backgroundLabel;
    private ArrayList<ImageIcon> backgrounds = new ArrayList<>();
    private int currentImageIndex = 0;

    private JTabbedPane tabbedPane;

    public LoginPage() {
        setTitle("University Management System - Login");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(false);
        setLayout(new BorderLayout());

        loadBackgroundImages();

        // Layered pane for slideshow background and foreground card
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(900, 600));

        // Background slideshow label
        backgroundLabel = new FadeLabel();
        backgroundLabel.setBounds(0, 0, 900, 600);
        if (!backgrounds.isEmpty()) backgroundLabel.setIcon(backgrounds.get(0));
        layeredPane.add(backgroundLabel, JLayeredPane.DEFAULT_LAYER);

        // Glass panel card with translucency
        GlassPanel glassCard = new GlassPanel();
        int cardWidth = 400, cardHeight = 480;
        glassCard.setBounds((900 - cardWidth) / 2, (600 - cardHeight) / 2, cardWidth, cardHeight);
        glassCard.setLayout(new BorderLayout(20, 20));
        glassCard.setBorder(new EmptyBorder(30, 30, 30, 30));
        layeredPane.add(glassCard, JLayeredPane.PALETTE_LAYER);

        // Title label at top inside glass card
        JLabel titleLabel = new JLabel("University Management System");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        glassCard.add(titleLabel, BorderLayout.NORTH);

        // Tabbed pane with login forms
        tabbedPane = new JTabbedPane();
        tabbedPane.setOpaque(false);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tabbedPane.setForeground(Color.WHITE);

        tabbedPane.addTab("Admin", buildAdminLoginPanel());
        tabbedPane.addTab("Teacher", buildTeacherLoginPanel());
        tabbedPane.addTab("Student", buildStudentLoginPanel());

        glassCard.add(tabbedPane, BorderLayout.CENTER);

        add(layeredPane, BorderLayout.CENTER);

        startSlideshow();

        setVisible(true);
    }

    private void loadBackgroundImages() {
        backgrounds.clear();
        for (int i = 1; i <= 6; i++) {
            String resourcePath = "/images/backgroundimage" + i + ".jpg";
            java.net.URL imgURL = getClass().getResource(resourcePath);
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(900, 600, Image.SCALE_SMOOTH));
                backgrounds.add(icon);
            } else {
                System.out.println("Image not found: " + resourcePath);
            }
        }
    }

    private void startSlideshow() {
        if (backgrounds.size() <= 1) return;

        Timer timer = new Timer(4000, e -> {
            int nextIndex = (currentImageIndex + 1) % backgrounds.size();
            backgroundLabel.fadeToImage(backgrounds.get(nextIndex));
            currentImageIndex = nextIndex;
        });
        timer.start();
    }

    private JPanel buildAdminLoginPanel() {
        JPanel panel = createTransparentPanel(new GridLayout(6, 1, 10, 10));

        JTextField useridField = createStyledTextField();
        JPasswordField passField = createStyledPasswordField();
        JButton loginBtn = createStyledButton("Login");
        JButton changePassBtn = createStyledButton("Change Password");

        panel.add(createStyledLabel("User ID:"));
        panel.add(useridField);
        panel.add(createStyledLabel("Password:"));
        panel.add(passField);
        panel.add(loginBtn);
        panel.add(changePassBtn);

        loginBtn.addActionListener(e -> {
            String userid = useridField.getText().trim();
            String pass = new String(passField.getPassword());

            if (userid.isEmpty() || pass.isEmpty()) {
                showError("All fields are required!");
                return;
            }

            if (checkAdminLogin(userid, pass)) {
                showMessage("Login Successful!");
                try {
                    Class<?> cls = Class.forName("admin.main_class");
                    cls.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Failed to load admin dashboard.");
                }
                dispose();
            } else {
                showError("Invalid credentials!");
            }
        });

        changePassBtn.addActionListener(e -> new ChangePassword("admin"));

        return panel;
    }

    private JPanel buildTeacherLoginPanel() {
        JPanel panel = createTransparentPanel(new GridLayout(7, 1, 10, 10));

        JTextField nameField = createStyledTextField();
        JPasswordField passField = createStyledPasswordField();
        JButton loginBtn = createStyledButton("Login");
        JButton signUpBtn = createStyledButton("Sign Up");
        JButton changePassBtn = createStyledButton("Change Password");

        panel.add(createStyledLabel("Name:"));
        panel.add(nameField);
        panel.add(createStyledLabel("Password:"));
        panel.add(passField);
        panel.add(loginBtn);
        panel.add(signUpBtn);
        panel.add(changePassBtn);

        loginBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String pass = new String(passField.getPassword());

            if (name.isEmpty() || pass.isEmpty()) {
                showError("All fields are required!");
                return;
            }

            String encrypted = SecurityUtil.sha256(pass);
            try {
                Conn c = new Conn();
                var rs = c.s.executeQuery("SELECT * FROM teacherlogin WHERE name='" + name + "' AND password='" + encrypted + "'");
                if (rs.next()) {
                    showMessage("Login Successful!");
                    try {
                        Class<?> cls = Class.forName("faculty.main_class");
                        cls.getDeclaredConstructor(String.class).newInstance(name);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showError("Failed to load teacher dashboard.");
                    }
                    dispose();
                } else {
                    showError("Invalid credentials!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Database error.");
            }
        });

        signUpBtn.addActionListener(e -> new TeacherSignUp());
        changePassBtn.addActionListener(e -> new ChangePassword("teacher"));

        return panel;
    }

    private JPanel buildStudentLoginPanel() {
        JPanel panel = createTransparentPanel(new GridLayout(7, 1, 10, 10));

        JTextField regField = createStyledTextField();
        JPasswordField passField = createStyledPasswordField();
        JButton loginBtn = createStyledButton("Login");
        JButton signUpBtn = createStyledButton("Sign Up");
        JButton changePassBtn = createStyledButton("Change Password");

        panel.add(createStyledLabel("Registration No:"));
        panel.add(regField);
        panel.add(createStyledLabel("Password:"));
        panel.add(passField);
        panel.add(loginBtn);
        panel.add(signUpBtn);
        panel.add(changePassBtn);

        loginBtn.addActionListener(e -> {
            String reg = regField.getText().trim();
            String pass = new String(passField.getPassword());

            if (reg.isEmpty() || pass.isEmpty()) {
                showError("All fields are required!");
                return;
            }

            String encrypted = SecurityUtil.sha256(pass);
            try {
                Conn c = new Conn();
                var rs = c.s.executeQuery("SELECT * FROM studentlogin WHERE registration='" + reg + "' AND password='" + encrypted + "'");
                if (rs.next()) {
                    showMessage("Login Successful!");
                    try {
                        Class<?> cls = Class.forName("student.main_class");
                        cls.getDeclaredConstructor(String.class).newInstance(reg);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showError("Failed to load student dashboard.");
                    }
                    dispose();
                } else {
                    showError("Invalid credentials!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Database error.");
            }
        });

        signUpBtn.addActionListener(e -> new StudentSignUp());
        changePassBtn.addActionListener(e -> new ChangePassword("student"));

        return panel;
    }

    // Helper: styled translucent panel for form sections
    private JPanel createTransparentPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                // Translucent white background with blur effect approximation
                g2d.setColor(new Color(255, 255, 255, 90));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setForeground(Color.WHITE);
        return panel;
    }

    // Helper: styled labels
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return label;
    }

    // Helper: styled text fields
    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBackground(new Color(255, 255, 255, 220)); // almost opaque white bg
        tf.setForeground(Color.BLACK);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 120, 215), 2),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return tf;
    }

    // Helper: styled password fields
    private JPasswordField createStyledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pf.setBackground(new Color(255, 255, 255, 220));
        pf.setForeground(Color.BLACK);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 120, 215), 2),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return pf;
    }
    // Helper: styled buttons
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(0, 150, 255)); // brighter blue
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(0, 180, 255));
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0, 180, 255), 2),
                        BorderFactory.createEmptyBorder(10, 20, 10, 20)
                ));
                btn.setForeground(Color.WHITE);
                btn.setOpaque(true);
                btn.setFocusPainted(false);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(0, 150, 255));
                btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
                btn.setForeground(Color.WHITE);
            }
        });
        return btn;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public boolean checkAdminLogin(String userid, String password) {
        try {
            Conn c = new Conn();
            String sql = "SELECT * FROM admin WHERE userid = ? AND password = ?";
            java.sql.PreparedStatement ps = c.c.prepareStatement(sql);
            ps.setString(1, userid);
            ps.setString(2, SecurityUtil.sha256(password));
            java.sql.ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // FadeLabel with smooth fade animation between images
    static class FadeLabel extends JLabel {
        private float alpha = 1f;
        private ImageIcon nextIcon;
        private Timer animationTimer;

        public void fadeToImage(ImageIcon next) {
            if (animationTimer != null) animationTimer.stop();
            nextIcon = next;
            alpha = 1f;

            animationTimer = new Timer(40, e -> {
                alpha -= 0.1f;
                if (alpha <= 0) {
                    setIcon(nextIcon);
                    alpha = 1f;
                    animationTimer.stop();
                }
                repaint();
            });
            animationTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (alpha < 1f && nextIcon != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                if (getIcon() != null) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    getIcon().paintIcon(this, g2, 0, 0);
                }
                float nextAlpha = 1f - alpha;
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, nextAlpha));
                nextIcon.paintIcon(this, g2, 0, 0);
                g2.dispose();
            } else {
                super.paintComponent(g);
            }
        }
    }

    // Glass panel with rounded corners and translucency
    static class GlassPanel extends JPanel {
        public GlassPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();

            // Enable antialiasing for smooth corners
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Gradient paint: bright cyan to bright magenta, semi-transparent
            Color startColor = new Color(0, 200, 255, 120); // bright cyan, alpha ~120/255
            Color endColor = new Color(255, 0, 255, 120);   // bright magenta, same alpha

            GradientPaint gradient = new GradientPaint(
                    0, 0, startColor,
                    getWidth(), getHeight(), endColor);

            g2d.setPaint(gradient);

            // Rounded rectangle fill
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 35, 35);

            // Optional: add a subtle white border with transparency
            g2d.setColor(new Color(255, 255, 255, 80));
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 35, 35);

            g2d.dispose();

            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginPage::new);
    }
}
*/
/*
package login;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import common.Conn;
import common.SecurityUtil;

public class LoginPage extends JFrame {

    private FadeLabel backgroundLabel;
    private ArrayList<ImageIcon> backgrounds = new ArrayList<>();
    private int currentImageIndex = 0;

    private JTabbedPane tabbedPane;
    private AnimatedTabBackgroundPanel tabBackgroundPanel;

    public LoginPage() {
        setTitle("University Management System - Login");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        loadBackgroundImages();

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(900, 600));

        backgroundLabel = new FadeLabel();
        backgroundLabel.setBounds(0, 0, 900, 600);
        if (!backgrounds.isEmpty()) {
            backgroundLabel.setIcon(backgrounds.get(0));
        }
        layeredPane.add(backgroundLabel, JLayeredPane.DEFAULT_LAYER);

        GlassPanel loginPanel = new GlassPanel();
        loginPanel.setLayout(new BorderLayout());
        loginPanel.setBounds(450, 50, 400, 520);
        loginPanel.setOpaque(false);

        JLabel title = new JLabel("Welcome to UMS", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(25, 10, 20, 10));
        addTextShadow(title);
        loginPanel.add(title, BorderLayout.NORTH);

        // Create animated tab background panel wrapping tabbedPane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tabbedPane.setForeground(Color.WHITE);
        tabbedPane.setOpaque(false);
        tabbedPane.setUI(new CustomTabbedPaneUI());

        tabbedPane.addTab("Admin", buildAdminLoginPanel());
        tabbedPane.addTab("Teacher", buildTeacherLoginPanel());
        tabbedPane.addTab("Student", buildStudentLoginPanel());

        tabBackgroundPanel = new AnimatedTabBackgroundPanel();
        tabBackgroundPanel.setLayout(new BorderLayout());
        tabBackgroundPanel.setOpaque(false);
        tabBackgroundPanel.add(tabbedPane, BorderLayout.CENTER);

        loginPanel.add(tabBackgroundPanel, BorderLayout.CENTER);

        layeredPane.add(loginPanel, JLayeredPane.PALETTE_LAYER);

        add(layeredPane, BorderLayout.CENTER);

        startSlideshow();
        tabBackgroundPanel.startAnimation();

        setVisible(true);
    }

    private void addTextShadow(JLabel label) {
        label.setForeground(Color.WHITE);
        label.setFont(label.getFont().deriveFont(Font.BOLD, label.getFont().getSize() + 6f));

        label.setUI(new javax.swing.plaf.basic.BasicLabelUI() {
            @Override
            protected void paintEnabledText(JLabel l, Graphics g, String s, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(0, 0, 0, 140));
                g2d.drawString(s, x + 2, y + 2); // shadow
                g2d.setColor(l.getForeground());
                g2d.drawString(s, x, y);
                g2d.dispose();
            }
        });
    }

    private void loadBackgroundImages() {
        backgrounds.clear();
        for (int i = 1; i <= 6; i++) {
            String resourcePath = "/images/backgroundimage" + i + ".jpg";
            java.net.URL imgURL = getClass().getResource(resourcePath);
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(900, 600, Image.SCALE_SMOOTH));
                backgrounds.add(icon);
            } else {
                System.out.println("Image not found: " + resourcePath);
            }
        }
    }

    private void startSlideshow() {
        if (backgrounds.size() <= 1) return;

        javax.swing.Timer slideshowTimer = new javax.swing.Timer(4000, e -> {
            int nextImageIndex = (currentImageIndex + 1) % backgrounds.size();
            backgroundLabel.fadeToImage(backgrounds.get(nextImageIndex));
            currentImageIndex = nextImageIndex;
        });
        slideshowTimer.start();
    }

    private JPanel buildAdminLoginPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 1, 12, 12));
        panel.setOpaque(false);

        JLabel userLabel = createBrightLabel("User ID:");
        JTextField useridField = createStyledTextField();
        JLabel passLabel = createBrightLabel("Password:");
        JPasswordField passField = createStyledPasswordField();
        JButton loginBtn = createStyledButton("Login");
        JButton changePassBtn = createStyledButton("Change Password");

        panel.add(userLabel);
        panel.add(useridField);
        panel.add(passLabel);
        panel.add(passField);
        panel.add(loginBtn);
        panel.add(changePassBtn);

        loginBtn.addActionListener(e -> {
            String userid = useridField.getText().trim();
            String pass = new String(passField.getPassword());

            if (userid.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (checkAdminLogin(userid, pass)) {
                JOptionPane.showMessageDialog(this, "Login Successful!");
                try {
                    Class<?> cls = Class.forName("admin.main_class");
                    cls.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Failed to load admin dashboard.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        changePassBtn.addActionListener(e -> new ChangePassword("admin"));

        return panel;
    }

    private JPanel buildTeacherLoginPanel() {
        JPanel panel = new JPanel(new GridLayout(7, 1, 12, 12));
        panel.setOpaque(false);

        JLabel nameLabel = createBrightLabel("Name:");
        JTextField nameField = createStyledTextField();
        JLabel passLabel = createBrightLabel("Password:");
        JPasswordField passField = createStyledPasswordField();
        JButton loginBtn = createStyledButton("Login");
        JButton signUpBtn = createStyledButton("Sign Up");
        JButton changePassBtn = createStyledButton("Change Password");

        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(passLabel);
        panel.add(passField);
        panel.add(loginBtn);
        panel.add(signUpBtn);
        panel.add(changePassBtn);

        loginBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String pass = new String(passField.getPassword());

            if (name.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String encrypted = SecurityUtil.sha256(pass);
            try {
                Conn c = new Conn();
                var rs = c.s.executeQuery("SELECT * FROM teacherlogin WHERE name='" + name + "' AND password='" + encrypted + "'");
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Login Successful!");
                    try {
                        Class<?> cls = Class.forName("faculty.main_class");
                        cls.getDeclaredConstructor(String.class).newInstance(name);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Failed to load teacher dashboard.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        signUpBtn.addActionListener(e -> new TeacherSignUp());
        changePassBtn.addActionListener(e -> new ChangePassword("teacher"));

        return panel;
    }

    private JPanel buildStudentLoginPanel() {
        JPanel panel = new JPanel(new GridLayout(7, 1, 12, 12));
        panel.setOpaque(false);

        JLabel regLabel = createBrightLabel("Registration No:");
        JTextField regField = createStyledTextField();
        JLabel passLabel = createBrightLabel("Password:");
        JPasswordField passField = createStyledPasswordField();
        JButton loginBtn = createStyledButton("Login");
        JButton signUpBtn = createStyledButton("Sign Up");
        JButton changePassBtn = createStyledButton("Change Password");

        panel.add(regLabel);
        panel.add(regField);
        panel.add(passLabel);
        panel.add(passField);
        panel.add(loginBtn);
        panel.add(signUpBtn);
        panel.add(changePassBtn);

        loginBtn.addActionListener(e -> {
            String reg = regField.getText().trim();
            String pass = new String(passField.getPassword());

            if (reg.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String encrypted = SecurityUtil.sha256(pass);
            try {
                Conn c = new Conn();
                var rs = c.s.executeQuery("SELECT * FROM studentlogin WHERE registration='" + reg + "' AND password='" + encrypted + "'");
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Login Successful!");
                    try {
                        Class<?> cls = Class.forName("student.main_class");
                        cls.getDeclaredConstructor(String.class).newInstance(reg);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Failed to load student dashboard.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        signUpBtn.addActionListener(e -> new StudentSignUp());
        changePassBtn.addActionListener(e -> new ChangePassword("student"));

        return panel;
    }

    private JLabel createBrightLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBackground(new Color(255, 255, 255, 230));
        field.setForeground(Color.BLACK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 191, 255), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBackground(new Color(255, 255, 255, 230));
        field.setForeground(Color.BLACK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 191, 255), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0, 191, 255));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(0, 139, 255), 2));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(0, 139, 255));
                btn.setBorder(BorderFactory.createLineBorder(new Color(0, 191, 255), 2));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(0, 191, 255));
                btn.setBorder(BorderFactory.createLineBorder(new Color(0, 139, 255), 2));
            }
        });
        return btn;
    }

    public boolean checkAdminLogin(String userid, String password) {
        try {
            Conn c = new Conn();
            String sql = "SELECT * FROM admin WHERE userid = ? AND password = ?";
            PreparedStatement ps = c.c.prepareStatement(sql);
            ps.setString(1, userid);
            ps.setString(2, SecurityUtil.sha256(password));
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Custom JLabel that supports fade transition between icons
    static class FadeLabel extends JLabel {
        private float alpha = 1f;
        private ImageIcon nextIcon;
        private javax.swing.Timer animationTimer;

        public void fadeToImage(ImageIcon next) {
            if (animationTimer != null) animationTimer.stop();
            nextIcon = next;
            alpha = 1f;

            animationTimer = new javax.swing.Timer(40, e -> {
                alpha -= 0.1f;
                if (alpha <= 0) {
                    setIcon(nextIcon);
                    alpha = 1f;
                    animationTimer.stop();
                }
                repaint();
            });
            animationTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (alpha < 1f && nextIcon != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                if (getIcon() != null) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    getIcon().paintIcon(this, g2, 0, 0);
                }
                float nextAlpha = 1f - alpha;
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, nextAlpha));
                nextIcon.paintIcon(this, g2, 0, 0);
                g2.dispose();
            } else {
                super.paintComponent(g);
            }
        }
    }

    // GlassPanel with bright colorful gradient and shadow
    static class GlassPanel extends JPanel {
        public GlassPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(64, 224, 208, 190),
                    getWidth(), getHeight(), new Color(255, 105, 180, 190)
            );
            g2d.setPaint(gradient);

            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);

            // Glossy highlight
            g2d.setPaint(new Color(255, 255, 255, 70));
            g2d.fillRoundRect(0, 0, getWidth(), getHeight() / 2, 40, 40);

            // Drop shadow border
            g2d.setColor(new Color(0, 0, 0, 60));
            g2d.setStroke(new BasicStroke(4));
            g2d.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 40, 40);

            g2d.dispose();

            super.paintComponent(g);
        }
    }

    // Animated gradient background behind tabbed pane
    static class AnimatedTabBackgroundPanel extends JPanel {
        private float hue = 0f;
        private javax.swing.Timer timer;

        public AnimatedTabBackgroundPanel() {
            setOpaque(false);
        }

        public void startAnimation() {
            timer = new javax.swing.Timer(50, e -> {
                hue += 0.005f;
                if (hue > 1f) hue = 0f;
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            int w = getWidth();
            int h = getHeight();

            Color c1 = Color.getHSBColor(hue, 0.6f, 1f);
            Color c2 = Color.getHSBColor((hue + 0.3f) % 1f, 0.6f, 1f);

            GradientPaint gp = new GradientPaint(0, 0, c1, w, h, c2);
            g2d.setPaint(gp);

            g2d.fillRoundRect(0, 0, w, h, 30, 30);

            g2d.dispose();

            super.paintComponent(g);
        }
    }

    // Custom tab UI to make tabs transparent and white font
    static class CustomTabbedPaneUI extends BasicTabbedPaneUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
            highlight = new Color(255, 255, 255, 100);
            lightHighlight = new Color(255, 255, 255, 150);
            shadow = new Color(0, 0, 0, 50);
            darkShadow = new Color(0, 0, 0, 100);
            focus = new Color(255, 255, 255, 200);
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                          int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2d = (Graphics2D) g.create();
            if (isSelected) {
                g2d.setColor(new Color(255, 199, 131, 255));
                g2d.fillRoundRect(x + 2, y + 2, w - 4, h - 4, 20, 20);
            } else {
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.fillRoundRect(x + 5, y + 5, w - 10, h - 10, 20, 20);
            }
            g2d.dispose();
        }

        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
                                 int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(font.deriveFont(Font.BOLD, 16f));
            g2.setColor(isSelected ? new Color(255, 119, 253) : Color.WHITE);
            g2.drawString(title, textRect.x, textRect.y + metrics.getAscent());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginPage::new);
    }
}
*/
package login;

import javax.swing.*;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import common.*;
import common.Conn;
import common.SecurityUtil;
import common.navigation.ILoginLauncher;
import common.navigation.NavigationHelper;

public class LoginPage extends JFrame {

    private FadeLabel backgroundLabel;
    private ArrayList<ImageIcon> backgrounds = new ArrayList<>();
    private int currentImageIndex = 0;

    private JTabbedPane tabbedPane;
    private AnimatedTabBackgroundPanel tabBackgroundPanel;

    private JPanel wrapPasswordField(JPasswordField passField) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        passField.setEchoChar('\u2022'); // bullet character
        JLabel toggleIcon = new JLabel("\uD83D\uDC41"); // 👁 emoji
        toggleIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleIcon.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        toggleIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        toggleIcon.addMouseListener(new MouseAdapter() {
            boolean visible = false;

            public void mouseClicked(MouseEvent e) {
                visible = !visible;
                passField.setEchoChar(visible ? (char) 0 : '\u2022');
                toggleIcon.setText(visible ? "\uD83D\uDC41\u200D\uD83D\uDDE8" : "\uD83D\uDC41"); // 👁️‍🗨 or 👁
            }
        });

        panel.add(passField, BorderLayout.CENTER);
        panel.add(toggleIcon, BorderLayout.EAST);
        return panel;
    }
    public LoginPage() {
        setTitle("University Management System - Login");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        loadBackgroundImages();

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(900, 600));

        backgroundLabel = new FadeLabel();
        backgroundLabel.setBounds(0, 0, 900, 600);
        if (!backgrounds.isEmpty()) {
            backgroundLabel.setIcon(backgrounds.get(0));
        }
        layeredPane.add(backgroundLabel, JLayeredPane.DEFAULT_LAYER);

        GlassPanel loginPanel = new GlassPanel();
        loginPanel.setLayout(new BorderLayout());
        loginPanel.setBounds(450, 50, 400, 520);
        loginPanel.setOpaque(false);

        JLabel title = new JLabel("Welcome to UMS", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(25, 10, 20, 10));
        addTextShadow(title);
        loginPanel.add(title, BorderLayout.NORTH);

        // Create animated tab background panel wrapping tabbedPane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tabbedPane.setForeground(Color.WHITE);
        tabbedPane.setOpaque(false);
        tabbedPane.setUI(new CustomTabbedPaneUI());

        tabbedPane.addTab("Admin", buildAdminLoginPanel());
        tabbedPane.addTab("Teacher", buildTeacherLoginPanel());
        tabbedPane.addTab("Student", buildStudentLoginPanel());

        tabbedPane.addChangeListener(e -> {
            Component selected = tabbedPane.getSelectedComponent();
            selected.setVisible(false);
            Timer t = new Timer(10, null);
            final int[] x = {400};
            t.addActionListener(ae -> {
                x[0] -= 20;
                selected.setLocation(x[0], selected.getY());
                selected.setVisible(true);
                if (x[0] <= 0) t.stop();
            });
            t.start();
        });

        tabBackgroundPanel = new AnimatedTabBackgroundPanel();
        tabBackgroundPanel.setLayout(new BorderLayout());
        tabBackgroundPanel.setOpaque(false);
        tabBackgroundPanel.add(tabbedPane, BorderLayout.CENTER);

        loginPanel.add(tabBackgroundPanel, BorderLayout.CENTER);

        layeredPane.add(loginPanel, JLayeredPane.PALETTE_LAYER);

        add(layeredPane, BorderLayout.CENTER);

        startSlideshow();
        tabBackgroundPanel.startAnimation();

        setVisible(true);
    }

    private void addTextShadow(JLabel label) {
        label.setForeground(Color.WHITE);
        label.setFont(label.getFont().deriveFont(Font.BOLD, label.getFont().getSize() + 6f));
        label.setUI(new BasicLabelUI() {
            @Override
            protected void paintEnabledText(JLabel l, Graphics g, String s, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(0, 0, 0, 140));
                g2d.drawString(s, x + 2, y + 2);
                g2d.setColor(l.getForeground());
                g2d.drawString(s, x, y);
                g2d.dispose();
            }
        });
    }

    private void loadBackgroundImages() {
        backgrounds.clear();
        for (int i = 1; i <= 6; i++) {
            String resourcePath = "/images/backgroundimage" + i + ".jpg";
            URL imgURL = getClass().getResource(resourcePath);
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(900, 600, Image.SCALE_SMOOTH));
                backgrounds.add(icon);
            } else {
                System.out.println("Image not found: " + resourcePath);
            }
        }
    }

    private void startSlideshow() {
        if (backgrounds.size() <= 1) return;

        new Timer(4000, e -> {
            int nextImageIndex = (currentImageIndex + 1) % backgrounds.size();
            backgroundLabel.fadeToImage(backgrounds.get(nextImageIndex));
            currentImageIndex = nextImageIndex;
        }).start();
    }
    private JPanel buildAdminLoginPanel() {
        JPanel panel = new GlassPanel();
        panel.setLayout(new GridLayout(6, 1, 12, 12));
        panel.setOpaque(false);

        JLabel userLabel = createBrightLabel("User ID:");
        JTextField useridField = createStyledTextField();
        JLabel passLabel = createBrightLabel("Password:");
        JPasswordField passField = createStyledPasswordField();
        JButton loginBtn = createStyledButton("Login");
        JButton changePassBtn = createStyledButton("Change Password");

        panel.add(userLabel);
        panel.add(useridField);
        panel.add(passLabel);
        panel.add(passField);
        panel.add(loginBtn);
        panel.add(changePassBtn);

        loginBtn.addActionListener(e -> {
            String userid = useridField.getText().trim();
            String pass = new String(passField.getPassword());

            if (userid.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (checkAdminLogin(userid, pass)) {
                JOptionPane.showMessageDialog(this, "Login Successful!");
                try {
                    if (userid.equals("2025")) {
                        dispose();
                        ILoginLauncher launcher = () -> new LoginPage().setVisible(true);
                        new admin.main_class(launcher).setVisible(true);
                    }


                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Failed to load admin dashboard.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        changePassBtn.addActionListener(e -> new ChangePassword("admin"));

        return panel;
    }

    private JPanel buildTeacherLoginPanel() {
        JPanel panel = new GlassPanel();
        panel.setLayout(new GridLayout(7, 1, 12, 12));
        panel.setOpaque(false);

        JLabel nameLabel = createBrightLabel("User Id:");
        JTextField nameField = createStyledTextField();
        JLabel passLabel = createBrightLabel("Password:");
        JPasswordField passField = createStyledPasswordField();
        JButton loginBtn = createStyledButton("Login");
        JButton signUpBtn = createStyledButton("Sign Up");
        JButton changePassBtn = createStyledButton("Change Password");

        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(passLabel);
        panel.add(passField);
        panel.add(loginBtn);
        panel.add(signUpBtn);
        panel.add(changePassBtn);

        loginBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String pass = new String(passField.getPassword());

            if (name.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String encrypted = SecurityUtil.sha256(pass);
            try {
                Conn c = new Conn();
                ResultSet rs = c.s.executeQuery("SELECT * FROM teacherlogin WHERE teacher_id='" + name + "' AND password='" + encrypted + "'");
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Login Successful!");
                    String teacherName = name;
                    new faculty.main_class(teacherName, () -> new LoginPage());  // ✅ clean
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        signUpBtn.addActionListener(e -> new TeacherSignUp());
        changePassBtn.addActionListener(e -> new ChangePassword("teacher"));

        return panel;
    }
    private JPanel buildStudentLoginPanel() {
        JPanel panel = new GlassPanel();
        panel.setLayout(new GridLayout(8, 1, 12, 12));
        panel.setOpaque(false);

        JLabel regLabel = createBrightLabel("Registration No:");
        JTextField regField = createStyledTextField();
        JLabel passLabel = createBrightLabel("Password:");
        JPasswordField passField = createStyledPasswordField();
        JCheckBox rememberBox = new JCheckBox("Remember Me");
        rememberBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rememberBox.setForeground(Color.WHITE);
        rememberBox.setOpaque(false);

        // Load saved registration
        String saved = Preferences.userRoot().node("ums/studentlogin").get("registration", "");
        regField.setText(saved);

        JButton loginBtn = createStyledButton("Login");
        JButton signUpBtn = createStyledButton("Sign Up");
        JButton changePassBtn = createStyledButton("Change Password");

        panel.add(regLabel);
        panel.add(regField);
        panel.add(passLabel);
        panel.add(passField);
        panel.add(rememberBox);
        panel.add(loginBtn);
        panel.add(signUpBtn);
        panel.add(changePassBtn);

        loginBtn.addActionListener(e -> {
            String reg = regField.getText().trim();
            String pass = new String(passField.getPassword());

            if (reg.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String encrypted = SecurityUtil.sha256(pass);
            try {
                Conn c = new Conn();
                ResultSet rs = c.s.executeQuery("SELECT * FROM studentlogin WHERE registration='" + reg + "' AND password='" + encrypted + "'");
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Login Successful!");
                    String registration = reg;
                    new student.main_class(registration, () -> new LoginPage());  // ✅ clean
                    dispose();
                }else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        signUpBtn.addActionListener(e -> new StudentSignUp());
        changePassBtn.addActionListener(e -> new ChangePassword("student"));

        return panel;
    }

    /* private JLabel createBrightLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        return label;
    }
*/
   private JLabel createBrightLabel(String text) {
       JLabel label = new JLabel(text);
       label.setForeground(Color.WHITE);
       label.setFont(new Font("Segoe UI", Font.BOLD, 16));
       label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
       label.setOpaque(false);  // make sure background is transparent
     //  label.setBackground(new Color(0,0,0,0));  // fully transparent background color (optional)
       return label;
   }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Semi-transparent white background
                g2.setColor(new Color(255, 255, 255, 50));  // adjust alpha here for transparency
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setOpaque(false);  // important for transparency
        field.setForeground(Color.BLACK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 191, 255), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Remove default background color so paintComponent is visible
      //  field.setBackground(new Color(0,0,0,0));

        return field;
    }


    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setOpaque(false);
        field.setBackground(new Color(0, 0, 0, 0));
        field.setForeground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 191, 255), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            private boolean pressed = false;

            {
                // Mouse listener to track hover and press states
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        pressed = false;
                        repaint();
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        pressed = true;
                        repaint();
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        pressed = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background gradient colors
                Color topColor = new Color(255, 255, 255, hovered ? 100 : 60);
                Color bottomColor = new Color(255, 255, 255, hovered ? 50 : 20);

                if (pressed) {
                    // Darker colors when pressed
                    topColor = new Color(200, 200, 200, 150);
                    bottomColor = new Color(150, 150, 150, 100);
                }

                GradientPaint gp = new GradientPaint(0, 0, topColor, 0, getHeight(), bottomColor);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                super.paintComponent(g2);

                // Border glow color
                Color borderColor = pressed ? new Color(0, 120, 200, 200) : new Color(0, 191, 255, 180);
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 30, 30);

                g2.dispose();
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Text color changes on hover
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(new Color(255, 240, 255));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setForeground(Color.WHITE);
            }
        });

        return btn;
    }

    public boolean checkAdminLogin(String userid, String password) {
        try (Conn c = new Conn()) {
            String sql = "SELECT * FROM admin WHERE userid = ? AND password = ?";
            PreparedStatement ps = c.c.prepareStatement(sql);
            ps.setString(1, userid);
            ps.setString(2, SecurityUtil.sha256(password)); // Password is encrypted
            ResultSet rs = ps.executeQuery();

            boolean matched = rs.next(); // ✅ Just once
            rs.close();
            ps.close();
            return matched;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // Custom JLabel that supports fade transition between icons
    static class FadeLabel extends JLabel {
        private float alpha = 1f;
        private ImageIcon nextIcon;
        private Timer animationTimer;

        public void fadeToImage(ImageIcon next) {
            if (animationTimer != null) animationTimer.stop();
            nextIcon = next;
            alpha = 1f;

            animationTimer = new Timer(40, e -> {
                alpha -= 0.1f;
                if (alpha <= 0) {
                    setIcon(nextIcon);
                    alpha = 1f;
                    animationTimer.stop();
                }
                repaint();
            });
            animationTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (alpha < 1f && nextIcon != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                if (getIcon() != null) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    getIcon().paintIcon(this, g2, 0, 0);
                }
                float nextAlpha = 1f - alpha;
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, nextAlpha));
                nextIcon.paintIcon(this, g2, 0, 0);
                g2.dispose();
            } else {
                super.paintComponent(g);
            }
        }
    }
    // GlassPanel with bright colorful gradient and shadow
    static class GlassPanel extends JPanel {
        public GlassPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(64, 224, 208, 190),
                    getWidth(), getHeight(), new Color(255, 105, 180, 190)
            );
            g2d.setPaint(gradient);

            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);

            // Glossy highlight
            g2d.setPaint(new Color(255, 255, 255, 70));
            g2d.fillRoundRect(0, 0, getWidth(), getHeight() / 2, 40, 40);

            // Drop shadow border
            g2d.setColor(new Color(0, 0, 0, 60));
            g2d.setStroke(new BasicStroke(4));
            g2d.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 40, 40);

            g2d.dispose();

            super.paintComponent(g);
        }
    }
    // Animated gradient background behind tabbed pane
    static class AnimatedTabBackgroundPanel extends JPanel {
        private float hue = 0f;
        private Timer timer;

        public AnimatedTabBackgroundPanel() {
            setOpaque(false);
        }

        public void startAnimation() {
            timer = new Timer(50, e -> {
                hue += 0.005f;
                if (hue > 1f) hue = 0f;
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            int w = getWidth();
            int h = getHeight();

            Color c1 = Color.getHSBColor(hue, 0.6f, 1f);
            Color c2 = Color.getHSBColor((hue + 0.3f) % 1f, 0.6f, 1f);

            GradientPaint gp = new GradientPaint(0, 0, c1, w, h, c2);
            g2d.setPaint(gp);

            g2d.fillRoundRect(0, 0, w, h, 30, 30);

            g2d.dispose();

            super.paintComponent(g);
        }
    }
    // Custom tab UI to make tabs transparent and white font
    static class CustomTabbedPaneUI extends BasicTabbedPaneUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
            highlight = new Color(255, 255, 255, 100);
            lightHighlight = new Color(255, 255, 255, 150);
            shadow = new Color(0, 0, 0, 50);
            darkShadow = new Color(0, 0, 0, 100);
            focus = new Color(255, 255, 255, 200);
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                          int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2d = (Graphics2D) g.create();
            if (isSelected) {
                g2d.setColor(new Color(255, 199, 131, 255));
                g2d.fillRoundRect(x + 2, y + 2, w - 4, h - 4, 20, 20);
            } else {
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.fillRoundRect(x + 5, y + 5, w - 10, h - 10, 20, 20);
            }
            g2d.dispose();
        }

        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
                                 int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(font.deriveFont(Font.BOLD, 16f));
            g2.setColor(isSelected ? new Color(255, 119, 253) : Color.WHITE);
            g2.drawString(title, textRect.x, textRect.y + metrics.getAscent());
        }
    }

    // FadeLabel, GlassPanel, AnimatedTabBackgroundPanel, CustomTabbedPaneUI remain unchanged
    // (Already included in your code and reused directly)

    public static void main(String[] args) {
        NavigationHelper.registerLoginLauncher(() -> new LoginPage());
        SwingUtilities.invokeLater(LoginPage::new);
    }
}
