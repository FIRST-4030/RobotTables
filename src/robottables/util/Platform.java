package robottables.util;

public class Platform {

    private static boolean ready = false;
    private static boolean onRobot = false;

    public static boolean onRobot() {
        if (!ready) {
            init();
        }
        return onRobot;
    }

    private static void init() {
        // One of these (or a combination thereof) will let me figure out if we're running on the robot
        System.getProperty("java.version");
        System.getProperty("java.vendor");
        System.getProperty("java.compiler");
        System.getProperty("os.name");

        ready = true;
        onRobot = false;
    }
}
