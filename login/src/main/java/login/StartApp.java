package login;

public class StartApp {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new login.LoginPage();
        });
    }
}
