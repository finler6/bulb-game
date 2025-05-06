package lightbulb.view;

/**
 * Starter class, does not inherit javafx.application.Application.
 * Needed for fat-JAR to run correctly without module-path.
 *
 * @author Gleb Litvinchuk (xlitvi02)
 */
public class AppLauncher {

    public static void main(String[] args) {
        MainWindow.main(args);
    }
}
