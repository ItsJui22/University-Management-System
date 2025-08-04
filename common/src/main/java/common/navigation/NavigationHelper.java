package common.navigation;

import java.util.function.Function;
import javax.swing.JFrame;

public class NavigationHelper implements ILoginLauncher{
    private static Function<String, JFrame> studentDashboardLauncher;

    private static Runnable loginLauncher;

    public static void registerLoginLauncher(Runnable launcher) {
        loginLauncher = launcher;
    }

    public static void registerStudentDashboard(Function<String, JFrame> launcher) {
        studentDashboardLauncher = launcher;
    }

    public static void launchStudentDashboard(String registration) {
        if (studentDashboardLauncher == null) {
            throw new IllegalStateException("Student dashboard not registered.");
        }
        studentDashboardLauncher.apply(registration);

    }

    @Override
    public void launchLogin() {
        if (loginLauncher == null) {
            throw new IllegalStateException("Login launcher not registered.");
        }
        loginLauncher.run();  // launch the login page
    }

}
