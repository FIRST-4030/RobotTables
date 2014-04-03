package org.ingrahamrobotics.robottables.util;

public class Platform {

    private static final Object LOCK = new Object();
    private static boolean ready = false;
    private static boolean onRobot = false;

    public static boolean onRobot() {
        synchronized (LOCK) {
            if (!ready) {
                init();
            }
        }
        return onRobot;
    }

    private static void init() {
        try {
            Class.forName("java.util.ArrayList");
            onRobot = false;
        } catch (ClassNotFoundException ex) {
            onRobot = true;
        }
        System.out.println("Initiated onRobot=" + onRobot);
//        // One of these (or a combination thereof) will let me figure out if we're running on the robot
//        System.getProperty("java.version");
//        System.getProperty("java.vendor");
//        System.getProperty("java.compiler");
//        System.getProperty("os.name");
//        onRobot = false;

        ready = true;
    }
}
