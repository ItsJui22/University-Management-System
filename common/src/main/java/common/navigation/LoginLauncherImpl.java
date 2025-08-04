// Inside login module (e.g., login.navigation package)
// File: common/navigation/LoginLauncherImpl.java
package common.navigation;

import javax.swing.SwingUtilities;

public class LoginLauncherImpl implements ILoginLauncher {
    @Override
    public void launchLogin() {
        try {
            // Use reflection to avoid direct dependency on login module
            Class<?> loginClass = Class.forName("login.LoginPage");
            Object loginPage = loginClass.getDeclaredConstructor().newInstance();
            if (loginPage instanceof javax.swing.JFrame) {
                SwingUtilities.invokeLater(() -> ((javax.swing.JFrame) loginPage).setVisible(true));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
